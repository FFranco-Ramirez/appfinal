package com.evaluacion.condominios

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ApiClient.init(this)

        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtTitle = findViewById<TextView>(R.id.title)

        val btnSensors = findViewById<Button>(R.id.btnSensors)
        val btnHistory = findViewById<Button>(R.id.btnHistory)
        val btnBarrier = findViewById<Button>(R.id.btnBarrier)
        val btnUsers = findViewById<Button>(R.id.btnUsers)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val editDeviceId = findViewById<EditText>(R.id.editDeviceId)

        btnSensors.visibility = View.GONE
        btnHistory.visibility = View.GONE
        btnBarrier.visibility = View.GONE
        btnUsers.visibility = View.GONE
        btnLogout.visibility = View.GONE
        editDeviceId.visibility = View.GONE

        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val pass = editPassword.text.toString()
            if (email.isEmpty() || pass.isEmpty()) return@setOnClickListener
            ApiClient.login(email, pass, object : ApiClient.Callback {
                override fun onSuccess(res: JSONObject) {
                    val ok = res.optBoolean("ok", false)
                    if (!ok) return
                    val userId = res.optInt("user_id", -1)
                    val depId = res.optInt("department_id", -1)
                    val role = res.optString("rol", "OPERADOR")
                    val name = res.optString("nombre", "")
                    SessionManager.save(this@MainActivity, userId, depId, role, name)
                    runOnUiThread {
                        txtTitle.text = "Bienvenido: $name ($role)"
                        btnSensors.visibility = View.VISIBLE
                        btnHistory.visibility = View.VISIBLE
                        editDeviceId.visibility = View.VISIBLE
                        btnBarrier.visibility = if (role == "ADMIN") View.VISIBLE else View.GONE
                        btnUsers.visibility = if (role == "ADMIN") View.VISIBLE else View.GONE
                        btnLogout.visibility = View.VISIBLE
                        if (role == "ADMIN" && depId <= 0) {
                            promptAssociateDepartment()
                        }
                    }
                }
                override fun onError(message: String) { /* mostrar error si se desea */ }
            })
        }

        fun promptAssociateDepartment() {
            btnSensors.visibility = View.GONE
            btnHistory.visibility = View.GONE
            editDeviceId.visibility = View.GONE
            btnBarrier.visibility = View.GONE
            btnUsers.visibility = View.GONE
            val options = arrayOf("Crear departamento", "Seleccionar existente sin admin")
            AlertDialog.Builder(this)
                .setTitle("Asociar departamento")
                .setCancelable(false)
                .setItems(options) { _, which ->
                    if (which == 0) promptCreateDepartment() else promptSelectDepartment()
                }
                .show()
        }

        fun promptCreateDepartment() {
            val input = EditText(this)
            input.hint = "Nombre del departamento"
            AlertDialog.Builder(this)
                .setTitle("Nuevo departamento")
                .setView(input)
                .setCancelable(false)
                .setNegativeButton("Cancelar") { _, _ -> promptAssociateDepartment() }
                .setPositiveButton("Crear") { _, _ ->
                    val nombre = input.text.toString().trim()
                    if (nombre.isEmpty()) return@setPositiveButton
                    val requestedBy = SessionManager.getUserId(this)
                    ApiClient.createDepartment(nombre, requestedBy, object : ApiClient.Callback {
                        override fun onSuccess(res: JSONObject) {
                            val ok = res.optBoolean("ok", false)
                            val dep = res.optInt("department_id", -1)
                            val usr = SessionManager.getUserId(this@MainActivity)
                            val roleNow = SessionManager.getRole(this@MainActivity) ?: "ADMIN"
                            val nameNow = SessionManager.getName(this@MainActivity) ?: ""
                            if (ok && dep > 0) {
                                SessionManager.save(this@MainActivity, usr, dep, roleNow, nameNow)
                                runOnUiThread {
                                    txtTitle.text = "Bienvenido: $nameNow ($roleNow)"
                                    btnSensors.visibility = View.VISIBLE
                                    btnHistory.visibility = View.VISIBLE
                                    editDeviceId.visibility = View.VISIBLE
                                    btnBarrier.visibility = View.VISIBLE
                                    btnUsers.visibility = View.VISIBLE
                                    btnLogout.visibility = View.VISIBLE
                                }
                            }
                        }
                        override fun onError(message: String) {}
                    })
                }
                .show()
        }

        fun promptSelectDepartment() {
            val requestedBy = SessionManager.getUserId(this)
            ApiClient.listDepartmentsWithoutAdmin(object : ApiClient.Callback {
                override fun onSuccess(res: JSONObject) {
                    val arr = res.optJSONArray("items")
                    if (arr == null || arr.length() == 0) {
                        runOnUiThread {
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("Sin departamentos disponibles")
                                .setMessage("Crea un nuevo departamento")
                                .setCancelable(false)
                                .setPositiveButton("Crear") { _, _ -> promptCreateDepartment() }
                                .setNegativeButton("Cancelar") { _, _ -> promptAssociateDepartment() }
                                .show()
                        }
                        return
                    }
                    val names = Array(arr.length()) { i -> arr.getJSONObject(i).optString("nombre") }
                    val ids = IntArray(arr.length()) { i -> arr.getJSONObject(i).optInt("department_id") }
                    runOnUiThread {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Seleccionar departamento")
                            .setCancelable(false)
                            .setItems(names) { _, idx ->
                                ApiClient.setAdminDepartment(requestedBy, ids[idx], object : ApiClient.Callback {
                                    override fun onSuccess(res2: JSONObject) {
                                        val ok = res2.optBoolean("ok", false)
                                        val dep = res2.optInt("department_id", -1)
                                        val usr = SessionManager.getUserId(this@MainActivity)
                                        val roleNow = SessionManager.getRole(this@MainActivity) ?: "ADMIN"
                                        val nameNow = SessionManager.getName(this@MainActivity) ?: ""
                                        if (ok && dep > 0) {
                                            SessionManager.save(this@MainActivity, usr, dep, roleNow, nameNow)
                                            txtTitle.text = "Bienvenido: $nameNow ($roleNow)"
                                            btnSensors.visibility = View.VISIBLE
                                            btnHistory.visibility = View.VISIBLE
                                            editDeviceId.visibility = View.VISIBLE
                                            btnBarrier.visibility = View.VISIBLE
                                            btnUsers.visibility = View.VISIBLE
                                            btnLogout.visibility = View.VISIBLE
                                        }
                                    }
                                    override fun onError(message: String) {}
                                })
                            }
                            .setNegativeButton("Cancelar") { _, _ -> promptAssociateDepartment() }
                            .show()
                    }
                }
                override fun onError(message: String) {}
            })
        }

        btnSensors.setOnClickListener {
            val dep = SessionManager.getDepartmentId(this)
            val usr = SessionManager.getUserId(this)
            val intent = Intent(this, SensorListActivity::class.java)
            intent.putExtra("department_id", dep)
            intent.putExtra("user_id", usr)
            startActivity(intent)
        }

        btnHistory.setOnClickListener {
            val dep = SessionManager.getDepartmentId(this)
            val intent = Intent(this, AccessHistoryActivity::class.java)
            intent.putExtra("department_id", dep)
            startActivity(intent)
        }

        btnBarrier.setOnClickListener {
            val dep = SessionManager.getDepartmentId(this)
            val usr = SessionManager.getUserId(this)
            val dev = editDeviceId.text.toString().toIntOrNull() ?: 1
            val intent = Intent(this, BarrierControlActivity::class.java)
            intent.putExtra("department_id", dep)
            intent.putExtra("user_id", usr)
            intent.putExtra("device_id", dev)
            startActivity(intent)
        }

        btnUsers.setOnClickListener {
            val dep = SessionManager.getDepartmentId(this)
            val intent = Intent(this, CreateUserActivity::class.java)
            intent.putExtra("department_id", dep)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            SessionManager.clear(this)
            txtTitle.text = "Login"
            editEmail.setText("")
            editPassword.setText("")
            btnSensors.visibility = View.GONE
            btnHistory.visibility = View.GONE
            btnBarrier.visibility = View.GONE
            btnUsers.visibility = View.GONE
            btnLogout.visibility = View.GONE
            editDeviceId.visibility = View.GONE
        }
    }
}