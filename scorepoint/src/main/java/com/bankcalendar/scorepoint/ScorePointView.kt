package com.bankcalendar.scorepoint

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin


class ScorePointView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : this(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) : this(
        context,
        attrs
    )

    private val baseTextColor = Color.parseColor("#2C3E50")
    private val baseCircleColor = Color.parseColor("#53C283")

    var score: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var maxScore: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var innerText: String = ""
        set(value) {
            field = value
            invalidate()
        }

    var innerTextColor: Int = baseTextColor
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

    var circleColor: Int = baseCircleColor
        set(value) {
            field = value
            invalidate()
        }

    private var circleWidth: Float = 0f
    private var circleRadius: Float = 0f
    private var outerCircleRadius: Float = 0f
    private var outerCircleWidth: Float = 0f
    private var dotRadius: Float = 0f

    private val circlePaint: Paint = Paint()
    private val textPaint: TextPaint = TextPaint()
    private val linePaint: Paint = Paint()

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ScorePointView,
            0, 0
        ).apply {
            try {
                score = getInteger(R.styleable.ScorePointView_score, 0)
                maxScore = getInteger(R.styleable.ScorePointView_maxScore, 0)

                innerText = getString(R.styleable.ScorePointView_innerText) ?: ""
                innerTextColor = getColor(R.styleable.ScorePointView_innerTextColor, baseTextColor)

                secondaryText = getString(R.styleable.ScorePointView_secondaryText) ?: ""
                secondaryTextColor = getColor(R.styleable.ScorePointView_secondaryTextColor, baseTextColor)

                tertiaryText = getString(R.styleable.ScorePointView_tertiaryText) ?: ""
                tertiaryTextColor = getColor(R.styleable.ScorePointView_tertiaryTextColor, baseTextColor)

                circleWidth = getDimension(R.styleable.ScorePointView_circleWidth, 0f)
                circleRadius = getDimension(R.styleable.ScorePointView_circleRadius, 0f)
                circleColor = getColor(R.styleable.ScorePointView_circleColor, baseCircleColor)

                outerCircleRadius = getDimension(
                    R.styleable.ScorePointView_outerCircleRadius,
                    0f
                )
                outerCircleWidth = getDimension(
                    R.styleable.ScorePointView_outerCircleWidth,
                    0f
                )
            } finally {
                recycle()
            }
        }
        initPaint()
        initCircleMeasurementsIfEmpty()
        isSaveEnabled = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val needWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            paddingStart + paddingEnd + suggestedMinimumWidth +
                    outerCircleRadius * 2 + outerCircleWidth
        } else {
            paddingLeft + paddingRight + suggestedMinimumWidth +
                    outerCircleRadius * 2 + outerCircleWidth
        }
        val needHeight = paddingTop + paddingBottom + suggestedMinimumHeight +
                outerCircleRadius * 2 + outerCircleWidth
        val measuredWidth = calculateSize(needWidth.toInt(), widthMeasureSpec)
        val measuredHeight = calculateSize(needHeight.toInt(), heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawShadow(canvas)
        drawWhiteCircle(canvas)
        drawPrimaryCircle(canvas)
        drawInnerText(canvas)
    }

    /*
    * Draws colored shadow on background*/
    private fun drawShadow(canvas: Canvas?) {
        circlePaint.style = Paint.Style.FILL
        val x = width / 2F + calculateHorizontalPadding()
        val y = outerCircleWidth / 2F + outerCircleRadius + paddingTop

        // adds 7% opacity to circle color
        val gradientBackgroundString = String.format("%06X", (0xFFFFFF and circleColor))
        val gradientBackgroundColor = Color.parseColor("#12$gradientBackgroundString")

        circlePaint.shader = RadialGradient(
            x + 30f,
            y + 30f,
            outerCircleRadius,
            circleColor, gradientBackgroundColor,
            Shader.TileMode.CLAMP
        )

        canvas?.drawCircle(
            x,
            y,
            outerCircleRadius + outerCircleWidth / 2,
            circlePaint
        )
    }

    /*
    * Draws white circle*/
    private fun drawWhiteCircle(canvas: Canvas?) {
        circlePaint.style = Paint.Style.FILL
        circlePaint.color = Color.WHITE
        circlePaint.shader = null
        val x = width / 2F + calculateHorizontalPadding()
        val y = outerCircleWidth / 2F + outerCircleRadius + paddingTop

        canvas?.drawCircle(
            x,
            y,
            circleRadius,
            circlePaint
        )
    }

    /*
    * Draws colored circle
    * */
    private fun drawPrimaryCircle(canvas: Canvas?) {
        val percent = 100 * score / maxScore
        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = circleColor
        circlePaint.strokeWidth = circleWidth
        circlePaint.shader = null
        val angle = 360 * percent / 100F
        val oval = getOval(circleRadius)
        canvas?.drawArc(oval, 90F, angle, false, circlePaint)

        circlePaint.style = Paint.Style.FILL

        val startX = (cos(Math.toRadians(90.toDouble())) * circleRadius + width / 2F).toFloat() +
                calculateHorizontalPadding()
        val startY = (sin(Math.toRadians(90.toDouble()))
                * circleRadius + outerCircleRadius + outerCircleWidth / 2F).toFloat() + paddingTop

        val endX = (cos(Math.toRadians(90 + angle.toDouble()))
                * circleRadius + width / 2F).toFloat() + calculateHorizontalPadding()
        val endY = (sin(Math.toRadians(90 + angle.toDouble()))
                * circleRadius + outerCircleRadius + outerCircleWidth / 2F).toFloat() + paddingTop

        canvas?.drawCircle(startX, startY, circleWidth / 2, circlePaint)
        canvas?.drawCircle(endX, endY, circleWidth / 2, circlePaint)
        drawDot(endX, endY, canvas)
    }

    /*
    * Draws little white dot on primary circle*/
    private fun drawDot(x: Float, y: Float, canvas: Canvas?) {
        circlePaint.color = Color.WHITE
        canvas?.drawCircle(x, y, dotRadius, circlePaint)
    }

    private fun getOval(radius: Float): RectF = RectF(
        width / 2F - radius + calculateHorizontalPadding(),
        outerCircleWidth / 2F + outerCircleRadius - radius + paddingTop,
        width / 2F + radius + calculateHorizontalPadding(),
        outerCircleWidth / 2F + outerCircleRadius + radius + paddingTop
    )

    private fun calculateHorizontalPadding(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            paddingStart - paddingEnd
        } else {
            paddingLeft - paddingRight
        }

    /*
    * Draws inner text*/
    private fun drawInnerText(canvas: Canvas?) {
        drawPrimaryText(canvas)
        if (secondaryText.isNotEmpty() && tertiaryText.isNotEmpty())
            drawLine(canvas)
        drawSecondaryText(canvas)
        drawTertiaryText(canvas)
    }

    /*
    * Draws big colored text*/
    private fun drawPrimaryText(canvas: Canvas?) {
        if (innerText.isEmpty()) {
            innerText = "$score"
        }
        textPaint.color = circleColor
        textPaint.textSize = circleRadius / 2F
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val textWidth = textPaint.measureText(innerText)
        val textX = width / 2f - textWidth / 2f + calculateHorizontalPadding()
        val textY = if (secondaryText.isEmpty() && tertiaryText.isEmpty()) {
            outerCircleWidth / 2f + outerCircleRadius + textPaint.textSize / 3 + paddingTop
        } else {
            outerCircleWidth / 2f + outerCircleRadius - textPaint.textSize / 3 + circleRadius / 8F + paddingTop
        }

        canvas?.drawText(innerText, textX, textY, textPaint)
    }

    /*
    * Draws small inner text (second line)*/
    private fun drawSecondaryText(canvas: Canvas?) {
        if (secondaryText.isNotEmpty()) {
            textPaint.color = secondaryTextColor
            textPaint.textSize = circleRadius / 8F
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val textWidth = textPaint.measureText(secondaryText)
            val textX = width / 2f - textWidth / 2f + calculateHorizontalPadding()
            val textY = outerCircleWidth / 2f + outerCircleRadius + textPaint.textSize * 2f + circleRadius / 8F + paddingTop

            canvas?.drawText(secondaryText, textX, textY, textPaint)
        }
    }

    /*
    * Draws small inner text (third line)*/
    private fun drawTertiaryText(canvas: Canvas?) {
        if (tertiaryText.isNotEmpty()) {
            textPaint.color = tertiaryTextColor
            textPaint.textSize = circleRadius / 5.6F
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val textWidth = textPaint.measureText(tertiaryText)
            val textX = width / 2f - textWidth / 2f + calculateHorizontalPadding()
            val textY = outerCircleWidth / 2f + outerCircleRadius + circleRadius / 2F + circleRadius / 8F + paddingTop

            canvas?.drawText(tertiaryText, textX, textY, textPaint)
        }
    }

    /*
   * Draws dotted line between score value and text */
    private fun drawLine(canvas: Canvas?) {
        val y = outerCircleWidth / 2f + outerCircleRadius + circleRadius / 8F + paddingTop
        val startX = outerCircleWidth / 2f + outerCircleRadius - outerCircleRadius / 2
        val endX = outerCircleWidth / 2f + outerCircleRadius + outerCircleRadius / 2
        val path = Path()
        path.moveTo(startX, y)
        path.lineTo(endX, y)
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
        linePaint.color = Color.parseColor("#BEC4C8")
        linePaint.strokeWidth = 2F
        linePaint.isAntiAlias = true
        linePaint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
    }

    private fun initCircleMeasurementsIfEmpty() {
        if (outerCircleRadius == 0f) {
            outerCircleRadius = if (height <= width) height * 0.39f else width * 0.39f
        }

        if (outerCircleWidth == 0f) {
            outerCircleWidth = outerCircleRadius / 2.1F
        }

        if (circleRadius == 0f) {
            circleRadius = outerCircleRadius - outerCircleWidth / 3
        }

        if (circleWidth == 0f) {
            circleWidth = outerCircleRadius / 7
        }

        dotRadius = circleWidth / 3.7f
    }

    private fun calculateSize(contentSize: Int, measureSpec: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> if (contentSize < specSize) contentSize else specSize
            else -> contentSize
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = super.onSaveInstanceState()?.let { SavedState(it) }
        savedState?.score = score
        savedState?.maxScore = maxScore

        savedState?.innerText = innerText
        savedState?.innerTextColor = innerTextColor

        savedState?.secondaryText = secondaryText
        savedState?.secondaryTextColor = secondaryTextColor

        savedState?.tertiaryText = tertiaryText
        savedState?.tertiaryTextColor = tertiaryTextColor

        savedState?.circleWidth = circleWidth
        savedState?.circleRadius = circleRadius
        savedState?.circleColor = circleColor

        savedState?.outerCircleWidth = outerCircleWidth
        savedState?.outerCircleRadius = outerCircleRadius

        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState)
        score = savedState.score
        maxScore = savedState.maxScore

        innerText = savedState.innerText
        innerTextColor = savedState.innerTextColor

        secondaryText = savedState.secondaryText
        secondaryTextColor = savedState.secondaryTextColor

        tertiaryText = savedState.tertiaryText
        tertiaryTextColor = savedState.tertiaryTextColor

        circleWidth = savedState.circleWidth
        circleRadius = savedState.circleRadius
        circleColor = savedState.circleColor

        outerCircleWidth = savedState.outerCircleWidth
        outerCircleRadius = savedState.outerCircleRadius
        invalidate()
    }
}