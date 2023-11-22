package com.cibertec.apptecnotienda

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MenuPrincipal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)

        val buttonLogin : Button = findViewById(R.id.buttonLogin)
        val buttonRegistrar : Button = findViewById(R.id.buttonRegistrar)

        buttonLogin.setOnClickListener{
            val LoginWindow = Intent(this, Login::class.java)
            startActivity(LoginWindow)
        }
        buttonRegistrar.setOnClickListener{
            val RegistrarWindow = Intent(this, Registrar::class.java)
            startActivity(RegistrarWindow)
        }
    }
}