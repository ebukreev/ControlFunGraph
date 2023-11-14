import guru.nidi.graphviz.engine.Format
import kastree.ast.Node
import kastree.ast.psi.Converter
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer.PLAIN_RELATIVE_PATHS
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.startOffset

object Entrypoint {
    fun buildCfg(code: String): String {
        val fileAst = parseCodeWithPos(code)
        val builder = CfgBuilder(fileAst.decls.first() as Node.Decl.Func)
        val graph = builder.build()

        return graph.render(Format.DOT).toString()
    }

    private fun parseCodeWithPos(code: String) = Parser(object : Converter() {
        override fun onNode(node: Node, elem: PsiElement) {
            val line = code.take(elem.startOffset).count { it == '\n' } + 1
            val col = elem.startOffset - code.lastIndexOf('\n', elem.startOffset)
            node.tag = line to col
        }
    }).parseFile(code)

    private open class Parser(private val converter: Converter = Converter) {
        private val configuration = CompilerConfiguration().apply {
            put(
                CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                PrintingMessageCollector(System.err, PLAIN_RELATIVE_PATHS, false)
            )
        }

        private val proj by lazy {
            KotlinCoreEnvironment.createForProduction(
                Disposer.newDisposable(),
                configuration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES
            ).project
        }

        fun parseFile(code: String) = converter.convertFile(parsePsiFile(code))

        fun parsePsiFile(code: String) =
            PsiManager.getInstance(proj).findFile(LightVirtualFile("temp.kt", KotlinFileType.INSTANCE, code)) as KtFile

        data class ParseError(
            val file: KtFile,
            val errors: List<PsiErrorElement>
        ) : IllegalArgumentException("Failed with ${errors.size} errors, first: ${errors.first().errorDescription}")

        companion object : Parser() {
            init {
                // To hide annoying warning on Windows
                System.setProperty("idea.use.native.fs.for.win", "false")
            }
        }
    }
}