package ru.itmo.controlfungraphintellij

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.util.Disposer
import org.w3c.dom.svg.SVGPolygonElement
import ru.itmo.controlfungraphintellij.services.DotContentService
import java.io.File
import java.net.URL


object GraphPositionContext {

    private var caretListener: CfgCaretListener? = null
    fun createForMethodLine(methodLine: Int) {
        caretListener?.let { EditorFactory.getInstance().eventMulticaster.removeCaretListener(it) }
        caretListener = CfgCaretListener(methodLine)
        EditorFactory.getInstance().eventMulticaster.addCaretListener(caretListener!!, Disposer.newDisposable())
    }

    private class CfgCaretListener(private val methodFirstLine: Int) : CaretListener {

        private var previousElement: SVGPolygonElement? = null

        override fun caretPositionChanged(event: CaretEvent) {
            val project = event.editor.project ?: return

            val dotContentService = project.service<DotContentService>()

            val svgLines = File(URL(dotContentService.cfgPanel.graphContentPanel.svgDocument.url).path).readLines()
            val commentLineInSvg = svgLines.indexOf("<!-- ${event.newPosition.line - methodFirstLine + 1} -->")

            if (commentLineInSvg == -1) return

            val edgeName = svgLines[commentLineInSvg + 1].substringAfter("id=\"").substringBefore("\"")

            val gNode = dotContentService.cfgPanel.graphContentPanel.svgDocument.rootElement.getElementById(edgeName)

            val updater = Runnable {
                for (i in 0 until gNode.childNodes.length) {
                    val child = gNode.childNodes.item(i)
                    if (child is SVGPolygonElement) {
                        child.setAttributeNS(null, "fill", "green")
                        previousElement?.setAttributeNS(null, "fill", "none")
                        previousElement = child
                    }
                }
            }

            dotContentService.cfgPanel.graphContentPanel.getUpdateManager().updateRunnableQueue
                .invokeLater(updater)
        }
    }

}