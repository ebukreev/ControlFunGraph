package ru.itmo.controlfungraphintellij.actions

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import org.jetbrains.kotlin.idea.KotlinLanguage
import ru.itmo.controlfungraphintellij.ui.FunToolWindowFactory

class ShowCfg : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = getMethodOrFunctionFromEvent(e) != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val methodOrFunction = getMethodOrFunctionFromEvent(e)

        val psiFile = e.getData(PSI_FILE) ?: return
        val lang = psiFile.language

        renderCfg(methodOrFunction?.text, e.project!!, lang)
        println(methodOrFunction?.text ?: "null!")
    }

    private fun renderCfg(functionText: String?, project: Project, language: Language) {
        val funToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Control Fun Graph")!!
        val svgPanel = (funToolWindow.contentManager.getContent(0)!!.component
                as FunToolWindowFactory.FunToolWindowPanel).graphContentPanel

        if (language.id == "kotlin") {
            val dotText = Entrypoint.buildCfg(functionText!!)
            val dotFile = kotlin.io.path.createTempFile("", ".dot").toFile().apply {
                deleteOnExit()
                Graphviz.fromString(dotText).render(Format.SVG).toFile(this)
            }

            svgPanel.setSvgURI(dotFile.toURI())
        }
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
