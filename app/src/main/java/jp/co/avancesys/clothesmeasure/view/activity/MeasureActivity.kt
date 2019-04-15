package jp.co.avancesys.clothesmeasure.view.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import jp.co.avancesys.clothesmeasure.R
import jp.co.avancesys.clothesmeasure.model.MeasureManager
import jp.co.avancesys.clothesmeasure.model.MeasureState
import jp.co.avancesys.clothesmeasure.model.getCoordinateData
import jp.co.avancesys.clothesmeasure.presenter.PresenterMeasureActivity
import jp.co.avancesys.clothesmeasure.view.widget.LineView
import jp.co.avancesys.clothesmeasure.view.widget.PointerImageView
import kotlinx.android.synthetic.main.activity_measure.*

class MeasureActivity : AppCompatActivity(), PresenterMeasureActivity.ViewCallback, View.OnTouchListener {

    private var mBackgroundHandler: Handler? = null
    private lateinit var mPresenter: PresenterMeasureActivity
    private val mLineList: ArrayList<LineView> = arrayListOf()
    private var mTrackingLine: LineView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measure)

        mPresenter = PresenterMeasureActivity(this, this)

        buttonUndo.setOnClickListener {
            mPresenter.undoQuestion()
        }

        buttonNext.setOnClickListener {
            mPresenter.nextQuestion(crateMeasureLine(pointer1.getCoordinateData(), pointer2.getCoordinateData()))
        }

        pointer1.setOnTouchListener(this)
        pointer1.visibility = View.INVISIBLE
        pointer2.setOnTouchListener(this)
        pointer2.visibility = View.INVISIBLE

        readPicture()
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (view != null) {
                        // x, y位置取得
                        val newDx = event.rawX.toInt()
                        val newDy = event.rawY.toInt()

                        // クリックイベントを記載しないと警告が出る
                        view.performClick()
                        when (view.id) {
                            pointer1.id, pointer2.id -> pointerMove(view, newDx, newDy)
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    // レイアウトパラメータを更新する
                    if (view != null) {
                        when (view.id) {
                            pointer1.id, pointer2.id -> {
                                val marginLayoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
                                marginLayoutParams.setMargins(view.left, view.top, 0, 0)
                                // マージン更新
                                view.layoutParams = marginLayoutParams
                            }
                        }
                    }
                }
            }
        }

        return true
    }

    override fun onReadPicture(bitmap: Bitmap) {
        runOnUiThread {
            imageViewCapture.setImageBitmap(bitmap)

            // 身長入力ダイアログ表示
            mPresenter.executeQuestion(MeasureState.Question.HEIGHT)
        }
    }

    override fun onSetImageViewHeightByPx() {
        mPresenter.setImageViewHeight(imageViewCapture.height.toFloat())
    }

    override fun onVisiblePointer(visibility: Int) {
        runOnUiThread {
            pointer1.visibility = visibility
            pointer2.visibility = visibility
            mTrackingLine?.let { it.visibility = visibility }
        }
    }

    override fun onStartTrackingLine() {
        runOnUiThread {
            if (mTrackingLine != null) {
                layoutRelative.removeView(mTrackingLine)
            }

            drawPointerLine(true)
        }
    }

    override fun onDrawLine() {
        runOnUiThread {
            drawPointerLine(false)
        }
    }

    override fun onUndoLine() {
        runOnUiThread {
            undoLine()
        }
    }

    /**
     * バックグラウンドハンドラーの取得
     */
    private fun getBackgroundHandler(): Handler? {
        if (mBackgroundHandler == null) {
            val thread = HandlerThread("backgroundMeasure")
            thread.start()
            mBackgroundHandler = Handler(thread.looper)
        }

        return mBackgroundHandler
    }

    /**
     * キャプチャ読込
     */
    private fun readPicture() {
        getBackgroundHandler()?.let { handler ->
            handler.post {
                mPresenter.readPicture()
            }
        }
    }

    /**
     * ポインター移動
     */
    private fun pointerMove(view: View, newDx: Int, newDy: Int) {
        // ImageView からはみ出さないようにする
        val location = IntArray(2)
        imageViewCapture.getLocationOnScreen(location)
        val imageViewLeft = location[0]
        val imageViewTop = location[1]
        val imageViewBottom = imageViewTop + imageViewCapture.height
        val imageViewRight = imageViewLeft + imageViewCapture.width

        val fixNewDx = when {
            (newDx < imageViewLeft) -> imageViewLeft
            (newDx > imageViewRight) -> imageViewRight
            else -> newDx
        }
        val fixNewDy = when {
            (newDy < imageViewTop) -> imageViewTop
            (newDy > imageViewBottom) -> imageViewBottom
            else -> newDy
        }

        // 座標移動に関するデータクラスの初期化
        val data = view.tag?.let {
            if (it is PointerImageView.CoordinateData) it else PointerImageView.CoordinateData(fixNewDx, fixNewDy)
        } ?: PointerImageView.CoordinateData(fixNewDx, fixNewDy)

        when (view.id) {
            pointer1.id -> {
                mPresenter.move(pointer1, data, fixNewDx, fixNewDy)

                val p1Coordinate = pointer1.getCoordinateData()
                val p2Coordinate = pointer2.getCoordinateData()
                trackingPointerLine(p1Coordinate, p2Coordinate)
            }
            pointer2.id -> {
                mPresenter.move(pointer2, data, fixNewDx, fixNewDy)

                val p1Coordinate = pointer1.getCoordinateData()
                val p2Coordinate = pointer2.getCoordinateData()
                trackingPointerLine(p1Coordinate, p2Coordinate)
            }
        }
    }

    /**
     * ポインター間に線を引く
     */
    private fun drawPointerLine(isTracking: Boolean): LineView {
        val centerP1X = pointer1.left + (pointer1.width / 2f)
        val centerP1Y = pointer1.top + (pointer1.height / 2f)
        val centerP2X = pointer2.left + (pointer2.width / 2f)
        val centerP2Y = pointer2.top + (pointer2.height / 2f)
        val pts = floatArrayOf(centerP1X, centerP1Y, centerP2X, centerP2Y)

        val lineView = LineView(this, pts, getColor(R.color.colorTeal)).apply {
            isDashLine = isTracking
            if (isTracking) {
                lineColor = getColor(R.color.colorWaterBlue)
            }
        }

        if (isTracking) {
            mTrackingLine = lineView
        } else {
            mLineList.add(lineView)
        }

        layoutRelative.addView(lineView)

        return lineView
    }

    /**
     * ポインターの移動に合わせて線を追従させる
     */
    private fun trackingPointerLine(
        fromCoordinate: PointerImageView.CoordinateData,
        toCoordinate: PointerImageView.CoordinateData
    ) {
        val line = crateMeasureLine(fromCoordinate, toCoordinate)
        mTrackingLine?.drawLine(line.fromX, line.fromY, line.toX, line.toY)
    }

    /**
     * 管理オブジェクトを生成
     */
    private fun crateMeasureLine(
        fromCoordinate: PointerImageView.CoordinateData,
        toCoordinate: PointerImageView.CoordinateData
    ): MeasureManager.MeasureLine {
        // 2つのポインターの大きさは同じとする
        val widthCenter = pointer1.width / 2f
        val heightCenter = pointer1.height / 2f
        val fromX = fromCoordinate.dx + widthCenter
        val fromY = fromCoordinate.dy + heightCenter
        val toX = toCoordinate.dx + widthCenter
        val toY = toCoordinate.dy + heightCenter

        return MeasureManager.MeasureLine(fromX, fromY, toX, toY)
    }


    /**
     * 線をクリア
     */
    private fun clearLines() {
        if (mLineList.isNotEmpty()) {
            mLineList.map { layoutRelative.removeView(it) }
            mLineList.clear()
        }
    }

    /**
     * 末尾の線を消す
     */
    private fun undoLine() {
        if (mLineList.isNotEmpty()) {
            val last = mLineList.last()
            layoutRelative.removeView(last)
            mLineList.removeAt(mLineList.lastIndex)
        }
    }

    companion object {
        private val TAG = "CaptureSubActivity"
        const val PICT_NAME = "picture.jpg"
    }
}
