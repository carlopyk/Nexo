package net.azarquiel.nexo.View

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*
import net.azarquiel.nexo.R
import org.jetbrains.anko.*


class LoginActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var loginShare: SharedPreferences
    private lateinit var usuario: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        usuario = intent.getStringExtra("usuario")
        btnLogin.setOnClickListener{
            login()
        }
        btnOlvidar.setOnClickListener{
            cambiarPass()
        }
        initShare()
        if (comprobarLogin()){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun initShare() {
        loginShare = this.getSharedPreferences("login", Context.MODE_PRIVATE)
    }

    private fun login() {
        val email:String=edEmail.text.toString()
        val pass:String=edPass.text.toString()

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)){

            mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(this){
                    task ->

                if (task.isSuccessful){
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.let {
                        val edit= loginShare.edit()
                        edit.clear()
                        edit.putString("${it.email}", Gson().toJson(it))
                        edit.putString("usuario", usuario)
                        edit.apply()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                }else{
                    Toast.makeText(this, "Usuario y/o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cambiarPass() {
        alert{
            title = "Cambiar contraseña"
            customView {
                verticalLayout {
                    lparams(width = wrapContent, height = wrapContent)
                    val etEmail = editText {
                        inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        hint = "Email"
                        padding = dip(16)
                    }
                    positiveButton("Aceptar") {
                        if (etEmail.text.toString().isEmpty())
                            toast("Campos Obligatorios")
                        else {
                            enviarCorreo(etEmail.text.toString())
                        }
                    }
                    negativeButton("Cancelar") {}
                }
            }
        }.show()

    }

    private fun enviarCorreo(email: String) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                toast("Se ha enviado un correo para restablecer contraseña").show()
            }
        }
    }

    private fun comprobarLogin():Boolean {
        return loginShare.all.isNotEmpty()
    }
}

