package cz.muni.pv239.android.ui.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.Party
import cz.muni.pv239.android.ui.fragments.CreateEventFormFragment

class CreateEventActivity : AppCompatActivity() {

    companion object {
        private const val PARTY_ID = "arg_party_id"
        private const val PARTY_NAME = "arg_party_name"

        const val CREATE_EVENT = 1002

        fun newIntent(context: Context, party: Party)
                = Intent(context, CreateEventActivity::class.java).apply {
            putExtra(PARTY_NAME, party.name)
            putExtra(PARTY_ID, party.id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Create event"

        if (savedInstanceState == null) {
            val fragment = CreateEventFormFragment
                .newInstance(intent.getLongExtra(PARTY_ID, -1), intent.getStringExtra(PARTY_NAME)!!)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}
