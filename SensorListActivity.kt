package com.evaluacion.condominios

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import org.json.JSONArray
import org.json.JSONObject

class SensorListActivity : AppCompatActivity() {
    private val TAG = "SensorListActivity"
    private lateinit var listView: ListView
    private lateinit var progress: ProgressBar
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var btnNewSensor: Button
    private val items = ArrayList<String>()
    private val ids = ArrayList<Int>()
    private var departmentId = 1
    private var userId = 1
    private var role: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_list)

        listView = findViewById(R.id.listSensors)
        progress = findViewById(R.id.progress)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        ApiClient.init(this)
        btnNewSensor = findViewById(R.id.btnNewSensor)
        departmentId = intent.getIntExtra("department_id", 1)
        userId = intent.getIntExtra("user_id", 1)
        role = SessionManager.getRole(this)
        btnNewSensor.visibility = if (role == "ADMIN") View.VISIBLE else View.GONE
        btnNewSensor.setOnClickListener { showCreateSensorDialog() }
        if (role == "ADMIN") {
            listView.setOnItemClickListener { _, _, position, _ ->
                showStatusMenu(position)
            }
        }
        loadSensors()
    }

    private fun loadSensors() {
        runOnUiThread { progress.visibility = View.VISIBLE }
        items.clear()
        ids.clear()
        ApiClient.listSensors(departmentId, object : ApiClient.Callback {
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
                            val s = arr.getJSONObject(i)
                            val codigo = s.optString("codigo_sensor", "")
                            val estado = s.optString("estado", "")
                            val tipo = s.optString("tipo", "")
                            var text = codigo
                            if (estado.isNotEmpty()) text += " | $estado"
                            if (tipo.isNotEmpty()) text += " | $tipo"
                            items.add(text)
                            ids.add(s.optInt("id_sensor"))
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
                Log.e(TAG, "listSensors ERROR: $message")
                runOnUiThread { progress.visibility = View.GONE }
            }
        })
    }

    private fun showCreateSensorDialog() {
        val input = EditText(this)
        input.hint = "UID/MAC"
        AlertDialog.Builder(this)
            .setTitle("Nuevo sensor")
            .setView(input)
            .setPositiveButton("Continuar") { _, _ ->
                val uid = input.text.toString().trim()
                if (uid.isEmpty()) return@setPositiveButton
                val estados = arrayOf("ACTIVO","INACTIVO","PERDIDO","BLOQUEADO")
                AlertDialog.Builder(this)
                    .setTitle("Estado inicial")
                    .setItems(estados) { _, which ->
                        val status = estados[which]
                        val tipos = arrayOf("LLAVERO","TARJETA")
                        AlertDialog.Builder(this)
                            .setTitle("Tipo")
                            .setItems(tipos) { _, w2 ->
                                val type = tipos[w2]
                                progress.visibility = View.VISIBLE
                                ApiClient.createSensor(uid, departmentId, userId, status, type, object : ApiClient.Callback {
                                    override fun onSuccess(res: JSONObject) {
                                        runOnUiThread {
                                            progress.visibility = View.GONE
                                            loadSensors()
                                        }
                                    }
                                    override fun onError(message: String) {
                                        Log.e(TAG, "createSensor ERROR: $message")
                                        runOnUiThread { progress.visibility = View.GONE }
                                    }
                                })
                            }
                            .show()
                    }
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showStatusMenu(position: Int) {
        val sensorId = ids[position]
        val estados = arrayOf("ACTIVO","INACTIVO","PERDIDO","BLOQUEADO")
        AlertDialog.Builder(this)
            .setTitle("Cambiar estado")
            .setItems(estados) { _, which ->
                val status = estados[which]
                progress.visibility = View.VISIBLE
                ApiClient.updateSensorStatus(sensorId, status, object : ApiClient.Callback {
                    override fun onSuccess(res: JSONObject) {
                        runOnUiThread {
                            progress.visibility = View.GONE
                            loadSensors()
                        }
                    }
                    override fun onError(message: String) {
                        Log.e(TAG, "updateStatus ERROR: $message")
                        runOnUiThread { progress.visibility = View.GONE }
                    }
                })
            }
            .show()
    }
}