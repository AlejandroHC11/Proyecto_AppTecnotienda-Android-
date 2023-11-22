package com.cibertec.apptecnotienda

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.json.responseJson
import org.json.JSONObject

class Registrar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar)

        val buttonRegistroUsuario : Button = findViewById(R.id.buttonRegistroUsuario)
        val buttonVolver : Button = findViewById(R.id.buttonVolver)

        buttonRegistroUsuario.setOnClickListener{
            val inputUsuariotxt : EditText = findViewById(R.id.textFieldUsuario)
            val inputContratxt : EditText = findViewById(R.id.textFieldContra)

            val usuario = inputUsuariotxt.text.toString()
            val contra = inputContratxt.text.toString()

            val jsonUsuario = JSONObject()
            jsonUsuario.put("userData", usuario)
            jsonUsuario.put("passwordData", contra)

            // POST
            val ipPrivada = getString(R.string.ipPrivada)
            Fuel.post("http://$ipPrivada:7211/CreateUser")
                .header("Content-Type", "application/json;charset=utf-8")
                .body(jsonUsuario.toString())
                .responseJson { _, _, result ->
                    result.fold(
                        success = {
                            mensajeExito();
                        },
                        failure = {
                            mensajeError();
                        }
                    )
                }
        }
        buttonVolver.setOnClickListener{
           val menuPrincipalWindow = Intent(this, MenuPrincipal::class.java)
           startActivity(menuPrincipalWindow)
        }
    }

    private fun mensajeExito() {
        showDialog("Registrado correctamente", true)
    }
    private fun mensajeError() {
        showDialog("Error en el registro", false)
    }
    private fun showDialog(mensaje: String, irAMenuPrincipal: Boolean){
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
            if (irAMenuPrincipal) {
                val menuPrincipalWindow = Intent(this, MenuPrincipal::class.java)
                startActivity(menuPrincipalWindow)
                finish()
            }
        }

        dialogConfirm.show()
    }
}