package ru.itmo.controlfungraphintellij.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBEmptyBorder
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SpringLayout


class FunToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = FunToolWindowPanel()
        val content = ContentFactory.getInstance().createContent(toolWindowContent, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private class FunToolWindowPanel : JPanel() {
        private val graphContentPanel = JPanel() // TODO
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