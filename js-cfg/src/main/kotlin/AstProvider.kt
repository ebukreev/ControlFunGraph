import com.oracle.js.parser.ErrorManager.ThrowErrorManager
import com.oracle.js.parser.Parser
import com.oracle.js.parser.ScriptEnvironment
import com.oracle.js.parser.Source
import com.oracle.js.parser.ir.FunctionNode

object AstProvider {
    private val errorManager = ThrowErrorManager()

    fun parse(code: String): FunctionNode {
        val env = ScriptEnvironment.builder().scripting(true).build()
        val parser = Parser(env, Source.sourceFor("code", code), errorManager)

        return parser.parse()
    }
}
