package edu.colorado.walautil

import com.ibm.wala.ipa.callgraph.{CGNode, CallGraph}
import com.ibm.wala.ipa.cfg.{EdgeFilter, PrunedCFG}
import com.ibm.wala.ipa.cha.IClassHierarchy
import com.ibm.wala.ssa._
import com.ibm.wala.types.TypeReference
import com.ibm.wala.util.graph.dominators.Dominators
import com.ibm.wala.util.graph.impl.GraphInverter
import com.ibm.wala.util.graph.traverse.{BFSPathFinder, DFS}
import com.ibm.wala.util.graph.{Acyclic, Graph, NumberedGraph}
import edu.colorado.walautil.Types._


import scala.collection.JavaConversions._


object CFGUtil {
  
  val DEBUG = false
  
  private def getWhile(succOrPred : (SSACFG, WalaBlock) => Set[WalaBlock], startBlk : WalaBlock,
                       startSet : Set[ISSABasicBlock], cfg : SSACFG, test : WalaBlock => Boolean,
                       inclusive : Boolean) : Set[WalaBlock] = {
    @annotation.tailrec
    def getWhileRec(blks : Set[WalaBlock], seen : Set[WalaBlock]) : Set[WalaBlock]= {
      val (passing, failing) = blks.partition(blk => test(blk))    
      val newSeen = if (inclusive) failing ++ seen.union(passing) else seen.union(passing)
      if (passing.isEmpty) newSeen
      else {
        val toExplore = passing.foldLeft (Set.empty[WalaBlock]) ((set,blk) => set ++ succOrPred(cfg, blk))
        // explore all succs that we have not already seen
        getWhileRec(toExplore diff newSeen, newSeen)
      }
    }
    getWhileRec(startSet + startBlk, Set.empty)
  }

  /**
   * @param inclusive if true, include the last blks that fail test 
   * @return transitive closure of successors of startBlk in cfg that pass test
   */
  def getSuccsWhile(startBlk : WalaBlock, cfg : SSACFG, startSet : Set[ISSABasicBlock] = Set.empty[ISSABasicBlock],
                    test : WalaBlock => Boolean = _ => true,
      inclusive : Boolean = false) : Set[WalaBlock] = 
    getWhile((cfg, blk) => getSuccessors(blk, cfg).toSet, startBlk, startSet, cfg, test, inclusive)

    /**
     * @param inclusive if true, return the last blks that fail test 
     * @return transitive closure of predecessors of startBlk in cfg that pass test
     */
  def getPredsWhile(startBlk : WalaBlock, cfg : SSACFG, test : WalaBlock => Boolean,
                    startSet : Set[ISSABasicBlock] = Set.empty[WalaBlock], inclusive : Boolean = false,
                    exceptional : Boolean = false) : Set[WalaBlock] =
    getWhile((cfg, blk) => (if (exceptional) cfg.getPredNodes(blk).toList else cfg.getNormalPredecessors(blk).toList).toSet,
             startBlk, startSet, cfg, test, inclusive)

  def getFallThroughBlocks(startBlk : WalaBlock, cfg : SSACFG, inclusive : Boolean = false, test : WalaBlock => Boolean =_ => true) : Set[WalaBlock] = {
    var last : WalaBlock = null
    // want to do getSuccsWhile(succs.size == 1, but we also want the last block that fails the test to be included
    val fallThrough =
      getSuccsWhile(startBlk, cfg, Set.empty[WalaBlock], (blk => { if (!test(blk)) { last = blk; false } else {
        val size = getSuccessors(blk, cfg).size
        if (size <= 1) true
        else { last = blk; false}
      }}), inclusive)
    if (last != null && last != startBlk) fallThrough + last // add last if applicable
    else fallThrough
  }
    
  /**
   * @return true if @param startBlk falls through to @param targetBlk (that is, if startBlk inevitably transitions
   * to targetBlk in a non-exceptional execution)
   */
  def fallsThroughTo(startBlk : WalaBlock, targetBlk : WalaBlock, cfg : SSACFG) : Boolean =
    getFallThroughBlocks(startBlk, cfg).contains(targetBlk)
      
