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
import com.bankcalendar.circlesview.util.dpToPx
import com.bankcalendar.circlesview.util.draw
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

    private val baseTextColor = Color.parseColor("#2C3E50")
    private val baseBackgroundCircleColor = Color.parseColor("#DFE4E5")
    private val baseLineColor = Color.parseColor("#BEC4C8")

    var maxScore: Int = 0
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

    var secondaryText: String = ""
        set(value) {
            field = value
            invalidate()
        }

    var secondaryTextColor: Int = baseTextColor
        set(value) {
            field = value
            invalidate()
        }

    var tertiaryText: String = ""
        set(value) {
            field = value
            invalidate()
        }

    var tertiaryTextColor: Int = baseTextColor
        set(value) {
            field = value
            invalidate()
        }

    var lineColor: Int = baseLineColor
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

    private var percents = mutableListOf<Int>()

    private val colors = listOf(
        Color.parseColor("#9D34AF"),
        Color.parseColor("#0097CE"),
        Color.parseColor("#53C283"),
        Color.parseColor("#F0C300"),
        Color.parseColor("#D14D57")
    )

    private var circleCount = 5

    private val circlePaint: Paint = Paint()
    private val textPaint: TextPaint = TextPaint()
    private val linePaint: Paint = Paint()

    private var dotRadius = DEFAULT_WIDTH / 4

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
                tertiaryText = getString(R.styleable.CirclesDiagramView_tertiaryText) ?: ""

                circleWidth =
                    getDimension(R.styleable.CirclesDiagramView_circleWidth, DEFAULT_WIDTH)
                outerCircleRadius = getDimension(
                    R.styleable.CirclesDiagramView_outerCircleRadius,
                    DEFAULT_RADIUS
                )

                innerTextColor =
                    getColor(R.styleable.CirclesDiagramView_innerTextColor, baseTextColor)
                secondaryTextColor =
                    getColor(R.styleable.CirclesDiagramView_secondaryTextColor, baseTextColor)
                tertiaryTextColor =
                    getColor(R.styleable.CirclesDiagramView_tertiaryTextColor, baseTextColor)

                lineColor = getColor(
                    R.styleable.CirclesDiagramView_lineColor,
                    baseLineColor
                )
                dotColor = getColor(R.styleable.CirclesDiagramView_dotColor, Color.WHITE)
                backgroundCircleColor = getColor(
                    R.styleable.CirclesDiagramView_backgroundCircleColor,
                    baseBackgroundCircleColor
                )
                circleCount = getInteger(R.styleable.CirclesDiagramView_circleCount, 5)
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
                    outerCircleRadius * 2 + circleWidth * 6 + outerCircleRadius / 2
        } else {
            paddingLeft + paddingRight + suggestedMinimumWidth +
                    outerCircleRadius * 2 + circleWidth * 6 + outerCircleRadius / 2
        }
        val needHeight = paddingTop + paddingBottom + suggestedMinimumHeight +
                outerCircleRadius * 2 + circleWidth * 6 + outerCircleRadius / 2
        val measuredWidth = calculateSize(needWidth.toInt(), widthMeasureSpec)
        val measuredHeight = calculateSize(needHeight.toInt(), heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        initCircleMeasurementsIfEmpty()
        drawBackground(canvas)
        drawSecondaryCircles(canvas)
        drawPrimaryCircles(canvas)
        drawStartLine(canvas)
        drawInnerText(canvas)
    }

    fun setValues(values: MutableList<Int>) {
        circleValues = values
        while (circleValues.size < circleCount) {
            circleValues.add(0)
        }
        invalidate()
    }

    fun setPercents(percents: MutableList<Int>) {
        this.percents = percents
        invalidate()
    }

    /*
    * Draws gray gradient and white circle on background*/
    private fun drawBackground(canvas: Canvas?) {
        circlePaint.style = Paint.Style.FILL

        val shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            Color.WHITE,
            Color.parseColor("#F7F7F8"),
            Shader.TileMode.CLAMP
        )

        circlePaint.shader = shader
        canvas?.drawCircle(
            width / 2F,
            height / 2f,
            width / 2f,
            circlePaint
        )

        circlePaint.shader = null
        circlePaint.color = Color.WHITE
        canvas?.drawCircle(
            width / 2F,
            height / 2f,
            circleRadiuses[0],
            circlePaint
        )

        circlePaint.shader = shader
        canvas?.drawCircle(
            width / 2F,
            height / 2f,
            circleRadiuses.last(),
            circlePaint
        )
        circlePaint.shader = null
    }

    /*
    * Draws grey thin circles on background*/
    private fun drawSecondaryCircles(canvas: Canvas?) {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = backgroundCircleColor
        circlePaint.strokeWidth = dpToPx(1f, context)
        for (i in 0 until circleCount) {
            canvas?.drawCircle(
                width / 2F,
                height / 2f,
                circleRadiuses[i],
                circlePaint
            )
        }
    }

    /*
    * Draws colorful circles
    * */
    private fun drawPrimaryCircles(canvas: Canvas?) {
        for (i in 0 until circleCount) {
            if (circleValues[i] != 0) {
                var percent = if (maxScore == 0) {
                    percents[i]
                } else {
                    100 * circleValues[i] / maxScore
                }
                if (percent < 2) percent = 2
                circlePaint.style = Paint.Style.STROKE
                circlePaint.color = colors[i]
                circlePaint.strokeWidth = circleWidth
                val angle = 360 * percent / 100F
                val oval = getOval(circleRadiuses[i])
                canvas?.drawArc(oval, 90F, angle, false, circlePaint)
                val endX = (cos(Math.toRadians(90 + angle.toDouble()))
                        * circleRadiuses[i] + width / 2F).toFloat()
                val endY = (sin(Math.toRadians(90 + angle.toDouble()))
                        * circleRadiuses[i] + height / 2f).toFloat()
                circlePaint.style = Paint.Style.FILL
                canvas?.drawCircle(endX, endY, circleWidth / 2, circlePaint)
                if (percent < 100)
                    drawDot(endX, endY, dotColor, canvas)

            }
        }
    }

    /*
    * Draws little white dot
    * Used on the end of colorful circles */
    private fun drawDot(x: Float, y: Float, color: Int, canvas: Canvas?) {
        circlePaint.color = color
        canvas?.drawCircle(x, y, circleWidth / 4f, circlePaint)
    }

    private fun getOval(radius: Float): RectF = RectF(
        width / 2F - radius,
        height / 2f - radius,
        width / 2F + radius,
        height / 2f + radius
    )

    private fun drawStartLine(canvas: Canvas?) {
        val x = width / 2F
        val yStart = height / 2f + circleRadiuses.last() - circleWidth / 2f
        val yEnd = height / 2f + circleRadiuses.first() + circleWidth / 2f
        linePaint.color = innerTextColor
        linePaint.strokeWidth = dpToPx(1f, context)

        canvas?.drawLine(x, yStart, x, yEnd, linePaint)
    }


    /**
     * Draws all inner text in primary circle*/
    private fun drawInnerText(canvas: Canvas?) {
        drawPrimaryText(canvas)
        if (primaryText.isNotEmpty() && secondaryText.isNotEmpty() && tertiaryText.isNotEmpty())
            drawInnerLine(canvas)
        drawSecondaryText(canvas)
        drawTertiaryText(canvas)
    }

    /**
     * Draws big text (usually used for average score value)*/
    private fun drawPrimaryText(canvas: Canvas?) {
        if (primaryText.isNotEmpty()) {
            textPaint.color = innerTextColor
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = circleRadiuses.last() / 1.7F
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val textWidth = textPaint.measureText(primaryText)
            val textX = width / 2f - textWidth / 2f
            var textY = height / 2f
            if (secondaryText.isEmpty() && tertiaryText.isEmpty())
                textY += textPaint.textSize / 3

            canvas?.drawText(primaryText, textX, textY, textPaint)
        }
    }

    /**
     * Draws small inner text (second line)*/
    private fun drawSecondaryText(canvas: Canvas?) {
        if (secondaryText.isNotEmpty()) {
            textPaint.color = secondaryTextColor
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize =
                if (primaryText.isNotEmpty()) circleRadiuses.last() / 6F else circleRadiuses.last() / 4.4F
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val textWidth =
                if (primaryText.isNotEmpty()) textPaint.measureText(secondaryText) else (circleRadiuses.last() - circleWidth) * 2
            val textX = width / 2f
            var textY = height / 2f
            if (primaryText.isNotEmpty()) textY += textPaint.textSize


            val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder
                    .obtain(secondaryText, 0, secondaryText.length, textPaint, textWidth.toInt())
                    .build()
            } else {
                StaticLayout(
                    secondaryText,
                    textPaint,
                    textWidth.toInt(),
                    Layout.Alignment.ALIGN_NORMAL,
                    1F,
                    0F,
                    false
                )
            }
            staticLayout.draw(
                canvas,
                textX,
                textY
            )
        }
    }

    /**
     * Draws small inner text (third line)*/
    private fun drawTertiaryText(canvas: Canvas?) {
        if (tertiaryText.isNotEmpty()) {
            textPaint.color = tertiaryTextColor
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize =
                if (primaryText.isNotEmpty()) circleRadiuses.last() / 4.6F else circleRadiuses.last() / 5.4F
            textPaint.typeface = if (primaryText.isNotEmpty()) Typeface.create(
                Typeface.DEFAULT,
                Typeface.BOLD
            ) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val textWidth = textPaint.measureText(tertiaryText)
            val textX = width / 2f - textWidth / 2f
            var textY = height / 2f
            if (primaryText.isNotEmpty()) textY += circleRadiuses.last() / 1.6F

            canvas?.drawText(tertiaryText, textX, textY, textPaint)
        }
    }

    /**
     * Draws dotted line between score value and text */
    private fun drawInnerLine(canvas: Canvas?) {
        linePaint.color = lineColor
        linePaint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)

        val y = height / 2f + textPaint.textSize / 5f
        val startX = width / 2 - circleRadiuses.last() / 1.6F
        val endX = width / 2 + circleRadiuses.last() / 1.6F

        drawLine(startX, y, endX, y, canvas)
    }

    /**
     * Draws line from (startX, startY) to (endX, endY)
     * */
    private fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float, canvas: Canvas?) {
        val path = Path()
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)
        canvas?.drawPath(path, linePaint)
    }

   /*
    * Draws small inner text*//*
    private fun drawSecondaryText(canvas: Canvas?) {
        if (secondaryText.isNotEmpty()) {
            textPaint.textSize = textPaint.textSize / 2.25F
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val textWidth = textPaint.measureText(secondaryText)
            val textX = width / 2f - textWidth / 2f
            val textY = height / 2f + textPaint.textSize * 1.5F
            canvas?.drawText(secondaryText, textX, textY, textPaint)
        }
    }*/

    private fun initPaint() {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.isAntiAlias = true
        circlePaint.isDither = true

        textPaint.style = Paint.Style.FILL
        textPaint.isAntiAlias = true
        textPaint.color = innerTextColor

        linePaint.style = Paint.Style.STROKE
        linePaint.color = baseLineColor
        linePaint.strokeWidth = 3F
        linePaint.isAntiAlias = true
    }

    private fun initCircleMeasurementsIfEmpty() {
        if (outerCircleRadius == DEFAULT_RADIUS) {
            outerCircleRadius = (if (height < width) height else width) * 0.44f
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
        savedState?.innerTextColor = innerTextColor
        savedState?.primaryText = primaryText
        savedState?.lineColor = lineColor
        savedState?.dotColor = dotColor
        savedState?.backgroundCircleColor = backgroundCircleColor
        savedState?.circleWidth = circleWidth
        savedState?.outerCircleRadius = outerCircleRadius
        savedState?.circleRadiuses = circleRadiuses
        savedState?.circleValues = circleValues
        savedState?.percents = percents
        savedState?.dotRadius = dotRadius
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState)
        maxScore = savedState.maxScore
        secondaryText = savedState.secondaryText
        innerTextColor = savedState.innerTextColor
        primaryText = savedState.primaryText
        lineColor = savedState.lineColor
        dotColor = savedState.dotColor
        backgroundCircleColor = savedState.backgroundCircleColor
        outerCircleRadius = savedState.outerCircleRadius
        circleRadiuses = savedState.circleRadiuses
        circleValues = savedState.circleValues
        percents = savedState.percents
        dotRadius = savedState.dotRadius
        invalidate()
    }
}