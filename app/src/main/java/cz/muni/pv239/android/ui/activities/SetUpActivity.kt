package cz.muni.pv239.android.ui.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import cz.muni.pv239.android.R
import cz.muni.pv239.android.ui.fragments.LoadingFragment
import cz.muni.pv239.android.ui.fragments.NickNameFragment
import kotlinx.android.synthetic.main.activity_single_container.*

class SetUpActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, SetUpActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_container)

        if (savedInstanceState == null) {
            val loadingFragment = LoadingFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, loadingFragment)
                .commit()

            // TODO check if user already exists
            val h = Handler()
            h.postDelayed({
                val fragment = NickNameFragment.newInstance()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            }, 2000)
        }



        // if not, let him choose a nick name
    }
}
