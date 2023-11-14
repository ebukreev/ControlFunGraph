import cfg.CfgBuilder
import cfg.render.DotGenerator

class JsCfgBuilder {
    fun getDot(code: String): String {
        val functionNode = AstProvider.parse(code)

        val file = CfgBuilder().visitFile(functionNode)
        val generator = DotGenerator()

        return generator.toDot(file)
    }
}
