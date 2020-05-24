package net.azarquiel.nexo.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rowavisos.view.*
import kotlinx.android.synthetic.main.rowcanjear.view.*
import net.azarquiel.nexo.Model.*

class AvisosAdapter(
    val context: Context,
    val layout: Int
) : RecyclerView.Adapter<AvisosAdapter.ViewHolder>() {

        private var dataList: List<Aviso> = emptyList()

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

        internal fun setAvisos(avisos: List<Aviso>) {
            this.dataList = avisos
            notifyDataSetChanged()
        }

        class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
            fun bind(
                dataItem: Aviso
            ){
                itemView.tvasignaturaavisorow.text = "AVISO en ${dataItem.asignatura}"
                itemView.tvdescripcionavisorow.text = dataItem.descripcion
                itemView.tag = dataItem

            }
        }
}