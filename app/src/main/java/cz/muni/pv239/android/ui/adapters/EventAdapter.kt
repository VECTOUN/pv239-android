package cz.muni.pv239.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.muni.pv239.android.R
import cz.muni.pv239.android.extensions.toReadableDate
import cz.muni.pv239.android.extensions.toReadableTime
import cz.muni.pv239.android.model.Event
import cz.muni.pv239.android.model.User
import cz.muni.pv239.android.util.PrefManager
import kotlinx.android.synthetic.main.event_preview.view.*
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(val userId: Long): RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    var onItemClick: ((Event) -> Unit)? = null
    private val events: MutableList<Event> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_preview, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    fun submitList(events: List<Event>) {
        this.events.clear()
        this.events.addAll(events)
        notifyDataSetChanged()
    }

    inner class EventViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(events[adapterPosition])
            }
        }

        fun bind(event: Event) {
            view.name_label.text = event.name
            view.date_label.text = event.dateTime.toReadableDate()
            view.time_label.text = event.dateTime.toReadableTime()
            view.party_label.text = event.party.name
            if (event.participants.map { u -> u.id }.contains(userId)) {
                view.attending_icon.visibility = View.VISIBLE
            } else {
                view.attending_icon.visibility = View.GONE
            }
        }
    }
}