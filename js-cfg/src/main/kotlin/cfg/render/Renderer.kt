package cfg.render

import cfg.nodes.FileCfgNode

object Renderer {
    private const val DOT_TOOL_PATH = "dot"

    fun render(file: FileCfgNode) {
        val generator = DotGenerator()
        val dot = generator.toDot(file)

        println("dot is:")
        println(dot)

        Runtime.getRuntime().exec(getCommand(dot)).waitFor()
    }

    private fun getCommand(dotSources: String): Array<String> {
        val processedDotSources = dotSources.replace("'", "\\'")
        return arrayOf(
            "sh",
            "-c",
            "echo '${processedDotSources}' | $DOT_TOOL_PATH -Tsvg -Kdot -n > output.svg && open output.svg"
        )
    }
}
