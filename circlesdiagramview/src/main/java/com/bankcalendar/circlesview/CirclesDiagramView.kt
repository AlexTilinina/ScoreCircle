package com.bankcalendar.circlesview

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Parcelable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.bankcalendar.circlesview.util.draw
import com.bankcalendar.circlesview.util.formatNumber
import kotlin.math.cos
import kotlin.math.sin


class CirclesDiagramView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        const val DEFAULT_RADIUS = 300F
        const val DEFAULT_WIDTH = DEFAULT_RADIUS / 11f
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : this(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) : this(
        context,
        attrs
    )

    private val baseTextColor = Color.parseColor("#34495e")
    private val baseCommentaryTextColor = Color.parseColor("#96a6a7")
    private val baseBackgroundCircleColor = Color.parseColor("#e1e5e7")
    private val baseCommentaryLineColor = Color.parseColor("#96a6a7")
    private val baseLabelsTextColor = Color.parseColor("#2c3e50")

    var maxScore: Int = 0
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

    var labelsTextColor: Int = baseLabelsTextColor
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

    private var circleWidth: Float = DEFAULT_WIDTH
    private var outerCircleRadius: Float = DEFAULT_RADIUS

    private var circleRadiuses = mutableListOf<Float>()
    private var circleValues = mutableListOf<Int>()
    private var circleValueLabels = mutableListOf(
        "Потребительский", "Автокредит",
        "Ипотека", "Кредитная карта", "Микрокредит", "Прочее"
    )
    private var percents = mutableListOf<Int>()

    private val colors = listOf(
        Color.parseColor("#0097CE"),
        Color.parseColor("#53C283"),
        Color.parseColor("#F0C300"),
        Color.parseColor("#D14D57"),
        Color.parseColor("#9d34af"),
        Color.parseColor("#4778ff")
    )
    private val darkerColors = listOf(
        Color.parseColor("#0084b5"),
        Color.parseColor("#4fab77"),
        Color.parseColor("#e0b600"),
        Color.parseColor("#c3454f"),
        Color.parseColor("#902ea1"),
        Color.parseColor("#3c64d2")
    )

    private var circleCount = 4

    private val circlePaint: Paint = Paint()
    private val textPaint: TextPaint = TextPaint()
    private val linePaint: Paint = Paint()

    private var dotRadius = DEFAULT_WIDTH / 4
    private var commentaryHeight = 0F
    private var bottomLabelsMargin = 0F

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
                circleWidth =
                    getDimension(R.styleable.CirclesDiagramView_circleWidth, DEFAULT_WIDTH)
                outerCircleRadius = getDimension(
                    R.styleable.CirclesDiagramView_outerCircleRadius,
                    DEFAULT_RADIUS
                )
                innerTextColor =
                    getColor(R.styleable.CirclesDiagramView_innerTextColor, baseTextColor)
                commentaryTextColor = getColor(
                    R.styleable.CirclesDiagramView_commentaryTextColor,
                    baseCommentaryTextColor
                )
                commentaryLineColor = getColor(
                    R.styleable.CirclesDiagramView_commentaryLineColor,
                    baseCommentaryLineColor
                )
                labelsTextColor = getColor(
                    R.styleable.CirclesDiagramView_labelsTextColor,
                    baseLabelsTextColor
                )
                dotColor = getColor(R.styleable.CirclesDiagramView_dotColor, Color.WHITE)
                backgroundCircleColor = getColor(
                    R.styleable.CirclesDiagramView_backgroundCircleColor,
                    baseBackgroundCircleColor
                )
                circleCount = getInteger(R.styleable.CirclesDiagramView_circleCount, 6)
            } finally {
                recycle()
            }
        }
        initPaint()
        isSaveEnabled = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val needWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            paddingStart + paddingEnd + suggestedMinimumWidth +
                    outerCircleRadius * 2 + circleWidth + outerCircleRadius / 3 * 2
        } else {
            paddingLeft + paddingRight + suggestedMinimumWidth +
                    outerCircleRadius * 2 + circleWidth + outerCircleRadius / 3 * 2
        }
        val valueCount = circleValues.count { it > 0 }
        val forBottom = if (valueCount != 0) {
            outerCircleRadius / 4f + (circleWidth * 3f) * valueCount
        } else {
            0f
        }
        val needHeight = paddingTop + paddingBottom + suggestedMinimumHeight +
                outerCircleRadius * 2 + circleWidth + outerCircleRadius / 2 + forBottom
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
        drawLabels(canvas)
    }

    fun setRadius(radius: Float) {
        outerCircleRadius = radius
        invalidate()
    }

    fun setWidth(width: Float) {
        circleWidth = width
        invalidate()
    }

    fun setData(values: MutableList<Int>, labels: MutableList<String>, percents: MutableList<Int>) {
        bottomLabelsMargin =
            outerCircleRadius / 2 + circleWidth * 2 * circleValues.count { it != 0 }
        circleValues = values
        circleValueLabels = labels
        this.percents = percents
        invalidate()
    }

    fun setValues(values: MutableList<Int>) {
        circleValues = values
        while (circleValues.size < circleCount) {
            circleValues.add(0)
        }
        bottomLabelsMargin =
            outerCircleRadius / 2 + circleWidth * 2 * circleValues.count { it != 0 }
        invalidate()
    }

    fun setLabels(labels: MutableList<String>) {
        circleValueLabels = labels
        invalidate()
    }

    fun setPercents(percents: MutableList<Int>) {
        this.percents = percents
        invalidate()
    }

    /*
    * Draws grey circles on background*/
    private fun drawSecondaryCircles(canvas: Canvas?) {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = backgroundCircleColor
        circlePaint.strokeWidth = circleWidth * 0.95f
        for (i in 0 until circleCount) {
            //if (circleValues[i] == 0) {
                canvas?.drawCircle(
                    width / 2F,
                    commentaryHeight + outerCircleRadius,
                    circleRadiuses[i],
                    circlePaint
                )
            /*} else {
                val oval = getOval(circleRadiuses[i])
                canvas?.drawArc(oval, 270F, 180F, false, circlePaint)
            }*/
        }
    }

    /*
    * Draws colorful circles
    * */
    private fun drawPrimaryCircles(canvas: Canvas?) {
        for (i in 0 until circleCount) {
            if (circleValues.all { it == 0 }) {
                val endX = (cos(Math.toRadians(90.0)) *
                        circleRadiuses[i] + width / 2F).toFloat()
                val endY = (sin(Math.toRadians(90.0)) *
                        circleRadiuses[i] + commentaryHeight + outerCircleRadius).toFloat()
                circlePaint.style = Paint.Style.FILL
                drawDot(endX, endY, dotColor, canvas)
            } else {
                if (circleValues[i] != 0) {
                    val percent = if (maxScore == 0) {
                        percents[i]
                    } else {
                        100 * circleValues[i] / maxScore
                    }
                    circlePaint.style = Paint.Style.STROKE
                    circlePaint.color = colors[circleCount - 1 - i]
                    circlePaint.strokeWidth = circleWidth
                    val angle = 360 * percent / 100F
                    val oval = getOval(circleRadiuses[i])
                    if (angle <= 180) {
                        canvas?.drawArc(oval, 90F, angle, false, circlePaint)
                    } else {
                        canvas?.drawArc(oval, 90F, 181F, false, circlePaint)
                        circlePaint.color = darkerColors[circleCount - 1 - i]
                        canvas?.drawArc(oval, 270F, angle - 180, false, circlePaint)
                    }
                    val endX = (cos(Math.toRadians(90 + angle.toDouble()))
                            * circleRadiuses[i] + width / 2F).toFloat()
                    val endY = (sin(Math.toRadians(90 + angle.toDouble()))
                            * circleRadiuses[i] + commentaryHeight + outerCircleRadius).toFloat()
                    circlePaint.style = Paint.Style.FILL
                    canvas?.drawCircle(endX, endY, circleWidth / 2, circlePaint)
                    if (percent < 100)
                        drawDot(endX, endY, dotColor, canvas)
                }
            }
        }
    }

    /*
    * Draws little white dot
    * Used on the end of colorful circles */
    private fun drawDot(x: Float, y: Float, color: Int, canvas: Canvas?) {
        circlePaint.color = color
        canvas?.drawCircle(x, y, circleWidth / 3.5f, circlePaint)
    }

    private fun getOval(radius: Float): RectF = RectF(
        width / 2F - radius,
        commentaryHeight + outerCircleRadius - radius,
        width / 2F + radius,
        commentaryHeight + outerCircleRadius + radius
    )

    /*
    * Draws big inner text*/
    private fun drawInnerText(canvas: Canvas?) {
        textPaint.color = innerTextColor
        textPaint.textSize = circleRadiuses.last() / 1.7F
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val textWidth = textPaint.measureText(primaryText)
        val textX = width / 2f - textWidth / 2f
        val textY = if (secondaryText.isEmpty()) {
            commentaryHeight + outerCircleRadius + textPaint.textSize / 3
        } else {
            commentaryHeight + outerCircleRadius + textPaint.textSize / 9
        }
        canvas?.drawText(primaryText, textX, textY, textPaint)
        drawSecondaryText(canvas)
    }

    /*
    * Draws small inner text*/
    private fun drawSecondaryText(canvas: Canvas?) {
        if (secondaryText.isNotEmpty()) {
            textPaint.textSize = textPaint.textSize / 2.25F
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val textWidth = textPaint.measureText(secondaryText)
            val textX = width / 2f - textWidth / 2f
            val textY = commentaryHeight + outerCircleRadius + textPaint.textSize * 1.5F
            canvas?.drawText(secondaryText, textX, textY, textPaint)
        }
    }

    /*
    * Draws text above circle*/
    private fun drawCommentaryText(canvas: Canvas?) {
        if (commentaryText.isNotEmpty()) {
            textPaint.color = commentaryTextColor
            textPaint.textSize = outerCircleRadius / 9f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val textWidth = width / 2 - (dotRadius * 3).toInt()
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
                0f
            )
            drawCommentaryLine(canvas)
        }
    }

    private fun drawLabels(canvas: Canvas?) {
        var dotY = commentaryHeight + outerCircleRadius * 2 + circleWidth * 2f + outerCircleRadius / 4
        val dotX = dotRadius * 6
        for (i in 0 until circleCount) {
            if (circleValues[i] > 0) {
                drawDot(dotX, dotY, colors[circleCount - 1 - i], canvas)
                writeLabelText(dotX, dotY, i, canvas)
                dotY += circleWidth * 2.5f
            }
        }
    }

    private fun writeLabelText(dotX: Float, dotY: Float, index: Int, canvas: Canvas?) {
        textPaint.color = labelsTextColor
        textPaint.textSize = outerCircleRadius / 8f
        val textX = dotX + dotRadius * 3f
        val textY = dotY + dotRadius * 1.5f
        canvas?.drawText(circleValueLabels[index], textX, textY, textPaint)
        writeValuePercent(textY, index, canvas)
        drawLabelLine(textX + textPaint.measureText(circleValueLabels[index]), textY, canvas)
    }

    private fun writeValuePercent(textY: Float, index: Int, canvas: Canvas?) {
        textPaint.color = commentaryTextColor
        val textX = calculatePercentX()
        val percent = if (maxScore == 0) {
            percents[index]
        } else {
            100 * circleValues[index] / maxScore
        }
        canvas?.drawText("$percent%", textX, textY, textPaint)
        writeValueText(textX, textY, index, canvas)
    }

    private fun writeValueText(textX: Float, textY: Float, index: Int, canvas: Canvas?) {
        textPaint.color = labelsTextColor
        canvas?.drawText(circleValues[index].formatNumber(), calculateValueX(textX), textY, textPaint)
    }

    /*
    * Draws dotted line between label and value */
    private fun drawLabelLine(textX: Float, textY: Float, canvas: Canvas?) {
        linePaint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
        linePaint.color = Color.parseColor("#bec4c8")
        val startX = textX + dotRadius * 2f
        val endX = calculateValueX(calculatePercentX()) - dotRadius * 2f
        val path = Path()
        path.moveTo(startX, textY)
        path.lineTo(endX, textY)
        canvas?.drawPath(path, linePaint)
    }

    private fun calculatePercentX(): Float = width - textPaint.measureText("100%") - dotRadius * 2

    private fun calculateValueX(textX: Float): Float =
        textX - textPaint.measureText(circleValues.max()?.formatNumber()) - dotRadius * 3

    private fun drawCommentaryLine(canvas: Canvas?) {
        val x = width / 2F
        val y = commentaryHeight - circleWidth / 2
        val path = Path()
        path.moveTo(x, y)
        path.lineTo(x, y - circleWidth)
        path.lineTo(x + width / 3F, y - circleWidth)
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
        linePaint.strokeWidth = 3F
        linePaint.isAntiAlias = true
        // linePaint.pathEffect = DashPathEffect(floatArrayOf(15f, 5f), 0f)
    }

    private fun initCircleMeasurementsIfEmpty() {
        if (outerCircleRadius == DEFAULT_RADIUS) {
            outerCircleRadius = height * 0.27f
        }
        if (circleWidth == DEFAULT_WIDTH) {
            circleWidth = outerCircleRadius / 11
        }
        if (dotRadius == DEFAULT_WIDTH / 4) {
            dotRadius = circleWidth / 4f
        }
        circleRadiuses.add(outerCircleRadius)
        if (circleCount > 1) {
            for (i in 1 until circleCount) {
                circleRadiuses.add(circleRadiuses[i - 1] - circleWidth * 1.2f)
            }
        }
        if (circleValues.isEmpty()) {
            for (i in 0 until circleCount) {
                circleValues.add(0)
            }
        }
        if (commentaryText.isNotEmpty()) {
            commentaryHeight = outerCircleRadius / 2
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

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = super.onSaveInstanceState()?.let { SavedState(it) }
        savedState?.maxScore = maxScore
        savedState?.secondaryText = secondaryText
        savedState?.commentaryText = commentaryText
        savedState?.innerTextColor = innerTextColor
        savedState?.primaryText = primaryText
        savedState?.commentaryTextColor = commentaryTextColor
        savedState?.labelsTextColor = labelsTextColor
        savedState?.commentaryLineColor = commentaryLineColor
        savedState?.dotColor = dotColor
        savedState?.backgroundCircleColor = backgroundCircleColor
        savedState?.circleWidth = circleWidth
        savedState?.outerCircleRadius = outerCircleRadius
        savedState?.circleRadiuses = circleRadiuses
        savedState?.circleValues = circleValues
        savedState?.circleValueLabels = circleValueLabels
        savedState?.percents = percents
        savedState?.dotRadius = dotRadius
        savedState?.commentaryHeight = commentaryHeight
        savedState?.bottomLabelsMargin = bottomLabelsMargin
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState)
        maxScore = savedState.maxScore
        secondaryText = savedState.secondaryText
        commentaryText = savedState.commentaryText
        innerTextColor = savedState.innerTextColor
        primaryText = savedState.primaryText
        commentaryTextColor = savedState.commentaryTextColor
        labelsTextColor = savedState.labelsTextColor
        commentaryLineColor = savedState.commentaryLineColor
        dotColor = savedState.dotColor
        backgroundCircleColor = savedState.backgroundCircleColor
        outerCircleRadius = savedState.outerCircleRadius
        circleRadiuses = savedState.circleRadiuses
        circleValues = savedState.circleValues
        circleValueLabels = savedState.circleValueLabels
        percents = savedState.percents
        dotRadius = savedState.dotRadius
        commentaryHeight = savedState.commentaryHeight
        bottomLabelsMargin = savedState.bottomLabelsMargin
        invalidate()
    }
}