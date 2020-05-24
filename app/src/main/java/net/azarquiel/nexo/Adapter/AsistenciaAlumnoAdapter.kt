package net.azarquiel.nexo.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rowasistencia.view.*
import kotlinx.android.synthetic.main.rowasistenciaalumno.view.*
import net.azarquiel.nexo.Model.Asignatura
import net.azarquiel.nexo.R

class AsistenciaAlumnoAdapter(
    val context: Context,
    val layout: Int
) : RecyclerView.Adapter<AsistenciaAlumnoAdapter.ViewHolder>() {

        private var dataList: List<String> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val viewlayout = layoutInflater.inflate(layout, parent, false)
            return ViewHolder(viewlayout, context)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataList[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        internal fun setAsistencias(asistencias: List<String>) {
            this.dataList = asistencias
            notifyDataSetChanged()
        }
        class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
            fun bind(
                dataItem: String
            ){
                val array = dataItem.split(" ")
                itemView.tvdescasistenciaalumnorow.text = array[1]
                itemView.tvfechaasistenciaalumnorow.text = array[0]
                when (array[1]) {
                    "falta" -> {
                        itemView.ivasistenciaalumnorow.setImageResource(R.drawable.falta)
                    }
                    "tarde" -> {
                        itemView.ivasistenciaalumnorow.setImageResource(R.drawable.tarde)
                    }
                    "temprano" -> {
                        itemView.ivasistenciaalumnorow.setImageResource(R.drawable.temprano)
                    }
                }
                itemView.tag = dataItem
            }

        }
    }