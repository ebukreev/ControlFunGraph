package ru.itmo.controlfungraphintellij.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import ru.itmo.controlfungraphintellij.actions.ShowPathFromRootAction

class ButtonPanel : ActionToolbarImpl(
    ActionPlaces.UNKNOWN,
    DefaultActionGroup(
        PreviousPathAction(),
        NextPathAction(),
    ),
    true,
    true,
    false
) {
    init {
        actions[0].templatePresentation.icon = AllIcons.Actions.Back
        actions[1].templatePresentation.icon = AllIcons.Actions.Forward
    }
}

class NextPathAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ShowPathFromRootAction().actionPerformed(e)
    }
}

class PreviousPathAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ShowPathFromRootAction().actionPerformed(e,true)
    }
}

