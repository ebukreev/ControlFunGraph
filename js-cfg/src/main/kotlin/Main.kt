import kotlin.io.path.Path
import kotlin.io.path.readText

fun main() {
    val source = Path("js-cfg/example.js").readText()
    println(JsCfgEntrypoint.buildCfg(source))
}