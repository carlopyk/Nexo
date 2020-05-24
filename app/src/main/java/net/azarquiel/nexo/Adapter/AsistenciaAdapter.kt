package net.azarquiel.nexo.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rowasistencia.view.*
import net.azarquiel.nexo.Model.Alumno
import net.azarquiel.nexo.Model.Asignatura
import net.azarquiel.nexo.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AsistenciaAdapter(
    val context: Context,
    val layout: Int,
    asistencias: ArrayList<String>,
    asignatura: Asignatura
) : RecyclerView.Adapter<AsistenciaAdapter.ViewHolder>() {

        private var dataList: List<Alumno> = emptyList()
        private val asistencias = asistencias
        private val asignatura = asignatura

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val viewlayout = layoutInflater.inflate(layout, parent, false)
            return ViewHolder(viewlayout, context)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataList[position]
            holder.bind(item, position, asistencias, asignatura, dataList.size)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        internal fun setAlumnos(alumnos: List<Alumno>) {
            this.dataList = alumnos
            notifyDataSetChanged()
        }

        internal fun getAsistencias():ArrayList<String> {
            return asistencias
        }

        class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
            fun bind(
                dataItem: Alumno,
                position: Int,
                asistencias: ArrayList<String>,
                asignatura: Asignatura,
                size: Int
            ){
                val sdf = SimpleDateFormat("dd-MM-YYYY")
                val fecha = sdf.format(Date())
                itemView.tvnombreasistenciarow.text = dataItem.nombre
                itemView.tvnombreasistenciarow.tag = position
                dataItem.asistencia.forEach {
                    val array = it.split(" ")
                    if (array[0] == fecha && array[2] == asignatura.nombre) {
                        itemView.iviconoasistenciarow.tag = array[1]
                        when (array[1]) {
                            "asiste" -> {
                                itemView.iviconoasistenciarow.setImageResource(R.drawable.asiste)
                            }
                            "falta" -> {
                                itemView.iviconoasistenciarow.setImageResource(R.drawable.falta)
                                itemView.tvdetallesistenciarow.text = "(Falta)"
                            }
                            "tarde" -> {
                                itemView.iviconoasistenciarow.setImageResource(R.drawable.tarde)
                                itemView.tvdetallesistenciarow.text = "(Tarde)"
                            }
                            "temprano" -> {
                                itemView.iviconoasistenciarow.setImageResource(R.drawable.temprano)
                                itemView.tvdetallesistenciarow.text = "(Temprano)"
                            }
                        }
                    }
                }
                if (asistencias.size == size) {
                    asistencias[position] = itemView.iviconoasistenciarow.tag as String
                }else {
                    asistencias.add(itemView.iviconoasistenciarow.tag as String)
                }
                itemView.tag = asistencias
            }

        }
    }