package types
package odf

import scala.collection.{ Seq, Map, SortedSet }
import scala.collection.immutable.{TreeSet => ImmutableTreeSet, HashMap => ImmutableHashMap }
import scala.collection.mutable.{TreeSet => MutableTreeSet, HashMap => MutableHashMap }
import scala.xml.NodeSeq
import parsing.xmlGen.xmlTypes.{ObjectsType, ObjectType}
import parsing.xmlGen.{odfDefaultScope, scalaxb, defaultScope}

class MutableODF private[odf](
  protected[odf] val nodes: MutableHashMap[Path,Node] = MutableHashMap.empty
) extends ODF[MutableHashMap[Path,Node],MutableTreeSet[Path]] {
  type M = MutableHashMap[Path,Node]
  type S = MutableTreeSet[Path]
  protected[odf] val paths: MutableTreeSet[Path] = MutableTreeSet( nodes.keys.toSeq:_* )(PathOrdering)
  def union( that: ODF[M,S]): ODF[M,S] = {
    val pathIntersection: SortedSet[Path] = this.paths.intersect( that.paths)
    val thatOnlyNodes: Set[Node] = (that.paths -- pathIntersection ).flatMap{
      case p: Path =>
        that.nodes.get(p)
    }.toSet
    val intersectingNodes: Set[Node] = pathIntersection.flatMap{
      case path: Path =>
        (this.nodes.get(path), that.nodes.get(path) ) match{
          case ( Some( node: Node ), Some( otherNode: Node ) ) =>
            (node, otherNode ) match{
              case (  ii: InfoItem , oii: InfoItem  ) => 
                Some( ii.union(oii) )
              case ( obj: Object ,  oo: Object ) =>
                Some( obj.union( oo ) )
              case ( obj: Objects ,  oo: Objects ) =>
                Some( obj.union( oo ) )
              case ( n, on) => 
                throw new Exception( 
                  "Found two different types in same Path when tried to create union." 
                )
            }
          case ( t, o) => t.orElse( o) 
        }
    }.toSet
    val allPaths = paths ++ that.paths
    val allNodes = thatOnlyNodes ++ intersectingNodes
    this.nodes ++= allNodes.map{ node => node.path -> node }
    this
  }
  def removePaths( removedPaths: Iterable[Path]) : ODF[M,S] = {
    val subtrees = removedPaths.flatMap( getSubTreePaths( _ ) )
    this.nodes --=( subtrees )
    this.paths --=( subtrees )
    this
  } 
  def immutable: ImmutableODF = ImmutableODF( 
      this.nodes.values.toVector
  )
  //Should be this? or create a copy?
  def mutable: MutableODF = MutableODF( 
      this.nodes.values.toVector
  )

  def valuesRemoved : ODF[M,S] ={
    this.nodes.mapValues{
      case ii: InfoItem => ii.copy( value = Vector() )
      case obj: Object => obj 
      case obj: Objects => obj
    }
    this
  }
  def removePath( path: Path) : ODF[M,S] ={
    val subtreeP = getSubTreePaths( path )
    this.nodes --=( subtreeP )
    this.paths --=( subtreeP )
    this
  }

  def add( node: Node) : ODF[M,S] ={
    if( !nodes.contains( node.path ) ){
      nodes( node.path) = node
      paths += node.path
      if( !nodes.contains(node.path.init) ){
        this.add( node.createParent )
      }
    } else {
      (nodes.get(node.path), node ) match{
        case (Some(old:Object), obj: Object ) =>
          nodes( node.path) = old.union(obj)
        case (Some(old:Objects), objs: Objects ) =>
          nodes( node.path) = old.union(objs)
        case (Some(old:InfoItem), iI: InfoItem ) => 
          nodes( node.path) = old.union(iI)
        case (old, n ) => 
          throw new Exception(
            "Found two different types in same Path when tried to add a new node" 
          )
      }
    }
    this
  }
  
  def addNodes( nodesToAdd: Seq[Node] ) : ODF[M,S] ={
    nodesToAdd.foreach{
      case node: Node =>
        this.add( node )
    }
    this
  }
  def getSubTreeAsODF( path: Path): ODF[M,S] = {
    val subtree: Seq[Node] = getSubTree( path)
    val ancestors: Seq[Node] = path.getAncestors.flatMap{
      case ap: Path =>
        nodes.get(ap)
    }
    MutableODF(
        (subtree ++ ancestors).toVector
    )
  }

  override def equals( that: Any ) : Boolean ={
    that match{
      case another: ODF[M,S] =>
        println( s"Path equals: ${paths equals another.paths}\n Nodes equals:${nodes equals another.nodes}" )
        (paths equals another.paths) && (nodes equals another.nodes)
      case a: Any => 
        println( s" Comparing ODF with something: $a")
        false
    }
  }
  override lazy val hashCode: Int = this.nodes.hashCode
}

object MutableODF{
  def apply(
      _nodes: Seq[Node]  = Vector.empty
  ) : MutableODF ={
    val mutableHMap : MutableHashMap[Path,Node] = MutableHashMap.empty
    val sorted = _nodes.sortBy( _.path)(PathOrdering)
    sorted.foreach{
      case node: Node =>
        if( mutableHMap.contains( node.path ) ){
            (node, mutableHMap.get(node.path) ) match{
              case (  ii: InfoItem , Some(oii: InfoItem) ) => 
                mutableHMap(ii.path) = ii.union(oii) 
              case ( obj: Object ,  Some(oo: Object) ) =>
                mutableHMap(obj.path) = obj.union( oo ) 
              case ( obj: Objects , Some( oo: Objects) ) =>
                mutableHMap(obj.path) = obj.union( oo ) 
              case ( n, on) => 
                throw new Exception( 
                  "Found two different types for same Path when tried to create ImmutableODF." 
                )
            }
        } else {
          var toAdd = node
          while( !mutableHMap.contains(toAdd.path) ){
            mutableHMap += toAdd.path -> toAdd
            toAdd = toAdd.createParent
          }
        }
    }
    new MutableODF(
      mutableHMap
    )
  }
}
