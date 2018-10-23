package edu.colorado.walautil

import com.ibm.wala.util.graph.Graph
import com.ibm.wala.util.graph.traverse.{BFSIterator, BoundedBFSIterator}

object GraphUtil {

  /** @return all nodes reachable in graph @param g from source node @param src in @param k steps or less */
  def kBFS[T](g : Graph[T], src : T, k : Int) : Set[T] = {
    val bfsIter = new BoundedBFSIterator(g, src, k)
    var l = Set.empty[T]
    while (bfsIter.hasNext()) {
      l = l + bfsIter.next()
    }
    l
  }
  
  /** @return true in a node in @param snks is reachable from @param src in @param k steps or less, false otherwise */
  def reachableInKSteps[T](g : Graph[T], src : T, snks : Set[T], k : Int) : Boolean = {
    val bfsIter = new BoundedBFSIterator(g, src, k)
    while (bfsIter.hasNext()) {
      if (snks.contains(bfsIter.next())) return true
    }
    false
  }

  /* perform a fold over nodes returned by WALA's BFSIterator **/
  @annotation.tailrec
  final def bfsIterFold[T1,T2](iter : BFSIterator[T1], acc : T2, f : (T2, T1) => T2) : T2 =
    if (iter.hasNext) bfsIterFold(iter, f(acc, iter.next()), f)
    else acc
  
}