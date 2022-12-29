package com.maxadamski.illogical

import scala.collection.immutable.HashMap

trait Named {
  val name: String

  require(name != "" && !name.contains(" "), s"Invalid name '$name'!")
}

trait WithArgs {
  val arguments: List[Term]

  require(!arguments.isEmpty, "Cannot pass empty argument list!")
}

case class Func(name: String, arguments: List[Term],
  signature: Option[(List[NodeType], NodeType)] = None) extends Term with WithArgs {

    require(signature == None || signature.get._1.size == arguments.size)

    override def typeCheck(context: HashMap[String, NodeType]): Option[NodeType] = signature match {
      case None => None
      case Some((argTypes, retType)) => {
        val unmatchFound = (arguments zip argTypes).map({ case (t, ty) =>
          t.recursiveTyping(context) match {
            case Some(value) if (value compatibleWith ty) => true
            case _ => false
          }
        }).find(!_)
        return unmatchFound match {
          case Some(_) => None
          case None => Some(retType)
        }
      }
    }
}

case class Var(name: String, typing: NodeType = AnyType) extends Term {
  override def typeCheck(context: HashMap[String, NodeType]): Option[NodeType] = typing match {
    case UnknownType => context.get(name)
    case _ => Some(typing)
  }
}
case class Con(name: String, typing: NodeType = AnyType) extends Term {
  override def typeCheck(context: HashMap[String, NodeType]): Option[NodeType] = typing match {
    case UnknownType => context.get(name)
    case _ => Some(typing)
  }
}

sealed abstract class Term extends Node {

  def substituting(sub: Set[Sub]): Term = this match {
    case Func(name, args, ty) => Func(name, args.map(_.substituting(sub)), ty)
    case v: Var => termForVar(v, sub) getOrElse this
    case _ => this
  }

  def vars: Set[Var] = this match {
    case Func(_, args, _) => args.map(_.vars).toSet.flatten
    case v: Var => Set(v)
    case c: Con => Set()
  }

  def cons: Set[Con] = this match {
    case Func(_, args, _) => args.map(_.cons).toSet.flatten
    case c: Con => Set(c)
    case v: Var => Set()
  }
  
  def termForVar(v: Var, subs: Set[Sub]): Option[Term] =
    subs.find(_.v.name == v.name).map(_.t)

  def renaming(x: Var, y: Var) =
    substituting(Set(Sub(x, y)))

  def contains(v: Var) = 
    vars contains v

  override def toString = 
    TextFormatter.fmt(this)

}

