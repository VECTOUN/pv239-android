package cz.muni.pv239.android.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.model.User
import cz.muni.pv239.android.repository.UserRepository
import cz.muni.pv239.android.ui.fragments.EventsFragment
import cz.muni.pv239.android.ui.fragments.GroupsFragment
import cz.muni.pv239.android.ui.fragments.HomePageFragment
import cz.muni.pv239.android.ui.fragments.ProfileFragment
import cz.muni.pv239.android.util.PrefManager
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class UserActivity : AppCompatActivity() {

    private var compositeDisposable: CompositeDisposable? = null
    private val prefManager: PrefManager? by lazy { PrefManager(applicationContext) }
    private val userRepository: UserRepository by lazy {
        Retrofit.Builder()
            .client(getHttpClient(applicationContext))
            .baseUrl(API_ROOT)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(UserRepository::class.java)
    }

    companion object {
        private const val TAG = "UserActivity"
        fun newIntent(context: Context) = Intent(context, UserActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)

        if (savedInstanceState == null) {
            compositeDisposable = CompositeDisposable()
            compositeDisposable?.add(
                userRepository.getUserInfo()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(this::userInfoSuccess, this::userInfoError)
            )
        } else {
            initNav()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable?.clear()
    }

    private fun showFragments() {
        val homePageFragment = HomePageFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_navigation_container, homePageFragment)
            .commit()

        initNav()
    }

    private fun initNav() {
        val bottomNavigation : BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener {
            var selectedFragment : Fragment = HomePageFragment()
            when(it.itemId) {
                R.id.menu_home -> {selectedFragment = HomePageFragment.newInstance()}

                R.id.menu_groups -> {selectedFragment = GroupsFragment.newInstance(bottomNavigation)}

                R.id.menu_events -> {selectedFragment = EventsFragment.newInstance(bottomNavigation)}

                R.id.menu_profile -> {selectedFragment = ProfileFragment.newInstance(bottomNavigation)}
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_navigation_container, selectedFragment)
                .commit()

            true
        }
    }

    private fun userInfoSuccess(user: User) {
        prefManager?.userId = user.id!!
        prefManager?.userName = user.nick
        Log.i(TAG, "Loaded user info.")

        showFragments()
    }

    private fun userInfoError(error: Throwable) {
        Log.e(TAG, "Failed to load user info.", error)
    }
}