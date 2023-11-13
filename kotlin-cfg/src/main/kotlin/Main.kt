import guru.nidi.graphviz.engine.Format
import kastree.ast.Node
import kastree.ast.psi.Parser
import java.io.File

fun main() {
    val fileName = "example.kt"
    val file = File(fileName)

    val codeStr = file.readText()
    val fileAst = Parser.parseFile(codeStr)
    val builder = CfgBuilder(fileAst.decls.first() as Node.Decl.Func)
    val graph = builder.build()

    graph.render(Format.PNG).toFile(File("./example.png"))
}
