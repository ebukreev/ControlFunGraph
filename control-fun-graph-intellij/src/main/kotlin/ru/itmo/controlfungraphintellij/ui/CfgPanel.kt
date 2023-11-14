package ru.itmo.controlfungraphintellij.ui

import com.intellij.ui.components.JBScrollPane
import com.kitfox.svg.app.beans.SVGPanel
import javax.swing.JPanel
import javax.swing.SpringLayout

class CfgPanel : JPanel() {
    val graphContentPanel = SVGPanel().apply {
        setAntiAlias(true)
        setAutosize(SVGPanel.AUTOSIZE_NONE)
    }
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
