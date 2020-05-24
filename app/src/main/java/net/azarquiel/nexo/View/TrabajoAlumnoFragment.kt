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
import net.azarquiel.nexo.Adapter.TrabajoAdapter
import net.azarquiel.nexo.Model.Alumno
import net.azarquiel.nexo.Model.Asignatura
import net.azarquiel.nexo.Model.InterfazOnLongClick
import net.azarquiel.nexo.Model.Trabajo

import net.azarquiel.nexo.R

class TrabajoAlumnoFragment(asignaturaPulsada: Asignatura) : Fragment() , InterfazOnLongClick {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapterTrabajo: TrabajoAdapter
    private lateinit var rvtrabajo: RecyclerView
    private var trabajos: ArrayList<Trabajo> = ArrayList()
    private val asignaturapulsada = asignaturaPulsada
    private lateinit var uid:String
    private lateinit var tvvaciotrabajoalumno: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trabajo_alumno, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        rvtrabajo = view.findViewById(R.id.rvtrabajoalumno) as RecyclerView
        tvvaciotrabajoalumno = view.findViewById(R.id.tvvaciotrabajoalumno) as TextView
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            uid = it.uid
            setListenerTrabajo(uid)
        }
        initRV()
    }

    override fun onLongClickTrabajo(trabajo: Trabajo): Boolean {
        return true
    }

    private fun setListenerTrabajo(uid: String) {
        val docRef = db.collection("Alumno").document(uid).collection("Trabajos")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen trabajos failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToListTrabajo(snapshot.documents)
                if (trabajos.isEmpty()) {
                    tvvaciotrabajoalumno.text = "No tienes ningún trabajo pendiente"
                }else {
                    tvvaciotrabajoalumno.text = ""
                    val trabajos2 =
                        ArrayList(trabajos.sortedWith(compareBy({ it.fecha }, { it.tipo })))
                    trabajos = trabajos2
                    adapterTrabajo.setTrabajos(trabajos)
                }
            } else {
                Log.d(Constraints.TAG, "Current data trabajos: null")
            }
        }
    }

    private fun documentToListTrabajo(documents: List<DocumentSnapshot>) {
        trabajos.clear()
        documents.forEach { d ->
            val actividad = d["actividad"] as String
            val descripcion = d["descripcion"] as String
            val asignatura = d["asignatura"] as String
            val fecha = d["fecha"] as String
            val tipo = d["tipo"] as String
            val terminado = d["terminado"] as Boolean
            if (asignatura == asignaturapulsada.nombre) {
                trabajos.add(
                    Trabajo(
                        actividad = actividad,
                        descripcion = descripcion,
                        asignatura = asignatura,
                        fecha = fecha,
                        tipo = tipo,
                        terminado = terminado
                    )
                )
            }
        }
    }

    private fun initRV() {
        adapterTrabajo = TrabajoAdapter(activity!!.baseContext, R.layout.rowtrabajo, this)
        rvtrabajo.adapter = adapterTrabajo
        rvtrabajo.layoutManager = LinearLayoutManager(activity)
    }
}
