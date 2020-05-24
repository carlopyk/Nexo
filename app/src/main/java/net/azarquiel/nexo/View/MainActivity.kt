package net.azarquiel.nexo.View

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.constraintlayout.widget.Constraints
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.rowasistencia.view.*
import net.azarquiel.nexo.Model.Asignatura
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.rowtrabajo.view.*
import net.azarquiel.nexo.Model.Alumno
import net.azarquiel.nexo.Model.Puntos
import net.azarquiel.nexo.Model.Trabajo
import org.jetbrains.anko.*
import net.azarquiel.nexo.R

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var asignaturaPulsada: Asignatura
    private lateinit var loginShare: SharedPreferences
    private lateinit var utilityShare: SharedPreferences
    private lateinit var alumnosShare: SharedPreferences
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String
    private var alumnos: ArrayList<Alumno> = ArrayList()
    private lateinit var usuario: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initShare()
        usuario = loginShare.getString("usuario", "nousuario") as String
        cambiarTheme()
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        nav_view.setNavigationItemSelectedListener(this)
        nav_viewBottom.setOnNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            uid = it.uid
        }
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        db = FirebaseFirestore.getInstance()
        updateHeader(usuario, uid)
        comprobarUsuario()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null
        when (item.itemId) {
            R.id.nav_clases -> {
                fragment = ClasesFragment(usuario)
                nav_viewBottom.menu.findItem(R.id.navigation_puntos).isChecked = true
                nav_viewBottom.visibility = View.INVISIBLE
            }
            R.id.nav_horario -> {
                fragment = HorarioFragment()
                nav_viewBottom.visibility = View.INVISIBLE
            }
            R.id.nav_logout -> {
                cerrarSesion()
                nav_viewBottom.visibility = View.INVISIBLE
            }
            R.id.navigation_alumnos -> {
                fragment = AlumnosFragment(asignaturaPulsada)
                nav_viewBottom.visibility = View.VISIBLE
            }
            R.id.navigation_asistencia -> {
                if (usuario == "profesor") {
                    fragment = AsistenciaFragment(asignaturaPulsada)
                }else {
                    fragment = AsistenciaAlumnoFragment(asignaturaPulsada)
                }
                nav_viewBottom.visibility = View.VISIBLE
            }
            R.id.navigation_trabajos -> {
                if (usuario == "profesor") {
                    fragment = TrabajoFragment(asignaturaPulsada)
                }else {
                    fragment = TrabajoAlumnoFragment(asignaturaPulsada)
                }
                nav_viewBottom.visibility = View.VISIBLE
            }
            R.id.navigation_puntos -> {
                fragment = PerfilPuntosFragment(asignaturaPulsada)
                nav_viewBottom.visibility = View.VISIBLE
            }
            R.id.navigation_avisos -> {
                fragment = AvisosFragment(asignaturaPulsada)
                nav_viewBottom.visibility = View.VISIBLE
            }
        }
        fragment?.let { replaceFragment(it) }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setInitialFragment(usuario: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        when (usuario){
            "profesor" -> {
                fragmentTransaction.add(R.id.frame,ClasesFragment(usuario))
                nav_view.setCheckedItem(R.id.nav_clases)
                nav_viewBottom.menu.findItem(R.id.navigation_alumnos).isVisible = true
                nav_viewBottom.menu.findItem(R.id.navigation_alumnos).isChecked = true
            }
            "alumno" -> {
                fragmentTransaction.add(R.id.frame,ClasesFragment(usuario))
                nav_view.setCheckedItem(R.id.nav_clases)
                nav_viewBottom.menu.findItem(R.id.navigation_puntos).isVisible = true
                nav_viewBottom.menu.findItem(R.id.navigation_puntos).isChecked = true
                nav_viewBottom.menu.findItem(R.id.navigation_avisos).isVisible = true
            }
        }

        nav_viewBottom.visibility = View.INVISIBLE
        fragmentTransaction.commit()
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame, fragment)
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun updateHeader(usuario: String, uid: String) {
        setListenerUsuario(usuario, uid)
    }

    fun onClickClase(v: View) {
        asignaturaPulsada = v.tag as Asignatura
        val edit= utilityShare.edit()
        edit.putString("asignatura", Gson().toJson(asignaturaPulsada))
        edit.apply()
        val fragment: Fragment?
        when(usuario) {
            "profesor" -> {
                fragment = AlumnosFragment(asignaturaPulsada)
            }
            else -> {
                fragment = PerfilPuntosFragment(asignaturaPulsada)
            }
        }
        nav_viewBottom.visibility = View.VISIBLE
        replaceFragment(fragment)
    }

    fun onClickTrabajo(v: View) {
        if (!v.cbtrabajorow.isChecked && usuario == "profesor") {
            val trabajoPulsado = v.tag as Trabajo
            val edit= utilityShare.edit()
            edit.putString("trabajo", Gson().toJson(trabajoPulsado))
            edit.apply()
            alert {
                title = "¿Quiéres dar por terminado el ${trabajoPulsado.tipo}?"
                positiveButton("Aceptar") {
                    v.cbtrabajorow.isChecked = true
                    updateTerminado(v.tvdescripciontrabajorow.tag as String)
                    trabajoPulsado.terminado = true
                    v.tag = trabajoPulsado
                }
                negativeButton("Cancelar") {  }
            }.show()
        }
    }

    private fun updateTerminado(
        id: String
    ) {
        val json = utilityShare.getString("asignatura", "noasignatura")
        val asignatura = Gson().fromJson(json, Asignatura::class.java)

        val trabajoDocument = db.collection("Profesor").document(uid).collection("Asignatura")
            .document(asignatura.codAsignatura).collection("Trabajos").document(id)
        trabajoDocument
            .update("terminado", true)
            .addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot profesor successfully updated!") }
            .addOnFailureListener { Log.w(ContentValues.TAG, "Error updating document") }

        val alumnosjson = alumnosShare.all
        for (entry in alumnosjson.entries) {
            val jsonalumno = entry.value.toString()
            val alumno = Gson().fromJson(jsonalumno, Alumno::class.java)
            alumnos.add(alumno)
        }
        alumnos.forEach {
            setListenerTrabajoAlumno(it)
        }
    }

    private fun setListenerTrabajoAlumno(
        alumno: Alumno
    ) {
        val docRef = db.collection("Alumno").document(alumno.id).collection("Trabajos")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen alumnos failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToListTrabajoAlumno(snapshot.documents, alumno)
            } else {
                Log.d(Constraints.TAG, "Current data alumnos: null")
            }
        }
    }

    private fun documentToListTrabajoAlumno(
        documents: List<DocumentSnapshot>,
        alumno: Alumno
    ) {
        val json = utilityShare.getString("trabajo", "notrabajo")
        val trabajo = Gson().fromJson(json, Trabajo::class.java)
        documents.forEach { d ->
            val actividad = d["actividad"] as String
            val descripcion = d["descripcion"] as String
            val asignatura = d["asignatura"] as String
            val fecha = d["fecha"] as String
            val tipo = d["tipo"] as String
            val terminado = d["terminado"] as Boolean
            if (Trabajo(
                    actividad = actividad,
                    descripcion = descripcion,
                    asignatura = asignatura,
                    fecha = fecha,
                    tipo = tipo,
                    terminado = terminado
                )== trabajo) {
                updateAlumnos(d.id, alumno)
            }
        }
    }

    private fun setListenerUsuario(usuario:String, uid: String) {
        val user: String
        when (usuario) {
            "profesor" -> user = "Profesor"
            else -> user = "Alumno"
        }
        val docRef = db.collection(user)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen usuario failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToListUsuario(snapshot.documents, uid)
            } else {
                Log.d(Constraints.TAG, "Current data usuario: null")
            }
        }
    }

    private fun documentToListUsuario(
        documents: List<DocumentSnapshot>,
        uid: String
    ) {
        documents.forEach { d ->
            val nombre = d["nombre"] as String
            val email = d["email"] as String
            if (d.id == uid) {
                val tvnombre = nav_view.getHeaderView(0).tvnombreheader
                tvnombre.text = nombre
                val tvemail = nav_view.getHeaderView(0).tvemailheader
                tvemail.text = email
            }
        }
    }

    private fun updateAlumnos(
        id: String,
        alumno: Alumno
    ) {
        db.collection("Alumno").document(alumno.id).collection("Trabajos").document(id)
            .update("terminado", true)
            .addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot alumno successfully updated!") }
            .addOnFailureListener { Log.w(ContentValues.TAG, "Error updating document") }
    }

    fun onClickAlumno(v: View) {
        val alumnoPulsado = v.tag as Alumno
        val edit= utilityShare.edit()
        edit.putString("alumno", Gson().toJson(alumnoPulsado))
        edit.apply()
        val json = utilityShare.getString(alumnoPulsado.id, "nopuntos")
        val puntos = Gson().fromJson(json, Puntos::class.java)

        val json2 = utilityShare.getString("asignatura", "noasignatura")
        val asignatura = Gson().fromJson(json2, Asignatura::class.java)
        val fragment: Fragment? = DetalleAlumnosFragment(alumnoPulsado, puntos, asignatura)
        replaceFragment(fragment!!)
        nav_viewBottom.visibility = View.VISIBLE
    }

    fun onClickAsistencia(v: View) {
        val asistencias = v.tag as ArrayList<String>
        when (v.iviconoasistenciarow.tag) {
            "asiste" -> {
                v.iviconoasistenciarow.setImageResource(R.drawable.falta)
                v.iviconoasistenciarow.tag = "falta"
                v.tvdetallesistenciarow.text = "(Falta)"
            }
            "falta" -> {
                v.iviconoasistenciarow.setImageResource(R.drawable.tarde)
                v.iviconoasistenciarow.tag = "tarde"
                v.tvdetallesistenciarow.text = "(Tarde)"
            }
            "tarde" -> {
                v.iviconoasistenciarow.setImageResource(R.drawable.temprano)
                v.iviconoasistenciarow.tag = "temprano"
                v.tvdetallesistenciarow.text = "(Temprano)"
            }
            "temprano" -> {
                v.iviconoasistenciarow.setImageResource(R.drawable.asiste)
                v.iviconoasistenciarow.tag = "asiste"
                v.tvdetallesistenciarow.text = ""
            }
        }
        asistencias[v.tvnombreasistenciarow.tag as Int] = v.iviconoasistenciarow.tag as String
        v.tag = asistencias
    }

    private fun initShare() {
        loginShare = this.getSharedPreferences("login", Context.MODE_PRIVATE)
        utilityShare = this.getSharedPreferences("asignatura", Context.MODE_PRIVATE)
        alumnosShare = this.getSharedPreferences("alumnos", Context.MODE_PRIVATE)
    }

    private fun comprobarUsuario() {
        when (usuario){
            "profesor" -> {
                val bounds = nav_view.getHeaderView(0).progressBarHeader.indeterminateDrawable.bounds
                nav_view.getHeaderView(0).progressBarHeader.indeterminateDrawable = getDrawable(R.drawable.progress_logoazul)
                nav_view.getHeaderView(0).progressBarHeader.indeterminateDrawable.bounds = bounds
                nav_view.menu.findItem(R.id.nav_clases).isVisible = true
                nav_view.menu.findItem(R.id.nav_horario).isVisible = true
                setInitialFragment("profesor")
            }
            "alumno" -> {
                val bounds = nav_view.getHeaderView(0).progressBarHeader.indeterminateDrawable.bounds
                nav_view.getHeaderView(0).progressBarHeader.indeterminateDrawable = getDrawable(R.drawable.progress_logoverde)
                nav_view.getHeaderView(0).progressBarHeader.indeterminateDrawable.bounds = bounds
                nav_view.menu.findItem(R.id.nav_clases).isVisible = true
                nav_view.menu.findItem(R.id.nav_horario).isVisible = true
                setInitialFragment("alumno")
            }
        }
    }

    private fun cambiarTheme() {
        when (loginShare.getString("usuario", "nousuario")){
            "profesor" -> {
                setTheme(R.style.AppTheme)
            }
            "alumno" -> {
                setTheme(R.style.AppTheme2)
            }
        }
    }

    private fun cerrarSesion() {
        toast("Sesión cerrada").show()
        val edit = loginShare.edit()
        edit.clear()
        edit.apply()
        startActivity(Intent(this, InicioActivity::class.java))
    }

}
