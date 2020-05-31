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
import cz.muni.pv239.android.model.Event
import cz.muni.pv239.android.model.Party
import cz.muni.pv239.android.repository.EventRepository
import cz.muni.pv239.android.repository.UserRepository
import cz.muni.pv239.android.ui.activities.EventDetailActivity
import cz.muni.pv239.android.ui.activities.EventDetailActivity.Companion.INSPECT_EVENT
import cz.muni.pv239.android.ui.adapters.EventAdapter
import cz.muni.pv239.android.ui.adapters.SideGroupAdapter
import cz.muni.pv239.android.util.PrefManager
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_events.*
import kotlinx.android.synthetic.main.fragment_events.view.*
import kotlinx.android.synthetic.main.fragment_groups.swipeContainer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EventsFragment(private val nav: BottomNavigationView) : Fragment(){

    private var eventAdapter : EventAdapter? = null
    private var sideGroupAdapter: SideGroupAdapter? = null
    private var compositeDisposable: CompositeDisposable? = null
    private var selectedGroup: Party? = null
    private val prefManager: PrefManager? by lazy { PrefManager(context) }
    private val eventRepository: EventRepository by lazy {
        Retrofit.Builder()
            .client(getHttpClient(activity?.applicationContext))
            .baseUrl(API_ROOT)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(EventRepository::class.java)
    }

    private val userRepository: UserRepository by lazy {
        Retrofit.Builder()
            .client(getHttpClient(activity?.applicationContext))
            .baseUrl(API_ROOT)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(UserRepository::class.java)
    }

    companion object {
        private const val TAG = "EventsFragment"

        @JvmStatic
        fun newInstance(nav : BottomNavigationView) = EventsFragment(nav)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeDisposable = CompositeDisposable()
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
        val view = inflater.inflate(R.layout.fragment_events, container, false).apply {
            recycler_view.layoutManager = LinearLayoutManager(context)
            side_recycler_view.layoutManager = LinearLayoutManager(context)

            eventAdapter = EventAdapter(prefManager?.userId!!)
            sideGroupAdapter = SideGroupAdapter(selectedGroup?.id)


            eventAdapter?.onItemClick = { event ->
                startActivityForResult(EventDetailActivity.newIntent(context, event.id!!), INSPECT_EVENT)
            }

            sideGroupAdapter?.onItemClick = { party ->
                group_name_label.text = party.name
                selectedGroup = party
                loadEvents()
            }

            recycler_view.adapter = eventAdapter
            side_recycler_view.adapter = sideGroupAdapter
        }

        if (savedInstanceState == null) {
            view.swipeContainer.isRefreshing = true
        }

        view.swipeContainer.setOnRefreshListener {
            loadGroups()
        }


        loadGroups()

        return view
    }

    private fun loadEvents() {
        compositeDisposable?.add(
            eventRepository.getFutureEvents(prefManager?.userId!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::loadEventsSuccess, this::loadEventsError)
        )
    }

    private fun loadEventsSuccess(events: List<Event>) {
        Log.i(TAG, "Loaded future events: ${events}.")
        swipeContainer.isRefreshing = false
        val eventsToShow: List<Event> = getEventsToShow(selectedGroup?.id, events)
        eventAdapter?.submitList(eventsToShow)

        if (eventsToShow.isEmpty()) {
            no_events_label.visibility = View.VISIBLE
        } else {
            no_events_label.visibility = View.GONE
        }
    }

    private fun loadEventsError(error: Throwable) {
        swipeContainer.isRefreshing = false
        Log.e(TAG, "Failed to load future events.", error)
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
        Log.i(TAG, "Loaded user parties: ${parties}.")
        sideGroupAdapter?.submitList(parties)
        if (parties.isNotEmpty() && selectedGroup == null){
            this.selectedGroup = parties[0]
        }

        if(parties.isEmpty()){
            group_name_label.visibility = View.GONE
        }

        group_name_label?.text = selectedGroup?.name
        loadEvents()
    }

    private fun getPartiesError(error: Throwable) {
        Log.e(TAG, "Failed to load users parties.", error)
        swipeContainer.isRefreshing = false
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == INSPECT_EVENT) {
            when (resultCode) {
                EventDetailActivity.JOINED_RESULT -> {
                    Snackbar
                        .make(view!!, R.string.event_joined, Snackbar.LENGTH_SHORT)
                        .show()
                    swipeContainer.isRefreshing = true
                    loadEvents()
                }
                EventDetailActivity.LEFT_RESULT -> {
                    Snackbar
                        .make(view!!, R.string.event_left, Snackbar.LENGTH_SHORT)
                        .show()
                    swipeContainer.isRefreshing = true
                    loadEvents()
                }
            }
        }
    }

    private fun getEventsToShow(groupId: Long?, events: List<Event>): List<Event>{
        val eventsToShow: MutableList<Event> = mutableListOf()
        if(groupId == null){
            return listOf()
        }
        for (event in events){
            if (event.party.id == groupId){
                eventsToShow?.add(event)
            }
        }
        return eventsToShow
    }

}