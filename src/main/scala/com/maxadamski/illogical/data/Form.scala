package com.maxadamski.illogical

import scala.collection.immutable.Map

case class Pred(name: String, arguments: List[Term], signature: Option[(List[NodeType])] = None) extends Form with WithArgs with Named {

  require(signature == None || signature.get.size == arguments.size)

  override def typeCheck(context: Map[String, NodeType]): Option[NodeType] = signature match {
    case None => None
    case Some(argTypes) => {
      val unmatchFound = (arguments zip argTypes).map({ case (t, ty) =>
        t.recursiveTyping(context) match {
          case Some(value) if (value compatibleWith ty) => true
          case _ => false
        }
      }).find(!_)
      return unmatchFound match {
        case Some(_) => None
        case None => Some(ConcreteType("Bool"))
      }
    }
  }
}

case class Not(form: Form) extends Form {
  override def typeCheck(context: Map[String, NodeType]): Option[NodeType] = form.recursiveTyping(context) match {
    case Some(ty) if ty compatibleWith ConcreteType("Bool") => Some(ConcreteType("Bool"))
    case _ => None
  }
}

case class Qu(token: QuToken, variable: Var, form: Form) extends Form {

  override def typeCheck(context: Map[String, NodeType]): Option[NodeType] = {
    if (variable.typing == UnknownType) {
      return None
    } else {
      return form.recursiveTyping(context + (variable.name -> variable.typing))
    }
  }
}

case class Op(leftForm: Form, token: OpToken, rightForm: Form) extends Form {

  def isAssociative = 
    token.isAssociative

  def isCommutative = 
    token.isCommutative

  def equals(o: Op) =
    token == o.token && ((leftForm == o.leftForm && rightForm == o.rightForm) || 
      (leftForm == o.rightForm && rightForm == o.leftForm && token.isCommutative))

  override def equals(o: Any) = o match {
    case o: Op => equals(o)
    case _ => false
  }

  override def typeCheck(context: Map[String, NodeType]): Option[NodeType] = Some(ConcreteType("Bool"))
}

sealed abstract class Form extends Node with LogicLaws {

  def isAtom: Boolean = this match {
    case _: Pred => true
    case _ => false
  }

  def isLiteral: Boolean = this match {
    case Not(term) => term.isAtom
    case _ => this.isAtom
  }

  def isClause: Boolean = this match {
    // """correct clause"""
    case Op(a, OR, b) if a.isLiteral && b.isLiteral => true 
    // long clause
    case Op(a, OR, b) if a.isClause && b.isClause => true 
    // degenerate clause
    case _ if isLiteral => true
    // not a clause at all
    case _ => false 
  }

  def clauseLength: Int = literals.size

  def literals: Set[Form] = this match {
    case Op(a, _, b) if isClause => a.literals ++ b.literals
    case _ if isLiteral => Set(this)
    case _ => Set()
  }

  def clauses: Set[Form] = this match {
    case _ if isClause => Set(this)
    case Op(p, _, q) => p.clauses ++ q.clauses
    case Qu(_, _, p) => p.clauses
    case _ => Set()
  }

  def pnf: Form = {
    val (suffix, qus) = simplifying.partialPNF
    suffix.wrapped(qus)
  }

  def cnf: Form = {
    var last = this.pnf
    while (true) {
      val simpler = last._cnf
      if (last == simpler) 
        return simpler
      else
        last = simpler
    }
    last
  }

  def dnf: Form = {
    var last = this.pnf
    while (true) {
      val simpler = last._dnf
      if (last == simpler) 
        return simpler
      else
        last = simpler
    }
    last
  }

  def _cnf: Form = this match {
    case Op(p, OR, Op(q, AND, r)) => 
      Op(Op(p._cnf, OR, q._cnf), AND, Op(p._cnf, OR, r._cnf))
    case Op(Op(q, AND, r), OR, p) =>
      Op(Op(p._cnf, OR, q._cnf), AND, Op(p._cnf, OR, r._cnf))
    case Op(p, t, q) => 
      Op(p._cnf, t, q._cnf)
    case Qu(token, v, p) =>
      Qu(token, v, p._cnf)
    case Not(p) =>
      Not(p._cnf)
    case _ =>
      this
  }

  def _dnf: Form = this match {
    case Op(p, AND, Op(q, OR, r)) => 
      Op(Op(p._dnf, AND, q._dnf), OR, Op(p._dnf, AND, r._dnf))
    case Op(Op(q, OR, r), AND, p) =>
      Op(Op(p._dnf, AND, q._dnf), OR, Op(p._dnf, AND, r._dnf))
    case Op(p, t, q) => 
      Op(p._dnf, t, q._dnf)
    case Qu(token, v, p) =>
      Qu(token, v, p._dnf)
    case Not(p) =>
      Not(p._dnf)
    case _ =>
      this
  }

