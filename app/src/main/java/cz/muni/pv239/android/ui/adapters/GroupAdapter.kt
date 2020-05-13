package cz.muni.pv239.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.Party
import kotlinx.android.synthetic.main.group_preview.view.*

class GroupAdapter: RecyclerView.Adapter<GroupAdapter.GroupViewHolder>(){

    private val groups: MutableList<Party> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupAdapter.GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_preview, parent, false)
        return GroupViewHolder(view)
    }

    override fun getItemCount(): Int = groups.size

    override fun onBindViewHolder(holder: GroupAdapter.GroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    fun submitList(groups: List<Party>) {
        this.groups.clear()
        this.groups.addAll(groups)
        notifyDataSetChanged()
    }

    inner class GroupViewHolder(private val view: View): RecyclerView.ViewHolder(view){
        fun bind(group: Party){
            view.group_name_label.text = group.name
            view.group_id_label.text = "#".plus(group.id)
        }
    }
}