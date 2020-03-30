package cz.muni.pv239.android.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import cz.muni.pv239.android.R
import cz.muni.pv239.android.ui.fragments.CreateGroupFragment
import cz.muni.pv239.android.ui.fragments.FindGroupFragment
import cz.muni.pv239.android.ui.fragments.GroupsFragment
import cz.muni.pv239.android.ui.fragments.HomePageFragment


class UserActivity : AppCompatActivity() {

    private val TAG = "HomeActivity"

    companion object {
        fun newIntent(context: Context) = Intent(context, UserActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)

        val homePageFragment = HomePageFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_navigation_container, homePageFragment)
            .commit()

        val bottomNavigation : BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener {
            var selectedFragment : Fragment = HomePageFragment()
            when(it.itemId) {
                R.id.menu_home -> {selectedFragment = HomePageFragment()}

                R.id.menu_groups -> {selectedFragment = GroupsFragment()}

                R.id.menu_create -> {selectedFragment = CreateGroupFragment()}

                R.id.menu_find -> {selectedFragment = FindGroupFragment()}
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_navigation_container, selectedFragment)
                .commit()

            true
        }
    }
}