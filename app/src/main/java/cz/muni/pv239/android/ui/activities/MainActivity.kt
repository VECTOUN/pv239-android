package cz.muni.pv239.android.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.ToolTipPopup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.FB_EXT_SOURCE
import cz.muni.pv239.android.model.GOOGLE_EXT_SOURCE
import cz.muni.pv239.android.util.PrefManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var callbackManager = CallbackManager.Factory.create()
    private val prefManager: PrefManager? by lazy { PrefManager(applicationContext) }

    private val RC_SIGN_IN = 1001

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedInFb = accessToken != null && !accessToken.isExpired
        if (isLoggedInFb) {
            prefManager?.accessToken = accessToken.token
            prefManager?.usedExtSource = FB_EXT_SOURCE
            onLoginSuccess()
            return
        }

        login_button.setToolTipStyle(ToolTipPopup.Style.BLACK)

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_app_id))
                .requestEmail()
                .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        sign_in_button.setSize(SignInButton.SIZE_WIDE)
        sign_in_button.setOnClickListener {
            val signInIntent: Intent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)}

        googleSignInClient.silentSignIn()
            .addOnCompleteListener(
                this
            ) { task -> handleSignInResult(task) }


        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    Log.d(TAG, "Facebook login success");
                    prefManager?.usedExtSource = FB_EXT_SOURCE
                    prefManager?.accessToken = result?.accessToken?.token
                    onLoginSuccess()
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                }
        })
    }

    fun onLoginSuccess() {
        startActivity(
            SetUpActivity.newIntent(
                this
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount?> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount?>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken

            prefManager?.usedExtSource = GOOGLE_EXT_SOURCE
            prefManager?.accessToken = idToken

            Log.d(TAG, "googleUserToken=$idToken");

            onLoginSuccess()
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }
}
