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
import net.azarquiel.nexo.Adapter.AvisosAdapter
import net.azarquiel.nexo.Model.Asignatura
import net.azarquiel.nexo.Model.Aviso

import net.azarquiel.nexo.R

class AvisosFragment(asignaturaPulsada: Asignatura) : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapterAviso: AvisosAdapter
    private lateinit var rvavisos: RecyclerView
    private var avisos: ArrayList<Aviso> = ArrayList()
    private lateinit var uid:String
    private lateinit var tvvacioaviso: TextView
    private val asignaturapulsada = asignaturaPulsada

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_avisos, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        rvavisos = view.findViewById(R.id.rvavisos) as RecyclerView
        tvvacioaviso = view.findViewById(R.id.tvvacioaviso) as TextView
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            uid = it.uid
            setListenerAvisos(uid)
        }
        initRV()
    }

    private fun setListenerAvisos(uid: String) {
        val docRef = db.collection("Alumno").document(uid).collection("Aviso")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen avisos failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToListAviso(snapshot.documents)
                if (avisos.isEmpty()) {
                    tvvacioaviso.text = "No tienes ningún aviso"
                }else {
                    tvvacioaviso.text = ""
                    adapterAviso.setAvisos(avisos)
                }
            } else {
                Log.d(Constraints.TAG, "Current data avisos: null")
            }
        }
    }

    private fun documentToListAviso(documents: List<DocumentSnapshot>) {
        avisos.clear()
        documents.forEach { d ->
            val descripcion = d["descripcion"] as String
            val asignatura = d["asignatura"] as String
            val profesor = d["profesor"] as String
            if (asignatura == asignaturapulsada.nombre) {
                avisos.add(
                    Aviso(
                        descripcion = descripcion,
                        asignatura = asignatura,
                        profesor = profesor
                    )
                )
            }
        }
    }

    private fun initRV() {
        adapterAviso = AvisosAdapter(activity!!.baseContext, R.layout.rowavisos)
        rvavisos.adapter = adapterAviso
        rvavisos.layoutManager = LinearLayoutManager(activity)
    }

}
