package ru.itmo.controlfungraphintellij.ui

import com.kitfox.svg.app.beans.SVGPanel
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.MouseInfo
import java.awt.Point
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import java.awt.geom.AffineTransform
import javax.swing.JComponent
import javax.swing.JPanel

class ZoomPanel(
    private val content: SVGPanel
) : JPanel(), MouseWheelListener, MouseListener {

    private var zoomFactor = 1.0
    private var prevZoomFactor = 1.0
    private var zoomer = false
    private var dragger = false
    private var released = false
    private var xOffset = 0.0
    private var yOffset = 0.0
    private var xDiff = 0
    private var yDiff = 0
    private var startPoint: Point? = null

    init {
        addMouseWheelListener(this)
        addMouseListener(this)
        content.isVisible = true
    }

    override fun paintComponents(g: Graphics) {
        super.paint(g)
        val g2 = g as Graphics2D

        if (zoomer) {
            val at = AffineTransform()
            val xRel = MouseInfo.getPointerInfo().location.getX() - locationOnScreen.getX()
            val yRel = MouseInfo.getPointerInfo().location.getY() - locationOnScreen.getY()
            val zoomDiv = zoomFactor / prevZoomFactor
            xOffset = zoomDiv * xOffset + (1 - zoomDiv) * xRel
            yOffset = zoomDiv * yOffset + (1 - zoomDiv) * yRel
            at.translate(xOffset, yOffset)
            at.scale(zoomFactor, zoomFactor)
            prevZoomFactor = zoomFactor
            g2.transform(at)
            zoomer = false
        }
        if (dragger) {
            val at = AffineTransform()
            at.translate(xOffset + xDiff, yOffset + yDiff)
            at.scale(zoomFactor, zoomFactor)
            g2.transform(at)
            if (released) {
                xOffset += xDiff.toDouble()
                yOffset += yDiff.toDouble()
                dragger = false
            }
        }

        // All drawings go here
        content.paintComponents(g)
    }

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        zoomer = true

        //Zoom in
        if (e.wheelRotation < 0) {
            zoomFactor *= 1.1
            repaint()
        }
        //Zoom out
        if (e.wheelRotation > 0) {
            zoomFactor /= 1.1
            repaint()
        }
    }

    fun mouseMoved(e: MouseEvent?) {}

    override fun mouseClicked(e: MouseEvent?) {}

    override fun mousePressed(e: MouseEvent?) {
        released = false
        startPoint = MouseInfo.getPointerInfo().location
    }

    override fun mouseReleased(e: MouseEvent?) {
        released = true
        repaint()
    }

    override fun mouseEntered(e: MouseEvent?) {}

    override fun mouseExited(e: MouseEvent?) {}
}
