package jp.co.avancesys.clothesmeasure.view.widget

import android.content.Context
import android.graphics.*
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.Log
import android.view.View

/**
 * ポインターオブジェクト
 */
class PointerImageView : AppCompatImageView {

    /**
     * オブジェクト移動に関するデータクラス
     */
    data class CoordinateData(
        var preDx: Int = 0,
        var preDy: Int = 0,
        var dx: Int = 0,
        var dy: Int = 0
    )

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}


/**
 * ポインター間の直線オブジェクト
 */
class LineView(context: Context?) : View(context) {

    constructor(context: Context?, pts: FloatArray, lineColor: Int) : this(context) {
        mPts = pts
        this.lineColor = lineColor
    }

    private var mPaint = Paint()
    private var mPath = Path()
    private var mPts = floatArrayOf(0f, 0f, 0f, 0f)
    var lineColor = Color.BLUE
    var isDashLine = false


    override fun onDraw(canvas: Canvas?) {
        if (isDashLine) {
            setDashPaint()
        } else {
            setNormalPaint()
        }

        mPath.apply {
            rewind()
            moveTo(mPts[0], mPts[1])
            lineTo(mPts[2], mPts[3])
        }

        // 線を引く
        canvas?.let {
            it.drawPath(mPath, mPaint)
        }
    }

    /**
     * 通常線
     */
    private fun setNormalPaint() {
        mPaint.apply {
            isAntiAlias = true
            color = lineColor
            strokeWidth = 10f
            style = Paint.Style.STROKE
            pathEffect = CornerPathEffect(10f)
        }
    }

    /**
     * 点線
     */
    private fun setDashPaint() {
        mPaint.apply {
            isAntiAlias = true
            color = lineColor
            strokeWidth = 10f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
    }

    /**
     * 指定した2点の座標間に線を引く
     */
    fun drawLine(p1x: Float, p1y: Float, p2x: Float, p2y: Float) {
        mPts = floatArrayOf(p1x, p1y, p2x, p2y)
        invalidate()
    }

    /**
     * 線の長さを取得
     */
    fun getLineLength() : Double {
        val xS = mPts[0]
        val yS = mPts[1]
        val xE = mPts[2]
        val yE = mPts[3]
        val width = Math.abs(xE - xS)
        val height = Math.abs(yE - yS)
        val length = Math.sqrt(Math.pow(width.toDouble(), 2.0) + Math.pow(height.toDouble(), 2.0))
        Log.d(TAG, "getLineLength: w=$width, h=$height, l=$length")

        return length
    }

    companion object {
        private const val TAG = "LineView"
    }
}