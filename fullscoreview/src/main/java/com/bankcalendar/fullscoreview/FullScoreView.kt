package com.bankcalendar.fullscoreview

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Parcelable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.bankcalendar.fullscoreview.util.dpToPx
import com.bankcalendar.fullscoreview.util.draw
import kotlin.math.cos
import kotlin.math.sin


class FullScoreView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : this(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) : this(
        context,
        attrs
    )

    private val baseTextColor = Color.parseColor("#2C3E50")
    private val baseCircleColor = Color.parseColor("#53C283")
    private val baseSecondaryInnerCircleColor = Color.parseColor("#dfe4e5")

    var scoreList: IntArray = intArrayOf(0, 0, 0, 0, 0)
        set(value) {
            field = value
            invalidate()
        }

    var maxScore: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var primaryText: String = ""
        set(value) {
            field = value
            invalidate()
        }

    var primaryTextColor: Int = baseTextColor
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

    var circleColors: IntArray = intArrayOf(
        baseCircleColor,
        baseCircleColor,
        baseCircleColor,
        baseCircleColor,
        baseCircleColor
    )
        set(value) {
            field = value
            invalidate()
        }

    private var circleWidth: Float = 0f
    private var circleRadius: Float = 0f
    private var outerCircleRadius: Float = 0f
    private var outerCircleWidth: Float = 0f
    private var secondaryCircleRadius: Float = 0f
    private var secondaryCircleWidth: Float = 0f

    private val circlePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var shadow: RadialGradient

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.FullScoreView,
            0, 0
        ).apply {
            try {
                maxScore = getInteger(R.styleable.FullScoreView_maxScore, 0)

                primaryText = getString(R.styleable.FullScoreView_primaryText) ?: ""
                primaryTextColor =
                    getColor(R.styleable.FullScoreView_primaryTextColor, baseTextColor)

                secondaryText = getString(R.styleable.FullScoreView_secondaryText) ?: ""
                secondaryTextColor =
                    getColor(R.styleable.FullScoreView_secondaryTextColor, baseTextColor)

                tertiaryText = getString(R.styleable.FullScoreView_tertiaryText) ?: ""
                tertiaryTextColor =
                    getColor(R.styleable.FullScoreView_tertiaryTextColor, baseTextColor)

                circleWidth = getDimension(R.styleable.FullScoreView_circleWidth, 0f)
                circleRadius = getDimension(R.styleable.FullScoreView_circleRadius, 0f)

                outerCircleRadius = getDimension(
                    R.styleable.FullScoreView_outerCircleRadius,
                    0f
                )
                outerCircleWidth = getDimension(
                    R.styleable.FullScoreView_outerCircleWidth,
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
                    outerCircleRadius * 2 + outerCircleWidth +
                    secondaryCircleRadius * 4 + secondaryCircleWidth * 4
        } else {
            paddingLeft + paddingRight + suggestedMinimumWidth +
                    outerCircleRadius * 2 + outerCircleWidth +
                    secondaryCircleRadius * 4 + secondaryCircleWidth * 4
        }
        val needHeight = paddingTop + paddingBottom + suggestedMinimumHeight +
                outerCircleRadius * 2 + outerCircleWidth +
                secondaryCircleRadius * 4 + secondaryCircleWidth * 4
        val measuredWidth = calculateSize(needWidth.toInt(), widthMeasureSpec)
        val measuredHeight = calculateSize(needHeight.toInt(), heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initCircleMeasurementsIfEmpty()

        drawShadow(canvas)
        drawSecondaryCircles(canvas)
        drawWhiteCircle(canvas)
        drawPrimaryCircle(canvas)
        drawGaps(canvas)
        drawInnerText(canvas)
    }

    /**
     * Draws shadow on background*/
    private fun drawShadow(canvas: Canvas?) {
        circlePaint.style = Paint.Style.FILL
        val x = width / 2F + calculateHorizontalPadding()
        val y = outerCircleWidth / 2F + outerCircleRadius + paddingTop +
                secondaryCircleWidth * 2 + secondaryCircleRadius * 2
        val shadowColor = Color.parseColor("#D6D7D8")

        // gets shadow color but with 7% opacity
        val gradientBackgroundString = String.format("%06X", (0xFFFFFF and shadowColor))
        val gradientBackgroundColor = Color.parseColor("#17$gradientBackgroundString")

        shadow = RadialGradient(
            x + 10f,
            y + 10f,
            outerCircleRadius,
            intArrayOf(gradientBackgroundColor, shadowColor, gradientBackgroundColor),
            floatArrayOf(0.65f, 0.8f, 1f),
            Shader.TileMode.CLAMP
        )

        circlePaint.shader = shadow

        canvas?.drawCircle(x, y, outerCircleRadius + outerCircleWidth / 2, circlePaint)
    }

    /**
     * Draws white arcs*/
    private fun drawWhiteCircle(canvas: Canvas?) {
        val centerY = outerCircleRadius + outerCircleWidth / 2f +
                secondaryCircleWidth * 2 + secondaryCircleRadius * 2 + paddingTop

        val oval = getOval(width / 2f, centerY, circleRadius)


        circlePaint.strokeWidth = circleWidth - 1
        circlePaint.color = Color.WHITE
        circlePaint.shader = null

        val step = (360 / scoreList.size).toFloat()
        val gap = 6f

        for (i in scoreList.indices) {
            circlePaint.style = Paint.Style.STROKE

            val currentAngle = 90F + step * i
            val angleStart = currentAngle /*+ gap*/
            val angleEnd = step /*- gap * 2*/

            canvas?.drawArc(oval, angleStart, angleEnd, false, circlePaint)

            /*circlePaint.style = Paint.Style.FILL

            val startX = (cos(Math.toRadians(angleStart.toDouble()))
                    * circleRadius + width / 2F).toFloat() + calculateHorizontalPadding()
            val startY = (sin(Math.toRadians(angleStart.toDouble()))
                    * circleRadius + centerY).toFloat() + paddingTop
            canvas?.drawCircle(startX, startY, circleWidth / 2, circlePaint)

            val endX = (cos(Math.toRadians((currentAngle + step - gap).toDouble()))
                    * circleRadius + width / 2F).toFloat() + calculateHorizontalPadding()
            val endY = (sin(Math.toRadians((currentAngle + step - gap).toDouble()))
                    * circleRadius + centerY).toFloat() + paddingTop
            canvas?.drawCircle(endX, endY, circleWidth / 2, circlePaint)*/
        }

    }

    /**
     * Draws colored arcs
     * */
    private fun drawPrimaryCircle(canvas: Canvas?) {
        if ((scoreList.isNotEmpty() && scoreList.any { it != 0 }) && circleColors.isNotEmpty() && scoreList.size <= circleColors.size) {

            val centerY =
                outerCircleRadius + outerCircleWidth / 2F + secondaryCircleWidth * 2 + secondaryCircleRadius * 2 + paddingTop

            val oval = getOval(width / 2F, centerY, circleRadius)
            val step = (360 / scoreList.size).toFloat()
            val gap = 6f

            for (i in scoreList.indices) {

                if (scoreList[i] != 0) {
                    circlePaint.style = Paint.Style.STROKE
                    circlePaint.strokeWidth = circleWidth
                    circlePaint.color = circleColors[i]
                    circlePaint.shader = null

                    val percent = 100 * scoreList[i] / maxScore
                    val angle = 360 * percent / 100F

                    val angleStart = 90F + step * i /*+ gap*/
                    val angleEnd = angle /*- gap * 2*/

                    canvas?.drawArc(oval, angleStart, angleEnd, false, circlePaint)

                    /*circlePaint.style = Paint.Style.FILL

                    val startX =
                        (cos(Math.toRadians(angleStart.toDouble())) * circleRadius + width / 2F).toFloat() +
                                calculateHorizontalPadding()
                    val startY = (sin(Math.toRadians(angleStart.toDouble()))
                            * circleRadius + centerY).toFloat() + paddingTop
                    canvas?.drawCircle(startX, startY, circleWidth / 2, circlePaint)

                    circlePaint.color = circleColors[i]
                    val endX = (cos(Math.toRadians((angleStart + angleEnd).toDouble()))
                            * circleRadius + width / 2F).toFloat() + calculateHorizontalPadding()
                    val endY = (sin(Math.toRadians((angleStart + angleEnd).toDouble()))
                            * circleRadius + centerY).toFloat() + paddingTop
                    canvas?.drawCircle(endX, endY, circleWidth / 2, circlePaint)*/
                }
            }
        }
    }

    private fun calculateHorizontalPadding(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            paddingStart - paddingEnd
        } else {
            paddingLeft - paddingRight
        }

    private fun drawGaps(canvas: Canvas?) {
        val centerY = outerCircleRadius + outerCircleWidth / 2f +
                secondaryCircleWidth * 2 + secondaryCircleRadius * 2 + paddingTop

        val gap = 8f
        circlePaint.strokeWidth = gap
        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = Color.WHITE

        val step = (360 / scoreList.size).toFloat()


        for (i in scoreList.indices) {

            circlePaint.shader = null

            val currentAngle = 90F + step * i
            val angleStart = currentAngle /*+ gap*/

            val startX =
                (cos(Math.toRadians(angleStart.toDouble())) * (circleRadius - circleWidth / 2) + width / 2F).toFloat() +
                        calculateHorizontalPadding()
            val startY = (sin(Math.toRadians(angleStart.toDouble()))
                    * (circleRadius - circleWidth / 2) + centerY).toFloat() + paddingTop

            val endX = (cos(Math.toRadians(angleStart.toDouble())) * (circleRadius + circleWidth / 2) + width / 2F).toFloat() +
                    calculateHorizontalPadding()
            val endY = (sin(Math.toRadians(angleStart.toDouble()))
                    * (circleRadius + circleWidth / 2) + centerY).toFloat() + paddingTop

            canvas?.drawLine(startX, startY, endX, endY, circlePaint)

            circlePaint.shader = shadow

            canvas?.drawLine(startX, startY, endX, endY, circlePaint)
        }
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
            textPaint.color = primaryTextColor
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = circleRadius / 2F
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val textWidth = textPaint.measureText(primaryText)
            val textX = width / 2f - textWidth / 2f + calculateHorizontalPadding()
            val textY = if (secondaryText.isEmpty() && tertiaryText.isEmpty()) {
                outerCircleWidth / 2f + outerCircleRadius + textPaint.textSize / 3 +
                        paddingTop + secondaryCircleWidth + secondaryCircleRadius * 2
            } else {
                outerCircleWidth / 2f + outerCircleRadius - textPaint.textSize / 3 + circleRadius / 8F +
                        paddingTop + secondaryCircleWidth + secondaryCircleRadius * 2
            }

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
                if (primaryText.isNotEmpty()) circleRadius / 8F else circleRadius / 6.4F
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val textWidth =
                if (primaryText.isNotEmpty()) textPaint.measureText(secondaryText) else (circleRadius - circleWidth) * 2
            val textX = width / 2f + calculateHorizontalPadding()
            var textY = outerCircleWidth / 2f + outerCircleRadius +
                    secondaryCircleRadius + secondaryCircleWidth + paddingTop
            if (primaryText.isNotEmpty()) textY += textPaint.textSize * 2 + secondaryCircleRadius


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
                if (primaryText.isNotEmpty()) circleRadius / 5.6F else circleRadius / 6.4F
            textPaint.typeface = if (primaryText.isNotEmpty()) Typeface.create(
                Typeface.DEFAULT,
                Typeface.BOLD
            ) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val textWidth = textPaint.measureText(tertiaryText)
            val textX = width / 2f - textWidth / 2f + calculateHorizontalPadding()
            var textY = outerCircleWidth / 2f + outerCircleRadius +
                    circleRadius / 2F + paddingTop +
                    secondaryCircleWidth + secondaryCircleRadius * 2
            if (primaryText.isNotEmpty()) textY += circleRadius / 8F

            canvas?.drawText(tertiaryText, textX, textY, textPaint)
        }
    }

    /**
     * Draws dotted line between score value and text */
    private fun drawInnerLine(canvas: Canvas?) {
        val y = outerCircleWidth / 2f + outerCircleRadius + circleRadius / 8F +
                paddingTop + secondaryCircleWidth + secondaryCircleRadius * 2
        val startX = width / 2 - outerCircleRadius / 2
        val endX = width / 2 + outerCircleRadius / 2
        drawLine(startX, y, endX, y, canvas)
    }

    /**
     * Draws small circles around primary one
     * Each circle has individual value for one of scores
     * */
    private fun drawSecondaryCircles(canvas: Canvas?) {
        var prevAngle = 126F

        // radius of circle where outer circles are placed
        val outerSecondaryRadius = outerCircleRadius + outerCircleWidth / 2 +
                secondaryCircleRadius + secondaryCircleWidth
        val step = (360 / scoreList.size).toFloat()

        for (i in scoreList.indices) {
            // coordinates of circle center
            val x = (cos(Math.toRadians(prevAngle.toDouble())) *
                    outerSecondaryRadius + width / 2F).toFloat() + calculateHorizontalPadding()
            val y = (sin(Math.toRadians(prevAngle.toDouble()))
                    * outerSecondaryRadius + outerSecondaryRadius
                    + secondaryCircleWidth + secondaryCircleRadius).toFloat() + paddingTop

            // coordinates for line start
            val startX = (cos(Math.toRadians(prevAngle.toDouble())) *
                    circleRadius + width / 2F).toFloat() + calculateHorizontalPadding()
            val startY = (sin(Math.toRadians(prevAngle.toDouble()))
                    * circleRadius + outerSecondaryRadius
                    + secondaryCircleWidth + secondaryCircleRadius).toFloat() + paddingTop
            drawLine(startX, startY, x, y, canvas)


            drawSecondaryCircle(x, y, canvas)
            drawIndividualScoreValue(scoreList[i], circleColors[i], x, y, canvas)
            drawIndividualScoreText(scoreList[i], x, y, canvas)
            prevAngle += step
        }
    }

    /**
     * Draws little white circle with gray border
     * */
    private fun drawSecondaryCircle(x: Float, y: Float, canvas: Canvas?) {
        circlePaint.style = Paint.Style.FILL
        circlePaint.color = Color.WHITE
        circlePaint.shader = null
        canvas?.drawCircle(x, y, secondaryCircleRadius, circlePaint)

        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = baseSecondaryInnerCircleColor
        circlePaint.strokeWidth = secondaryCircleWidth
        canvas?.drawCircle(x, y, secondaryCircleRadius, circlePaint)
    }

    /**
     * Draws colored arc, that shows score value
     * */
    private fun drawIndividualScoreValue(
        score: Int,
        color: Int,
        x: Float,
        y: Float,
        canvas: Canvas?
    ) {
        if (score != 0) {
            val percent = 100 * score / (maxScore / 5)
            circlePaint.style = Paint.Style.STROKE
            circlePaint.color = color
            circlePaint.strokeWidth = secondaryCircleWidth
            circlePaint.shader = null
            val individualValueRadius = secondaryCircleRadius + secondaryCircleWidth / 2
            val angle = 360 * percent / 100F
            val oval = getOval(x, y, individualValueRadius)
            canvas?.drawArc(oval, 90F, angle, false, circlePaint)

            circlePaint.style = Paint.Style.FILL

            val startX = (cos(Math.toRadians(90.toDouble()))
                    * individualValueRadius + x).toFloat() + calculateHorizontalPadding()
            val startY = (sin(Math.toRadians(90.toDouble()))
                    * individualValueRadius + y).toFloat() + paddingTop

            val endX = (cos(Math.toRadians(90 + angle.toDouble()))
                    * individualValueRadius + x).toFloat() + calculateHorizontalPadding()
            val endY = (sin(Math.toRadians(90 + angle.toDouble()))
                    * individualValueRadius + y).toFloat() + paddingTop

            canvas?.drawCircle(startX, startY, secondaryCircleWidth / 2, circlePaint)
            canvas?.drawCircle(endX, endY, secondaryCircleWidth / 2, circlePaint)
        }
    }

    /**
     * Draws value inside of secondary circle
     * */
    private fun drawIndividualScoreText(score: Int, x: Float, y: Float, canvas: Canvas?) {
        textPaint.color = primaryTextColor
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = secondaryCircleRadius / 1.5F
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val textWidth = textPaint.measureText(score.toString())

        val textX = x - textWidth / 2f + calculateHorizontalPadding()
        val textY = y + textPaint.textSize / 3

        canvas?.drawText(score.toString(), textX, textY, textPaint)
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

    private fun getOval(centerX: Float, centerY: Float, radius: Float) = RectF(
        centerX - radius + calculateHorizontalPadding(),
        centerY - radius,
        centerX + radius + calculateHorizontalPadding(),
        centerY + radius
    )

    private fun initPaint() {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.isDither = true

        textPaint.style = Paint.Style.FILL
        textPaint.color = primaryTextColor

        linePaint.style = Paint.Style.STROKE
        linePaint.color = Color.parseColor("#BEC4C8")
        linePaint.strokeWidth = 2F
        linePaint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
    }

    private fun initCircleMeasurementsIfEmpty() {
        if (outerCircleRadius == 0f) {
            outerCircleRadius = if (height < width) height * 0.27f else width * 0.27f
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

        if (secondaryCircleRadius == 0f) {
            secondaryCircleRadius = outerCircleRadius / 3.5f
        }

        if (secondaryCircleWidth == 0f) {
            secondaryCircleWidth = secondaryCircleRadius / 5f
        }
        requestLayout()
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
        savedState?.score = scoreList
        savedState?.maxScore = maxScore

        savedState?.innerText = primaryText
        savedState?.innerTextColor = primaryTextColor

        savedState?.secondaryText = secondaryText
        savedState?.secondaryTextColor = secondaryTextColor

        savedState?.tertiaryText = tertiaryText
        savedState?.tertiaryTextColor = tertiaryTextColor

        savedState?.circleWidth = circleWidth
        savedState?.circleRadius = circleRadius

        savedState?.outerCircleWidth = outerCircleWidth
        savedState?.outerCircleRadius = outerCircleRadius

        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState)
        scoreList = savedState.score
        maxScore = savedState.maxScore

        primaryText = savedState.innerText
        primaryTextColor = savedState.innerTextColor

        secondaryText = savedState.secondaryText
        secondaryTextColor = savedState.secondaryTextColor

        tertiaryText = savedState.tertiaryText
        tertiaryTextColor = savedState.tertiaryTextColor

        circleWidth = savedState.circleWidth
        circleRadius = savedState.circleRadius

        outerCircleWidth = savedState.outerCircleWidth
        outerCircleRadius = savedState.outerCircleRadius
        invalidate()
    }
}