package cz.muni.pv239.android.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.model.Party
import cz.muni.pv239.android.model.User
import cz.muni.pv239.android.repository.UserRepository
import cz.muni.pv239.android.util.PrefManager

import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_groups.view.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GroupsFragment : Fragment() {

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
        private const val TAG = "GroupsFragment"
        @JvmStatic
        fun newInstance() = GroupsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groups, container, false)

        compositeDisposable = CompositeDisposable()

        compositeDisposable?.add(
            userRepository.getUserInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::getInfoSuccess, this::getInfoError)
        )


        view.add_group_button.setOnClickListener{
            val dialogFragment = CreateGroupDialogFragment()
            activity?.supportFragmentManager?.let { fragmentManager ->
                dialogFragment.show(fragmentManager, "CreateGroupFragment")}

        }

        return view
    }

    private fun getInfoSuccess(user: User) {
        Log.i(TAG, "Loaded user info: ${user}.")

        compositeDisposable?.add(
            userRepository.getOwnedParties(user.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::getPartiesSuccess, this::getPartiesError)
        )

    }

    private fun getInfoError(error: Throwable) {
        Log.e(TAG, "Failed to load user info.", error)
    }

    private fun getPartiesSuccess(parties: List<Party>) {
        Log.i(TAG, "Loaded users parties: ${parties}.")
    }

    private fun getPartiesError(error: Throwable) {
        Log.e(TAG, "Failed to load users parties.", error)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }

}