package jp.co.avancesys.clothesmeasure.view.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.Toast
import com.google.android.cameraview.CameraView
import jp.co.avancesys.clothesmeasure.R
import jp.co.avancesys.clothesmeasure.presenter.PresenterCameraFragment
import kotlinx.android.synthetic.main.fragment_camera.*

class CameraFragment : Fragment(), PresenterCameraFragment.ViewCallback, View.OnTouchListener {

    private var mBackgroundHandler: Handler? = null
    private lateinit var mGesture: GestureDetector
    private lateinit var mPresenter: PresenterCameraFragment
    var notifyActivity: CameraFragment.OnActivityNotify? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_camera, container, false)

        mPresenter = PresenterCameraFragment(root.context, this)
        mGesture = GestureDetector(root.context, mGestureSimple)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cameraView.setOnTouchListener(this)
        cameraView.addCallback(mCameraCallback)

        mPresenter.showCaptionDialog()
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        notifyActivity = null
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        return mGesture.onTouchEvent(p1)
    }

    /**
     * ジェスチャーコールバック
     */
    private val mGestureSimple = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            Log.d(TAG, "onDown: GestureSimple")
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            Log.d(TAG, "onSingleTapUp: GestureSimple")
            cameraView.takePicture()
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            return super.onSingleTapConfirmed(e)
        }

        override fun onShowPress(e: MotionEvent?) {
            super.onShowPress(e)
        }

        override fun onLongPress(e: MotionEvent?) {
            super.onLongPress(e)
        }
    }

    /**
     * カメラコールバック
     */
    private val mCameraCallback = object : CameraView.Callback() {
        override fun onPictureTaken(cameraView: CameraView?, data: ByteArray?) {
            Log.d(TAG, "onPictureTaken: dataSize= ${data?.size}byte")

            context?.let {
                Toast.makeText(it, "Capture", Toast.LENGTH_SHORT).show()
            }

            getBackgroundHandler()?.let { handler ->
                handler.post {
                    data?.let { data ->
                        mPresenter.saveCapture(data)
                        notifyActivity?.moveMeasure()
                    }
                }
            }
        }
    }

    /**
     * バックグランドのハンドラーを取得
     */
    fun getBackgroundHandler(): Handler? {
        if (mBackgroundHandler == null) {
            val thread = HandlerThread("backgroundCameraFrag")
            thread.start()
            mBackgroundHandler = Handler(thread.looper)
        }

        return mBackgroundHandler
    }

    companion object {

        private const val TAG = "CameraFragment"

        @JvmStatic
        fun newInstance() = CameraFragment()
    }

    interface OnActivityNotify {
        fun moveMeasure()
    }
}
