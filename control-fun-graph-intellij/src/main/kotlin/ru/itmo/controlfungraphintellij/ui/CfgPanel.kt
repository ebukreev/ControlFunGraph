package ru.itmo.controlfungraphintellij.ui

import org.apache.batik.swing.JSVGCanvas
import org.apache.batik.swing.JSVGScrollPane
import javax.swing.JPanel
import javax.swing.SpringLayout

class CfgPanel : JPanel() {
    val graphContentPanel = JSVGCanvas()
    private val scrollPane = JSVGScrollPane(graphContentPanel)

    init {
        val springLayout = SpringLayout()
        layout = springLayout
        graphContentPanel

        springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, this)
        springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, this)
        springLayout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, this)
        springLayout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, this)
        scrollPane.isOpaque = false

        this.add(scrollPane)
    }

}
