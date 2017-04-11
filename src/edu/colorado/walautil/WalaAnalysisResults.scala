package edu.colorado.walautil

import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ipa.callgraph.propagation.{PointerAnalysis, InstanceKey}

/** simple struct containing all the useful output from a run of WALA */
class WalaAnalysisResults(val cg : CallGraph, pa : PointerAnalysis[InstanceKey]) {
  val cha = cg.getClassHierarchy()
  val hg = pa.getHeapGraph()
  val hm = pa.getHeapModel()
}

