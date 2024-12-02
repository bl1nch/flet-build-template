package {{ cookiecutter.org_name }}.{{ cookiecutter.project_name }}

import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File
import android.os.Build
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider

class MainActivity: FlutterActivity() {
    private val fletPlatformChannel = "fletPlatformChannel"
    private lateinit var updateTarget: File
    private lateinit var updateDir: File
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, fletPlatformChannel).setMethodCallHandler {
            call: MethodCall, result: Result ->
            when (call.method) {
                "getUpdateTarget" -> {
                    val response = getUpdateTarget()
                    result.success(response)
                }
                "cleanupUpdateTarget" -> {
                    cleanupUpdateTarget()
                    result.success(null)
                }
                "installUpdateTarget" -> {
                    installUpdateTarget()
                    result.success(null)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateDir = getExternalFilesDir(null) ?: return
        updateTarget = File(updateDir, "app.apk")
    }
    private fun getUpdateTarget(): String {
        return updateTarget.toString()
    }
    private fun cleanupUpdateTarget() {
        if (updateTarget.exists()) {
            updateTarget.delete()
        }
    }
    private fun installUpdateTarget() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                data = FileProvider.getUriForFile(
                    this@MainActivity,
                    "${packageName}.fileprovider",
                    updateTarget
                )
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
        } else {
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.fromFile(updateTarget), "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        startActivity(intent)
    }
}
