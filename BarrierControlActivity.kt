package com.evaluacion.condominios

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class BarrierControlActivity : AppCompatActivity() {
    private val TAG = "BarrierControlActivity"
    private var departmentId = 1
    private var userId = 1
    private var deviceId = 1
    private lateinit var progress: ProgressBar
    private lateinit var txtStatus: TextView
    private lateinit var btnOpen: Button
    private lateinit var btnClose: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barrier_control)
        ApiClient.init(this)

        departmentId = intent.getIntExtra("department_id", 1)
        userId = intent.getIntExtra("user_id", 1)
        deviceId = intent.getIntExtra("device_id", 1)

        progress = findViewById(R.id.progress)
        txtStatus = findViewById(R.id.txtStatus)
        btnOpen = findViewById(R.id.btnOpen)
        btnClose = findViewById(R.id.btnClose)

        btnOpen.setOnClickListener { sendAction("open") }
        btnClose.setOnClickListener { sendAction("close") }
    }

    private fun sendAction(action: String) {
        progress.visibility = View.VISIBLE
        txtStatus.text = ""
        ApiClient.barrierControl(deviceId, departmentId, action, userId, object : ApiClient.Callback {
            override fun onSuccess(res: JSONObject) {
                val ok = res.optBoolean("ok", false)
                val id = res.optInt("command_id", -1)
                val text = if (ok) "Acción $action enviada (id=$id)" else "Respuesta no OK: $res"
                runOnUiThread {
                    txtStatus.text = text
                    progress.visibility = View.GONE
                }
            }
            override fun onError(message: String) {
                Log.e(TAG, "barrierControl ERROR: $message")
                runOnUiThread {
                    txtStatus.text = "Error: $message"
                    progress.visibility = View.GONE
                }
            }
        })
    }
}