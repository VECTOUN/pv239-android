package cz.muni.pv239.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cz.muni.pv239.android.R
import cz.muni.pv239.android.ui.activities.CreateEventActivity
import kotlinx.android.synthetic.main.fragment_events.view.*

class EventsFragment : Fragment(){

    companion object {
        @JvmStatic
        fun newInstance() = EventsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true
        return inflater.inflate(R.layout.fragment_events, container, false).apply {
            this.create_event_button.setOnClickListener {
                startActivity(CreateEventActivity.newIntent(context))
            }
        }
    }
}