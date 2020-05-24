package net.azarquiel.nexo.View


import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.constraintlayout.widget.Constraints
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.nexo.Adapter.TrabajoAdapter
import net.azarquiel.nexo.Model.*
import net.azarquiel.nexo.R
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

class TrabajoFragment(asignaturaPulsada: Asignatura) : Fragment(), InterfazOnLongClick {

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapterTrabajo: TrabajoAdapter
    private lateinit var rvtrabajo: RecyclerView
    private var trabajos: ArrayList<Trabajo> = ArrayList()
    private lateinit var fab: FloatingActionButton
    private val asignatura = asignaturaPulsada
    private var alumnos: ArrayList<Alumno> = ArrayList()
    private lateinit var uid:String
    private lateinit var documentsTrabajo: List<DocumentSnapshot>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trabajo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        rvtrabajo = view.findViewById(R.id.rvtrabajo) as RecyclerView
        fab = view.findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { addTrabajo() }
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            uid = it.uid
            setListenerTrabajo(uid)
        }
        setListenerAlumno()
        initRV()
    }

    private fun setListenerTrabajo(uid: String) {
        val docRef = db.collection("Profesor").document(uid).collection("Asignatura")
            .document(asignatura.codAsignatura).collection("Trabajos")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen trabajos failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToListTrabajo(snapshot.documents)
                documentsTrabajo = snapshot.documents
                adapterTrabajo.setDocuments(documentsTrabajo)
                adapterTrabajo.setTrabajos(trabajos)
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

    private fun setListenerAlumno() {
        val docRef = db.collection("Alumno")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen alumnos failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty && snapshot.documents.isNotEmpty()) {
                documentToListAlumno(snapshot.documents)
            } else {
                Log.d(Constraints.TAG, "Current data alumnos: null")
            }
        }
    }

    private fun documentToListAlumno(documents: List<DocumentSnapshot>) {
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

    private fun addTrabajo() {
        activity!!.alert {
            title = "Añadir nuevo trabajo"
            lateinit var dp: DatePicker
            customView {
                verticalLayout {
                    textView("Titulo: ")
                    val edactividad = editText()
                    textView("Descripcion: ")
                    val eddescripcion = editText()
                    val spinner = spinner {
                        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, arrayListOf("Ejercicio", "Examen", "Trabajo"))
                    }
                    dp = datePicker {
                        minDate = System.currentTimeMillis()
                    }
                    space()
                    positiveButton("Aceptar") {
                        val trabajo = Trabajo(edactividad.text.toString(), eddescripcion.text.toString(), asignatura.nombre,
                            SimpleDateFormat("dd-MM-YYYY").format(SimpleDateFormat("dd-MM-yyyy").parse("${dp.dayOfMonth}-${dp.month+1}-${dp.year}")),
                            false, spinner.selectedItem.toString())
                        enviarTrabajo(trabajo)
                        activity!!.toast("${trabajo.tipo} añadido").show()
                    }
                    negativeButton("Cancelar") {  }
                }
            }
        }.show()
    }

    private fun enviarTrabajo(trabajo: Trabajo) {
        val trabajoDocument = db.collection("Profesor").document(uid).collection("Asignatura")
            .document(asignatura.codAsignatura).collection("Trabajos")
        trabajoDocument.add(trabajo)

        alumnos.forEach {
            val alumnoTrabajoDocument = db.collection("Alumno").document(it.id).collection("Trabajos")
            alumnoTrabajoDocument.add(trabajo)
        }
    }

    override fun onLongClickTrabajo(trabajo: Trabajo): Boolean {
        activity!!.alert {
            title = "¿Eliminar ${trabajo.tipo}?"
            positiveButton("Aceptar") {
                eliminarTrabajo(trabajo)
                activity!!.toast("${trabajo.tipo} eliminado").show()
            }
            negativeButton("Cancelar") {  }
        }.show()
        return true
    }

    private fun eliminarTrabajo(trabajo: Trabajo) {
        var id = ""
        documentsTrabajo.forEach{ d ->
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
                id = d.id
            }
        }
        db.collection("Profesor").document(uid).collection("Asignatura")
            .document(asignatura.codAsignatura).collection("Trabajos").document(id)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }

        alumnos.forEach {
            setListenerTrabajoAlumno(it, trabajo)
        }
    }

    private fun setListenerTrabajoAlumno(
        alumno: Alumno,
        trabajo: Trabajo
    ) {
        val docRef = db.collection("Alumno").document(alumno.id).collection("Trabajos")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen alumnos failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToListTrabajoAlumno(snapshot.documents, trabajo, alumno)
            } else {
                Log.d(Constraints.TAG, "Current data alumnos: null")
            }
        }
    }

    private fun documentToListTrabajoAlumno(
        documents: List<DocumentSnapshot>,
        trabajo: Trabajo,
        alumno: Alumno
    ) {
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
                    deleteTrabajo(d.id, alumno)
            }
        }
    }

    private fun deleteTrabajo(id: String, alumno: Alumno) {
        db.collection("Alumno").document(alumno.id).collection("Trabajos").document(id)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }

    private fun initRV() {
        adapterTrabajo = TrabajoAdapter(activity!!.baseContext, R.layout.rowtrabajo, this)
        rvtrabajo.adapter = adapterTrabajo
        rvtrabajo.layoutManager = LinearLayoutManager(activity)
    }
}