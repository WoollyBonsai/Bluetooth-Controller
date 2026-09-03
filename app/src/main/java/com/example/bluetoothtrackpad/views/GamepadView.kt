package com.example.bluetoothtrackpad.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class GamepadView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface Listener {
        fun onGamepadReport(buttons: Short, dpad: Byte, lx: Byte, ly: Byte, rx: Byte, ry: Byte)
    }

    var listener: Listener? = null

    // Layout config
    private var isPortrait = false

    // Button states
    private var buttonsMask: Short = 0
    private var currentDpad: Byte = 0
    
    // Joystick states (0-255, center 128)
    private var leftStickX: Byte = 128.toByte()
    private var leftStickY: Byte = 128.toByte()
    private var rightStickX: Byte = 128.toByte()
    private var rightStickY: Byte = 128.toByte()

    // Touch pointers mapping
    private val activePointers = mutableMapOf<Int, String>()

    // Paints
    private val bgPaint = Paint().apply { color = Color.parseColor("#1a1a1a") }
    private val btnUpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#333333"); style = Paint.Style.FILL }
    private val btnDownPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#555555"); style = Paint.Style.FILL }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textAlign = Paint.Align.CENTER; textSize = 40f; typeface = Typeface.DEFAULT_BOLD }
    
    private val stickBasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#222222"); style = Paint.Style.FILL }
    private val stickThumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#666666"); style = Paint.Style.FILL }

    private val colorA = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#34A853") } // Green
    private val colorB = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#EA4335") } // Red
    private val colorX = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#4285F4") } // Blue
    private val colorY = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FBBC05") } // Yellow

    // Rects & Centers
    private var leftStickCenter = PointF()
    private var leftStickCurrent = PointF()
    private var stickRadius = 0f
    private var thumbRadius = 0f

    private var rightStickCenter = PointF()
    private var rightStickCurrent = PointF()

    private var dpadCenter = PointF()
    private var dpadRadius = 0f

    private val btns = mutableMapOf<String, RectF>()

    // IDs for buttons (1 to 16)
    // 1: A, 2: B, 3: X, 4: Y
    // 5: LB, 6: RB, 7: LT, 8: RT
    // 9: Select, 10: Start, 11: L3, 12: R3, 13: W Logo
    private val btnMasks = mapOf(
        "A" to 1, "B" to 2, "X" to 4, "Y" to 8,
        "LB" to 16, "RB" to 32, "LT" to 64, "RT" to 128,
        "Select" to 256, "Start" to 512, "L3" to 1024, "R3" to 2048,
        "W" to 4096
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        isPortrait = h > w
        buildLayout(w.toFloat(), h.toFloat())
    }

    private fun buildLayout(w: Float, h: Float) {
        btns.clear()
        val unit = min(w, h) / 10f
        
        if (!isPortrait) {
            // LANDSCAPE LAYOUT
            stickRadius = unit * 1.5f
            thumbRadius = stickRadius * 0.6f
            
            // Left Stick
            leftStickCenter.set(w * 0.2f, h * 0.4f)
            leftStickCurrent.set(leftStickCenter)
            
            // D-Pad (Below Left Stick)
            dpadCenter.set(w * 0.35f, h * 0.7f)
            dpadRadius = unit * 1.2f
            
            // Right Stick (Below ABXY)
            rightStickCenter.set(w * 0.65f, h * 0.7f)
            rightStickCurrent.set(rightStickCenter)
            
            // ABXY
            val abxyCenter = PointF(w * 0.8f, h * 0.4f)
            val btnR = unit * 0.6f
            val spacing = unit * 1.4f
            btns["Y"] = RectF(abxyCenter.x - btnR, abxyCenter.y - spacing - btnR, abxyCenter.x + btnR, abxyCenter.y - spacing + btnR)
            btns["A"] = RectF(abxyCenter.x - btnR, abxyCenter.y + spacing - btnR, abxyCenter.x + btnR, abxyCenter.y + spacing + btnR)
            btns["X"] = RectF(abxyCenter.x - spacing - btnR, abxyCenter.y - btnR, abxyCenter.x - spacing + btnR, abxyCenter.y + btnR)
            btns["B"] = RectF(abxyCenter.x + spacing - btnR, abxyCenter.y - btnR, abxyCenter.x + spacing + btnR, abxyCenter.y + btnR)

            // Bumpers & Triggers
            val trW = unit * 2f
            val trH = unit * 0.8f
            btns["LT"] = RectF(w * 0.1f, h * 0.05f, w * 0.1f + trW, h * 0.05f + trH)
            btns["LB"] = RectF(w * 0.1f, h * 0.15f, w * 0.1f + trW, h * 0.15f + trH)
            
            btns["RT"] = RectF(w * 0.9f - trW, h * 0.05f, w * 0.9f, h * 0.05f + trH)
            btns["RB"] = RectF(w * 0.9f - trW, h * 0.15f, w * 0.9f, h * 0.15f + trH)

            // Center Buttons
            val cw = w * 0.5f
            btns["W"] = RectF(cw - btnR, h * 0.15f - btnR, cw + btnR, h * 0.15f + btnR)
            btns["Select"] = RectF(cw - unit * 2f, h * 0.4f - btnR*0.5f, cw - unit, h * 0.4f + btnR*0.5f)
            btns["Start"] = RectF(cw + unit, h * 0.4f - btnR*0.5f, cw + unit * 2f, h * 0.4f + btnR*0.5f)
        } else {
            // PORTRAIT LAYOUT
            stickRadius = unit * 1.5f
            thumbRadius = stickRadius * 0.6f
            
            // Left Stick
            leftStickCenter.set(w * 0.25f, h * 0.6f)
            leftStickCurrent.set(leftStickCenter)
            
            // D-Pad
            dpadCenter.set(w * 0.25f, h * 0.85f)
            dpadRadius = unit * 1.2f
            
            // Right Stick
            rightStickCenter.set(w * 0.75f, h * 0.85f)
            rightStickCurrent.set(rightStickCenter)
            
            // ABXY
            val abxyCenter = PointF(w * 0.75f, h * 0.6f)
            val btnR = unit * 0.6f
            val spacing = unit * 1.4f
            btns["Y"] = RectF(abxyCenter.x - btnR, abxyCenter.y - spacing - btnR, abxyCenter.x + btnR, abxyCenter.y - spacing + btnR)
            btns["A"] = RectF(abxyCenter.x - btnR, abxyCenter.y + spacing - btnR, abxyCenter.x + btnR, abxyCenter.y + spacing + btnR)
            btns["X"] = RectF(abxyCenter.x - spacing - btnR, abxyCenter.y - btnR, abxyCenter.x - spacing + btnR, abxyCenter.y + btnR)
            btns["B"] = RectF(abxyCenter.x + spacing - btnR, abxyCenter.y - btnR, abxyCenter.x + spacing + btnR, abxyCenter.y + btnR)

            // Bumpers & Triggers
            val trW = w * 0.4f
            val trH = unit * 1.5f
            btns["LT"] = RectF(w * 0.05f, h * 0.05f, w * 0.05f + trW, h * 0.05f + trH)
            btns["LB"] = RectF(w * 0.05f, h * 0.05f + trH + 20f, w * 0.05f + trW, h * 0.05f + trH * 2 + 20f)
            
            btns["RT"] = RectF(w * 0.95f - trW, h * 0.05f, w * 0.95f, h * 0.05f + trH)
            btns["RB"] = RectF(w * 0.95f - trW, h * 0.05f + trH + 20f, w * 0.95f, h * 0.05f + trH * 2 + 20f)

            // Center Buttons
            val cw = w * 0.5f
            btns["W"] = RectF(cw - btnR, h * 0.35f - btnR, cw + btnR, h * 0.35f + btnR)
            btns["Select"] = RectF(cw - unit * 2.5f, h * 0.45f - btnR*0.5f, cw - unit * 0.5f, h * 0.45f + btnR*0.5f)
            btns["Start"] = RectF(cw + unit * 0.5f, h * 0.45f - btnR*0.5f, cw + unit * 2.5f, h * 0.45f + btnR*0.5f)
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        
        // Draw Buttons
        for ((name, rect) in btns) {
            val isPressed = activePointers.containsValue(name)
            val paint = if (isPressed) btnDownPaint else btnUpPaint
            
            if (name in listOf("A", "B", "X", "Y", "W")) {
                canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() / 2, paint)
                
                val txtColor = when(name) {
                    "A" -> colorA; "B" -> colorB; "X" -> colorX; "Y" -> colorY; else -> textPaint
                }
                textPaint.color = txtColor.color
                canvas.drawText(name, rect.centerX(), rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
                textPaint.color = Color.WHITE
            } else {
                canvas.drawRoundRect(rect, 20f, 20f, paint)
                canvas.drawText(name, rect.centerX(), rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
            }
        }

        // Draw D-Pad
        canvas.drawCircle(dpadCenter.x, dpadCenter.y, dpadRadius, stickBasePaint)
        val dpadPressed = activePointers.containsValue("DPAD")
        if (dpadPressed) {
            canvas.drawCircle(dpadCenter.x, dpadCenter.y, dpadRadius * 0.8f, btnDownPaint)
        }
        
        canvas.drawRect(dpadCenter.x - dpadRadius * 0.3f, dpadCenter.y - dpadRadius, dpadCenter.x + dpadRadius * 0.3f, dpadCenter.y + dpadRadius, btnUpPaint)
        canvas.drawRect(dpadCenter.x - dpadRadius, dpadCenter.y - dpadRadius * 0.3f, dpadCenter.x + dpadRadius, dpadCenter.y + dpadRadius * 0.3f, btnUpPaint)

        // Draw Left Stick
        canvas.drawCircle(leftStickCenter.x, leftStickCenter.y, stickRadius, stickBasePaint)
        canvas.drawCircle(leftStickCurrent.x, leftStickCurrent.y, thumbRadius, stickThumbPaint)
        
        // Draw Right Stick
        canvas.drawCircle(rightStickCenter.x, rightStickCenter.y, stickRadius, stickBasePaint)
        canvas.drawCircle(rightStickCurrent.x, rightStickCurrent.y, thumbRadius, stickThumbPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)
        
        var stateChanged = false

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                // Check Joysticks
                if (dist(x, y, leftStickCenter.x, leftStickCenter.y) < stickRadius) {
                    activePointers[pointerId] = "L_STICK"
                    updateStick(x, y, true)
                    stateChanged = true
                } else if (dist(x, y, rightStickCenter.x, rightStickCenter.y) < stickRadius) {
                    activePointers[pointerId] = "R_STICK"
                    updateStick(x, y, false)
                    stateChanged = true
                } 
                // Check D-Pad
                else if (dist(x, y, dpadCenter.x, dpadCenter.y) < dpadRadius) {
                    activePointers[pointerId] = "DPAD"
                    updateDpad(x, y)
                    stateChanged = true
                }
                // Check Buttons
                else {
                    for ((name, rect) in btns) {
                        // Expanded hitboxes for easier touch
                        val hit = RectF(rect.left - 20, rect.top - 20, rect.right + 20, rect.bottom + 20)
                        if (hit.contains(x, y)) {
                            activePointers[pointerId] = name
                            buttonsMask = (buttonsMask.toInt() or btnMasks[name]!!).toShort()
                            stateChanged = true
                            break
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pId = event.getPointerId(i)
                    val px = event.getX(i)
                    val py = event.getY(i)
                    
                    val target = activePointers[pId]
                    if (target == "L_STICK") {
                        updateStick(px, py, true)
                        stateChanged = true
                    } else if (target == "R_STICK") {
                        updateStick(px, py, false)
                        stateChanged = true
                    } else if (target == "DPAD") {
                        updateDpad(px, py)
                        stateChanged = true
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                val target = activePointers.remove(pointerId)
                if (target == "L_STICK") {
                    leftStickCurrent.set(leftStickCenter)
                    leftStickX = 128.toByte(); leftStickY = 128.toByte()
                    stateChanged = true
                } else if (target == "R_STICK") {
                    rightStickCurrent.set(rightStickCenter)
                    rightStickX = 128.toByte(); rightStickY = 128.toByte()
                    stateChanged = true
                } else if (target == "DPAD") {
                    currentDpad = 0
                    stateChanged = true
                } else if (target != null && btnMasks.containsKey(target)) {
                    buttonsMask = (buttonsMask.toInt() and btnMasks[target]!!.inv()).toShort()
                    stateChanged = true
                }
            }
        }

        if (stateChanged) {
            invalidate()
            sendReport()
        }
        return true
    }

    private fun updateStick(px: Float, py: Float, isLeft: Boolean) {
        val center = if (isLeft) leftStickCenter else rightStickCenter
        val current = if (isLeft) leftStickCurrent else rightStickCurrent
        
        val dx = px - center.x
        val dy = py - center.y
        val dist = sqrt(dx * dx + dy * dy)
        
        if (dist > stickRadius) {
            val angle = atan2(dy, dx)
            current.x = center.x + cos(angle) * stickRadius
            current.y = center.y + sin(angle) * stickRadius
        } else {
            current.x = px
            current.y = py
        }

        // Map to 0-255
        val mappedX = ((current.x - center.x) / stickRadius * 127 + 128).toInt().coerceIn(0, 255)
        val mappedY = ((current.y - center.y) / stickRadius * 127 + 128).toInt().coerceIn(0, 255)
        
        if (isLeft) {
            leftStickX = mappedX.toByte()
            leftStickY = mappedY.toByte()
        } else {
            rightStickX = mappedX.toByte()
            rightStickY = mappedY.toByte()
        }
    }
    
    private fun updateDpad(px: Float, py: Float) {
        val dx = px - dpadCenter.x
        val dy = py - dpadCenter.y
        
        currentDpad = 0
        if (abs(dx) > abs(dy)) {
            currentDpad = if (dx > 0) 3 else 7 // 3=Right, 7=Left (HAT uses 1=Up, 2=UR, 3=R, 4=DR, 5=D, 6=DL, 7=L, 8=UL, 0=Neutral)
        } else {
            currentDpad = if (dy > 0) 5 else 1 // 5=Down, 1=Up
        }
    }

    private fun sendReport() {
        listener?.onGamepadReport(buttonsMask, currentDpad, leftStickX, leftStickY, rightStickX, rightStickY)
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
}
