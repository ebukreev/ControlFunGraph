package cfg.nodes

import com.oracle.js.parser.ir.FunctionNode

// In graal-js, files are represented as a function
// with declarations in its body
class FileCfgNode(
    val functions: List<FunctionCfgNode>,
    override val node: FunctionNode,
    override var line: Int
) : AbstractCfgNode<FunctionNode>()
