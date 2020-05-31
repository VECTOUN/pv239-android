package cz.muni.pv239.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.Party
import kotlinx.android.synthetic.main.side_group_preview.view.*

class SideGroupAdapter(private val groupId: Long?): RecyclerView.Adapter<SideGroupAdapter.SideGroupViewHolder>() {

    var onItemClick: ((Party) -> Unit)? = null
    val groups: MutableList<Party> = mutableListOf()
    var selected: Int? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SideGroupAdapter.SideGroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.side_group_preview, parent, false)
        return SideGroupViewHolder(view)
    }

    override fun getItemCount(): Int = groups.size

    override fun onBindViewHolder(holder: SideGroupAdapter.SideGroupViewHolder, position: Int) {
        if(selected == null){
            selected = getGroupPosById()
        }
        holder.itemView.group_card.isChecked = selected == position
        holder.bind(groups[position])
    }

    fun submitList(groups: List<Party>) {
        this.groups.clear()
        this.groups.addAll(groups)
        notifyDataSetChanged()
    }

    private fun getGroupPosById(): Int{
        for (i in 0 until groups.size){
            if (groups[i].id == groupId){
                return i
            }
        }
        return 0
    }


    inner class SideGroupViewHolder(private val view: View): RecyclerView.ViewHolder(view){

        init {
            itemView.setOnClickListener{
                onItemClick?.invoke(groups[adapterPosition])
                selected?.let { it1 -> notifyItemChanged(it1) }
                selected = adapterPosition
                selected?.let { it1 -> notifyItemChanged(it1) }
            }
        }

        fun bind(group: Party){
            view.group_label.text = group.name.first().toUpperCase().toString()
        }
    }
}