package com.youzix.nekoneko

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import com.youzix.nekoneko.ui.FloatingWindowContent
import com.youzix.nekoneko.ui.theme.AppTheme

/**
 * 悬浮窗服务：以 ComposeView 承载 Material You 风格的悬浮窗 UI。
 * Service 无 Activity 生命周期，手动提供 LifecycleOwner / SavedStateRegistryOwner。
 */
class FloatingWindowService : Service(), Logger.LogListener {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null

    private val lifecycleOwner = object : LifecycleOwner {
        private val registry = LifecycleRegistry(this).apply {
            currentState = Lifecycle.State.RESUMED
        }
        override val lifecycle: Lifecycle
            get() = registry
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Logger.i("悬浮窗服务正在启动...")
        Logger.setLogListener(this)

        try {
            createFloatingView()
            Logger.i("悬浮窗服务启动完成")
        } catch (e: Exception) {
            Logger.e("悬浮窗创建失败", e)
            val detail = buildString {
                append(e.javaClass.simpleName).append(": ").append(e.message ?: e.toString())
                e.cause?.let { cause ->
                    append("\n根因: ").append(cause.javaClass.simpleName).append(": ").append(cause.message)
                }
            }
            Toast.makeText(this, "悬浮窗创建失败: $detail", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun createFloatingView() {
        Logger.d("正在创建悬浮窗视图...")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val composeView = ComposeView(this)
        composeView.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView.setContent {
            AppTheme {
                FloatingWindowContent(
                    onClose = { stopSelf() },
                    windowManager = windowManager,
                    view = composeView,
                    params = params ?: error("params not initialized"),
                )
            }
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val p = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        )
        p.gravity = Gravity.TOP or Gravity.START
        p.x = 0
        p.y = 100
        params = p

        windowManager.addView(composeView, p)
        floatingView = composeView

        Logger.d("悬浮窗视图创建完成")
    }

    override fun onLogAdded(logEntry: String) {
        // 日志由 Compose 侧轮询刷新，此处无需处理
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.setLogListener(null)
        floatingView?.let {
            runCatching { windowManager.removeView(it) }
        }
        Logger.w("悬浮窗服务已销毁")
    }
}
