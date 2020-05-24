package net.azarquiel.nexo.View

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_inicio.*
import net.azarquiel.nexo.R

class InicioActivity : AppCompatActivity() {

    private lateinit var loginShare: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        initShare()
        if (comprobarLogin()){
            startActivity(Intent(this, MainActivity::class.java))
        }
        btnAlumno.setOnClickListener{onClickBoton(btnAlumno)}
        btnProfesor.setOnClickListener{onClickBoton(btnProfesor)}
        btnPadre.setOnClickListener{}
    }

    private fun onClickBoton(v: View) {
        val botonPulsado = v.tag as String
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("usuario", botonPulsado)
        startActivity(intent)
    }

    private fun comprobarLogin():Boolean {
        return loginShare.all.isNotEmpty()
    }

    private fun initShare() {
        loginShare = this.getSharedPreferences("login", Context.MODE_PRIVATE)
    }
}
