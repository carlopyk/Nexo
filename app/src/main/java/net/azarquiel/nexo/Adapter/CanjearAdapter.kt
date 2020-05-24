package net.azarquiel.nexo.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rowcanjear.view.*
import net.azarquiel.nexo.Model.*

class CanjearAdapter(
    val context: Context,
    val layout: Int,
    val interfaz: InterfazOnLongClickCanjear
) : RecyclerView.Adapter<CanjearAdapter.ViewHolder>() {

        private var dataList: List<Canjear> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val viewlayout = layoutInflater.inflate(layout, parent, false)
            return ViewHolder(viewlayout, context)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataList[position]
            holder.bind(item, interfaz)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        internal fun setCanjear(canjear: List<Canjear>) {
            this.dataList = canjear
            notifyDataSetChanged()
        }

        class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
            fun bind(
                dataItem: Canjear,
                interfaz: InterfazOnLongClickCanjear
            ){
                itemView.tvcanjearrow.text = dataItem.descripcion
                itemView.tvpuntoscanjearrow.text = dataItem.puntos.toString()
                itemView.tvfechacanjearrow.text = dataItem.fecha
                itemView.tag = dataItem
                itemView.setOnLongClickListener{
                interfaz.onLongClickCanjear(dataItem)
                }

            }
        }
}