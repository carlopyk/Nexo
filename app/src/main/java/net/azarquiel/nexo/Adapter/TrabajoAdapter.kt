package net.azarquiel.nexo.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.rowtrabajo.view.*
import net.azarquiel.nexo.Model.Asignatura
import net.azarquiel.nexo.Model.InterfazOnLongClick
import net.azarquiel.nexo.Model.Trabajo
import net.azarquiel.nexo.R

class TrabajoAdapter(
    val context: Context,
    val layout: Int,
    val interfaz: InterfazOnLongClick
) : RecyclerView.Adapter<TrabajoAdapter.ViewHolder>() {

        private var dataList: List<Trabajo> = emptyList()
        private var documentsTrabajo: List<DocumentSnapshot> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val viewlayout = layoutInflater.inflate(layout, parent, false)
            return ViewHolder(viewlayout, context)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataList[position]
            holder.bind(item, interfaz, documentsTrabajo, position)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        internal fun setTrabajos(trabajos: List<Trabajo>) {
            this.dataList = trabajos
            notifyDataSetChanged()
        }

        internal fun setDocuments(documents: List<DocumentSnapshot>) {
            this.documentsTrabajo = documents
            notifyDataSetChanged()
        }

        class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
            fun bind(
                dataItem: Trabajo,
                interfaz: InterfazOnLongClick,
                documentsTrabajo: List<DocumentSnapshot>,
                position: Int
            ){
                itemView.tvtipotrabajorow.text = dataItem.tipo
                itemView.tvdescripciontrabajorow.text = dataItem.actividad
                if (documentsTrabajo.isNotEmpty()) {
                    itemView.tvdescripciontrabajorow.tag = documentsTrabajo[position].id
                }
                itemView.cbtrabajorow.isChecked = dataItem.terminado
                when (itemView.tvtipotrabajorow.text) {
                    "Examen" -> itemView.setBackgroundResource(R.color.colorExamen)
                    "Ejercicio" -> itemView.setBackgroundResource(R.color.colorEjercicio)
                    "Trabajo" -> itemView.setBackgroundResource(R.color.colorTrabajo)
                }
                if (itemView.cbtrabajorow.isChecked) {
                    itemView.setBackgroundResource(R.color.colorTerminado)
                }

                itemView.tag = dataItem
                itemView.setOnLongClickListener{
                    interfaz.onLongClickTrabajo(dataItem)
                }

            }
        }
}