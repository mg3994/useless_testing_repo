package ch.zeitmessungen.equestre.ui.overlay_settings

import android.graphics.*
import android.text.style.ReplacementSpan

class ParallelogramSpan(
    private val backgroundColor: Int,
    private val textColor: Int = Color.WHITE,
    private val topLeftRadius: Float = 16f,
    private val topRightRadius: Float = 16f,
    private val bottomRightRadius: Float = 16f,
    private val bottomLeftRadius: Float = 16f,
    private val skewOffset: Float = 30f, // skew X from top-left
    private val paddingH: Float = 20f,
    private val paddingV: Float = 12f
) : ReplacementSpan() {

    override fun getSize(
        paint: Paint, text: CharSequence, start: Int, end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val textWidth = paint.measureText(text, start, end)
        return (textWidth + paddingH * 2).toInt()
    }

    override fun draw(
        canvas: Canvas, text: CharSequence, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        val originalColor = paint.color
        val originalStyle = paint.style

        val textWidth = paint.measureText(text, start, end)
        val rectLeft = x
        val rectTop = y + paint.ascent() - paddingV
        val rectRight = x + textWidth + paddingH * 2
        val rectBottom = y + paint.descent() + paddingV

        val path = Path()

        // Apply skewed parallelogram with custom radii
        path.moveTo(rectLeft + skewOffset + topLeftRadius, rectTop)
        path.lineTo(rectRight - topRightRadius, rectTop)
        path.quadTo(rectRight, rectTop, rectRight, rectTop + topRightRadius)

        path.lineTo(rectRight - skewOffset, rectBottom - bottomRightRadius)
        path.quadTo(rectRight - skewOffset, rectBottom, rectRight - skewOffset - bottomRightRadius, rectBottom)

        path.lineTo(rectLeft + bottomLeftRadius, rectBottom)
        path.quadTo(rectLeft, rectBottom, rectLeft, rectBottom - bottomLeftRadius)

        path.lineTo(rectLeft + skewOffset, rectTop + topLeftRadius)
        path.quadTo(rectLeft + skewOffset, rectTop, rectLeft + skewOffset + topLeftRadius, rectTop)

        path.close()

        // Draw shape
        paint.color = backgroundColor
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)

        // Draw text
        paint.color = textColor
        paint.style = Paint.Style.FILL
        canvas.drawText(text, start, end, x + paddingH, y.toFloat(), paint)

        // Restore paint
        paint.color = originalColor
        paint.style = originalStyle
    }
}
