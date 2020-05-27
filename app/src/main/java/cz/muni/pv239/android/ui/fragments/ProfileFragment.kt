package cz.muni.pv239.android.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView

import cz.muni.pv239.android.R
import cz.muni.pv239.android.ui.activities.MainActivity
import cz.muni.pv239.android.util.PrefManager
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ProfileFragment(private val nav: BottomNavigationView) : Fragment() {

    private val prefManager: PrefManager? by lazy { PrefManager(context) }

    companion object {
        private const val TAG = "ProfileFragment"

        @JvmStatic
        fun newInstance(nav : BottomNavigationView) = ProfileFragment(nav)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    nav.selectedItemId = R.id.menu_profile
                }
            }
        )

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false).apply {
            nick_label.text = prefManager?.userName
            sign_out_button.setOnClickListener { logout() }
        }
    }

    private fun logout() {
        when (prefManager?.usedExtSource) {
            "facebook" -> {
                LoginManager.getInstance().logOut()
                startActivity(MainActivity.newIntent(context!!))
            }
            "google" -> {
                GoogleSignIn.getClient(context!!,
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                    .signOut().addOnCompleteListener {
                        startActivity(MainActivity.newIntent(context!!))
                    }
            }
        }
    }
}
