package net.azarquiel.nexo.View


import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Constraints
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.nexo.Adapter.CanjearAdapter
import net.azarquiel.nexo.Model.*

import net.azarquiel.nexo.R
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DetalleAlumnosFragment(
    alumnoPulsado: Alumno,
    puntos: Puntos,
    asignatura: Asignatura
) : Fragment(), InterfazOnLongClickCanjear {
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapterCanjear: CanjearAdapter
    private var canjearal: ArrayList<Canjear> = ArrayList()
    private lateinit var rvcanjear: RecyclerView
    private val alumnoPulsado = alumnoPulsado
    private val puntos = puntos
    private val asignatura = asignatura
    private lateinit var tvpuntuacion: TextView
    private var puntosActuales: Long = 0
    private lateinit var documentsCanjear: List<DocumentSnapshot>
    private var puntosdesc: ArrayList<String> = ArrayList()
    private var unavez = false
    private var desc = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalle_alumnos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        rvcanjear = view.findViewById(R.id.rvcanjear) as RecyclerView
        tvpuntuacion = view.findViewById(R.id.tvpuntuacion) as TextView
        val tvpuntosder = view.findViewById(R.id.tvpuntosder) as TextView
        tvpuntosder.text = alumnoPulsado.nombre
        tvpuntuacion.text = puntos.puntos.toString()
        val ivtareahecha = view.findViewById(R.id.ivtareahecha) as ImageView
        ivtareahecha.setOnClickListener { sumarPunto(ivtareahecha) }
        val ivenequipo = view.findViewById(R.id.ivenequipo) as ImageView
        ivenequipo.setOnClickListener { sumarPunto(ivenequipo) }
        val ivpuntual = view.findViewById(R.id.ivpuntual) as ImageView
        ivpuntual.setOnClickListener { sumarPunto(ivpuntual) }
        val ivparticipativo = view.findViewById(R.id.ivparticipativo) as ImageView
        ivparticipativo.setOnClickListener { sumarPunto(ivparticipativo) }
        val ivtareanohecha = view.findViewById(R.id.ivtareanohecha) as ImageView
        ivtareanohecha.setOnClickListener { restarPunto(ivtareanohecha) }
        val ivimpuntual = view.findViewById(R.id.ivimpuntual) as ImageView
        ivimpuntual.setOnClickListener { restarPunto(ivimpuntual) }
        val ivhablador = view.findViewById(R.id.ivhablador) as ImageView
        ivhablador.setOnClickListener { restarPunto(ivhablador) }
        val ivdistraido = view.findViewById(R.id.ivdistraido) as ImageView
        ivdistraido.setOnClickListener { restarPunto(ivdistraido) }
        initRV()
        setListener()
        setListenerPuntos("")
    }

    private fun setListener() {
        val docRef = db.collection("Alumno").document(alumnoPulsado.id).collection("Canjear")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen canjear failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToList(snapshot.documents)
                documentsCanjear = snapshot.documents
                adapterCanjear.setCanjear(canjearal)
            } else {
                Log.d(Constraints.TAG, "Current data canjear: null")
            }
        }
    }

    private fun documentToList(documents: List<DocumentSnapshot>) {
        canjearal.clear()
        documents.forEach { d ->
            val puntos = d["puntos"] as Long
            val descripcion = d["descripcion"] as String
            val fecha = d["fecha"] as String
            val codAsignatura = d["codAsignatura"] as String
            if (codAsignatura == asignatura.codAsignatura) {
                canjearal.add(Canjear(
                    puntos = puntos,
                    descripcion = descripcion,
                    fecha = fecha,
                    codAsignatura = codAsignatura
                ))
            }
        }
    }

    private fun initRV() {
        adapterCanjear = CanjearAdapter(activity!!.baseContext, R.layout.rowcanjear, this)
        rvcanjear.adapter = adapterCanjear
        rvcanjear.layoutManager = LinearLayoutManager(activity)
    }

    override fun onLongClickCanjear(canjear: Canjear): Boolean {
        activity!!.alert {
            title = "¿Eliminar canjeo?"
            positiveButton("Aceptar") {
                eliminarCanjear(canjear)
                activity!!.baseContext.toast("Canjeo eliminado").show()
            }
            negativeButton("Cancelar") {  }
        }.show()
        return true
    }

    private fun eliminarCanjear(
        canjear: Canjear
    ) {
        var id = ""
        documentsCanjear.forEach{ d ->
            val puntos = d["puntos"] as Long
            val descripcion = d["descripcion"] as String
            val fecha = d["fecha"] as String
            val codAsignatura = d["codAsignatura"] as String
            if(Canjear(
                    puntos = puntos,
                    descripcion = descripcion,
                    fecha = fecha,
                    codAsignatura = codAsignatura
                ) == canjear) {
                id = d.id
            }
        }
        db.collection("Alumno").document(alumnoPulsado.id).collection("Canjear").document(id)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }

    private fun sumarPunto(v: View) {
        unavez = false
        desc = v.tag as String
        val sdf = SimpleDateFormat("dd-MM-YYYY")
        desc = desc + ".+1." + sdf.format(Date())
        val puntoDocument = db.collection("Alumno").document(alumnoPulsado.id).collection("Puntos").document(asignatura.codAsignatura)
        puntoDocument
            .update("puntos", puntosActuales+1)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { Log.w(TAG, "Error updating document") }
        tvpuntuacion.text = puntosActuales.toString()
        setListenerPuntos(desc)
        activity!!.baseContext.toast("+1 punto").show()
    }

    private fun restarPunto(v: View) {
        unavez = false
        desc = v.tag as String
        val sdf = SimpleDateFormat("dd-MM-YYYY")
        desc = desc + ".-1." + sdf.format(Date())
        val puntoDocument = db.collection("Alumno").document(alumnoPulsado.id).collection("Puntos").document(asignatura.codAsignatura)
        puntoDocument
            .update("puntos", puntosActuales-1)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { Log.w(TAG, "Error updating document") }
        tvpuntuacion.text = puntosActuales.toString()
        setListenerPuntos(desc)
        activity!!.baseContext.toast("-1 punto").show()
    }

    private fun setListenerPuntos(desc: String) {
        val docRef = db.collection("Alumno").document(alumnoPulsado.id).collection("Puntos")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen puntos failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                doAsync {
                    documentToListPuntos(snapshot.documents, desc)
                    uiThread{
                        if (desc.isNotEmpty() && !unavez) {
                            unavez = true
                            tvpuntuacion.text = puntosActuales.toString()
                            val puntoDocument = db.collection("Alumno").document(alumnoPulsado.id).collection("Puntos").document(asignatura.codAsignatura)
                            puntoDocument
                                .update("puntosdesc", puntosdesc)
                                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                                .addOnFailureListener { Log.w(TAG, "Error updating document") }
                        }
                    }
                }
            } else {
                Log.d(Constraints.TAG, "Current data puntos: null")
            }
        }
    }

    private fun documentToListPuntos(
        documents: List<DocumentSnapshot>,
        desc: String
    ) {
        documents.forEach { d ->
            val puntos = d["puntos"] as Long
            val puntosdescri = d["puntosdesc"] as ArrayList<String>
            if (d.id == asignatura.codAsignatura) {
                puntosActuales = puntos
                puntosdesc = puntosdescri
                if (desc.isNotEmpty()) {
                    puntosdesc.add(desc)
                }
            }
        }
    }
}
