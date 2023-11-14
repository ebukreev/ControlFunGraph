package cfg.nodes.statements

import com.oracle.js.parser.ir.Node

sealed class AbstractStatement <T : Node> {
    abstract val node: T
    abstract val line: Int
}
