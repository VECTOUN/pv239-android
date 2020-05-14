package cz.muni.pv239.android.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.model.Party
import cz.muni.pv239.android.repository.UserRepository
import cz.muni.pv239.android.ui.adapters.GroupAdapter
import cz.muni.pv239.android.util.PrefManager
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_groups.*
import kotlinx.android.synthetic.main.fragment_groups.view.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GroupsFragment : Fragment() {

    private val adapter = GroupAdapter()
    private val prefManager: PrefManager? by lazy { PrefManager(context) }
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

        retainInstance = true

        if (savedInstanceState == null) {
            compositeDisposable = CompositeDisposable()

            view.swipeContainer.isRefreshing = true
            loadGroups()
        }

        view.swipeContainer.setOnRefreshListener {
            loadGroups()
        }

        view.add_group_button.setOnClickListener{
            val dialogFragment = CreateGroupDialogFragment
                .newInstance(object: CreateGroupDialogFragment.OnGroupCreatedListener{
                    override fun groupCreated() {
                        Snackbar
                            .make(view.add_group_button, R.string.create_group_success, Snackbar.LENGTH_LONG)
                            .show()
                        loadGroups()
                    }

                    override fun groupCreateFailed() {
                        Snackbar
                            .make(view.add_group_button, R.string.create_group_fail, Snackbar.LENGTH_LONG)
                            .show()
                    }


                })

            activity?.supportFragmentManager?.let { fragmentManager ->
                dialogFragment.show(fragmentManager, "CreateGroupFragment")}
        }

        view.join_group_button.setOnClickListener{
            val dialogFragment = JoinGroupDialogFragment
                .newInstance(object : JoinGroupDialogFragment.OnGroupJoinedListener {
                    override fun groupJoined() {
                        Snackbar
                            .make(view.join_group_button, R.string.join_group_success, Snackbar.LENGTH_LONG)
                            .show()
                        loadGroups()
                    }

                    override fun groupJoinFailed() {
                        Snackbar
                            .make(view.join_group_button, R.string.join_group_failed, Snackbar.LENGTH_LONG)
                            .show()
                    }
                })
            activity?.supportFragmentManager?.let {fragmentManager ->
                dialogFragment.show(fragmentManager, "JoinGroupFragment")
            }
        }

        return view.apply{
            group_recycler_view.layoutManager = LinearLayoutManager(context)
            group_recycler_view.adapter = adapter
        }
    }


    private fun loadGroups() {
        compositeDisposable?.add(
            userRepository.getParties(prefManager?.userId!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::getPartiesSuccess, this::getPartiesError)
        )
    }

    private fun getPartiesSuccess(parties: List<Party>) {
        Log.i(TAG, "Loaded users parties: ${parties}.")
        swipeContainer.isRefreshing = false
        adapter.submitList(parties)
    }

    private fun getPartiesError(error: Throwable) {
        Log.e(TAG, "Failed to load users parties.", error)
        swipeContainer.isRefreshing = false
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }

}