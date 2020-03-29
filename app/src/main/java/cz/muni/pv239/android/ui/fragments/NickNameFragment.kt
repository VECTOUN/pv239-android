package cz.muni.pv239.android.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.model.User
import cz.muni.pv239.android.repository.UserRepository
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_nick_name.*
import kotlinx.android.synthetic.main.fragment_nick_name.view.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Button
import cz.muni.pv239.android.ui.activities.UserActivity



class NickNameFragment : Fragment() {


    private var compositeDisposable: CompositeDisposable? = null
    private val userRepository: UserRepository by lazy {
        Retrofit.Builder()
            .client(getHttpClient(activity?.applicationContext))
            .baseUrl(API_ROOT)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(UserRepository::class.java)
    }

    companion object {
        private const val TAG = "NickNameFragment"

        @JvmStatic
        fun newInstance() = NickNameFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_nick_name, container, false)

        view.nick_name_edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                confirm_button.isEnabled = s.toString().trim().isNotEmpty()
            }
        })


        compositeDisposable = CompositeDisposable()

        view.confirm_button.setOnClickListener {
            createUser()
        }



        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }

    private fun createUser() {
        Log.d(TAG, "createUserCalled")

        val nick = nick_name_edit.text.toString().trim()

        compositeDisposable?.add(
            userRepository.createUser(User(nick))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::createUserSuccess, this::createUserError))
    }

    private fun createUserSuccess(id: Long) {
        Log.i(TAG, "Created user with id: $id")

        Toast.makeText(activity?.applicationContext, "Created user with id: $id", Toast.LENGTH_LONG)
            .show()
    }

    private fun createUserError(error: Throwable) {
        Log.e(TAG, "Failed to create user.", error)

        Toast.makeText(activity?.applicationContext, "Failed to create user", Toast.LENGTH_LONG)
            .show()
    }
}
