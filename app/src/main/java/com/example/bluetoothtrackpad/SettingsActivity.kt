package com.example.bluetoothtrackpad

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        val switchGyro = findViewById<Switch>(R.id.switchGyro)
        val seekSensitivity = findViewById<SeekBar>(R.id.seekSensitivity)

        switchGyro.isChecked = SettingsManager.isGyroEnabled(this, -1) // -1 for default layouts
        seekSensitivity.progress = SettingsManager.getSensitivity(this, -1)

        switchGyro.setOnCheckedChangeListener { _, isChecked -> SettingsManager.setGyroEnabled(this, -1, isChecked) }
        seekSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SettingsManager.setSensitivity(this@SettingsActivity, -1, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<Button>(R.id.btnCreateLayout).setOnClickListener {
            val intent = Intent(this, LayoutEditorActivity::class.java)
            // Using Intent Extra to create new vs edit
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshLayoutsList()
    }

    private fun refreshLayoutsList() {
        val container = findViewById<LinearLayout>(R.id.containerLayouts)
        container.removeAllViews()

        val layouts = LayoutManager.getLayouts(this)
        for (layout in layouts) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 16, 0, 16)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            val tvName = TextView(this).apply {
                text = "${layout.name} (${layout.widgets.size} items)"
                setTextColor(Color.WHITE)
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val btnEdit = Button(this).apply {
                text = "Edit"
                setOnClickListener {
                    val intent = Intent(this@SettingsActivity, LayoutEditorActivity::class.java)
                    intent.putExtra("layout_id", layout.id)
                    startActivity(intent)
                }
            }
            
            val btnDel = Button(this).apply {
                text = "Del"
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.RED)
                setOnClickListener {
                    val list = LayoutManager.getLayouts(this@SettingsActivity).filter { it.id != layout.id }
                    LayoutManager.saveLayouts(this@SettingsActivity, list)
                    refreshLayoutsList()
                }
            }

            row.addView(tvName)
            row.addView(btnEdit)
            row.addView(btnDel)
            container.addView(row)
        }
    }
}
