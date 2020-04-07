package cz.muni.pv239.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.Event
import kotlinx.android.synthetic.main.event_preview.view.*
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter: RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private val events: MutableList<Event> = mutableListOf();
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

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
        fun bind(event: Event) {
            view.name_label.text = event.name
            view.date_label.text = dateFormat.format(event.dateTime)
            view.time_label.text = timeFormat.format(event.dateTime)
            view.party_label.text = event.party.name
        }
    }
}