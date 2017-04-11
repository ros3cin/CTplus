package edu.colorado.walautil

import com.ibm.wala.classLoader.IClass
import com.ibm.wala.ipa.callgraph.{CGNode, CallGraph}

import scala.collection.JavaConversions._

object CGNodeUtil {
  
  /** find fakeWorldClinit node (class initializers) */      
  def getFakeWorldClinitNode(cg : CallGraph) : Option[CGNode] = {
    val fakeRoot = cg.getFakeRootNode()
    // if there is a fakeWorldClinit, it is always called by fakeRoot
    fakeRoot.iterateCallSites().find(site => {
      val target = site.getDeclaredTarget()
      target.getDeclaringClass() == fakeRoot.getMethod().getDeclaringClass().getReference() &&
      target.getName().toString() == "fakeWorldClinit"
    }) match {
      case Some(target) => 
        val nodes = cg.getPossibleTargets(fakeRoot, target)
        assert(nodes.size() == 1) // there should only be one fakeWorldClinit!
        Some(nodes.iterator().next())
      case None => None
    }
  }
   
  def getClassInitializerFor(clazz : IClass, cg : CallGraph) : Option[CGNode] = clazz.getClassInitializer() match {
    case null => None
    case clinit => 
      val clinits = cg.getNodes(clinit.getReference())
      assert(clinits.size == 1)
      Some(clinits.iterator().next())
  }            
  
}
  