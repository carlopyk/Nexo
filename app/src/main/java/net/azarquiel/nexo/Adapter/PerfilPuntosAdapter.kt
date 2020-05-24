package net.azarquiel.nexo.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rowperfilpuntos.view.*
import net.azarquiel.nexo.R
import org.jetbrains.anko.textColorResource

class PerfilPuntosAdapter(
    val context: Context,
    val layout: Int
) : RecyclerView.Adapter<PerfilPuntosAdapter.ViewHolder>() {

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

        internal fun setDesc(descAL: List<String>) {
            this.dataList = descAL
            notifyDataSetChanged()
        }

        class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
            fun bind(
                dataItem: String
            ){
                val desc = dataItem.split(".")
                itemView.tvperfilrow.text = desc[0]
                itemView.tvpuntosperfilrow.text = desc[1]
                if (desc[1].contains("+")) {
                    itemView.tvpuntosperfilrow.textColorResource = R.color.colorAlumno
                    itemView.setBackgroundResource(R.color.colorAlumnoClaro)
                }else {
                    itemView.tvpuntosperfilrow.textColorResource = R.color.colorRojo
                    itemView.setBackgroundResource(R.color.colorExamen)
                }
                itemView.tvfechaperfilrow.text = desc[2]
                itemView.tag = dataItem

            }
        }
}