  def fallsThroughToConditional(startBlk : WalaBlock, cfg : SSACFG) : Boolean =
    getFallThroughBlocks(startBlk, cfg).find(blk => CFGUtil.endsWithConditionalInstr(blk)).isDefined
     
  def fallsThroughToWithoutLoopConstructs(startBlk : WalaBlock, targetBlk : WalaBlock,
                                          breaksAndContinues : Map[WalaBlock,WalaBlock], cfg : SSACFG) : Boolean =
  getFallThroughBlocks(startBlk, cfg, false, blk => !breaksAndContinues.contains(blk)).contains(targetBlk)
           
  def isReachableFrom(targetBlk : WalaBlock, startBlk : WalaBlock, cfg : SSACFG) : Boolean =
    getSuccsWhile(startBlk, cfg).contains(targetBlk)
    
  def isReachableFromWithoutLoopConstructs(targetBlk : WalaBlock, startBlk : WalaBlock, bodyBlocks : Set[WalaBlock],
                                           breaksAndContinues : Map[WalaBlock,WalaBlock], cfg : SSACFG,
                                           inclusive : Boolean = false) : Boolean =
  getReachableWithoutLoopConstructs(startBlk, breaksAndContinues, bodyBlocks, cfg, inclusive).contains(targetBlk)
    
  def getReachableWithoutLoopConstructs(startBlk : WalaBlock, breaksAndContinues : Map[WalaBlock,WalaBlock],
                                        bodyBlocks : Set[WalaBlock], cfg : SSACFG,
                                        inclusive : Boolean = false) : Set[WalaBlock] = {
    def getSuccs(blk : WalaBlock, cfg : SSACFG) : List[WalaBlock] = {
      val succs = getSuccessors(blk, cfg)
      if (breaksAndContinues.contains(blk))
        if (succs.size == 1) succs //List.empty[WalaBlock] // normal break / continue--single succ
        else {
          // one branch of a conditional or loop head is a break / continue. follow the succ that is NOT a break / continue
          val jmpSucc = breaksAndContinues.getOrElse(blk, sys.error("this can't happen"))
          succs.filterNot(blk => blk == jmpSucc)
        }
      else succs
    }
    getWhile((cfg, blk) => getSuccs(blk, cfg).toSet, startBlk, Set.empty[ISSABasicBlock], cfg,
      blk => (bodyBlocks.isEmpty || bodyBlocks.contains(blk))
             && !breaksAndContinues.contains(blk) || endsWithConditionalInstr(blk), inclusive)
  }
          
  /**
   * @return true if @param blk0 and @param blk1 both have a single successor, and that successor is the same block
   */
  def transitionToSameBlock(blk0 : WalaBlock, blk1 : WalaBlock, cfg : SSACFG) : Boolean = {
    val (succs0, succs1) = (getSuccessors(blk0, cfg), getSuccessors(blk1, cfg))
    if (succs0.size == succs1.size && succs0.size == 1) succs0 == succs1
    else false
  }

  def endsWithThrowInstr(blk : WalaBlock) : Boolean =
    blk.getLastInstructionIndex > -1 && blk.getLastInstruction.isInstanceOf[SSAThrowInstruction]

  def endsWithSwitchInstr(blk : WalaBlock) : Boolean =
    blk.getLastInstructionIndex > -1 && blk.getLastInstruction.isInstanceOf[SSASwitchInstruction]

  def endsWithGotoInstr(blk : WalaBlock) : Boolean =
    blk.getLastInstructionIndex > -1 && blk.getLastInstruction.isInstanceOf[SSAGotoInstruction]

  def endsWithConditionalInstr(blk : WalaBlock) : Boolean =
    blk.getLastInstructionIndex > -1 && blk.getLastInstruction.isInstanceOf[SSAConditionalBranchInstruction]

  def endsWithReturnInstr(blk : WalaBlock) : Boolean =
    blk.getLastInstructionIndex > -1 && blk.getLastInstruction.isInstanceOf[SSAReturnInstruction]
      
  /**
   * @return true if @param src falls through to exit block
   */
  def isExitBlock(src : WalaBlock, cfg : SSACFG) : Boolean = fallsThroughTo(src, cfg.exit().asInstanceOf[ISSABasicBlock], cfg)
    
