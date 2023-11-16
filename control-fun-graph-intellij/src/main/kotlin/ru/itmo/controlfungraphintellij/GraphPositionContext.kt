package ru.itmo.controlfungraphintellij

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.util.Disposer
import org.w3c.dom.Element
import ru.itmo.controlfungraphintellij.actions.ShowPathFromRootAction
import ru.itmo.controlfungraphintellij.services.DotContentService
import java.io.File
import java.net.URL


object GraphPositionContext {

    var caretListener: CfgCaretListener? = null
    fun createForMethodLine(methodLine: Int) {
        caretListener?.let { EditorFactory.getInstance().eventMulticaster.removeCaretListener(it) }
        caretListener = CfgCaretListener(methodLine)
        EditorFactory.getInstance().eventMulticaster.addCaretListener(caretListener!!, Disposer.newDisposable())
    }

    class CfgCaretListener(private val methodFirstLine: Int) : CaretListener {

        private var previousElement: Element? = null
        var currentNodeTitle: String? = null

        override fun caretPositionChanged(event: CaretEvent) {
            val project = event.editor.project ?: return

            val dotContentService = project.service<DotContentService>()

            val svgLines = File(URL(dotContentService.cfgPanel.graphContentPanel.svgDocument.url).path).readLines()
            val commentLineInSvg = svgLines.indexOf("<!-- ${event.newPosition.line - methodFirstLine + 1} -->")

            if (commentLineInSvg == -1) return

            val edgeName = svgLines[commentLineInSvg + 1].substringAfter("id=\"").substringBefore("\"")

            val gNode = dotContentService.cfgPanel.graphContentPanel.svgDocument.rootElement.getElementById(edgeName)

            val titleNode = gNode.childNodes.item(1)
            currentNodeTitle = titleNode.textContent

            val updater = Runnable {

                dotContentService.contentPanel.changeCurrentPath(0,
                    ShowPathFromRootAction.transitionsTree?.get(currentNodeTitle)!!.size)

                val titles = dotContentService.cfgPanel.graphContentPanel.svgDocument.rootElement
                    .getElementsByTagName("title")
                ShowPathFromRootAction.previousTransition?.let {
                    for (i in 0 until titles.length) {
                        if (it.contains(titles.item(i).textContent)) {
                            (titles.item(i).parentNode.childNodes.item(3) as Element)
                                .setAttributeNS(null, "fill", "none")
                        }
                    }
                }

                val figureNode = gNode.childNodes.item(3) as Element
                if (figureNode != previousElement) {
                    figureNode.setAttributeNS(null, "fill", "#66FFB2")
                    previousElement?.setAttributeNS(null, "fill", "none")
                    previousElement = figureNode
                }
            }

            dotContentService.cfgPanel.graphContentPanel.getUpdateManager().updateRunnableQueue
                .invokeLater(updater)
        }
    }

}