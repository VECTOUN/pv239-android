package cz.muni.pv239.android.ui.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        const val CHANNEL_ID = "cz.muni.pv239.android"
        private const val TAG = "MainActivity"

        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val i = Intent(Intent.ACTION_MAIN)
                    i.addCategory(Intent.CATEGORY_HOME)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(i)
                }
            }
        )

        setContentView(R.layout.activity_main)

        createNotificationChannel()


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
            .addOnCompleteListener(this) { task -> handleSignInResult(task) }
//            .addOn(this) { root_view.visibility = VISIBLE }

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
            root_view.visibility = VISIBLE
        }
    }


    // It's safe to call this repeatedly because creating an existing notification channel performs no operation
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
