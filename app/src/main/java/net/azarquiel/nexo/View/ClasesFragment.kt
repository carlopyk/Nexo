package net.azarquiel.nexo.View


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Constraints
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.nexo.Adapter.AsignaturaAdapter
import net.azarquiel.nexo.Model.Asignatura

import net.azarquiel.nexo.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class ClasesFragment(usuario: String) : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapterAsignaturas: AsignaturaAdapter
    private var asignaturas: ArrayList<Asignatura> = ArrayList()
    private lateinit var rvclases: RecyclerView
    private val usuario = usuario
    private lateinit var uid: String
    private lateinit var clase: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_clases, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        rvclases = view.findViewById(R.id.rvclases) as RecyclerView
        initRV()
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            uid = it.uid
        }
        setListenerAlumno()
    }

    private fun setListener(uid: String) {
        val docRef = db.collection("Profesor").document(uid).collection("Asignatura")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen asignaturas failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToList(snapshot.documents)
                val asignaturas2 = ArrayList(asignaturas.sortedWith(compareBy { it.nombre }))
                asignaturas = asignaturas2
                adapterAsignaturas.setAsignaturas(asignaturas)
            } else {
                Log.d(Constraints.TAG, "Current data asignaturas: null")
            }
        }
    }

    private fun documentToList(documents: List<DocumentSnapshot>) {
        asignaturas.clear()
        documents.forEach { d ->
            val nombre = d["nombre"] as String
            val clase = d["clase"] as String
            val codAsignatura = d["codAsignatura"] as String

            asignaturas.add(Asignatura(nombre = nombre,clase = clase, codAsignatura = codAsignatura))
        }
    }

    private fun setListenerAsignatura() {
        val curso: String
        when(clase[0]) {
            '1' -> curso = "1 ESO"
            '2' -> curso = "2 ESO"
            '3' -> curso = "3 ESO"
            else -> curso = "4 ESO"
        }
        val docRef = db.collection("Curso").document(curso).collection("Clase").document(clase).collection("Asignaturas")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen asignaturas failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToList(snapshot.documents)
                val asignaturas2 = ArrayList(asignaturas.sortedWith(compareBy { it.nombre }))
                asignaturas = asignaturas2
                adapterAsignaturas.setAsignaturas(asignaturas)
            } else {
                Log.d(Constraints.TAG, "Current data asignaturas: null")
            }
        }
    }

    private fun setListenerAlumno() {
        val docRef = db.collection("Alumno")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen alumno failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToListAlumno(snapshot.documents)
            } else {
                Log.d(Constraints.TAG, "Current data alumno: null")
            }
        }
    }

    private fun documentToListAlumno(documents: List<DocumentSnapshot>) {
        doAsync {
            asignaturas.clear()
            documents.forEach { d ->
                val clasealumno = d["clase"] as String
                if (d.id == uid) {
                    clase = clasealumno
                }
            }
            uiThread {
                comprobarUsuario(usuario)
            }
        }

    }

    private fun initRV() {
        adapterAsignaturas = AsignaturaAdapter(activity!!.baseContext, R.layout.rowclases)
        rvclases.adapter = adapterAsignaturas
        rvclases.layoutManager = LinearLayoutManager(activity)
    }

    private fun comprobarUsuario(usuario: String) {
        when (usuario){
            "profesor" -> {
                setListener(uid)
            }
            "alumno" -> {
                setListenerAsignatura()
            }
        }
    }
}
