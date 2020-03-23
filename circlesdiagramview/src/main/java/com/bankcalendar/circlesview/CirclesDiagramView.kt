package com.bankcalendar.circlesview

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
import com.bankcalendar.circlesview.util.draw


class CirclesDiagramView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        const val DEFAULT_RADIUS = 100F
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : this(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) : this(
        context,
        attrs
    )

    private val baseTextColor = Color.parseColor("#34495e")
    private val baseCommentaryTextColor = Color.parseColor("#96a6a7")
    private val baseBackgroundCircleColor = Color.parseColor("#e1e5e7")
    private val baseCommentaryLineColor = Color.parseColor("#979797")

    var maxScore: Int = 1000
        set(value) {
            field = value
            invalidate()
        }

    var secondaryText: String = ""
        set(value) {
            field = value
            invalidate()
        }

    var commentaryText: String = ""
        set(value) {
            field = value
            invalidate()
        }

    var innerTextColor: Int = baseTextColor
        set(value) {
            field = value
            invalidate()
        }

    var primaryText: String = ""
        set(value) {
            field = value
            invalidate()
        }

    var commentaryTextColor: Int = baseCommentaryTextColor
        set(value) {
            field = value
            invalidate()
        }

    var commentaryLineColor: Int = baseCommentaryLineColor
        set(value) {
            field = value
            invalidate()
        }

    var dotColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    var backgroundCircleColor: Int = baseBackgroundCircleColor
        set(value) {
            field = value
            invalidate()
        }

    private var circleWidth: Float = 0F
    private var outerCircleRadius: Float = DEFAULT_RADIUS

    private var circleRadiuses = mutableListOf<Float>()
    private var circleValues = mutableListOf(0, 0, 0, 0)
    private val colors = listOf(
        Color.parseColor("#d14d57"),
        Color.parseColor("#f0c300"),
        Color.parseColor("#53c283"),
        Color.parseColor("#0097ce")
    )
    private val darkerColors = listOf(
        Color.parseColor("#c3454f"),
        Color.parseColor("#e0b600"),
        Color.parseColor("#4fab77"),
        Color.parseColor("#0084b5")
    )

    private var circleCount = 4

    private val circlePaint: Paint = Paint()
    private val textPaint: TextPaint = TextPaint()
    private val linePaint: Paint = Paint()

    private var dotRadius: Float = 0F
    private var commentaryHeight = 0F

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CirclesDiagramView,
            0, 0
        ).apply {
            try {
                maxScore = getInteger(R.styleable.CirclesDiagramView_maxScore, 0)
                primaryText = getString(R.styleable.CirclesDiagramView_primaryText) ?: ""
                secondaryText = getString(R.styleable.CirclesDiagramView_secondaryText) ?: ""
                commentaryText = getString(R.styleable.CirclesDiagramView_commentaryText) ?: ""
                circleWidth = getDimension(R.styleable.CirclesDiagramView_circleWidth, 0F)
                outerCircleRadius = getDimension(
                    R.styleable.CirclesDiagramView_outerCircleRadius,
                    DEFAULT_RADIUS
                )
                innerTextColor =
                    getColor(R.styleable.CirclesDiagramView_innerTextColor, baseTextColor)
                commentaryTextColor =
                    getColor(
                        R.styleable.CirclesDiagramView_commentaryTextColor,
                        baseCommentaryTextColor
                    )
                commentaryLineColor = getColor(
                    R.styleable.CirclesDiagramView_commentaryLineColor,
                    baseCommentaryLineColor
                )
                dotColor = getColor(R.styleable.CirclesDiagramView_dotColor, Color.WHITE)
                backgroundCircleColor =
                    getColor(
                        R.styleable.CirclesDiagramView_backgroundCircleColor,
                        baseBackgroundCircleColor
                    )
                circleCount = getInteger(R.styleable.CirclesDiagramView_circleCount, 4)
            } finally {
                recycle()
            }
        }
        initPaint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val needWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            paddingStart + paddingEnd + suggestedMinimumWidth +
                    outerCircleRadius * 2 + circleWidth + outerCircleRadius / 3 * 2
        } else {
            paddingLeft + paddingRight + suggestedMinimumWidth +
                    outerCircleRadius * 2 + circleWidth + outerCircleRadius / 3 * 2
        }
        val needHeight = paddingTop + paddingBottom + suggestedMinimumHeight +
                outerCircleRadius * 2 + circleWidth + outerCircleRadius * 4 / 9
        val measuredWidth = calculateSize(needWidth.toInt(), widthMeasureSpec)
        val measuredHeight = calculateSize(needHeight.toInt(), heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        initCircleMeasurementsIfEmpty()
        drawSecondaryCircles(canvas)
        drawPrimaryCircles(canvas)
        drawInnerText(canvas)
        drawCommentaryText(canvas)
    }

    fun setRadius(radius: Float) {
        outerCircleRadius = radius
        invalidate()
    }

    fun setWidth(width: Float) {
        circleWidth = width
        invalidate()
    }

    fun setValues(vararg values: Int) {
        circleValues = values.toMutableList()
        invalidate()
    }

    private fun drawSecondaryCircles(canvas: Canvas?) {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = backgroundCircleColor
        circlePaint.strokeWidth = circleWidth * 0.95f
        for (i in 0 until circleCount) {
            if (circleValues[i] == 0) {
                canvas?.drawCircle(
                    width / 2F,
                    height / 2F + commentaryHeight,
                    circleRadiuses[i],
                    circlePaint
                )
            } else {
                val oval = RectF(
                    width / 2F - circleRadiuses[i],
                    height / 2F - circleRadiuses[i] + commentaryHeight,
                    width / 2F + circleRadiuses[i],
                    height / 2F + circleRadiuses[i] + commentaryHeight
                )
                canvas?.drawArc(oval, 270F, 180F, false, circlePaint)
            }
        }
    }

    private fun drawPrimaryCircles(canvas: Canvas?) {
        for (i in 0 until circleCount) {
            if (circleValues.all { it == 0 }) {
                val endX =
                    (cos(Math.toRadians(90.0)) * circleRadiuses[i] + width / 2F).toFloat()
                val endY =
                    (sin(Math.toRadians(90.0)) * circleRadiuses[i] + height / 2F + commentaryHeight).toFloat()
                circlePaint.style = Paint.Style.FILL
                drawDot(endX, endY, canvas)
            } else {
                if (circleValues[i] != 0) {
                    val percent = 100 * circleValues[i] / maxScore
                    circlePaint.style = Paint.Style.STROKE
                    circlePaint.color = colors[i]
                    circlePaint.strokeWidth = circleWidth
                    val angle = 360 * percent / 100F
                    val oval = RectF(
                        width / 2F - circleRadiuses[i],
                        height / 2F - circleRadiuses[i] + commentaryHeight,
                        width / 2F + circleRadiuses[i],
                        height / 2F + circleRadiuses[i] + commentaryHeight
                    )
                    if (angle <= 180) {
                        canvas?.drawArc(oval, 90F, angle, false, circlePaint)
                    } else {
                        canvas?.drawArc(oval, 90F, 181F, false, circlePaint)
                        circlePaint.color = darkerColors[i]
                        canvas?.drawArc(oval, 270F, angle - 180, false, circlePaint)
                    }
                    val endX =
                        (cos(Math.toRadians(90 + angle.toDouble())) * circleRadiuses[i] + width / 2F).toFloat()
                    val endY =
                        (sin(Math.toRadians(90 + angle.toDouble())) * circleRadiuses[i] + height / 2F + commentaryHeight).toFloat()
                    circlePaint.style = Paint.Style.FILL
                    canvas?.drawCircle(endX, endY, circleWidth / 2, circlePaint)
                    if (percent < 100)
                        drawDot(endX, endY, canvas)
                }
            }
        }
    }

    private fun drawDot(x: Float, y: Float, canvas: Canvas?) {
        circlePaint.color = dotColor
        canvas?.drawCircle(x, y, circleWidth / 3.5f, circlePaint)
    }

    private fun drawInnerText(canvas: Canvas?) {
        textPaint.color = innerTextColor
        textPaint.textSize = outerCircleRadius * 2 / 6f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val textWidth = textPaint.measureText(primaryText)
        val textX = width / 2f - textWidth / 2f
        val textY = if (secondaryText.isEmpty()) {
            height / 2f + commentaryHeight + textPaint.textSize / 3
        } else {
            height / 2f + commentaryHeight
        }
        canvas?.drawText(primaryText, textX, textY, textPaint)
        drawSecondaryText(canvas)
    }

    private fun drawSecondaryText(canvas: Canvas?) {
        if (secondaryText.isNotEmpty()) {
            textPaint.textSize = textPaint.textSize / 2.25F
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val textWidth = textPaint.measureText(secondaryText)
            val textX = width / 2f - textWidth / 2f
            val textY = height / 2f + textPaint.textSize * 1.5F + commentaryHeight
            canvas?.drawText(secondaryText, textX, textY, textPaint)
        }
    }

    private fun drawCommentaryText(canvas: Canvas?) {
        if (commentaryText.isNotEmpty()) {
            textPaint.color = commentaryTextColor
            textPaint.textSize = outerCircleRadius * 2 / 18f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val textWidth = width / 2
            val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder
                    .obtain(commentaryText, 0, commentaryText.length, textPaint, textWidth)
                    .build()
            } else {
                StaticLayout(
                    commentaryText,
                    textPaint,
                    textWidth,
                    Layout.Alignment.ALIGN_NORMAL,
                    1F,
                    0F,
                    false
                )
            }
            staticLayout.draw(
                canvas,
                width / 2F,
                height / 2 - outerCircleRadius - outerCircleRadius / 6f
            )
            drawLine(canvas)
        }
    }

    private fun drawLine(canvas: Canvas?) {
        val x = width / 2F
        val y = height / 2F + commentaryHeight - outerCircleRadius - circleWidth / 2
        val path = Path()
        path.moveTo(x, y)
        path.lineTo(x, y - circleWidth)
        path.lineTo(x + width / 3.5F, y - circleWidth)
        canvas?.drawPath(path, linePaint)
    }

    private fun initPaint() {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.isAntiAlias = true
        circlePaint.isDither = true

        textPaint.style = Paint.Style.FILL
        textPaint.isAntiAlias = true
        textPaint.color = innerTextColor

        linePaint.style = Paint.Style.STROKE
        linePaint.color = baseCommentaryLineColor
        linePaint.strokeWidth = 2F
        linePaint.isAntiAlias = true
        linePaint.pathEffect = DashPathEffect(floatArrayOf(15f, 5f), 0f)
    }

    private fun initCircleMeasurementsIfEmpty() {
        if (outerCircleRadius == DEFAULT_RADIUS) {
            outerCircleRadius = width / 2f - (width / 10)
        }
        if (circleWidth == 0F) {
            circleWidth = outerCircleRadius / 9
        }
        outerCircleRadius -= circleWidth / 2
        dotRadius = circleWidth / 4f
        circleRadiuses.add(outerCircleRadius)
        if (circleCount > 1) {
            for (i in 1 until circleCount) {
                circleRadiuses.add(circleRadiuses[i - 1] - circleWidth - circleWidth / 5)
            }
        }
        for (i in 0 until circleCount) {
            circleValues.add(0)
        }
        if (commentaryText.isNotEmpty()) {
            commentaryHeight = outerCircleRadius * 4 / 9
        }
    }

    private fun calculateSize(contentSize: Int, measureSpec: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> if (contentSize < specSize) contentSize else specSize
            else -> contentSize // MeasureSpec.UNSPECIFIED
        }
    }
}