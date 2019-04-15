package jp.co.avancesys.clothesmeasure.presenter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.view.View
import jp.co.avancesys.clothesmeasure.model.MeasureManager
import jp.co.avancesys.clothesmeasure.model.MeasureState
import jp.co.avancesys.clothesmeasure.view.activity.MeasureActivity
import jp.co.avancesys.clothesmeasure.view.widget.PointerImageView
import java.io.ByteArrayOutputStream
import java.io.IOException

class PresenterMeasureActivity(
    private val mContext: Context,
    private val mView: PresenterMeasureActivity.ViewCallback
) {

    private val mMeasureManager: MeasureManager = MeasureManager()

    /**
     * キャプチャ画像の読込(ローカルエリア)
     */
    fun readPicture() {
        val buffer = ByteArray(1024)

        try {
            mContext.openFileInput(MeasureActivity.PICT_NAME).use { fis ->
                ByteArrayOutputStream().use { baos ->
                    while (fis.read(buffer) > 0) {
                        baos.write(buffer)
                    }

                    val pictBytes = baos.toByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(pictBytes, 0, pictBytes.size)
                    val fixBitmap = fixPictOrientation(bitmap)

                    Log.d(TAG, "readPicture: w= ${bitmap.width}px, h= ${bitmap.height}px")
                    Log.d(TAG, "readPicture: w(fix)= ${fixBitmap.width}px, h(fix)= ${fixBitmap.height}px")

                    // 通知
                    mView.onReadPicture(fixBitmap)
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "readPicture: ", e)
        }
    }

    /**
     * プレビューの画像の向きを調整
     * (横 > 高さ である場合、表示の向きが撮影時の向きと異なるため)
     */
    private fun fixPictOrientation(bitmap: Bitmap): Bitmap {
        return if (bitmap.width > bitmap.height) {
            val matrix = Matrix().apply {
                postRotate(90f)
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    /**
     * 現在の質問シーケンスと指定した質問が正しい場合、処理を実行
     */
    fun executeQuestion(question: MeasureState.Question) {
        if (mMeasureManager.isNowQuestion(question)) {
            mMeasureManager.executeQuestion(mContext)
        }
    }

    /**
     * 前の質問シーケンスに移行
     */
    fun undoQuestion() {
        // 前のシーケンスに移行する際、View側で必要な処理をする
        when {
            mMeasureManager.isNowQuestion(MeasureState.Question.LENGTH) -> {
                mView.onVisiblePointer(View.INVISIBLE)
            }
            mMeasureManager.isNowQuestion(MeasureState.Question.END) -> {
                mView.onVisiblePointer(View.VISIBLE)
            }
        }

        mView.onUndoLine()
        mMeasureManager.undoQuestion(mContext)
    }

    /**
     * 次の質問シーケンスに移行
     */
    fun nextQuestion(lineData: MeasureManager.MeasureLine?) {
        // 次のシーケンスに移行する際、View側で必要な処理をする
        when {
            mMeasureManager.isNowQuestion(MeasureState.Question.HEIGHT) -> {
                mView.onVisiblePointer(View.VISIBLE)
                mView.onStartTrackingLine()
                mView.onSetImageViewHeightByPx()
            }
            mMeasureManager.isNowQuestion(MeasureState.Question.SLEEVE_LENGTH) -> {
                mView.onDrawLine()
                mView.onVisiblePointer(View.INVISIBLE)
            }
            mMeasureManager.isNowQuestion(MeasureState.Question.END) -> {
            }
            else -> {
                mView.onDrawLine()
            }
        }

        mMeasureManager.nextQuestion(mContext, lineData)
    }

    /**
     * ポインターオブジェクトの位置を決定する処理
     */
    fun move(pointer: PointerImageView, data: PointerImageView.CoordinateData, newDx: Int, newDy: Int) {
        data.dx = pointer.left + (newDx - data.preDx)
        data.dy = pointer.top + (newDy - data.preDy)
        val imgW = data.dx + pointer.width
        val imgH = data.dy + pointer.height

        // 画像の位置を設定
        pointer.layout(data.dx, data.dy, imgW, imgH)
        //Log.d(TAG, "pointMove: dx=${data.dx}, dy=${data.dy}")

        // タッチした位置を古い位置とする
        data.preDx = newDx
        data.preDy = newDy

        // Viewのタグにデータクラスを格納
        pointer.tag = data
    }

    /**
     * イメージビューの高さをマネージャーに設定
     *
     * @height px
     */
    fun setImageViewHeight(height: Float) {
        mMeasureManager.pxHeight = height
    }


    companion object {
        private val TAG = PresenterMeasureActivity::class.java.simpleName
    }

    interface ViewCallback {
        fun onReadPicture(bitmap: Bitmap)
        fun onSetImageViewHeightByPx()
        fun onVisiblePointer(visibility: Int)
        fun onStartTrackingLine()
        fun onDrawLine()
        fun onUndoLine()
    }

}