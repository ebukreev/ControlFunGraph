import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableNode
import guru.nidi.graphviz.toGraphviz
import kastree.ast.Node

class CfgBuilder(private val functionAst: Node.Decl.Func) {

    private val graph = graph(directed = true)
    private var currentNode: MutableNode? = toMutableNode(functionAst).add(Shape.ELLIPSE).also { graph.add(it) }

    fun build(): Graphviz {
        buildFunction(functionAst)
        return graph.toGraphviz()
    }

    private fun buildFunction(function: Node.Decl.Func) {
        when (function.body) {

            is Node.Decl.Func.Body.Expr -> {
                val body = function.body as Node.Decl.Func.Body.Expr
                buildExpression(body.expr)
            }

            is Node.Decl.Func.Body.Block -> {
                val body = function.body as Node.Decl.Func.Body.Block
                buildBlock(body.block)
            }

            null -> {}
        }
    }

    var prevIsCycle = mutableListOf<Boolean>()
    private fun buildExpression(expr: Node.Expr) {

        fun x() {
            when (expr) {
                is Node.Expr.If -> buildIf(expr).also { prevIsCycle.add(false) }
                is Node.Expr.For -> buildFor(expr).also { prevIsCycle.add(true) }
                is Node.Expr.While -> buildWhile(expr).also { prevIsCycle.add(true) }
                is Node.Expr.When -> buildWhen(expr).also { prevIsCycle.add(false) }
                is Node.Expr.Brace -> buildBrace(expr).also { prevIsCycle.add(false) }
                is Node.Expr.Return -> buildWithoutContinuation(expr).also { prevIsCycle.add(false) }
                is Node.Expr.Break -> buildBreak(expr).also { prevIsCycle.add(false) }
                is Node.Expr.Continue -> buildContinue(expr).also { prevIsCycle.add(false) }
                else -> linkNode(expr).also { prevIsCycle.add(false) }
            }
        }

        val p = if (prevIsCycle.isNotEmpty()) prevIsCycle.last().also { prevIsCycle.removeLast() } else false

        if (p) {
            if (breakWaiter.isNotEmpty()) {
                val last = breakWaiter.last()
                x()
                val m = toMutableNode(expr)
                graph.add(m)
                graph.use { gr, ctx ->
                    last!!.links().add(last.linkTo(m))
                }
                breakWaiter.removeLast()
            }
            else {
                x()
            }
        } else {
            x()
        }

    }

    private var suspendedSources = mutableSetOf<MutableNode>()
    private var suspendedTargets = mutableSetOf<MutableNode>()

    private var currentCondition: Boolean? = null
    private fun buildIf(ifExpr: Node.Expr.If) {
        linkNode(ifExpr)
        currentCondition = true
        val startPtr = currentNode
        buildExpression(ifExpr.body)
        val truePtr = currentNode
        val suspendedSrc = suspendedSources.apply { clear() }
        val suspendedTrg = suspendedTargets.apply { clear() }
        currentNode = startPtr
        if (ifExpr.elseBody != null) {
            currentCondition = false
            val elseBody = ifExpr.elseBody as Node.Expr
            buildExpression(elseBody)
        }
        currentCondition = null

        truePtr?.let { suspendedSrc.add(it) }
        suspendedSources.addAll(suspendedSrc)
        suspendedTargets.addAll(suspendedTrg)
    }

    private var breakContinueWaiter: MutableNode? = null

    private fun buildFor(forExpr: Node.Expr.For) {
        linkNode(forExpr)
        currentCondition = true
        val previousWaiter = breakContinueWaiter
        breakContinueWaiter = currentNode
        val loopStart = currentNode
        buildExpression(forExpr.body)
        currentCondition = null
        if (loopStart != null && currentNode != null) {
            graph.add(currentNode, loopStart)
            linkWaiting(loopStart)
        }
        currentNode = loopStart
        breakContinueWaiter = previousWaiter
    }

    private fun buildWhile(whileExpr: Node.Expr.While) {

        if (whileExpr.doWhile) {
            buildDoWhile(whileExpr)
        } else {
            linkNode(whileExpr)
            currentCondition = true
            val previousWaiter = breakContinueWaiter
            breakContinueWaiter = currentNode
            val whileNode = currentNode
            buildExpression(whileExpr.body)
            currentCondition = null
            if (currentNode != null) {
                graph.use { gr, ctx ->
                    if (whileNode != null) {
                        gr.links().add(currentNode!!.linkTo(whileNode))
                    }
                }
                linkWaiting(whileNode)
            }
            currentNode = whileNode
            breakContinueWaiter = previousWaiter
        }

    }

    private fun buildDoWhile(whileExpr: Node.Expr.While) {
        val node: MutableNode = toMutableNode(whileExpr)
        graph.add(node)
        suspendedSources.add(node)
        buildExpression(whileExpr.body)

        if (currentNode != null) {
            val previousWaiter = breakContinueWaiter
            breakContinueWaiter = currentNode
            graph.use { gr, ctx ->
                currentNode!!.links().add(currentNode!!.linkTo(node))
            }
            currentCondition = true
            linkWaiting(node, true)
            currentCondition = null
            breakContinueWaiter = previousWaiter
        }
        currentNode = node
    }

