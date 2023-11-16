package ru.itmo.controlfungraphintellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import org.w3c.dom.Element
import ru.itmo.controlfungraphintellij.GraphPositionContext
import ru.itmo.controlfungraphintellij.services.DotContentService

class ShowPathFromRootAction : AnAction() {
    companion object {
        var transitionsTree: Map<String, List<List<String>>>? = null

        var previousNodeTitle: String? = null
        var transitions: List<List<String>>? = null
        var previousTransition: List<String>? = null
        var currentIndex = -1
    }
    override fun actionPerformed(e: AnActionEvent) {
       actionPerformed(e, false)
    }

    fun actionPerformed(e: AnActionEvent, previousPath: Boolean) {
        val currentNodeTitle = GraphPositionContext.caretListener?.currentNodeTitle ?: return

        if (previousNodeTitle != currentNodeTitle) {
            previousNodeTitle = currentNodeTitle
            transitions = transitionsTree!![previousNodeTitle]
            previousTransition = null
            currentIndex = -1
        }

        if (previousPath) {
            --currentIndex
            if (currentIndex < 0) currentIndex = transitions!!.size - 1
        } else {
            currentIndex = ++currentIndex % transitions!!.size
        }

        val transition = transitions!![currentIndex]

        val dotContentService = e.project!!.service<DotContentService>()

        dotContentService.contentPanel.changeCurrentPath(currentIndex + 1, transitions!!.size)

        val titles = dotContentService.cfgPanel.graphContentPanel.svgDocument.rootElement
            .getElementsByTagName("title")
        val updater = Runnable {
            previousTransition?.let {
                for (i in 0 until titles.length) {
                    if (it.contains(titles.item(i).textContent)) {
                        (titles.item(i).parentNode.childNodes.item(3) as Element)
                            .setAttributeNS(null, "fill", "none")
                    }
                }
            }
            previousTransition = transition

            for (i in 0 until titles.length) {
                if (transition.contains(titles.item(i).textContent)) {
                    (titles.item(i).parentNode.childNodes.item(3) as Element)
                        .setAttributeNS(null, "fill", "green")
                }
            }
        }
        dotContentService.cfgPanel.graphContentPanel.getUpdateManager().updateRunnableQueue
            .invokeLater(updater)
    }
}