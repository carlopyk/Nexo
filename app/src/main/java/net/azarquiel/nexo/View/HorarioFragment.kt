package net.azarquiel.nexo.View


import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.Constraints
import androidx.core.view.isEmpty
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.nexo.Model.Dia
import net.azarquiel.nexo.R

class HorarioFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private var dias = ArrayList<Dia>()
    private lateinit var lv: LinearLayout
    private lateinit var loginShare: SharedPreferences
    private lateinit var horario: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_horario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        lv = view.findViewById(R.id.lv) as LinearLayout
        val user = FirebaseAuth.getInstance().currentUser
        initShare()
        user?.let {
            setListener(it.uid, horario)
        }
    }

    private fun initShare() {
        loginShare = activity!!.getSharedPreferences("login", Context.MODE_PRIVATE)
        if (loginShare.getString("usuario", "nousuario") == "profesor") {
            horario = "HorarioProfesor"
        }else{
            horario = "HorarioAlumno"
        }
    }

    private fun setListener(uid: String, horario: String) {
        val docRef = db.collection("Horario").document("2020").collection(horario)
            .document(uid).collection("Dias")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(Constraints.TAG, "Listen dias failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToList(snapshot.documents)
            } else {
                Log.d(Constraints.TAG, "Current data dias: null")
            }
        }
    }

    private fun documentToList(documents: List<DocumentSnapshot>) {
        dias.clear()
        documents.forEach { d ->
            val asignaturas = d["asignaturas"] as ArrayList<String>
            val clase = d["clase"] as ArrayList<String>
            val horas = d["horas"] as ArrayList<String>
            dias.add(Dia(asignaturas = asignaturas, clase = clase, horas = horas))
        }
        rellenarHorario(dias)
    }

    private fun rellenarHorario(dias: ArrayList<Dia>) {
        val ordenDias = intArrayOf(1, 2, 3, 0, 4)
        val nombreDias = arrayOf("Lunes", "Martes", "Miercoles", "Jueves", "Viernes")
        for (i in 0 until 7) {
            val lh = LinearLayout(activity)
            lh.orientation = LinearLayout.HORIZONTAL
            for (j in 0 until 6) {
                val lh2 = LinearLayout(activity)
                val textView2 = TextView(activity)
                val textView = TextView(activity)
                textView.textSize = 10F
                textView.gravity = Gravity.CENTER
                textView.setTextColor(Color.BLACK)
                textView.width = 180
                textView.height = 70
                textView.setPadding(10, 10, 10, 10)
                textView.setTypeface(null, Typeface.NORMAL)
                if (i == 0) {
                    if (j != 0) {
                        textView.setTypeface(null, Typeface.BOLD)
                        textView.text = nombreDias[j-1]
                    }
                    textView.setBackgroundColor(resources.getColor(R.color.colorAlumnoClaro))
                    textView.setBackgroundResource(R.drawable.bordertextview)
                }else {
                    if (j == 0) {
                        textView.setTypeface(null, Typeface.BOLD)
                        textView.text = dias[ordenDias[j]].horas[i-1]
                        textView.setBackgroundResource(R.drawable.bordertextview)
                        textView.height = 110
                    }else {
                        lh2.orientation = LinearLayout.VERTICAL
                        textView.text = dias[ordenDias[j-1]].asignaturas[i-1]
                        lh2.addView(textView)
                        textView2.text = dias[ordenDias[j-1]].clase[i-1]
                        textView2.textSize = 9F
                        textView2.gravity = Gravity.CENTER
                        textView2.setTextColor(Color.BLACK)
                        textView2.setPadding(0, 0, 0,10)
                        lh2.addView(textView2)
                        lh2.setBackgroundResource(R.drawable.bordertextview)
                    }
                }
                if (lh2.isEmpty() ) {
                    lh.addView(textView)
                }else {
                    lh.addView(lh2)
                }
                lh.setBackgroundResource(R.drawable.bordertextview)
            }
            lv.addView(lh)
            if (i == 3) {
                val lhR = LinearLayout(activity)
                val textViewR1 = TextView(activity)
                val textViewR2 = TextView(activity)
                textViewR1.setTypeface(null, Typeface.BOLD)
                textViewR1.text = "11:00 - 11:30"
                textViewR1.width = 180
                textViewR1.height = 70
                textViewR1.textSize = 10F
                textViewR1.gravity = Gravity.CENTER
                textViewR1.setTextColor(Color.BLACK)
                textViewR1.setBackgroundResource(R.drawable.bordertextview)
                textViewR1.setPadding(10, 10, 10, 10)
                textViewR2.setTypeface(null, Typeface.BOLD)
                textViewR2.text = "R  E  C  R  E  O"
                textViewR2.width = 900
                textViewR2.height = 70
                textViewR2.textSize = 10F
                textViewR2.gravity = Gravity.CENTER
                textViewR2.setTextColor(Color.BLACK)
                textViewR2.setBackgroundResource(R.drawable.bordertextview)
                textViewR2.setPadding(10, 10, 10, 10)
                lhR.addView(textViewR1)
                lhR.addView(textViewR2)
                lv.addView(lhR)
            }
        }
    }
}
