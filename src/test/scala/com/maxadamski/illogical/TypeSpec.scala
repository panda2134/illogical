import com.maxadamski.illogical.UnitSpec
import com.maxadamski.illogical.Qu
import com.maxadamski.illogical.FORALL
import com.maxadamski.illogical.Var
import com.maxadamski.illogical.ConcreteType
import com.maxadamski.illogical.UnknownType
import com.maxadamski.illogical.Pred
import com.maxadamski.illogical.Con
import com.maxadamski.illogical.SexprFormatter
import scala.collection.immutable.HashMap
import com.maxadamski.illogical.EXISTS
import com.maxadamski.illogical.Skolemizer
class TypeSpec extends UnitSpec {
  describe("Typing") {
    describe("basic") {
      it("should pass typecheck") {
        val form = Qu(
          FORALL,
          Var("x", ConcreteType("Int")),
          Pred(
            "=",
            List(Var("x", UnknownType), Con("1", ConcreteType("Int"))),
            Some(List(ConcreteType("Int"), ConcreteType("Int")))
          )
        )
        val res = form.recursiveTyping(HashMap.empty)
        res should not equal None
      }
    }
    describe("skolem") {
      it("should keep types in newly-introduced skolem variables & functions") {
        val form = Qu(
          FORALL,
          Var("x", ConcreteType("Int")),
          Qu(
            EXISTS,
            Var("y", ConcreteType("Int")),
            Pred(
              "=",
              List(Var("x", UnknownType), Var("y", UnknownType)),
              Some(List(ConcreteType("Int"), ConcreteType("Int")))
            )
          )
        )
        val skolem = Skolemizer.skolemized(form)
        skolem.recursiveTyping(HashMap.empty)
        val s = SexprFormatter.fmtSexprWithType(skolem)
        s should equal ("(! (forall ((x Int)) (! (= (! x :typed Int) (! (s1 (! x :typed Int)) :typed Int)) :typed Bool)) :typed Bool)")
      }
    }
  }
}
