package cfg.nodes.statements.blocks


fun Collection<StatementsBlock>.getExitBlocks(): Set<StatementsBlock> {
    return flatMap { it.getExitBlocks() }.toSet()
}
