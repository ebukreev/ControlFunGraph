package ru.itmo.controlfungraphintellij.ui

import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import ru.itmo.controlfungraphintellij.services.DotContentService

class FunToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val dotContentService = project.service<DotContentService>()
        val toolWindowContent = dotContentService.cfgPanel

        val content = ContentFactory.getInstance().createContent(toolWindowContent, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
