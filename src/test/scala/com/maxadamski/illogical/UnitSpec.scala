package com.maxadamski.illogical

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

abstract class UnitSpec extends AnyFunSpec with Matchers {
  def shouldParse(formula: String, result: Form, comment: String = "") {
    it(s"should parse `$formula`" + comment) {
      Parser.parse(formula) shouldEqual Some(result)
    }
  }

  def shouldEqual(message: String, in: String, out: String, transform: Form => Form): Unit = {
    it(message) {
      Parser.parse(in).map(transform) shouldEqual Parser.parse(out)
    }
  }

  def itShouldPNF(in: String, out: String): Unit = {
    shouldEqual(s"should pnf $in", in, out, _.pnf)
  }

  def itShouldSimplify(in: String, out: String): Unit = {
    shouldEqual(s"should simplify $in", in, out, _.simplifying)
  }

  def itShouldCNF(in: String, out: String): Unit = {
    shouldEqual(s"should cnf $in", in, out, _.cnf)
  }

  def itShouldSkolemize(in: String, out: Form): Unit = {
    it(s"should skolemize $in") {
      Skolemizer.skolemized(out) shouldEqual out
    }
  }

  def itShouldSkolemize(in: String, out: String): Unit = {
    it(s"should skolemize $in") {
      val skol = Parser.parse(in).map(Skolemizer.skolemized)
      skol shouldEqual Parser.parse(out)
      skol should not equal None
    }
  }

  def itShouldMGU(pString: String, qString: String, mgu: Set[Sub]): Unit = {
    val (p, q) = (Parser.parse(pString).get, Parser.parse(qString).get)
    it(s"should mgu $pString and $qString") {
      Unifier.mgu(p, q) shouldEqual Some(mgu)
    }
  }

  def itShouldNotMGU(pString: String, qString: String): Unit = {
    val (p, q) = (Parser.parse(pString).get, Parser.parse(qString).get)
    it(s"should not mgu $pString and $qString") {
      Unifier.mgu(p, q) shouldEqual None
    }
  }


  val (x, y, z) = (Var("x"), Var("y"), Var("z"))
  val (p, q, r) = (Pred("p", List(x)), Pred("q", List(x)), Pred("r", List(x)))
}
