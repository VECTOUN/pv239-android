package cz.muni.pv239.android.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.muni.pv239.android.R
import cz.muni.pv239.android.ui.fragments.EventDetailFragment

class EventDetailActivity : AppCompatActivity() {

    companion object {
        private const val EVENT_ID_ARG = "arg_event_id"

        const val INSPECT_EVENT = 1000

        const val JOINED_RESULT = 10000
        const val LEFT_RESULT = 10001

        fun newIntent(context: Context, eventId: Long)
                = Intent(context, EventDetailActivity::class.java).apply {
            putExtra(EVENT_ID_ARG, eventId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        setSupportActionBar(findViewById(R.id.my_toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (savedInstanceState == null) {
            val fragment = EventDetailFragment.newInstance(intent.getLongExtra(EVENT_ID_ARG, -1))
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}
