package com.evaluacion.condominios

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class CreateUserActivity : AppCompatActivity() {
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var btnCreate: Button
    private lateinit var progress: ProgressBar
    private lateinit var txtStatus: TextView
    private var departmentId: Int = -1
    private var requestedBy: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        ApiClient.init(this)

        departmentId = intent.getIntExtra("department_id", SessionManager.getDepartmentId(this))
        requestedBy = SessionManager.getUserId(this)

        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        spinnerRole = findViewById(R.id.spinnerRole)
        btnCreate = findViewById(R.id.btnCreate)
        progress = findViewById(R.id.progress)
        txtStatus = findViewById(R.id.txtStatus)

        val roles = arrayOf("OPERADOR", "ADMIN")
        spinnerRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        btnCreate.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val pass = editPassword.text.toString()
            val role = spinnerRole.selectedItem.toString()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                txtStatus.text = "Completa todos los campos"
                return@setOnClickListener
            }
            progress.visibility = View.VISIBLE
            txtStatus.text = ""
            ApiClient.createUser(name, email, pass, role, departmentId, requestedBy, object : ApiClient.Callback {
                override fun onSuccess(res: JSONObject) {
                    val ok = res.optBoolean("ok", false)
                    val msg = if (ok) "Usuario creado" else "Error: $res"
                    runOnUiThread {
                        txtStatus.text = msg
                        progress.visibility = View.GONE
                        if (ok) finish()
                    }
                }
                override fun onError(message: String) {
                    runOnUiThread {
                        txtStatus.text = "Error: $message"
                        progress.visibility = View.GONE
                    }
                }
            })
        }
    }
}