    private fun buildWhen(whenExpr: Node.Expr.When) {
        linkNode(whenExpr)
        val whenNode = currentNode
        val outs = mutableSetOf<MutableNode>()
        for (expr in whenExpr.entries) {
            val sources = mutableSetOf<MutableNode>()
            for (cond in expr.conds) {
                currentNode = whenNode
                linkNode(
                    when (cond) {
                        is Node.Expr.When.Cond.Expr -> cond.expr
                        is Node.Expr.When.Cond.In -> cond.expr
                        is Node.Expr.When.Cond.Is -> cond.type
                    }
                )
            }
            suspendedSources = sources
            currentCondition = true
            buildExpression(expr.body)
            currentCondition = null
            val out = currentNode
            if (out != null) {
                outs.add(out)
            }
        }
        suspendedSources = outs
    }

    private fun buildBrace(braceExpr: Node.Expr.Brace) {
        if (braceExpr.block != null) {
            val block = braceExpr.block as Node.Block
            buildBlock(block)
        }
    }

    private fun buildWithoutContinuation(expression: Node.Expr) {
        linkNode(expression)
        currentNode = null
    }

    private fun buildBreak(expression: Node.Expr) {
//        linkNode(currentNode!!, false)
//        currentNode = null
//        suspendedTargets.add(toMutableNode(expression))
//        if (waiter != null)
//            suspendedSources = mutableSetOf(waiter)

        breakWaiter.add(currentNode!!)
        currentNode = null
    }

    private fun buildContinue(expression: Node.Expr) {
//        linkNode(currentNode!!, false)
        val waiter = currentNode
//        currentNode = null
//        suspendedTargets.add(toMutableNode(expression))
//        if (waiter != null)
//            suspendedSources = mutableSetOf(waiter)

        if (breakContinueWaiter != null) {
            graph.use { gr, ctx ->
                currentNode!!.links().add((currentNode!!.linkTo(breakContinueWaiter)))
            }
        }
        currentNode = null
    }

    private var breakWaiter = mutableListOf<MutableNode>()
    private fun buildBlock(block: Node.Block) {
        for (stmt in block.stmts) {
           buildStatement(stmt)

        }
    }

    private fun buildDeclaration(decl: Node.Decl) {
        when (decl) {
            is Node.Decl.Property -> linkNode(decl)
            else -> throw Exception("")
        }
    }

    private fun buildStatement(stmt: Node.Stmt) {
        when (stmt) {
            is Node.Stmt.Decl -> buildDeclaration(stmt.decl)
            is Node.Stmt.Expr -> buildExpression(stmt.expr)
        }
    }

    private fun linkNode(node: Node, addNode: Boolean = true) {
        linkNode(toMutableNode(node), addNode)
    }

    private fun linkNode(node: MutableNode, addNode: Boolean = true) {
        if (addNode) graph.add(node)
        if (currentNode != null) {
            graph.use { gr, ctx ->
                if (currentCondition == true && currentNode!!.attrs().any { it.value == "diamond" }) {
                    currentNode!!.links().add((currentNode!!.linkTo(node).add(Label.of("true"))))
                } else if (currentCondition == false && currentNode!!.attrs().any { it.value == "diamond" }) {
                    currentNode!!.links().add((currentNode!!.linkTo(node).add(Label.of("false"))))
                } else {
                    currentNode!!.links().add((currentNode!!.linkTo(node)))

                }
            }
        }
        linkWaiting(node)
        currentNode = node
    }

    private fun linkWaiting(target: MutableNode?, needTrue: Boolean = false) {
        if (suspendedSources.isNotEmpty()) {
            for (suspendedSource in suspendedSources) {
                if (target != null)
                    graph.use { gr, ctx ->
                        if (suspendedSource.any { it.value == "diamond" } && suspendedSource.name().toString().contains("while"))
                            suspendedSource.links().add(suspendedSource.linkTo(target).add(Label.of("true")))
                            else
                        suspendedSource.links().add(suspendedSource.linkTo(target))
                    }
            }
            suspendedSources = mutableSetOf()
        }
        if (suspendedTargets.isNotEmpty()) {
            for (suspendedTarget in suspendedTargets) {
                if (target != null)
                    graph.use { gr, ctx ->
                        target.links().add(target.linkTo(suspendedTarget))
                    }
            }
            suspendedTargets = mutableSetOf()
        }
    }

    private fun toMutableNode(node: Node): MutableNode {
        val text = when (node) {
            is Node.Decl -> declToString(node)
            is Node.Expr -> exprToString(node)
            is Node.Expr.When.Cond -> condToString(node)
            is Node.Type -> typeToString(node)
            else -> "${node::class}"
        }
        val mutNode = mutNode(text)
        return when (node) {
            is Node.Expr.If -> mutNode.add(Shape.DIAMOND)
            is Node.Expr.For -> mutNode.add(Shape.DIAMOND)
            is Node.Expr.While -> mutNode.add(Shape.DIAMOND)
            is Node.Expr.When.Cond -> mutNode.add(Shape.DIAMOND)
            else -> mutNode.add(Shape.BOX)
        }
    }

