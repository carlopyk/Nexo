package net.azarquiel.nexo.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rowclases.view.*
import net.azarquiel.nexo.Model.Asignatura

class AsignaturaAdapter (val context: Context,
                         val layout: Int
    ) : RecyclerView.Adapter<AsignaturaAdapter.ViewHolder>() {

        private var dataList: List<Asignatura> = emptyList()

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

        internal fun setAsignaturas(asignaturas: List<Asignatura>) {
            this.dataList = asignaturas
            notifyDataSetChanged()
        }

        class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
            fun bind(dataItem: Asignatura){
                itemView.tvasignaturarow.text = dataItem.nombre
                itemView.tvclaserow.text = dataItem.clase

                itemView.tag = dataItem
            }

        }
    }