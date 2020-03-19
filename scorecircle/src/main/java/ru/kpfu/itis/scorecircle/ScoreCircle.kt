package ru.kpfu.itis.scorecircle

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import ru.kpfu.itis.scorecircle.util.spToPx
import kotlin.math.cos
import kotlin.math.sin


class ScoreCircle(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        const val SCORE_AND_TEXT = 0
        const val CURRENT_SCORE = 1
        const val DEFAULT_RADIUS = 100F
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : this(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) : this(
        context,
        attrs
    )

    var currentScore: Int = 0
        set(value) {
            field = value
            invalidate()
        }

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

    var scoreColor: Int = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }

    var secondaryTextColor: Int = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }

    var valueDisplayingMode: Int = CURRENT_SCORE
        set(value) {
            field = value
            invalidate()
        }

    var dotColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    var primaryCircleColor: Int = Color.GREEN
        set(value) {
            field = value
            invalidate()
        }

    var secondaryCircleColor: Int = Color.GRAY
        set(value) {
            field = value
            invalidate()
        }

    var circleWidth: Float = 0F
        set(value) {
            field = value
            invalidate()
        }

    var circleRadius: Float = 0F
        set(value) {
            field = value
            invalidate()
        }

    private val circlePaint: Paint = Paint()
    private val textPaint: Paint = Paint()

    private val primaryTextSize = spToPx(48F, context)
    private val secondaryTextSize = spToPx(16F, context)

    private var dotRadius : Float = 0F

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ScoreCircle,
            0, 0
        ).apply {
            try {
                currentScore = getInteger(R.styleable.ScoreCircle_currentScore, 0)
                maxScore = getInteger(R.styleable.ScoreCircle_maxScore, 1000)
                secondaryText = getString(R.styleable.ScoreCircle_secondaryText) ?: ""
                valueDisplayingMode = getInt(R.styleable.ScoreCircle_valueDisplayingMode, SCORE_AND_TEXT)
                circleWidth = getDimension(R.styleable.ScoreCircle_circleWidth, 0F)
                circleRadius = getDimension(R.styleable.ScoreCircle_circleRadius, DEFAULT_RADIUS)
                scoreColor = getColor(R.styleable.ScoreCircle_scoreColor, Color.BLACK)
                secondaryTextColor = getColor(R.styleable.ScoreCircle_secondaryTextColor, Color.BLACK)
                dotColor = getColor(R.styleable.ScoreCircle_dotColor, Color.WHITE)
                primaryCircleColor = getColor(R.styleable.ScoreCircle_primaryCircleColor, Color.GREEN)
                secondaryCircleColor = getColor(R.styleable.ScoreCircle_secondaryCircleColor, Color.GRAY)
            }
            finally {
                recycle()
            }
        }
        initPaint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val needWidth = paddingStart + paddingEnd + suggestedMinimumWidth +
                circleRadius * 2 + circleWidth
        val needHeight = paddingTop + paddingBottom + suggestedMinimumHeight +
                circleRadius * 2 + circleWidth
        val measuredWidth = calculateSize(needWidth.toInt(), widthMeasureSpec)
        val measuredHeight = calculateSize(needHeight.toInt(), heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        initCircleMeasurementsIfEmpty()
        drawSecondaryCircle(canvas)
        drawPrimaryCircle(canvas)
        drawText(canvas)
    }

    private fun drawSecondaryCircle(canvas: Canvas?) {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = secondaryCircleColor
        circlePaint.strokeWidth = circleWidth * 0.95f
        canvas?.drawCircle(width / 2F, height / 2F, circleRadius, circlePaint)
    }

    private fun drawPrimaryCircle(canvas: Canvas?) {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = primaryCircleColor
        circlePaint.isDither = true
        circlePaint.strokeWidth = circleWidth
        val percent = 100 * currentScore / maxScore
        val angle = 360 * percent / 100F
        val oval = RectF(width / 2F - circleRadius,
            height / 2F - circleRadius,
            width / 2F + circleRadius,
            height / 2F + circleRadius)
        canvas?.drawArc(oval, 90F, angle, false, circlePaint)
        val endX = (cos(Math.toRadians(90 + angle.toDouble())) * circleRadius + width / 2F).toFloat()
        val endY = (sin(Math.toRadians(90 + angle.toDouble())) * circleRadius + height / 2F).toFloat()
        circlePaint.style = Paint.Style.FILL
        canvas?.drawCircle(endX, endY, circleWidth / 2, circlePaint)
        drawDot(endX, endY, canvas)
    }

    private fun drawDot(x: Float, y: Float, canvas: Canvas?) {
        circlePaint.color = dotColor
        canvas?.drawCircle(x, y, circleWidth / 3.5f, circlePaint)
    }

    private fun drawText(canvas: Canvas?) {
        textPaint.color = scoreColor
        textPaint.textSize = circleRadius * 2 / 5f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        val text = currentScore.toString()
        var textWidth = textPaint.measureText(text)
        var textX = width / 2f - textWidth / 2f
        var textY = height / 2f
        canvas?.drawText(text, textX, textY, textPaint)
        if (valueDisplayingMode == SCORE_AND_TEXT) {
            if (secondaryText.isNotEmpty()) {
                textPaint.color = secondaryTextColor
                textPaint.textSize = textPaint.textSize / 3
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
                textWidth = textPaint.measureText(secondaryText)
                textX = width / 2f - textWidth / 2f
                textY = height / 2f + textPaint.textSize * 2
                canvas?.drawText(secondaryText, textX, textY, textPaint)
            }
        }
    }

    private fun initPaint() {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.isAntiAlias = true

        textPaint.style = Paint.Style.FILL
        textPaint.isAntiAlias = true
    }

    private fun initCircleMeasurementsIfEmpty() {
        if (circleRadius == DEFAULT_RADIUS) {
            circleRadius = width / 2f
        }
        if (circleWidth == 0F) {
            circleWidth = circleRadius / 6
        }
        circleRadius -= circleWidth / 2
        dotRadius = circleWidth / 4f
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