package net.azarquiel.nexo.View


import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Constraints
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.nexo.Adapter.AsistenciaAdapter
import net.azarquiel.nexo.Model.Alumno
import net.azarquiel.nexo.Model.Asignatura

import net.azarquiel.nexo.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AsistenciaFragment(asignaturaPulsada: Asignatura) : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapterAsistencia: AsistenciaAdapter
    private var alumnos: ArrayList<Alumno> = ArrayList()
    private var asistencias: ArrayList<String> = ArrayList()
    private lateinit var rvasistencia: RecyclerView
    private val asignatura = asignaturaPulsada
    private lateinit var fecha: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_asistencia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        rvasistencia = view.findViewById(R.id.rvasistencia) as RecyclerView
        initRV()
        setListener()
        val sdf = SimpleDateFormat("dd-MM-YYYY")
        fecha = sdf.format(Date())
        view.findViewById<TextView>(R.id.tvfechaasistencia).text = fecha
        view.findViewById<Button>(R.id.btnasistencia).setOnClickListener { guardarAsistencia() }
    }

    private fun guardarAsistencia() {
        var existe = false
        var num: Int
        var num2 = 0
        asistencias = adapterAsistencia.getAsistencias()
        alumnos.forEachIndexed { i, alumno ->
            num = i
            alumno.asistencia.forEachIndexed { n, it ->
                    if (!existe) {
                        existe = it.contains(fecha)
                        num2 = n
                    }
            }
            val alumnoDocument = db.collection("Alumno").document(alumno.id)
            val asistencianuevo = alumno.asistencia
            if (existe) {
                alumno.asistencia.forEach { asis ->
                    if (asis.contains(fecha)) {
                        asistencianuevo[num2] = "$fecha ${asistencias[num]} ${asignatura.nombre}"
                    }
                }
                alumnoDocument
                    .update("asistencia", asistencianuevo)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { Log.w(TAG, "Error updating document") }
            }else {
                asistencianuevo.add("$fecha ${asistencias[num]} ${asignatura.nombre}")
                alumnoDocument
                    .update("asistencia", asistencianuevo)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { Log.w(TAG, "Error updating document") }
            }
        }
        Toast.makeText(activity, "Cambios guardados", Toast.LENGTH_SHORT).show()
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
                adapterAsistencia.setAlumnos(alumnos)
                val alumnos2 = ArrayList(alumnos.sortedWith(compareBy { it.nombre }))
                alumnos = alumnos2
                asistencias = adapterAsistencia.getAsistencias()
            } else {
                Log.d(Constraints.TAG, "Current data alumnos: null")
            }
        }
    }

    private fun documentToList(documents: List<DocumentSnapshot>) {
        alumnos.clear()
        documents.forEach { d ->
            val nombre = d["nombre"] as String
            val clase = d["clase"] as String
            val asistencia = d["asistencia"] as ArrayList<String>
            val email = d["email"] as String
            val id = d["id"] as String
            if (clase == asignatura.clase) {
                alumnos.add(Alumno(nombre = nombre, clase = clase, asistencia = asistencia, email = email, id = id))
            }
        }
    }

    private fun initRV() {
        adapterAsistencia = AsistenciaAdapter(activity!!.baseContext, R.layout.rowasistencia, asistencias, asignatura)
        rvasistencia.adapter = adapterAsistencia
        rvasistencia.layoutManager = LinearLayoutManager(activity)
    }

}
