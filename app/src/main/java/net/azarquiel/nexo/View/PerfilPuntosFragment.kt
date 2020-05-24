package net.azarquiel.nexo.View


import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.Constraints
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.nexo.Adapter.PerfilPuntosAdapter
import net.azarquiel.nexo.Model.Asignatura
import net.azarquiel.nexo.Model.Canjear
import net.azarquiel.nexo.Model.Puntos

import net.azarquiel.nexo.R
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PerfilPuntosFragment(asignaturaPulsada: Asignatura) : Fragment() {

    private val asignatura = asignaturaPulsada
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var tvpuntuacion: TextView
    private lateinit var adapterPerfilPuntos: PerfilPuntosAdapter
    private var puntos: ArrayList<Puntos> = ArrayList()
    private lateinit var rvperfilpuntos: RecyclerView
    private lateinit var puntosActual: Puntos
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil_puntos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        tvpuntuacion = view.findViewById(R.id.tvpuntuacionperfilpuntos) as TextView
        rvperfilpuntos = view.findViewById(R.id.rvperfilpuntos) as RecyclerView
        val btncanjear = view.findViewById(R.id.btncanjear) as Button
        btncanjear.setOnClickListener { onClickCanjear() }
        val tvpuntosizqperfilpuntos = view.findViewById(R.id.tvpuntosizqperfilpuntos) as TextView
        tvpuntosizqperfilpuntos.text = asignatura.nombre
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            uid = it.uid
            setListenerPuntos(uid)
        }
        initRV()
    }

    private fun onClickCanjear() {
        if (tvpuntuacion.text.toString().toInt() >= 5) {
            val sdf = SimpleDateFormat("dd-MM-YYYY")
            activity!!.alert {
                title = "Canjear 5 puntos por:"
                customView {
                    linearLayout {
                        space()
                        val spinner = spinner {
                            adapter = ArrayAdapter(
                                context, android.R.layout.simple_spinner_dropdown_item,
                                arrayListOf(
                                    "Poder cambiar una pregunta del examen",
                                    "2 minutos con los apuntes en el examen",
                                    "Subir 0.25 puntos a la nota del examen"
                                )
                            )
                        }
                        space()
                        positiveButton("Aceptar") {
                            val puntoCanjeado = Canjear(5, spinner.selectedItem.toString(), sdf.format(Date()), asignatura.codAsignatura)
                            enviarCanjeado(puntoCanjeado)
                            activity!!.toast("Puntos canjeados").show()
                        }
                        negativeButton("Cancelar") {  }
                    }
                }
            }.show()
        }else{
            activity!!.toast("Al menos debes tener 5 puntos para poder canjear").show()
        }
    }

    private fun enviarCanjeado(puntoCanjeado: Canjear) {
        val canjearDocument = db.collection("Alumno").document(uid).collection("Canjear")
        canjearDocument.add(puntoCanjeado)
        puntosActual.puntos = puntosActual.puntos - 5
        val puntosDocument = db.collection("Alumno").document(uid).collection("Puntos").document(asignatura.codAsignatura)
        puntosDocument
            .update("puntos", puntosActual.puntos)
            .addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { Log.w(ContentValues.TAG, "Error updating document") }
        tvpuntuacion.text = puntosActual.puntos.toString()
    }

    private fun setListenerPuntos(uid: String) {
        val docRef = db.collection("Alumno").document(uid).collection("Puntos")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen puntos failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty && snapshot.documents.isNotEmpty()) {
                documentToListPuntos(snapshot.documents)
                if (puntos.isNotEmpty()) {
                    adapterPerfilPuntos.setDesc(puntos[0].puntosdesc)
                }
            } else {
                Log.d(Constraints.TAG, "Current data puntos: null")
            }
        }
    }

    private fun documentToListPuntos(documents: List<DocumentSnapshot>) {
        puntos.clear()
        documents.forEach { d ->
            val puntoss = d["puntos"] as Long
            val puntosdesc = d["puntosdesc"] as ArrayList<String>
            val punto = Puntos(puntos = puntoss, puntosdesc = puntosdesc)
            if (d.id == asignatura.codAsignatura) {
                puntos.add(punto)
                puntosActual = punto
                tvpuntuacion.text = puntoss.toString()
            }
        }
    }

    private fun initRV() {
        adapterPerfilPuntos = PerfilPuntosAdapter(activity!!.baseContext, R.layout.rowperfilpuntos)
        rvperfilpuntos.adapter = adapterPerfilPuntos
        rvperfilpuntos.layoutManager = LinearLayoutManager(activity)
    }
}
