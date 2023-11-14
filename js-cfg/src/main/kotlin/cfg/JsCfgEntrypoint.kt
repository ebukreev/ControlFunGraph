import cfg.CfgBuilder
import cfg.render.DotGenerator

object JsCfgEntrypoint {
    fun buildCfg(code: String): String {
        val functionNode = AstProvider.parse(code)

        val file = CfgBuilder().visitFile(functionNode)
        val generator = DotGenerator()

        return generator.toDot(file)
    }
}
