package net.azarquiel.nexo.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rowalumnos.view.*
import net.azarquiel.nexo.Model.Alumno
import net.azarquiel.nexo.Model.Puntos
import net.azarquiel.nexo.R

class AlumnosAdapter (val context: Context,
                      val layout: Int
    ) : RecyclerView.Adapter<AlumnosAdapter.ViewHolder>() {

        private var dataList: List<Alumno> = emptyList()
        private var dataListPuntos: List<Puntos> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val viewlayout = layoutInflater.inflate(layout, parent, false)
            return ViewHolder(viewlayout, context)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataList[position]
            val itemPuntos = dataListPuntos[position]
            holder.bind(item, itemPuntos)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        internal fun setAlumnos(alumnos: List<Alumno>) {
            this.dataList = alumnos
        }

        internal fun setPuntos(puntos: List<Puntos>) {
            this.dataListPuntos = puntos
            if(puntos.size == dataList.size) {
                notifyDataSetChanged()
            }
        }

        class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
            fun bind(
                dataItem: Alumno,
                dataItemPuntos: Puntos
            ){
                itemView.tvtiporow.text = dataItem.nombre
                itemView.tvpuntosalumnorow.text = dataItemPuntos.puntos.toString()
                when {
                    dataItemPuntos.puntos > 0 -> itemView.tvpuntosalumnorow.setBackgroundResource(R.drawable.circuloverde)
                    dataItemPuntos.puntos < 0 -> itemView.tvpuntosalumnorow.setBackgroundResource(R.drawable.circulorojo)
                    else -> itemView.tvpuntosalumnorow.setBackgroundResource(R.drawable.circulogris)
                }
                itemView.tag = dataItem
            }

        }
    }