  /**
   * @return true if @param src falls through to a throw block
   */
  def isThrowBlock(src : WalaBlock, cfg : SSACFG) : Boolean = {
    var last : WalaBlock = src
    // want to do getSuccsWhile(succs.size == 1, but we also want the last block that fails the test to be included
    getSuccsWhile(src, cfg, Set.empty[ISSABasicBlock], (blk => {
      val size = getSuccessors(blk, cfg).size
      if (size == 1) true
      else { last = blk; false}
    }))
    endsWithThrowInstr(last)
  }

  def isReturnBlock(src : WalaBlock, cfg : SSACFG) : Boolean = {
    var last : WalaBlock = src
    // want to do getSuccsWhile(succs.size == 1, but we also want the last block that fails the test to be included
    getSuccsWhile(src, cfg, Set.empty[ISSABasicBlock], (blk => {
      val size = getSuccessors(blk, cfg).size
      if (size == 1) true
      else { last = blk; false}
    }))
    endsWithReturnInstr(last)
  }

  /** @return true if @param b is a block containing a conditional or switch instruction */
  def isConditionalBlock(b : ISSABasicBlock) : Boolean =
    b.exists(i => i.isInstanceOf[SSAConditionalBranchInstruction] || i.isInstanceOf[SSASwitchInstruction])

  def getCatchBlocks(cfg : SSACFG) : Iterable[ISSABasicBlock] =
    // we could use cfg.getCatchBlocks(), but it returns a bitvector that is a pain to iterate over
    cfg.filter(blk => blk.isCatchBlock())

  /**
   * @return true if a catch block falls through to @param snk
   */
  def catchBlockFallsThroughTo(snk : WalaBlock, cfg : SSACFG) : Boolean =
    getCatchBlocks(cfg).foldLeft (Set.empty[WalaBlock]) ((set, blk) => set ++ getFallThroughBlocks(blk, cfg))
    .contains(snk)
    
  def catchBlockTransitionsTo(snk : WalaBlock, cfg : SSACFG) : Boolean =
    getCatchBlocks(cfg).foldLeft (Set.empty[WalaBlock]) ((set, blk) => set ++ getSuccsWhile(blk, cfg))
    .contains(snk)



  // general template for checking simple cfg structural properties interprocedurally
  def interprocCheck(intraProcCheck : (ISSABasicBlock, SSACFG) => Boolean, startBlk : ISSABasicBlock,
                     n : CGNode, cg : CallGraph, cgNodeFilter : CGNode => Boolean) : Boolean =
    // check is true if it is true intraprocedurally...
    intraProcCheck(startBlk, n.getIR.getControlFlowGraph) || {
      // ...or interprocedurally at each caller
      def extendWorklistWithPreds(n: CGNode, worklist : List[(CGNode,CGNode)]) : List[(CGNode,CGNode)] =
        cg.getPredNodes(n).filter(n => cgNodeFilter(n))
                          .foldLeft (worklist) ((worklist, caller) => (caller, n) :: worklist)

      @annotation.tailrec
      def interprocCheckRec(worklist : List[(CGNode,CGNode)], seen : Set[(CGNode,CGNode)]) : Boolean =
        worklist match {
          case Nil => false
          case pair :: worklist =>
            !seen.contains(pair) && {
              val (caller, callee) = pair
              val ir = caller.getIR
              val cfg = ir.getControlFlowGraph
              val protectedAtAllCallSites = {
                val siteBlks =
                  // check true if it is true for all calls to callee in caller
                  cg.getPossibleSites(caller, callee).foldLeft(Set.empty[ISSABasicBlock])((siteBlks, site) =>
                    siteBlks ++ ir.getBasicBlocksForCall(site))
                siteBlks.forall(blk => intraProcCheck(blk, cfg))
              }

              if (protectedAtAllCallSites)
                // callee satisfies property, we can recurse to checking the rest of the list
                worklist.isEmpty || interprocCheckRec(worklist, seen)
              else
                // callee does not satisfy property; can only do so if all of its callers do
                interprocCheckRec(extendWorklistWithPreds(caller, worklist), seen + pair)
            }
        }

      interprocCheckRec(extendWorklistWithPreds(n, Nil), Set.empty[(CGNode,CGNode)])
    }

