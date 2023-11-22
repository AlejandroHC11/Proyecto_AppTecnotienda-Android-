package com.cibertec.apptecnotienda

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.json.responseJson
import org.json.JSONObject

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val buttonLoginUsuario : Button = findViewById(R.id.buttonLoginUsuario)
        val buttonVolver : Button = findViewById(R.id.buttonVolver2)

        buttonLoginUsuario.setOnClickListener{


            val inputUsuariotxt : EditText = findViewById(R.id.textFieldUsuario)
            val inputContratxt : EditText = findViewById(R.id.textFieldContra)

            val usuario = inputUsuariotxt.text.toString()
            val contra = inputContratxt.text.toString()

            val jsonUsuario = JSONObject()
            jsonUsuario.put("userData", usuario)
            jsonUsuario.put("passwordData", contra)

            // POST
            val ipPrivada = getString(R.string.ipPrivada)
            Fuel.post("http://$ipPrivada:7211/LoginUser")
                .header("Content-Type", "application/json;charset=utf-8")
                .body(jsonUsuario.toString())
                .responseJson { _, _, result ->
                    result.fold(
                        success = { json ->
                            val success = json.content.toBoolean()
                            if (success) {
                                val inicioWindow = Intent(this, Inicio::class.java)
                                startActivity(inicioWindow)
                                finish()
                            } else {
                                showDialog("Usuario o contraseña incorrecta")
                            }
                        },
                        failure = { error ->
                            showDialog("Error del login")
                        }
                    )
                }
        }

        buttonVolver.setOnClickListener{
            val menuPrincipalWindow = Intent(this, MenuPrincipal::class.java)
            startActivity(menuPrincipalWindow)
        }
    }
    private fun showDialog(mensaje: String){
        val dialogConfirm = Dialog(this)
        dialogConfirm.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogConfirm.setCancelable(false)
        dialogConfirm.setContentView(R.layout.dialog_mensaje)

        dialogConfirm.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val modalMessageTextView: TextView = dialogConfirm.findViewById(R.id.textViewMensaje)
        modalMessageTextView.text = mensaje

        val btnDialogAceptar : Button = dialogConfirm.findViewById(R.id.btnDialogAceptar)

        btnDialogAceptar.setOnClickListener {
            dialogConfirm.dismiss()
        }

        dialogConfirm.show()
    }
}