package com.pandey.royalcoupon

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.atomic.AtomicBoolean

class CouponAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: CouponAccessibilityService? = null
        private val running = AtomicBoolean(false)
        private val paused = AtomicBoolean(false)

        private var codes: List<String> = emptyList()
        private var delayMs: Long = 3000L
        private var index = 0

        var onProgress: ((Int, Int, String) -> Unit)? = null

        fun startAutomation(newCodes: List<String>, newDelayMs: Long) {
            codes = newCodes.take(200)
            delayMs = newDelayMs
            index = 0
            paused.set(false)
            running.set(true)
            instance?.processCurrent()
        }

        fun pauseAutomation() {
            paused.set(true)
            onProgress?.invoke(index, codes.size, "Paused")
        }

        fun stopAutomation() {
            running.set(false)
            paused.set(false)
            onProgress?.invoke(index, codes.size, "Stopped")
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var lastActionAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        onProgress?.invoke(index, codes.size, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!running.get() || paused.get()) return
        if (event == null) return

        // Process only when the target UI is visible.
        handler.postDelayed({ processCurrent() }, 250)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        instance = null
        running.set(false)
        super.onDestroy()
    }

    private fun processCurrent() {
        if (!running.get() || paused.get()) return
        if (index >= codes.size) {
            running.set(false)
            onProgress?.invoke(codes.size, codes.size, "Completed")
            return
        }

        val root = rootInActiveWindow ?: run {
            onProgress?.invoke(index, codes.size, "Waiting for target screen")
            return
        }

        // Look for the visible coupon input field.
        val input = findEditableNode(root)
        if (input == null) {
            onProgress?.invoke(index, codes.size, "Open Redeem Coupon screen")
            return
        }

        // Safety: don't overwrite a field that already contains text.
        val existing = input.text?.toString()?.trim().orEmpty()
        if (existing.isEmpty()) {
            val ok = input.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                android.os.Bundle().apply {
                    putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        codes[index]
                    )
                }
            )
            if (!ok) {
                onProgress?.invoke(index, codes.size, "Could not enter code")
                return
            }

            onProgress?.invoke(index + 1, codes.size, "Code entered; waiting for OK")
            return
        }

        // Only click a clearly labeled OK button.
        val okButton = findButtonByText(root, listOf("OK", "Ok", "ok"))
        if (okButton == null) {
            onProgress?.invoke(index + 1, codes.size, "Waiting for OK button")
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastActionAt < 700) return
        lastActionAt = now

        if (!okButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            onProgress?.invoke(index + 1, codes.size, "Could not click OK")
            return
        }

        onProgress?.invoke(index + 1, codes.size, "Submitted; waiting")
        handler.postDelayed({
            if (!running.get() || paused.get()) return@postDelayed
            index++
            processCurrent()
        }, delayMs)
    }

    private fun findEditableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            val cls = node.className?.toString().orEmpty()
            if (node.isVisibleToUser &&
                (node.isEditable || cls.contains("EditText", ignoreCase = true))) {
                return node
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let(queue::add)
            }
        }
        return null
    }

    private fun findButtonByText(
        root: AccessibilityNodeInfo,
        labels: List<String>
    ): AccessibilityNodeInfo? {
        for (label in labels) {
            val matches = root.findAccessibilityNodeInfosByText(label)
            for (node in matches) {
                if (node.isVisibleToUser &&
                    (node.isClickable || node.className?.toString()?.contains("Button", true) == true)) {
                    return node
                }
            }
        }
        return null
    }
}