  /** @return true if @param block is protected by a catch block when it throws exception @exc */
  def isProtectedByCatchBlockIntraprocedural(blk : ISSABasicBlock, cfg : SSACFG, exc : TypeReference,
                                             cha : IClassHierarchy) : Boolean = {
    val excClass = cha.lookupClass(exc)
    cfg.getExceptionalSuccessors(blk).exists(b => b.isCatchBlock && {
      b.getCaughtExceptionTypes.exists(t => {
        val caughtExc = cha.lookupClass(t)
        cha.isAssignableFrom(caughtExc, excClass)
      })
    })
  }

  def isProtectedByCatchBlockInterprocedural(startBlk : ISSABasicBlock, n : CGNode, exc : TypeReference, cg : CallGraph,
                                             cha : IClassHierarchy,
                                             cgNodeFilter : CGNode => Boolean = Util.RET_TRUE) : Boolean = {
    def intraCheckClosure(blk : ISSABasicBlock, cfg : SSACFG) =
      isProtectedByCatchBlockIntraprocedural(blk, cfg, exc, cha)
    interprocCheck(intraCheckClosure, startBlk, n, cg, cgNodeFilter)
  }

  /** @return true if @param startBlk is lexically enclosed in a try block in the IR for @param n */
  def isInTryBlockIntraprocedural(startBlk : ISSABasicBlock, cfg : SSACFG) : Boolean = {

    cfg.exists(blk => blk.isCatchBlock) && {
      val bwReachable = getBackwardReachableFrom(startBlk, cfg, inclusive = true)
      // get back edge-free view of CFG
      val cfgWithoutBackEdges = getBackEdgePrunedCFG(cfg)
      // startBlk is in a catch block if one of its predecessor instructions transitions to a catch block and no paths
      // exist from from the catch block to startBlk without traversing a back edge. if such a path exists, the try
      // block precedes startBlk rather than enclosing it
      bwReachable.exists(blk => cfg.getExceptionalSuccessors(blk).exists(blk =>
        blk.isCatchBlock && !isReachableFrom(startBlk, blk, cfgWithoutBackEdges)))
    }
  }

  def isInTryBlockInterprocedural(blk : ISSABasicBlock, n : CGNode, cg : CallGraph,
                                    cgNodeFilter : CGNode => Boolean = Util.RET_TRUE) : Boolean =
    interprocCheck(isInTryBlockIntraprocedural, blk, n, cg, cgNodeFilter)

  /** @return true if @param i is lexically enclosed in a conditional block in the IR for @param n */
  def isInConditionalIntraprocedural(startBlk : ISSABasicBlock, cfg : SSACFG) : Boolean = {
    val bwReachable = getBackwardReachableFrom(startBlk, cfg, inclusive = true).toSet
    // if instrBlk is guarded by a conditional, its backward-reachable blocks will contain a conditional blocks whose
    // successors are not all contained in bwReachable.

    // note that if we want to re-use this code to compute the exact list of dominating conditionals at some point in
    // the future, we need to do something a bit different: eliminate back edges from consideration during the backward
    // reachability check. this code is fine for a boolean check like this procedure because we'll always report the
    // loop conditional as dominating
    bwReachable.exists(blk => isConditionalBlock(blk) &&
                              cfg.getSuccNodes(blk).exists(blk => !bwReachable.contains(blk)))
  }

  def isInConditionalInterprocedural(blk : ISSABasicBlock, n : CGNode, cg : CallGraph,
                                     cgNodeFilter : CGNode => Boolean = Util.RET_TRUE) : Boolean =
    interprocCheck(isInConditionalIntraprocedural, blk, n, cg, cgNodeFilter)
      
  /**
   * Get the normal successors of a block AND any exceptional successors ending in a throw statement
   * this is necessary because WALA regards the transition to a throw block as an exceptional successor.
   * That is, if we have blk(v1 = new Exception) -> blk(throw v1), we will not see "blk(throw v1) as
   * a successor of "blk(v1 = new Exception)". This method is meant to correct this
   */
  def getSuccessors(blk : WalaBlock, cfg : SSACFG) = {
    cfg.getExceptionalSuccessors(blk).foldLeft (cfg.getNormalSuccessors(blk).toList) ((lst, succ) => {
      if (endsWithThrowInstr(succ) && !succ.isCatchBlock()) succ :: lst
      else lst
    })
  }
      
