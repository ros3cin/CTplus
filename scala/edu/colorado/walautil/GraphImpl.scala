package edu.colorado.walautil

import com.ibm.wala.util.graph.impl.BasicNodeManager
import com.ibm.wala.util.graph.{AbstractGraph, EdgeManager}
import scala.collection.JavaConversions._

/** simple adjacency set implementation of WALA graph interface. this has wasteful memory usage but fast lookups of
  * successor and predecessor nodes */
final class GraphImpl[T](val root : Option[T] = None) extends AbstractGraph[T]() {

  private val nodeManager = new BasicNodeManager[T]
  root match {
    case Some(root) => nodeManager.addNode(root)
    case None => ()
  }

  private val succMap = new java.util.HashMap[T,java.util.Set[T]]()
  private val predMap = new java.util.HashMap[T,java.util.Set[T]]()

  private val edgeManager = new EdgeManager[T] {

    private def getOrElseEmpty(m : java.util.Map[T,java.util.Set[T]], k : T) : java.util.Set[T] = {
      val l = m.get(k)
      if (l != null) l else java.util.Collections.emptySet()
    }

    private def addKeyValuePairToSetMap(k : T, v : T, m : java.util.Map[T,java.util.Set[T]]) : Unit = {
      val values = m.get(k)
      if (values == null) {
        val l = new java.util.HashSet[T]()
        l.add(v)
        m.put(k, l)
      } else values.add(v)
    }

    override def getSuccNodeCount(p1: T): Int = getOrElseEmpty(succMap, p1).size()

    override def getPredNodeCount(p1: T): Int = getOrElseEmpty(predMap, p1).size()

    override def getSuccNodes(p1: T): java.util.Iterator[T] = getOrElseEmpty(succMap, p1).iterator()

    override def getPredNodes(p1: T): java.util.Iterator[T] = getOrElseEmpty(predMap, p1).iterator()

    override def hasEdge(p1: T, p2: T): Boolean = getOrElseEmpty(succMap, p1).contains(p2)

    override def addEdge(src: T, snk: T): Unit = {
      nodeManager.addNode(src)
      nodeManager.addNode(snk)
      addKeyValuePairToSetMap(src, snk, succMap)
      addKeyValuePairToSetMap(snk, src, predMap)
    }

    override def removeEdge(p1: T, p2: T): Unit = sys.error("Unimp: removeEdge")
    override def removeIncomingEdges(p1: T): Unit = sys.error("Unimp: removeIncomingEdges")
    override def removeOutgoingEdges(p1: T): Unit = sys.error("Unimp: removeOutgoingEdges")
    override def removeAllIncidentEdges(p1: T): Unit = sys.error("Unimp: removeAllIncidentEdges")
  }

  override def getNodeManager = nodeManager
  override def getEdgeManager = edgeManager

  def nodes() : Iterable[T] = nodeManager.iterator().toIterable

  def edges() : Iterable[(T,T)] =
    succMap.foldLeft (List.empty[(T,T)]) ((l, entry) => {
      val src = entry._1
      entry._2.foldLeft (l) ((l, snk) => (src, snk) :: l)
    })

}
