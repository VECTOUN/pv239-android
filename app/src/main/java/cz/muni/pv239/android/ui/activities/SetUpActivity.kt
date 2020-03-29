package cz.muni.pv239.android.ui.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.repository.UserRepository
import cz.muni.pv239.android.ui.fragments.LoadingFragment
import cz.muni.pv239.android.ui.fragments.NickNameFragment
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SetUpActivity : AppCompatActivity() {

    private var compositeDisposable: CompositeDisposable? = null
    private val userRepository: UserRepository by lazy {
        Retrofit.Builder()
            .client(getHttpClient(applicationContext))
            .baseUrl(API_ROOT)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(UserRepository::class.java)
    }

    private val TAG = "SetUpActivity";

    companion object {
        fun newIntent(context: Context) = Intent(context, SetUpActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_container)

        compositeDisposable = CompositeDisposable()

        if (savedInstanceState == null) {
            val loadingFragment = LoadingFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, loadingFragment)
                .commit()

            // check if user exists
            compositeDisposable?.add(userRepository.userExists()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::userExistsSuccess, this::userExistsError))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable?.clear()
    }

    private fun userExistsSuccess(userExists: Boolean) {
        Log.d(TAG, "Response: $userExists")

        // TODO
        // user exists, go to the next activity
        if (userExists) {
            Toast.makeText(applicationContext, "User already exists", Toast.LENGTH_LONG)
                .show()
        } else {
            showNickSelectFragment()
        }
    }

    private fun userExistsError(error: Throwable) {
        Log.e(TAG, "Failed to verify if the user exists.", error)
    }

    private fun showNickSelectFragment() {
        val fragment = NickNameFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

    }
}
