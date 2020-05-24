package net.azarquiel.nexo.View


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.Constraints
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import net.azarquiel.nexo.Adapter.AlumnosAdapter
import net.azarquiel.nexo.Model.*

import net.azarquiel.nexo.R
import org.jetbrains.anko.*
import org.jetbrains.anko.collections.forEachWithIndex

class AlumnosFragment(asignaturaPulsada: Asignatura) : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapterAlumnos: AlumnosAdapter
    private lateinit var alumnosShare: SharedPreferences
    private lateinit var utilityShare: SharedPreferences
    private var alumnos: ArrayList<Alumno> = ArrayList()
    private var puntos: ArrayList<Puntos> = ArrayList()
    private lateinit var correos: Array<String>
    private lateinit var rvalumnos: RecyclerView
    private val asignatura = asignaturaPulsada
    private lateinit var fabCorreo: FloatingActionButton
    private lateinit var fabAvisos: FloatingActionButton
    private lateinit var fabAlumno: FloatingActionsMenu
    private lateinit var idProfesor: String
    private var profesor = Profesor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alumnos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        initShare()
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            idProfesor = it.uid
            setListenerProfesor()
        }
        rvalumnos = view.findViewById(R.id.rvalumnos) as RecyclerView
        fabCorreo = view.findViewById(R.id.fab1) as FloatingActionButton
        fabAvisos = view.findViewById(R.id.fab2) as FloatingActionButton
        fabAlumno = view.findViewById(R.id.fabalumno) as FloatingActionsMenu
        fabCorreo.setOnClickListener { onClickCorreo() }
        fabAvisos.setOnClickListener { onClickAviso() }
        initRV()
        setListener()
    }

    private fun setListener() {
        val docRef = db.collection("Alumno")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen alumnos failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToList(snapshot.documents)
                adapterAlumnos.setAlumnos(alumnos)
            } else {
                Log.d(Constraints.TAG, "Current data alumnos: null")
            }
        }
    }

    private fun setListenerPuntos(id: String) {
        val docRef = db.collection("Alumno").document(id).collection("Puntos")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen puntos failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToListPuntos(snapshot.documents, id)
                adapterAlumnos.setPuntos(puntos)
            } else {
                Log.d(Constraints.TAG, "Current data puntos: null")
            }
        }
    }

    private fun documentToListPuntos(
        documents: List<DocumentSnapshot>,
        id: String
    ) {
        documents.forEach { d ->
            val puntoss = d["puntos"] as Long
            val puntosdesc = d["puntosdesc"] as ArrayList<String>
            val punto = Puntos(puntos = puntoss, puntosdesc = puntosdesc)
            if (d.id == asignatura.codAsignatura) {
                puntos.add(punto)
                val edit= utilityShare.edit()
                edit.putString(id, Gson().toJson(punto))
                edit.apply()
            }
        }
    }

    private fun documentToList(documents: List<DocumentSnapshot>) {
        var contador = 0
        alumnos.clear()
        documents.forEach { d ->
            val nombre = d["nombre"] as String
            val clase = d["clase"] as String
            val asistencia = d["asistencia"] as ArrayList<String>
            val email = d["email"] as String
            val id = d["id"] as String
            if (clase == asignatura.clase) {
                val alumno = Alumno(nombre = nombre, clase = clase, asistencia = asistencia, email = email, id = id)
                alumnos.add(alumno)
                setListenerPuntos(alumno.id)
                val edit= alumnosShare.edit()
                edit.putString("$contador", Gson().toJson(alumno))
                edit.apply()
                contador++
            }
        }
    }

    private fun initShare() {
        alumnosShare = activity!!.getSharedPreferences("alumnos", Context.MODE_PRIVATE)
        utilityShare = activity!!.getSharedPreferences("asignatura", Context.MODE_PRIVATE)
    }

    private fun initRV() {
        adapterAlumnos = AlumnosAdapter(activity!!.baseContext, R.layout.rowalumnos)
        rvalumnos.adapter = adapterAlumnos
        rvalumnos.layoutManager = GridLayoutManager(activity, 3)
    }

    private fun onClickAviso() {
        fabAlumno.collapse()
        val alumnosspinner = ArrayList<String> ()
        alumnosspinner.add("Toda la clase")
        alumnos.forEach { alumnosspinner.add(it.nombre) }
        activity!!.alert {
            title = "Aviso"
            customView {
                verticalLayout {
                    textView("Descripcion: ")
                    val eddescripcion = editText()
                    val spinner = spinner {
                        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, alumnosspinner)
                    }
                    space()
                    positiveButton("Aceptar") {
                        enviarAviso(eddescripcion.text.toString(), spinner.selectedItem.toString())
                        activity!!.baseContext.toast("Aviso enviado").show()
                    }
                    negativeButton("Cancelar") {  }
                }
            }
        }.show()
    }

    private fun enviarAviso(descripcion: String, spinner: String) {
        if (spinner != "Toda la clase") {
            alumnos.forEach {
                if (it.nombre == spinner) {
                    val avisoDocument = db.collection("Alumno").document(it.id).collection("Aviso")
                    avisoDocument.add(Aviso(descripcion, asignatura.nombre, profesor.nombre))
                }
            }
        }else{
            alumnos.forEach {
                val avisoDocument = db.collection("Alumno").document(it.id).collection("Aviso")
                avisoDocument.add(Aviso(descripcion, asignatura.nombre, profesor.nombre))
            }
        }
    }

    private fun setListenerProfesor() {
        val docRef = db.collection("Profesor")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen profesor failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToListProfesor(snapshot.documents)
            } else {
                Log.d(Constraints.TAG, "Current data profesor: null")
            }
        }
    }

    private fun documentToListProfesor(documents: List<DocumentSnapshot>) {
        documents.forEach { d ->
            if (d.id == idProfesor) {
                val nombre = d["nombre"] as String
                val email = d["email"] as String
                profesor = Profesor(nombre = nombre, email = email)
            }
        }
    }

    private fun onClickCorreo() {
        fabAlumno.collapse()
        correos = Array(alumnos.size) { "it = $it" }
        alumnos.forEachWithIndex { i, alumno ->
            correos[i] = alumno.email
        }
        val emailIntent = Intent (Intent.ACTION_SEND)
        activity!!.intent.data = Uri.parse("mailto:")
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, correos)
        startActivity(Intent.createChooser(emailIntent, "Elige una app para enviar el correo"))
    }
}
