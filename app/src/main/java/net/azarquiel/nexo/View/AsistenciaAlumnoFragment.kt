package net.azarquiel.nexo.View


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Constraints
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.nexo.Adapter.AsistenciaAlumnoAdapter
import net.azarquiel.nexo.Model.Asignatura

import net.azarquiel.nexo.R

class AsistenciaAlumnoFragment(asignaturaPulsada: Asignatura) : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapterAsistencias: AsistenciaAlumnoAdapter
    private lateinit var rvasistenciaalumno: RecyclerView
    private var asistencias: ArrayList<String> = ArrayList()
    private val asignaturapulsada = asignaturaPulsada
    private lateinit var uid:String
    private lateinit var tvvacioasistenciaalumno: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_asistencia_alumno, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        rvasistenciaalumno = view.findViewById(R.id.rvasistenciaalumno) as RecyclerView
        tvvacioasistenciaalumno = view.findViewById(R.id.tvvacioasistenciaalumno) as TextView
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            uid = it.uid
            setListenerAsistencia(uid)
        }
        initRV()
    }

    private fun setListenerAsistencia(uid: String) {
        val docRef = db.collection("Alumno")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen asistencia failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToListTrabajo(snapshot.documents, uid)
                if (asistencias.isEmpty()) {
                    tvvacioasistenciaalumno.text = "Sin faltas de asistencia"
                }else {
                    tvvacioasistenciaalumno.text = ""
                    val asistencias2 = ArrayList(asistencias.sortedWith(compareBy { it.substring(0, 11) }))
                    asistencias = asistencias2
                    adapterAsistencias.setAsistencias(asistencias)
                }
            } else {
                Log.d(Constraints.TAG, "Current data asistencia: null")
            }
        }
    }

    private fun documentToListTrabajo(
        documents: List<DocumentSnapshot>,
        uid: String
    ) {
        asistencias.clear()
        documents.forEach { d ->
            val asistencia = d["asistencia"] as ArrayList<String>
            if (d.id == uid) {
                asistencia.forEach {
                    if (!it.contains("asiste") && it.contains(asignaturapulsada.nombre)) {
                        asistencias.add(it)
                    }
                }
            }
        }
    }

    private fun initRV() {
        adapterAsistencias = AsistenciaAlumnoAdapter(activity!!.baseContext, R.layout.rowasistenciaalumno)
        rvasistenciaalumno.adapter = adapterAsistencias
        rvasistenciaalumno.layoutManager = LinearLayoutManager(activity)
    }
}
