import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {
    val programText = Files.readString(Path.of(args[0]))
    val walker = ProgramWalker(programText)
    val program = walker.walkProgram()
//    println(program)

    val graphBuilder = CFGBuilder(program)
    val graph = graphBuilder.build()

    println(graph)
}
