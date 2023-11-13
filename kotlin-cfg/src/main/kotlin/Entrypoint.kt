import guru.nidi.graphviz.engine.Format
import kastree.ast.Node
import kastree.ast.psi.Converter
import kastree.ast.psi.Parser
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.psiUtil.startOffset

object Entrypoint {
    fun buildCfg(code: String) {
        val fileAst = parseCodeWithPos(code)
        val builder = CfgBuilder(fileAst.decls.first() as Node.Decl.Func)
        val graph = builder.build()

        graph.render(Format.DOT).toOutputStream(System.out)
    }

    private fun parseCodeWithPos(code: String) = Parser(object : Converter() {
        override fun onNode(node: Node, elem: PsiElement) {
            val line = code.take(elem.startOffset).count { it == '\n' } + 1
            val col = elem.startOffset - code.lastIndexOf('\n', elem.startOffset)
            node.tag = line to col
        }
    }).parseFile(code)

}