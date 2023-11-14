package ru.itmo.controlfungraphintellij.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.kitfox.svg.app.beans.SVGPanel
import javax.swing.JPanel
import javax.swing.SpringLayout


class FunToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = FunToolWindowPanel()
        val content = ContentFactory.getInstance().createContent(toolWindowContent, "", false)
        toolWindow.contentManager.addContent(content)
    }

    class FunToolWindowPanel : JPanel() {
        val graphContentPanel = SVGPanel()
        private val scrollPane = JBScrollPane(graphContentPanel)

        init {
            val springLayout = SpringLayout()
            layout = springLayout

            springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, this)
            springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, this)
            springLayout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, this)
            springLayout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, this)
            scrollPane.isOpaque = false
            this.add(scrollPane)
        }

    }

}