    private fun typeToString(node: Node.Type?): String {
        node ?: return ""
        return when (val typeRef = node.ref) {
                is Node.TypeRef.Simple -> typeRef.pieces.joinToString { it.name }
                else -> "TYPE"
            }
    }

    private fun propToString(prop: Node.Decl.Property?): String {
        prop ?: return ""
        val name = prop.vars[0]?.name
        val value = if (prop.expr != null) exprToString(prop.expr) else ""
        return "$name${if (value != "") " = $value" else ""}"
    }

    private fun varToString(variable: Node.Decl.Property.Var?): String {
        variable ?: return ""
        return variable.name
    }

    private fun condToString(cond: Node.Expr.When.Cond): String {
        return when (cond) {
            is Node.Expr.When.Cond.Expr -> "(==)${exprToString(cond.expr)}"
            is Node.Expr.When.Cond.In -> "${if (cond.not) "!in" else "in"}${cond.expr}"
            is Node.Expr.When.Cond.Is -> "${if (cond.not) "!is" else "is"}${typeToString(cond.type)}"
        }
    }

    private fun exprToString(expr: Node.Expr?): String {
        return when (expr) {
            is Node.Expr.StringTmpl -> expr.elems.joinToString { it: Node.Expr.StringTmpl.Elem ->
                when (it) {
                    is Node.Expr.StringTmpl.Elem.Regular -> "\'${it.str}\'"
                    is Node.Expr.StringTmpl.Elem.ShortTmpl -> it.str
                    is Node.Expr.StringTmpl.Elem.UnicodeEsc -> it.digits
                    is Node.Expr.StringTmpl.Elem.RegularEsc -> it.char.toString()
                    is Node.Expr.StringTmpl.Elem.LongTmpl -> exprToString(it.expr)
                }
            }

            is Node.Expr.Const -> expr.value
            is Node.Expr.When -> "when ${exprToString(expr.expr)}"
            is Node.Expr.Return -> "return ${exprToString(expr.expr)}"
            is Node.Expr.While -> "while ${exprToString(expr.expr)}"
            is Node.Expr.BinaryOp -> {
                val oper = expr.oper
                return "${exprToString(expr.lhs)} ${
                    when (oper) {
                        is Node.Expr.BinaryOp.Oper.Infix -> oper.str
                        is Node.Expr.BinaryOp.Oper.Token -> oper.token.str
                    }
                } ${exprToString(expr.rhs)}"
            }

            is Node.Expr.UnaryOp -> if (expr.prefix) "${expr.oper.token.str}${exprToString(expr.expr)}"
                                    else "${exprToString(expr.expr)}${expr.oper.token.str}"

            is Node.Expr.If -> "if ${exprToString(expr.expr)}"

            is Node.Expr.Try -> "try ${
                expr.block.stmts.joinToString { stmt ->
                    when (stmt) {
                        is Node.Stmt.Decl -> declToString(stmt.decl)
                        is Node.Stmt.Expr -> exprToString(stmt.expr)
                    }
                }
            }"

            is Node.Expr.For -> "${varToString(expr.vars[0])} in ${exprToString(expr.inExpr)}"
            is Node.Expr.TypeOp -> "${expr.lhs} ${expr.oper} ${expr.rhs}"
            is Node.Expr.DoubleColonRef.Callable -> expr.name
            is Node.Expr.DoubleColonRef.Class -> "::${expr.recv?.toString()}"
            is Node.Expr.Paren -> exprToString(expr.expr)
            is Node.Expr.This -> "this${expr.label ?: ""}"
            is Node.Expr.Super -> "super${expr.label ?: ""}"
            is Node.Expr.Throw -> "throw ${exprToString(expr.expr)}"
            is Node.Expr.Continue -> "continue ${expr.label ?: ""}"
            is Node.Expr.Break -> "break ${expr.label ?: ""}"
            is Node.Expr.CollLit -> expr.exprs.joinToString { exprToString(it) }
            is Node.Expr.Name -> expr.name
            is Node.Expr.Labeled -> "${expr.label}: ${exprToString(expr.expr)}"
            is Node.Expr.Annotated -> exprToString(expr.expr)
            is Node.Expr.Call -> "${exprToString(expr.expr)}(${expr.args.joinToString { exprToString(it.expr) }})"
            is Node.Expr.ArrayAccess -> "${exprToString(expr.expr)}[${expr.indices.joinToString { exprToString(it) }}]"
            else -> ""
        }
    }

    private fun declToString(decl: Node.Decl): String {
        return when (decl) {
            is Node.Decl.Property -> propToString(decl)
            is Node.Decl.Structured -> "structured"
            is Node.Decl.Init -> "init"
            is Node.Decl.Func -> "function ${decl.name}(${decl.params.joinToString(",") { it.name }})"
            is Node.Decl.TypeAlias -> "typealias"
            is Node.Decl.Constructor -> "constructor"
            is Node.Decl.EnumEntry -> "enum"
        }
    }
}