  def getNormalPredecessors(blk : WalaBlock, cfg : SSACFG) : Iterable[WalaBlock] = 
    cfg.getNormalPredecessors(blk)
      
  def getThenBranch(blk : ISSABasicBlock, cfg : SSACFG) = getSuccessors(blk, cfg)(0)
  
  /** @ return a list of blocks whose immediate dominator is @param goal */
  def getJoins[T](goal : T, worklist : List[T], domInfo : Dominators[T]) : List[T] = { //cfg : SSACFG) : List[T] = {
    // we assume the nodes in preds are unique, that they are the predecessors of node,
    // and that they are ordered from in ascending order by number (or accordingly, depth in the CFG)
    // our objective is to push each node in preds up to goal, the immediate dominator of node 
    // while performing as many joins as possible
    @annotation.tailrec
    def getJoinsRec(worklist : List[T], acc : List[T]) : List[T] = worklist match {
      case node :: worklist =>
        val idom = domInfo.getIdom(node)
        println("node is " + node + " idom is " + idom)
        assert (idom != null, "couldn't get to " + goal)
        assert (idom != node)
        if (node == goal || idom == goal) getJoinsRec(worklist, node :: acc)
        else worklist match { // try to match with idom of next pred
          case nextPred :: worklist =>
            if (idom == nextPred || idom == domInfo.getIdom(nextPred)) getJoinsRec(nextPred :: worklist, acc)
            else getJoinsRec(idom :: nextPred :: worklist, acc)
          case Nil => getJoinsRec(List(idom), acc) 
        }
      case Nil => acc
    }     
    getJoinsRec(worklist, List.empty[T])
  }

  def findInstr(ir : IR, i : SSAInstruction) : Option[(ISSABasicBlock, Int)] = {
    require(ir != null)
    // find index of instr in block
    val startBlk = ir.getBasicBlockForInstruction(i)
    if (startBlk == null) None
    else
      startBlk.asInstanceOf[SSACFG#BasicBlock].getAllInstructions.zipWithIndex.find(pair => pair._1 == i) match {
        case Some((_, index)) => Some(startBlk, index)
        case None => None
      }
  }

  /** @return true if @param snk is reachable from @param src in @param g */
  def isReachableFrom(snk : ISSABasicBlock, src : ISSABasicBlock, g : NumberedGraph[ISSABasicBlock]) : Boolean =
    isReachableFrom(snk.getNumber, src.getNumber, g)

  /** @return true if @param snk is reachable from @param src in @param g */
  def isReachableFrom[T](snk : Int, src : Int, g : NumberedGraph[T]) : Boolean = {
    // subvert WALA caching issues. ugh
    val (newSrc, newSnk) = (g.getNode(src), g.getNode(snk))
    val finder = new BFSPathFinder(g, newSrc, newSnk)
    finder.find() != null 
  }
  
  def getBackwardReachableFrom[T](src : T, g : Graph[T], inclusive : Boolean) : List[T] =
    getReachableFrom(src, GraphInverter.invert(g))
    
  def getReachableFrom[T](src : T, g : Graph[T], inclusive : Boolean = true) : List[T] = {
    val srcs = new java.util.LinkedList[T]
    if (inclusive) srcs.add(src) else {
      val succs = g.getSuccNodes(src)
      while (succs.hasNext()) srcs.add(succs.next())
    }
    DFS.getReachableNodes(g, srcs).toList
  }

  /** @return a view of @param cfg with all back edges removed */
  def getBackEdgePrunedCFG(cfg : SSACFG) = PrunedCFG.make(cfg, new BackEdgePruner(cfg))
}

private final class BackEdgePruner(cfg : SSACFG) extends EdgeFilter[ISSABasicBlock] {
  val backEdges = Acyclic.computeBackEdges(cfg, cfg.entry())

  override def hasNormalEdge(src : ISSABasicBlock, dst : ISSABasicBlock) : Boolean =
    cfg.getNormalSuccessors(src).toList.filter(blk => !backEdges.contains(src.getNumber, blk.getNumber)).contains(dst)

  override def hasExceptionalEdge(src : ISSABasicBlock, dst : ISSABasicBlock) : Boolean =
    cfg.getExceptionalSuccessors(src).contains(dst)

}