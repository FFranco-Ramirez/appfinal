package com.evaluacion.condominios

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class AccessHistoryActivity : AppCompatActivity() {
    // ... existing code ...
    private val TAG = "AccessHistoryActivity"
    private lateinit var listView: ListView
    private lateinit var progress: ProgressBar
    private lateinit var adapter: ArrayAdapter<String>
    private val items = ArrayList<String>()

    private var departmentId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_history)

        listView = findViewById(R.id.listEvents)
        progress = findViewById(R.id.progress)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        ApiClient.init(this)
        departmentId = intent.getIntExtra("department_id", SessionManager.getDepartmentId(this))
        loadEvents()
    }

    private fun loadEvents() {
        runOnUiThread { progress.visibility = View.VISIBLE }
        items.clear()

        ApiClient.listAccessEvents(departmentId, 50, null, null, object : ApiClient.Callback {
            override fun onSuccess(res: JSONObject) {
                try {
                    val ok = res.optBoolean("ok", false)
                    if (!ok) {
                        Log.e(TAG, "Respuesta no OK: $res")
                        runOnUiThread { progress.visibility = View.GONE }
                        return
                    }
                    val arr: JSONArray? = res.optJSONArray("items")
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            val ev = arr.getJSONObject(i)
                            val fecha = ev.optString("fecha_hora", "")
                            val tipo = ev.optString("tipo_evento", "")
                            val resultado = ev.optString("resultado", "")
                            val codigo = ev.optString("codigo_sensor", "")
                            val usuario = ev.optString("usuario_nombre", "")
                            var text = "$fecha | $tipo | $resultado"
                            if (codigo.isNotEmpty()) text += " | $codigo"
                            if (usuario.isNotEmpty()) text += " | $usuario"
                            items.add(text)
                        }
                    }
                    runOnUiThread {
                        adapter.notifyDataSetChanged()
                        progress.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Parse error: ${e.message}", e)
                    runOnUiThread { progress.visibility = View.GONE }
                }
            }

            override fun onError(message: String) {
                Log.e(TAG, "listAccessEvents ERROR: $message")
                runOnUiThread { progress.visibility = View.GONE }
            }
        })
    }
    // ... existing code ...
}