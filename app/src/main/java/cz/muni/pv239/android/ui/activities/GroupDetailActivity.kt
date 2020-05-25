package cz.muni.pv239.android.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cz.muni.pv239.android.R
import cz.muni.pv239.android.ui.fragments.GroupDetailFragment

class GroupDetailActivity : AppCompatActivity() {

    companion object {
        private const val GROUP_ID_ARG = "arg_group_id"

        private const val TAG = "GroupDetailActivity"

        const val INSPECT_GROUP = 1000

        const val LEFT_RESULT = 10000


        fun newIntent(context: Context, groupId: Long)
                = Intent(context, GroupDetailActivity::class.java).apply {
            putExtra(GROUP_ID_ARG, groupId)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        setSupportActionBar(findViewById(R.id.my_toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (savedInstanceState == null) {
            val fragment = GroupDetailFragment.newInstance(intent.getLongExtra(GROUP_ID_ARG, -1))
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }


}