object RustCfgEntrypoint {
    fun buildCfg(code: String): String {
        val walker = ProgramWalker(code)
        val program = walker.walkProgram()

        val graphBuilder = CFGBuilder(program)
        val graph = graphBuilder.build()

        return graph
    }
}