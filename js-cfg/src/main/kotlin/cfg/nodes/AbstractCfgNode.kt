package cfg.nodes

import com.oracle.js.parser.ir.Node

sealed class AbstractCfgNode <T : Node> {
    abstract val node: T
    abstract val line: Int
}
