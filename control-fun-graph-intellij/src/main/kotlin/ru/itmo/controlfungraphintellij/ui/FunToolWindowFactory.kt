package ru.itmo.controlfungraphintellij.ui

import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import ru.itmo.controlfungraphintellij.services.DotContentService
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

class FunToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val dotContentService = project.service<DotContentService>()

        val contentPanel = dotContentService.contentPanel
        contentPanel.viewPanel.add(dotContentService.cfgPanel)

        val content = ContentFactory.getInstance().createContent(contentPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}


open class ToolWindowContentPanel : JPanel(BorderLayout()) {

    private val headerLabel = JLabel()

    private val buttonPanel = ButtonPanel()

    protected val header = JPanel(BorderLayout()).also {
        headerLabel.text = "Possible path  (0/0)"
        it.add(headerLabel, BorderLayout.WEST)
        it.add(buttonPanel, BorderLayout.EAST)
        this.add(it, BorderLayout.NORTH)
    }

    val viewPanel = JPanel()

    val mainPanel = JBScrollPane().also {
        viewPanel.layout = BoxLayout(viewPanel, BoxLayout.Y_AXIS)
        it.viewport.add(viewPanel)
        this.add(it, BorderLayout.CENTER)
    }

    fun changeCurrentPath(pathNumber: Int, totalCount: Int) {
        headerLabel.text = "Possible path  ($pathNumber/$totalCount)"
    }
}