  def substituting(g: Set[Sub]): Form = this match {
    case Op(p, t, q) => Op(p.substituting(g), t, q.substituting(g))
    case Qu(t, v, p) => Qu(t, v, p.substituting(g))
    case Pred(t, a, ty) => Pred(t, a.map(_.substituting(g)), ty)
    case Not(p) => Not(p.substituting(g))
  }

  def renaming(v1: Var, v2: Var): Form = this match {
    case Pred(t, args, ty) =>
      Pred(t, args.map(_.renaming(v1, v2)), ty)
    case Op(p, t, q) =>
      Op(p.renaming(v1, v2), t, q.renaming(v1, v2))
    case Qu(t, v, p) =>
      val vNew = if (v == v1) v2 else v
      Qu(t, vNew, p.renaming(v1, v2))
    case Not(p) =>
      Not(p.renaming(v1, v2))
  }

  def simplifying: Form = {
    var last = this
    while (true) {
      val simpler = last
        .simplifyingOperators
        .simplifyingNegation
      if (last == simpler) 
        return simpler
      else
        last = simpler
    }
    last
  }

  def wrapped(qs: List[PartialQu]): Form = qs match {
    case x :: _ => 
      qs.last.complete(this).wrapped(qs.dropRight(1))
    case Nil => 
      this
  }

  def partialPNF: (Form, List[PartialQu]) = {
    val (suffix, vars, qus) = partialPNF(List(), List())
    (suffix, qus)
  }

  def partialPNF(vars: List[Var], qs: List[PartialQu]): (Form, List[Var], List[PartialQu]) = this match {
    case Qu(t, v, p) =>
      var newVar = v
      while (vars.contains(newVar)) newVar = Var(newVar.name + "'")
      val renamed = p.renaming(v, newVar)
      val (newP, newVars, newQs) = renamed.partialPNF(vars :+ newVar, qs)
      (newP, newVars, PartialQu(t, newVar) +: newQs)
    case Op(p, t, q) =>
      val (newP, newPVars, newPQs) = p.partialPNF(vars, qs)
      val (newQ, newQVars, newQQs) = q.partialPNF(newPVars, qs)
      (Op(newP, t, newQ), newPVars ++ newQVars, newPQs ++ newQQs)
    case Not(p) =>
      val (newP, newVars, newQs) = p.partialPNF(vars, qs)
      (Not(newP), newVars, newQs)
    case Pred(_, _, _) =>
      (this, vars, qs)
  }

  def simplifyingBinaryOperator: Option[Form] = this match {
      case Op(_, NAND,_) => expand_nand(this)
      case Op(_, NOR, _) => expand_nor(this)
      case Op(_, XOR, _) => expand_xor(this)
      case Op(_, IMP, _) => expand_imp(this)
      case Op(_, EQV, _) => expand_eqv(this)
      case _ => None
  }

  def simplifyingOperators: Form = this match {
    case Op(_, NAND | NOR | XOR | IMP | EQV, _) =>
      // TODO: do something about the force unwrapping
      simplifyingBinaryOperator.get.simplifyingOperators

    case Op(p, t, q) =>
      val p2 = p.simplifyingOperators
      val q2 = q.simplifyingOperators
      Op(p2, t, q2)

    case Qu(t, v, p) =>
      val p2 = p.simplifyingOperators
      Qu(t, v, p2)

    case Not(p) =>
      val p2 = p.simplifyingOperators
      Not(p2)

    case Pred(_, _, _) =>
      this
  }

  def simplifyingNegation: Form = this match {
    // TODO: do something about the force unwrapping
    case Not(form) => form match {
      case Qu(_, _, _) =>
        expand_not_quantifier(this).get.simplifyingNegation
      case Not(_) =>
        expand_not_not(this).get
      case Op(_, AND, _) | Op(_, OR, _) =>
        expand_de_morgan(this).get
      case Op(_, _, _) =>
        simplifyingOperators.simplifyingNegation
      case Pred(_, _, _) =>
        this
    }
    case Op(form_a, token, form_b) =>
      Op(form_a.simplifyingNegation, token, form_b.simplifyingNegation)
    case Qu(token, variable, form) =>
      Qu(token, variable, form.simplifyingNegation)
    case Pred(_, _, _) =>
      this
  }

  override def toString: String = 
    TextFormatter.formatted(this)

}
