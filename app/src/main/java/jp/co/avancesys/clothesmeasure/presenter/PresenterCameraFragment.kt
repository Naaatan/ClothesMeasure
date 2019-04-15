package jp.co.avancesys.clothesmeasure.presenter

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.Log
import jp.co.avancesys.clothesmeasure.view.activity.MeasureActivity
import java.io.IOException

class PresenterCameraFragment(
    private val mContext: Context,
    private val mView: PresenterCameraFragment.ViewCallback
) {

    interface ViewCallback

    /**
     * キャプチャーデータ保存
     */
    fun saveCapture(data: ByteArray) {
        try {
            mContext.openFileOutput(MeasureActivity.PICT_NAME, Context.MODE_PRIVATE).use {
                it.write(data)
            }
        } catch (e: IOException) {
            Log.w(TAG, "saveCapture: ", e)
        }
    }

    /**
     * 注意点をダイアログで表示
     */
    fun showCaptionDialog() {
        AlertDialog.Builder(mContext)
            .setTitle("採寸について")
            .setMessage("手を少し広げ、頭から足先まで画面いっぱいに映るように撮影して下さい。")
            .setCancelable(true)
            .create()
            .show()
    }

    companion object {
        private const val TAG = "PresenterCameraFragment"
    }
}