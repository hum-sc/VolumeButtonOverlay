package com.humsc.actionbarservice

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.humsc.actionbarservice.ui.theme.ActionBarServiceTheme

class ActionBarService: AccessibilityService(), LifecycleOwner, SavedStateRegistryOwner {
    private final val TAG = "AccessKeyDetector"
    lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val _lifecycleRegistry = LifecycleRegistry(this)
    private val _savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry =
        _savedStateRegistryController.savedStateRegistry
    override val lifecycle: LifecycleRegistry = _lifecycleRegistry

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        _savedStateRegistryController.performAttach()
        _savedStateRegistryController.performRestore(null)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onServiceConnected() {
        Log.i(TAG, "Service connected")
        showOverlay()

        super.onServiceConnected()
    }

    override fun onDestroy() {
        hideOverlay()
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        Log.i("ACCESEVENT", p0.toString())
    }

    override fun onInterrupt() {
        Log.i("INTERRUPT", "INTERRUPTED")
    }

    private fun showOverlay() {
        if (overlayView != null) return

        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@ActionBarService)
            setViewTreeSavedStateRegistryOwner(this@ActionBarService)
            setContent {
                ActionBarComponent()
            }
        }
        windowManager.addView(overlayView, getLayoutParams())

    }

    private fun hideOverlay() {
        if (overlayView == null) return
        windowManager.removeView(overlayView)
        overlayView = null
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

    }

    fun showAudioBar() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        // Muestra la barra de audio
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
    }

    private fun getLayoutParams(): WindowManager.LayoutParams? {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            // Right and half bottom

            gravity = Gravity.END
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {

        val keyCode = event?.keyCode;
        Log.i("KEY PRESSED", keyCode.toString());
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_POWER -> return true;
        }

        return super.onKeyEvent(event)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ActionBarComponent() {
        //ActionBarServiceTheme {
        // On left-right swipe, show/hide the audio bar
        Card(
            modifier = Modifier
                .size(7.dp, 64.dp)
                ,
            colors = CardDefaults.cardColors(
                // Transparency of the card
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            ),
            //No corner radius in left side
            shape = RoundedCornerShape(
                topEnd = 0.dp,
                bottomEnd = 0.dp,
                topStart = 15.dp,
                bottomStart = 15.dp
            ),
            onClick = {
                showAudioBar()
            },
            //Borde traslucido
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))

        ) {

        }
        //}
    }


}



