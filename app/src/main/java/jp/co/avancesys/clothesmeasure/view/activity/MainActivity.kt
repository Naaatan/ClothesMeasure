package jp.co.avancesys.clothesmeasure.view.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import jp.co.avancesys.clothesmeasure.R
import jp.co.avancesys.clothesmeasure.view.fragment.CameraFragment
import permissions.dispatcher.*

@RuntimePermissions
class MainActivity : AppCompatActivity(), CameraFragment.OnActivityNotify {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            cameraViewWithPermissionCheck()
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun cameraView() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, CameraFragment.newInstance().also { it.notifyActivity = this })
            .commit()
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun showDenied() {
        // 権限を許可されなかったとき
        when {
            PermissionUtils.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "カメラを使用する権限を取得できませんでした", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    fun showRational(request: PermissionRequest) {
        // 再度、権限を要求
        AlertDialog.Builder(this)
            .setMessage("カメラ使用の権限を取得する必要があります")
            .setPositiveButton("許可") { _, _ -> request.proceed() }
            .setNegativeButton("今はしない", null)
            .create()
            .show()
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun showNeverAsk() {
        // 今後表示しないを選択
        AlertDialog.Builder(this)
            .setTitle("カメラを利用できません")
            .setMessage("設定 > 許可から権限を許可して下さい")
            .setPositiveButton("設定画面") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("今はしない", null)
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun moveMeasure() {
        runOnUiThread {
            val intent = Intent(this, MeasureActivity::class.java)
            startActivity(intent)
        }
    }
}
