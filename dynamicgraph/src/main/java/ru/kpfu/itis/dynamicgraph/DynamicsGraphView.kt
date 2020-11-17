package ru.kpfu.itis.dynamicgraph

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import ru.kpfu.itis.dynamicgraph.util.dpToPx
import ru.kpfu.itis.dynamicgraph.util.getFormattedMonth
import java.util.*

class DynamicsGraphView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : this(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) : this(
        context,
        attrs
    )

    private val baseAxisColor = Color.parseColor("#96A6A7")
    private val baseTextColor = Color.parseColor("#96A6A7")
    private val baseCurveColor = Color.parseColor("#0097CE")

    private val baseMonthColor = Color.parseColor("#DFE4E5")
    private val baseCurrentMonthColor = Color.parseColor("#0097CE")

    var scoreList: IntArray = intArrayOf(0, 0, 0, 0, 0, 0)
        set(value) {
            field = value
            step = yAxisMaxValue / (value.size - 1)
            invalidate()
        }

    var yAxisMaxValue: Int = 1000
        set(value) {
            field = value
            step = value / (scoreList.size - 1)
            maxWidthOfYAxisText = textPaint.measureText((yAxisMaxValue).toString()).toInt()
            invalidate()
        }

    var yAxisValuesCount: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var xAxisValuesCount: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var axisColor: Int = baseAxisColor
        set(value) {
            field = value
            invalidate()
        }

    var textColor: Int = baseTextColor
        set(value) {
            field = value
            invalidate()
        }

    var curveColor: Int = baseCurveColor
        set(value) {
            field = value
            invalidate()
        }

    var curveWidth: Float = 0F
        set(value) {
            field = value
            invalidate()
        }

    var monthColor: Int = baseMonthColor
        set(value) {
            field = value
            invalidate()
        }

    var currentMonthColor: Int = baseCurrentMonthColor
        set(value) {
            field = value
            invalidate()
        }

    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val monthPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var marginXAxisAndValueInDp = 0f
    private var marginYAxisAndValueInDp = 0f

    private var maxWidthOfYAxisText = 0

    private var axisStartX = 0f
    private var step = 0

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DynamicsGraphView,
            0, 0
        ).apply {
            try {
                yAxisMaxValue = getInteger(R.styleable.DynamicsGraphView_yAxisMaxValue, 1000)

                xAxisValuesCount = getInteger(R.styleable.DynamicsGraphView_xAxisValuesCount, 6)
                yAxisValuesCount = getInteger(R.styleable.DynamicsGraphView_yAxisValuesCount, 6)

                axisColor = getColor(R.styleable.DynamicsGraphView_axisColor, baseAxisColor)
                textColor = getColor(R.styleable.DynamicsGraphView_textColor, baseTextColor)

                curveColor = getColor(R.styleable.DynamicsGraphView_curveColor, baseCurveColor)
                curveWidth = getDimension(
                    R.styleable.DynamicsGraphView_curveWidth,
                    dpToPx(3f, context)
                )

                monthColor = getColor(R.styleable.DynamicsGraphView_monthColor, baseMonthColor)
                currentMonthColor =
                    getColor(R.styleable.DynamicsGraphView_currentMonthColor, baseCurrentMonthColor)
            } finally {
                recycle()
            }
        }
        marginXAxisAndValueInDp = dpToPx(10f, context)
        marginYAxisAndValueInDp = dpToPx(20f, context)

        initPaint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = calculateSize(700, widthMeasureSpec)
        val measuredHeight = calculateSize(500, heightMeasureSpec)

        axisStartX = paddingLeft + maxWidthOfYAxisText + marginYAxisAndValueInDp * 2
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawHorizontalLinesAndYLabels(canvas)
        drawVerticalLinesAndXLabels(canvas)
        drawGraph(canvas)
    }

    private fun drawHorizontalLinesAndYLabels(canvas: Canvas?) {
        linePaint.color = axisColor
        linePaint.strokeWidth = 2F
        linePaint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
        val path = Path()

        textPaint.textAlign = Paint.Align.RIGHT
        val textX = axisStartX - marginYAxisAndValueInDp
        var textY = textPaint.textSize

        for (i in yAxisMaxValue downTo 0 step step) {
            val lineY = textY - textPaint.textSize / 3
            if (i != 0) {
                path.moveTo(axisStartX, lineY)
                path.lineTo(width.toFloat() - marginYAxisAndValueInDp, lineY)
                canvas?.drawPath(path, linePaint)
            } else {
                linePaint.pathEffect = null
                canvas?.drawLine(
                    axisStartX,
                    lineY,
                    width.toFloat() - marginYAxisAndValueInDp,
                    lineY,
                    linePaint
                )
            }

            canvas?.drawText(i.toString(), textX, textY, textPaint)
            textY += textPaint.textSize * 2
        }
    }

    private fun drawVerticalLinesAndXLabels(canvas: Canvas?) {
        monthPaint.shader = getLinearGradient(monthColor)
        textPaint.textAlign = Paint.Align.CENTER

        val gap = 3f
        val monthLineWidth = (width - axisStartX - marginYAxisAndValueInDp * 2) / scoreList.size

        var rectStart = axisStartX + marginYAxisAndValueInDp / 2

        val rectBottom = textPaint.textSize * 11 - textPaint.textSize / 3
        val lineHeight = dpToPx(2.5f, context)

        val currentMonth = Calendar.getInstance().apply {
            add(Calendar.MONTH, -5)
        }

        for (i in scoreList.indices) {
            if (i == scoreList.indices.last) {
                monthPaint.shader = getLinearGradient(currentMonthColor)
            }
            canvas?.drawRect(
                rectStart + gap,
                textPaint.textSize,
                rectStart + monthLineWidth - gap * 2,
                rectBottom,
                monthPaint
            )

            canvas?.drawLine(
                rectStart + monthLineWidth / 2,
                rectBottom - lineHeight,
                rectStart + monthLineWidth / 2,
                rectBottom + lineHeight,
                linePaint
            )

            canvas?.drawText(
                currentMonth.getFormattedMonth(),
                rectStart + monthLineWidth / 2,
                rectBottom + marginXAxisAndValueInDp + textPaint.textSize,
                textPaint
            )
            currentMonth.add(Calendar.MONTH, 1)

            rectStart += monthLineWidth
        }
    }

    private fun drawGraph(canvas: Canvas?) {
        linePaint.color = curveColor
        linePaint.strokeWidth = curveWidth
        val size = xAxisValuesCount + 2
        val points = mutableListOf<PointF>()
        val pointsCon1 = mutableListOf<PointF>()
        val pointsCon2 = mutableListOf<PointF>()
        for (i in 0 until size) {
            points.add(PointF())
            pointsCon1.add(PointF())
            pointsCon2.add(PointF())
        }
        val monthLineWidth = (width - axisStartX - marginYAxisAndValueInDp * 2) / scoreList.size

        val baseX = axisStartX + marginYAxisAndValueInDp / 2 + monthLineWidth / 2
        val baseY = textPaint.textSize - textPaint.textSize / 3

        var y = 0f

        for (i in 1 until size - 1) {
            y = baseY + ((yAxisMaxValue - scoreList[i - 1]) / step.toFloat() * textPaint.textSize * 2)
            points[i].x = baseX + (i - 1) * monthLineWidth
            points[i].y = y
        }
        points[0].set(axisStartX, points[1].y)
        points[size - 1].set(width.toFloat() - marginYAxisAndValueInDp, y)

        for (i in 1 until size) {
            pointsCon1[i].set((points[i].x + points[i - 1].x) / 2, points[i - 1].y)
            pointsCon2[i].set((points[i].x + points[i - 1].x) / 2, points[i].y)
        }
        val path = Path()
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until size) {
            path.cubicTo(
                pointsCon1[i].x,
                pointsCon1[i].y,
                pointsCon2[i].x,
                pointsCon2[i].y,
                points[i].x,
                points[i].y
            )
        }
        canvas?.drawPath(path, linePaint)
    }

    private fun getLinearGradient(color: Int): LinearGradient {
        // opacity 66 (40%)
        val gradientBackgroundString = String.format("%06X", (0xFFFFFF and color))
        val gradientBackgroundColorBottom = Color.parseColor("#66$gradientBackgroundString")
        val gradientBackgroundColorTop = Color.parseColor("#00$gradientBackgroundString")

        return LinearGradient(
            0f,
            0f,
            0f,
            textPaint.textSize * 11,
            gradientBackgroundColorTop,
            gradientBackgroundColorBottom,
            Shader.TileMode.CLAMP
        )
    }

    private fun initPaint() {
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = dpToPx(14f, context)
        textPaint.color = textColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        linePaint.style = Paint.Style.STROKE

        monthPaint.style = Paint.Style.FILL
        monthPaint.isDither = true
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
}