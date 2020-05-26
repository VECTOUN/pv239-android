package cz.muni.pv239.android.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.model.Party
import cz.muni.pv239.android.repository.UserRepository
import cz.muni.pv239.android.ui.activities.GroupDetailActivity
import cz.muni.pv239.android.ui.activities.GroupDetailActivity.Companion.INSPECT_GROUP
import cz.muni.pv239.android.ui.adapters.GroupAdapter
import cz.muni.pv239.android.util.PrefManager
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_groups.*
import kotlinx.android.synthetic.main.fragment_groups.swipeContainer
import kotlinx.android.synthetic.main.fragment_groups.view.*
import kotlinx.android.synthetic.main.fragment_groups.view.swipeContainer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GroupsFragment(private val nav: BottomNavigationView) : Fragment() {

    private var adapter: GroupAdapter? = null
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
        fun newInstance(nav : BottomNavigationView) = GroupsFragment(nav)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    nav.selectedItemId = R.id.menu_home
                }
            }
        )

        retainInstance = true
        val view = inflater.inflate(R.layout.fragment_groups, container, false).apply{
            group_recycler_view.layoutManager = LinearLayoutManager(context)

            adapter = GroupAdapter()

            adapter?.onItemClick = { party ->
                startActivityForResult(
                    GroupDetailActivity.newIntent(context, party.id!!), INSPECT_GROUP)
            }

            group_recycler_view.adapter = adapter
        }

        view.swipeContainer.setOnRefreshListener {
            loadGroups()
        }

        if (savedInstanceState == null) {
            compositeDisposable = CompositeDisposable()

            view.swipeContainer.isRefreshing = true
        }

        loadGroups()

        view.add_group_button.setOnClickListener{
            val dialogFragment = CreateGroupDialogFragment
                .newInstance(object: CreateGroupDialogFragment.OnGroupCreatedListener{
                    override fun groupCreated(id: Long) {
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

        return view
    }


    private fun loadGroups() {
        compositeDisposable?.add(
            userRepository.getMemberParties(prefManager?.userId!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::getPartiesSuccess, this::getPartiesError)
        )
    }

    private fun getPartiesSuccess(parties: List<Party>) {
        Log.i(TAG, "Loaded users parties: ${parties}.")
        swipeContainer.isRefreshing = false
        adapter?.submitList(parties)
        if (parties.isEmpty()) {
            no_groups_label.visibility = View.VISIBLE
        } else {
            no_groups_label.visibility = View.GONE
        }
    }

    private fun getPartiesError(error: Throwable) {
        Log.e(TAG, "Failed to load users parties.", error)
        swipeContainer.isRefreshing = false
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == INSPECT_GROUP) {
            when (resultCode) {
                GroupDetailActivity.LEFT_RESULT -> {
                    Snackbar
                        .make(view!!, R.string.group_left, Snackbar.LENGTH_SHORT)
                        .show()
                    swipeContainer.isRefreshing = true
                    loadGroups()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }
}