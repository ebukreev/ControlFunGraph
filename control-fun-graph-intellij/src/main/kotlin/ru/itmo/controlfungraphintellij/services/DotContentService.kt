package ru.itmo.controlfungraphintellij.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import ru.itmo.controlfungraphintellij.ui.CfgPanel
import ru.itmo.controlfungraphintellij.ui.ToolWindowContentPanel

@Service(Service.Level.PROJECT)
class DotContentService private constructor(private val project: Project) {
    val cfgPanel = CfgPanel()
    val contentPanel = ToolWindowContentPanel()
}
