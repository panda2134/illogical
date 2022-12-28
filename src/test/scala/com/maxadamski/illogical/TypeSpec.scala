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
        form.recursiveTyping(HashMap.empty)
        println(SexprFormatter.fmtSexprWithType(form))
      }
    }
  }
}
