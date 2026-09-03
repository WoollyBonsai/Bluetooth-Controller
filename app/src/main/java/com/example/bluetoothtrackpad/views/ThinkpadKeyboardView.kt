package com.example.bluetoothtrackpad.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.bluetoothtrackpad.KeyboardKey
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ThinkpadKeyboardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface Listener {
        fun onKeyboardReport(modifiers: Byte, keyCodes: ByteArray)
        fun onMouseReport(buttons: Byte, dx: Int, dy: Int, wheel: Int)
    }

    var listener: Listener? = null

    private val bgPaint = Paint().apply { color = Color.parseColor("#222222") }
    private val keyPaint = Paint().apply { color = Color.parseColor("#333333"); style = Paint.Style.FILL }
    private val keyPressedPaint = Paint().apply { color = Color.parseColor("#555555"); style = Paint.Style.FILL }
    private val keyStrokePaint = Paint().apply { color = Color.parseColor("#111111"); style = Paint.Style.STROKE; strokeWidth = 4f }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textAlign = Paint.Align.CENTER; textSize = 32f }
    private val trackPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.RED; style = Paint.Style.FILL }
    private val trackPointStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#880000"); style = Paint.Style.STROKE; strokeWidth = 2f }
    
    private val mouseBtnLeftPaint = Paint().apply { color = Color.parseColor("#d83a3a"); style = Paint.Style.FILL }
    private val mouseBtnMidPaint = Paint().apply { color = Color.parseColor("#2951cc"); style = Paint.Style.FILL }
    private val mouseBtnRightPaint = Paint().apply { color = Color.parseColor("#d83a3a"); style = Paint.Style.FILL }

    private data class DrawnKey(val key: KeyboardKey, val rect: RectF)
    private val drawnKeys = mutableListOf<DrawnKey>()
    
    private val pressedKeyCodes = mutableSetOf<Byte>()
    private var currentModifiers: Byte = 0
    private var currentMouseButtons: Byte = 0

    // Multitouch tracking
    private val pointerToKey = mutableMapOf<Int, KeyboardKey>()

    // Portrait slider state
    private var isPortrait = false
    private var currentScrollX = 0f
    private var maxScrollX = 0f
    private var threeFingerDownX = 0f
    private var initialScrollX = 0f
    private var isThreeFingerSwiping = false

    // TrackPoint Joystick state
    private var trackPointRect = RectF()
    private var isJoysticking = false
    private var joystickPointerId = -1
    private var tpLastX = 0f
    private var tpLastY = 0f
    private var tpDownTime = 0L
    private var tpMoved = false
    private var tpTapCount = 0
    
    // Mouse buttons
    private var leftBtnRect = RectF()
    private var midBtnRect = RectF()
    private var rightBtnRect = RectF()

    private val joystickRunnable = object : Runnable {
        override fun run() {
            if (isJoysticking) {
                val dx = (tpLastX - trackPointRect.centerX()) / 2f
                val dy = (tpLastY - trackPointRect.centerY()) / 2f
                if (abs(dx) > 1 || abs(dy) > 1) {
                    listener?.onMouseReport(currentMouseButtons, dx.toInt(), dy.toInt(), 0)
                }
                postDelayed(this, 30)
            }
        }
    }

    private val landscapeRows = listOf(
        listOf(
            KeyboardKey("Esc", 0x29, widthWeight = 1.2f),
            KeyboardKey("F1", 0x3A), KeyboardKey("F2", 0x3B), KeyboardKey("F3", 0x3C), KeyboardKey("F4", 0x3D),
            KeyboardKey("F5", 0x3E), KeyboardKey("F6", 0x3F), KeyboardKey("F7", 0x40), KeyboardKey("F8", 0x41),
            KeyboardKey("F9", 0x42), KeyboardKey("F10", 0x43), KeyboardKey("F11", 0x44), KeyboardKey("F12", 0x45),
            KeyboardKey("PrtSc", 0x46), KeyboardKey("Del", 0x4C, widthWeight = 1.2f)
        ),
        listOf(
            KeyboardKey("`", 0x35), KeyboardKey("1", 0x1E), KeyboardKey("2", 0x1F), KeyboardKey("3", 0x20),
            KeyboardKey("4", 0x21), KeyboardKey("5", 0x22), KeyboardKey("6", 0x23), KeyboardKey("7", 0x24),
            KeyboardKey("8", 0x25), KeyboardKey("9", 0x26), KeyboardKey("0", 0x27), KeyboardKey("-", 0x2D),
            KeyboardKey("=", 0x2E), KeyboardKey("Backspace", 0x2A, widthWeight = 2f),
            // Numpad Right Side
            KeyboardKey("Num", 0x53), KeyboardKey("/", 0x54), KeyboardKey("*", 0x55), KeyboardKey("-", 0x56)
        ),
        listOf(
            KeyboardKey("Tab", 0x2B, widthWeight = 1.5f),
            KeyboardKey("Q", 0x14), KeyboardKey("W", 0x1A), KeyboardKey("E", 0x08), KeyboardKey("R", 0x15),
            KeyboardKey("T", 0x17), KeyboardKey("Y", 0x1C), KeyboardKey("U", 0x18), KeyboardKey("I", 0x0C),
            KeyboardKey("O", 0x12), KeyboardKey("P", 0x13), KeyboardKey("[", 0x2F), KeyboardKey("]", 0x30),
            KeyboardKey("\\", 0x31, widthWeight = 1.5f),
            // Numpad Right Side
            KeyboardKey("7", 0x5F), KeyboardKey("8", 0x60), KeyboardKey("9", 0x61), KeyboardKey("+", 0x57)
        ),
        listOf(
            KeyboardKey("Caps", 0x39, widthWeight = 1.8f),
            KeyboardKey("A", 0x04), KeyboardKey("S", 0x16), KeyboardKey("D", 0x07), KeyboardKey("F", 0x09),
            KeyboardKey("G", 0x0A), KeyboardKey("H", 0x0B), KeyboardKey("J", 0x0D), KeyboardKey("K", 0x0E),
            KeyboardKey("L", 0x0F), KeyboardKey(";", 0x33), KeyboardKey("'", 0x34),
            KeyboardKey("Enter", 0x28, widthWeight = 2.2f),
            // Numpad Right Side
            KeyboardKey("4", 0x5C), KeyboardKey("5", 0x5D), KeyboardKey("6", 0x5E), KeyboardKey(".", 0x63)
        ),
        listOf(
            KeyboardKey("Shift", 0x00, isModifier = true, modifierMask = 0x02, widthWeight = 2.3f),
            KeyboardKey("Z", 0x1D), KeyboardKey("X", 0x1B), KeyboardKey("C", 0x06), KeyboardKey("V", 0x19),
            KeyboardKey("B", 0x05), KeyboardKey("N", 0x11), KeyboardKey("M", 0x10), KeyboardKey(",", 0x36),
            KeyboardKey(".", 0x37), KeyboardKey("/", 0x38),
            KeyboardKey("Shift", 0x00, isModifier = true, modifierMask = 0x20, widthWeight = 2.7f),
            // Numpad Right Side
            KeyboardKey("1", 0x59), KeyboardKey("2", 0x5A), KeyboardKey("3", 0x5B), KeyboardKey("Ent", 0x58)
        ),
        listOf(
            KeyboardKey("Ctrl", 0x00, isModifier = true, modifierMask = 0x01, widthWeight = 1.5f),
            KeyboardKey("Win", 0x00, isModifier = true, modifierMask = 0x08, widthWeight = 1.2f),
            KeyboardKey("Alt", 0x00, isModifier = true, modifierMask = 0x04, widthWeight = 1.2f),
            KeyboardKey("Space", 0x2C, widthWeight = 6f),
            KeyboardKey("Alt", 0x00, isModifier = true, modifierMask = 0x40, widthWeight = 1.2f),
            KeyboardKey("Ctrl", 0x00, isModifier = true, modifierMask = 0x10, widthWeight = 1.5f),
            KeyboardKey("<-", 0x50, widthWeight = 1f),
            KeyboardKey("v", 0x51, widthWeight = 1f),
            KeyboardKey("^", 0x52, widthWeight = 1f),
            KeyboardKey("->", 0x4F, widthWeight = 1f),
            // Numpad Right Side
            KeyboardKey("0", 0x62, widthWeight = 3f)
        )
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        isPortrait = h > w
        buildLayout()
    }

    private fun buildLayout() {
        drawnKeys.clear()
        if (width == 0 || height == 0) return

        if (isPortrait) {
            val kbHeight = height / 2f
            val startY = height / 2f
            val rowHeight = kbHeight / landscapeRows.size
            
            // In portrait, make the layout twice as wide, so user can 3-finger swipe to see Numpad and Dpad
            val layoutWidth = width * 2f
            maxScrollX = layoutWidth - width
            
            for ((rowIndex, row) in landscapeRows.withIndex()) {
                val totalWeight = row.sumOf { it.widthWeight.toDouble() }.toFloat()
                val unitWidth = layoutWidth / totalWeight
                var currentX = 0f
                
                for (key in row) {
                    val keyWidth = unitWidth * key.widthWeight
                    val rect = RectF(currentX, startY + rowIndex * rowHeight, currentX + keyWidth, startY + (rowIndex + 1) * rowHeight)
                    drawnKeys.add(DrawnKey(key, rect))
                    currentX += keyWidth
                }
            }
            trackPointRect.setEmpty()
            leftBtnRect.setEmpty()
            midBtnRect.setEmpty()
            rightBtnRect.setEmpty()
        } else {
            val rowHeight = (height * 0.8f) / landscapeRows.size
            maxScrollX = 0f
            currentScrollX = 0f
            
            for ((rowIndex, row) in landscapeRows.withIndex()) {
                val totalWeight = row.sumOf { it.widthWeight.toDouble() }.toFloat()
                val unitWidth = width / totalWeight
                var currentX = 0f
                
                for (key in row) {
                    val keyWidth = unitWidth * key.widthWeight
                    val rect = RectF(currentX, rowIndex * rowHeight, currentX + keyWidth, (rowIndex + 1) * rowHeight)
                    drawnKeys.add(DrawnKey(key, rect))
                    currentX += keyWidth
                }
            }
            
            val mouseBtnY = landscapeRows.size * rowHeight
            val btnWidth = width / 3f
            leftBtnRect = RectF(0f, mouseBtnY, btnWidth, height.toFloat())
            midBtnRect = RectF(btnWidth, mouseBtnY, btnWidth * 2, height.toFloat())
            rightBtnRect = RectF(btnWidth * 2, mouseBtnY, width.toFloat(), height.toFloat())

            val gKey = drawnKeys.find { it.key.label == "G" }?.rect
            val hKey = drawnKeys.find { it.key.label == "H" }?.rect
            val bKey = drawnKeys.find { it.key.label == "B" }?.rect

            if (gKey != null && hKey != null && bKey != null) {
                val cx = gKey.right
                val cy = gKey.bottom
                val radius = rowHeight * 0.4f
                trackPointRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        if (isPortrait) {
            canvas.drawRect(0f, 0f, width.toFloat(), height / 2f, Paint().apply { color = Color.parseColor("#1a1a1a") })
            canvas.drawText("Trackpad Area", width / 2f, height / 4f, textPaint)
            // Note: actual trackpad events for top half are routed to the parent/MainActivity
        }

        canvas.save()
        canvas.translate(-currentScrollX, 0f)

        for (dk in drawnKeys) {
            val isPressed = pressedKeyCodes.contains(dk.key.hidCode) || (dk.key.isModifier && (currentModifiers.toInt() and dk.key.modifierMask.toInt()) != 0)
            
            val r = dk.rect
            val inset = RectF(r.left + 4, r.top + 4, r.right - 4, r.bottom - 4)
            canvas.drawRoundRect(inset, 8f, 8f, if (isPressed) keyPressedPaint else keyPaint)
            canvas.drawRoundRect(inset, 8f, 8f, keyStrokePaint)
            
            val textY = inset.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(dk.key.label, inset.centerX(), textY, textPaint)
        }

        if (!isPortrait) {
            canvas.drawRoundRect(RectF(leftBtnRect.left + 4, leftBtnRect.top + 4, leftBtnRect.right - 4, leftBtnRect.bottom - 4), 16f, 16f, mouseBtnLeftPaint)
            canvas.drawRoundRect(RectF(midBtnRect.left + 4, midBtnRect.top + 4, midBtnRect.right - 4, midBtnRect.bottom - 4), 16f, 16f, mouseBtnMidPaint)
            canvas.drawRoundRect(RectF(rightBtnRect.left + 4, rightBtnRect.top + 4, rightBtnRect.right - 4, rightBtnRect.bottom - 4), 16f, 16f, mouseBtnRightPaint)
            
            if (!trackPointRect.isEmpty) {
                canvas.drawCircle(trackPointRect.centerX(), trackPointRect.centerY(), trackPointRect.width() / 2, trackPointPaint)
                canvas.drawCircle(trackPointRect.centerX(), trackPointRect.centerY(), trackPointRect.width() / 2, trackPointStroke)
            }
        }
        canvas.restore()
        
        // Scrollbar indicator
        if (isPortrait && maxScrollX > 0) {
            val indicatorWidth = width * (width / (width + maxScrollX))
            val indicatorX = (currentScrollX / maxScrollX) * (width - indicatorWidth)
            canvas.drawRect(indicatorX, height - 10f, indicatorX + indicatorWidth, height.toFloat(), trackPointPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        val rawX = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)
        val x = rawX + currentScrollX

        // 3-finger swipe handling for Portrait
        if (isPortrait && event.pointerCount == 3) {
            when (action) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (event.pointerCount == 3) {
                        isThreeFingerSwiping = true
                        threeFingerDownX = event.getX(0)
                        initialScrollX = currentScrollX
                        
                        // Release all keys
                        pressedKeyCodes.clear()
                        currentModifiers = 0
                        sendReport()
                        invalidate()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isThreeFingerSwiping) {
                        val dx = event.getX(0) - threeFingerDownX
                        currentScrollX = max(0f, min(maxScrollX, initialScrollX - dx))
                        invalidate()
                        return true
                    }
                }
                MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (event.pointerCount <= 3) {
                        isThreeFingerSwiping = false
                    }
                }
            }
            if (isThreeFingerSwiping) return true
        }

        if (isPortrait && y < height / 2f) {
            // Forward to MainActivity's trackpad by returning false? 
            // Better to route it manually if we were in the same class, but since we are a custom View,
            // we can just not consume it. However, multi-touch might get confused.
            return false
        }

        if (!isPortrait && (trackPointRect.contains(x, y) || pointerId == joystickPointerId)) {
            when (action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    if (trackPointRect.contains(x, y)) {
                        joystickPointerId = pointerId
                        isJoysticking = true
                        tpLastX = x
                        tpLastY = y
                        tpDownTime = System.currentTimeMillis()
                        tpMoved = false
                        post(joystickRunnable)
                        return true
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (pointerId == joystickPointerId) {
                        tpLastX = x
                        tpLastY = y
                        if (abs(x - trackPointRect.centerX()) > 15 || abs(y - trackPointRect.centerY()) > 15) {
                            tpMoved = true
                        }
                        return true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                    if (pointerId == joystickPointerId) {
                        isJoysticking = false
                        removeCallbacks(joystickRunnable)
                        joystickPointerId = -1
                        
                        val upTime = System.currentTimeMillis()
                        if (!tpMoved && (upTime - tpDownTime) < 250) {
                            tpTapCount++
                            if (tpTapCount == 1) {
                                postDelayed({
                                    if (tpTapCount == 1) {
                                        listener?.onMouseReport(1, 0, 0, 0)
                                        postDelayed({ listener?.onMouseReport(0, 0, 0, 0) }, 30)
                                    }
                                    tpTapCount = 0
                                }, 250)
                            } else if (tpTapCount == 2) {
                                listener?.onMouseReport(2, 0, 0, 0)
                                postDelayed({ listener?.onMouseReport(0, 0, 0, 0) }, 30)
                                tpTapCount = 0
                            }
                        }
                        return true
                    }
                }
            }
        }

        if (!isPortrait && y > leftBtnRect.top) {
            val mask: Byte = when {
                leftBtnRect.contains(rawX, y) -> 1
                midBtnRect.contains(rawX, y) -> 4
                rightBtnRect.contains(rawX, y) -> 2
                else -> 0
            }
            
            when (action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    if (mask > 0) {
                        currentMouseButtons = (currentMouseButtons.toInt() or mask.toInt()).toByte()
                        listener?.onMouseReport(currentMouseButtons, 0, 0, 0)
                        return true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                    currentMouseButtons = 0
                    listener?.onMouseReport(0, 0, 0, 0)
                    return true
                }
            }
        }

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val key = drawnKeys.find { it.rect.contains(x, y) }?.key
                if (key != null) {
                    pointerToKey[pointerId] = key
                    if (key.isModifier) {
                        currentModifiers = (currentModifiers.toInt() or key.modifierMask.toInt()).toByte()
                    } else {
                        pressedKeyCodes.add(key.hidCode)
                    }
                    sendReport()
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                val key = pointerToKey.remove(pointerId)
                if (key != null) {
                    if (key.isModifier) {
                        currentModifiers = (currentModifiers.toInt() and key.modifierMask.toInt().inv()).toByte()
                    } else {
                        pressedKeyCodes.remove(key.hidCode)
                    }
                    sendReport()
                    invalidate()
                }
            }
        }
        
        return true
    }

    private fun sendReport() {
        val codes = ByteArray(6)
        var i = 0
        for (code in pressedKeyCodes) {
            if (i >= 6) break
            codes[i++] = code
        }
        listener?.onKeyboardReport(currentModifiers, codes)
    }
}
