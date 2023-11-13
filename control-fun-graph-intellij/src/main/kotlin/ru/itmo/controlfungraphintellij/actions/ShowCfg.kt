package ru.itmo.controlfungraphintellij.actions

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys.*
import com.intellij.psi.PsiElement

class ShowCfg : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = getMethodOrFunctionFromEvent(e) != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val methodOrFunction = getMethodOrFunctionFromEvent(e)
        println(methodOrFunction?.text ?: "null!")
    }

    private fun getMethodOrFunctionFromEvent(e: AnActionEvent): PsiElement? {
        val psiFile = e.getData(PSI_FILE) ?: return null
        val caret = e.getData(CARET) ?: return null
        val lang = psiFile.language

        if (!checkLanguageIsSupported(lang)) {
            return null
        }

        val psiElement = psiFile.findElementAt(caret.offset) ?: return null

        return getParentMethodOrFunction(psiElement)
    }

    private fun checkLanguageIsSupported(languages: Language): Boolean {
        val supportedLangs = listOf("CSharpLanguage", "ECMA6Language", "kotlin", "rust")

        return supportedLangs.any { lang -> languages::class.java.name.contains(lang) }
    }

    private fun getParentMethodOrFunction(element: PsiElement): PsiElement? {
        val targetClassNames = listOf(
            "CSharpMethodDeclaration",
            "JSFunction",
            "KtNamedFunction",
            "RsFunction"
        )

        var parent = element.parent

        while (parent != null) {
            if (targetClassNames.any { klass -> parent::class.java.name.contains(klass) }) {
                return parent
            }

            parent = parent.parent
        }

        return null
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
