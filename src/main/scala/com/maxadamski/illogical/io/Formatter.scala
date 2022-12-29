package com.maxadamski.illogical

trait Formatter {
  def formatted(node: Form) = {
    fmt(node)
  }

  def fmt(n: Node): String = n match {
    case x: Func => x.name + args(x.arguments)
    case x: Pred => x.name + args(x.arguments)
    case x: Not => "¬" + fmt(x.form)
    case x: Con => x.name
    case x: Var => x.name
    case x: Op => brace(x.leftForm) + " " + symbol(x.token) + " " + brace(x.rightForm)
    case x: Qu => symbol(x.token) + fmt(x.variable) + "." + brace(x.form)
  }

  def symbol(x: OpToken) = x match {
    case OR   => "∨"
    case AND  => "∧"
    case NOR  => "↓"
    case NAND => "↑"
    case XOR  => "⊕"
    case IMP  => "→"
    case EQV  => "≡"
  }

  def symbol(x: QuToken) = x match {
    case FORALL => "∀"
    case EXISTS => "∃"
  }

  def brace(n: Node) = n match {
    case x: Op => "(" + fmt(x) + ")"
    case x => fmt(x)
  }

  def args(x: List[Term]) = {
    "(" + x.map(fmt).mkString(", ") + ")"
  }
}

object TextFormatter extends Formatter

object LatexFormatter extends Formatter {
  override def formatted(node: Form) = {
    "$$" + fmt(node) + "$$"
  }

  override def fmt(n: Node) = n match {
    case x: Qu => symbol(x.token) + "_{" + fmt(x.variable) + "} " + fmt(x.form)
    case x => super.fmt(x)
  }
}

object SexprFormatter extends Formatter {
  override def formatted(node: Form): String = fmtSexpr(node)
  def formattedWithType(node: Form): String = fmtSexprWithType(node)

  val fmtSexpr: (Node) => String = fmtSexprImpl(_, fmtSexpr)
  def fmtSexprWithType(n: Node): String = s"(! ${fmtSexprImpl(n, fmtSexprWithType)} :typed ${n.lastTypingValue.getOrElse("None")})"

  def fmtSexprImpl(n: Node, fix: (Node) => String): String = n match {
    case x: Func => s"(${x.name} ${x.arguments.map(fix).mkString(" ")})"
    case x: Pred => s"(${x.name} ${x.arguments.map(fix).mkString(" ")})"
    case x: Not => s"(not ${fix(x.form)})"
    case x: Con => x.name
    case x: Var => x.name
    case x: Op => s"(${symbol(x.token)} ${fix(x.leftForm)} ${fix(x.rightForm)})"
    case x: Qu => s"(${symbol(x.token)} ((${x.variable.name} ${x.variable.typing})) ${fix(x.form)})"
  }

  override def symbol(x: OpToken) = x match {
    case OR   => "or"
    case AND  => "and"
    case NOR  => "nor"
    case NAND => "nand"
    case XOR  => "xor"
    case IMP  => "=>"
    case EQV  => "="
  }

  override def symbol(x: QuToken) = x match {
    case FORALL => "forall"
    case EXISTS => "exists"
  }
}