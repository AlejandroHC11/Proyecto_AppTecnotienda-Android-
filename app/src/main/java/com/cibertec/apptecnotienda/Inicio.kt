package com.cibertec.apptecnotienda

import android.app.Dialog
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.bibliotecaapp.RetroFitHelper
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Inicio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        val buttonCerrarSesion : Button = findViewById(R.id.buttonCerrarSesion)

        buttonCerrarSesion.setOnClickListener{
            val MenuPrincipalWindow = Intent(this, MenuPrincipal::class.java)
            startActivity(MenuPrincipalWindow)
            finish()
        }

        val buttonCrear : Button = findViewById(R.id.buttonCrear)
        buttonCrear.setOnClickListener{
            showDialogCrear()
        }
        val buttonReporte : Button = findViewById(R.id.buttonReporte)
        buttonReporte.setOnClickListener {
            descargarInformePDF();
        }

        val recycleViewProductos : RecyclerView = findViewById(R.id.recycleViewProductos)
        recycleViewProductos.layoutManager = LinearLayoutManager(this)

        // Configura el RecyclerView con un GridLayoutManager y 2 columnas
        val layoutManager = GridLayoutManager(this, 2)
        recycleViewProductos.layoutManager = layoutManager

        val data = ArrayList<Producto>();

        val adapter = ProductoAdapter(data)
        recycleViewProductos.adapter = adapter

        val quoteApi = RetroFitHelper.getRetrofitInstance(this).create(QuoteApi::class.java)

        lifecycleScope.launch {
            try {
                // Llamada
                val result: Response<List<Producto>> = withContext(Dispatchers.IO) {
                    quoteApi.getQuotes()
                }
                // Verificar
                if (result.isSuccessful) {
                    // Extraer de la respuesta
                    val productoList = result.body()
                    // Actualizar recycle view
                    productoList?.let {
                        data.clear()
                        data.addAll(it.map { produ ->
                            Producto(produ.idProduct,produ.nombre, produ.img, produ.precio,produ.stock)
                        })
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    // Excepcion
                    Log.e("REST RESPONSE", "Error: ${result.code()}")
                }
            } catch (e: Exception) {
                // Excepcion
                Log.e("REST RESPONSE", "Exception: ${e.message}")
            }
        }
    }
    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    private fun descargarInformePDF() {
        val ipPrivada = getString(R.string.ipPrivada)
        val urlInforme = "http://${ipPrivada}:7211/GenerateJasperReport/"

        val retrofit = Retrofit.Builder()
            .baseUrl(urlInforme)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val quoteApi = retrofit.create(QuoteApi::class.java)

        // Solicitud
        quoteApi.generateJasperReport().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        descargarArchivo(response.body())
                    } else {
                        requestPermissions(
                            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PERMISSION_REQUEST_CODE
                        )
                    }
                } else {
                    showDialog("Error en procesar Reporte")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showDialog("Error en generar Reporte")
            }
        })
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso concedido, procede con la descarga
                    descargarInformePDF()
                } else {
                    // Permiso denegado, muestra un mensaje o toma alguna acción adicional
                    showDialog("Permiso de almacenamiento denegado")
                }
            }
        }
    }
    private fun descargarArchivo(body: ResponseBody?) {
        if (body == null) {
            return
        }

        val directorioDescargas = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val archivoInforme = File(directorioDescargas, "informe.pdf")

        val fileOutputStream = FileOutputStream(archivoInforme)
        fileOutputStream.use { output ->
            output.write(body.bytes())
        }

        // Utilizar MediaStore para escanear el nuevo archivo
        val mediaStoreUpdate = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DATA, archivoInforme.absolutePath)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.Files.FileColumns.TITLE, "informe.pdf")
        }

        contentResolver.insert(MediaStore.Files.getContentUri("external"), mediaStoreUpdate)

        Toast.makeText(this, "Descarga completada", Toast.LENGTH_SHORT).show()
    }
    fun showDialogCrear(){
        val dialogConfirm = Dialog(this)
        dialogConfirm.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogConfirm.setCancelable(false)
        dialogConfirm.setContentView(R.layout.card_create)

        dialogConfirm.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnDialogCrear: Button = dialogConfirm.findViewById(R.id.btnDialogCrear)
        btnDialogCrear.setOnClickListener {
            //Declaracion de textField
            val textInputNombre: TextInputEditText = dialogConfirm.findViewById(R.id.txtFieldNombre)
            val textInputLinkImg: TextInputEditText = dialogConfirm.findViewById(R.id.txtFieldLinkImg)
            val textInputPrecio: TextInputEditText = dialogConfirm.findViewById(R.id.txtFieldPrecio)
            val textInputStock: TextInputEditText = dialogConfirm.findViewById(R.id.txtFieldStock)
            //Obtener datos de textField
            val nombre = textInputNombre.text.toString()
            Log.e("Test","${nombre}")
            val linkImg = textInputLinkImg.text.toString()
            val precio = textInputPrecio.text.toString()
            val stock = textInputStock.text.toString()
            //Convertir a json
            val jsonProducto = JSONObject()
            jsonProducto.put("nombre", nombre)
            jsonProducto.put("img", linkImg)
            jsonProducto.put("precio", precio)
            jsonProducto.put("stock", stock)
            val ipPrivada = getString(R.string.ipPrivada)
            Fuel.post("http://${ipPrivada}:7211/CreateProduct/")
                .header("Content-Type", "application/json;charset=utf-8")
                .body(jsonProducto.toString())
                .response { _, _, result ->
                    result.fold(
                        success = {
                            showDialog("Producto creado")
                            val intent = Intent(this, Inicio::class.java)
                            startActivity(intent)
                        },
                        failure = { error ->
                            showDialog("Error al crear")
                        }
                    )
                }

            dialogConfirm.dismiss()
        }


        dialogConfirm.show()
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