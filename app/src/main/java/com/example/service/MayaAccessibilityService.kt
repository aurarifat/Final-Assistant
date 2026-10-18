package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MayaAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceBound.value = true
        activeInstance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString()
        if (!pkg.isNullOrBlank() && !pkg.contains("com.example") && !pkg.contains("aistudio")) {
            _foregroundApp.value = pkg
        }
    }

    override fun onInterrupt() {
        _isServiceBound.value = false
        if (activeInstance == this) {
            activeInstance = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceBound.value = false
        if (activeInstance == this) {
            activeInstance = null
        }
    }

    fun triggerTap(x: Float, y: Float, durationMs: Long = 100, onComplete: ((Boolean) -> Unit)? = null) {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onComplete?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                onComplete?.invoke(false)
            }
        }, null)
    }

    fun triggerSwipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300, onComplete: ((Boolean) -> Unit)? = null) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onComplete?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                onComplete?.invoke(false)
            }
        }, null)
    }

    fun triggerClickByText(label: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(label)
        if (!nodes.isNullOrEmpty()) {
            for (node in nodes) {
                if (node.isClickable) {
                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                    parent = parent.parent
                }
            }
        }
        return false
    }

    fun scanVisibleElements(): List<String> {
        val results = mutableListOf<String>()
        val root = rootInActiveWindow ?: return listOf("Root window unavailable")
        fun traverse(node: AccessibilityNodeInfo?) {
            node ?: return
            val text = node.text?.toString()
            val desc = node.contentDescription?.toString()
            val viewId = node.viewIdResourceName
            val isClickable = node.isClickable

            if (!text.isNullOrBlank() || !desc.isNullOrBlank()) {
                val label = text ?: desc
                val bounds = Rect()
                node.getBoundsInScreen(bounds)
                val item = buildString {
                    append(label)
                    if (isClickable) append(" [Clickable]")
                    if (!viewId.isNullOrBlank()) append(" ($viewId)")
                    append(" at (${bounds.centerX()}, ${bounds.centerY()})")
                }
                results.add(item)
            }
            for (i in 0 until node.childCount) {
                traverse(node.getChild(i))
            }
        }
        traverse(root)
        return results.take(30)
    }

    companion object {
        private val _isServiceBound = MutableStateFlow(false)
        val isServiceBound: StateFlow<Boolean> = _isServiceBound.asStateFlow()

        private val _foregroundApp = MutableStateFlow("Home / Launcher")
        val foregroundApp: StateFlow<String> = _foregroundApp.asStateFlow()

        var activeInstance: MayaAccessibilityService? = null
            private set
    }
}
