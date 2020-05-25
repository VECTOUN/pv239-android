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
import cz.muni.pv239.android.repository.EventRepository
import cz.muni.pv239.android.ui.activities.CreateEventActivity
import cz.muni.pv239.android.ui.activities.EventDetailActivity
import cz.muni.pv239.android.ui.activities.EventDetailActivity.Companion.INSPECT_EVENT
import cz.muni.pv239.android.ui.adapters.EventAdapter
import cz.muni.pv239.android.util.PrefManager
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_events.view.*
import kotlinx.android.synthetic.main.fragment_groups.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EventsFragment(private val nav: BottomNavigationView) : Fragment(){

    private var adapter : EventAdapter? = null
    private var compositeDisposable: CompositeDisposable? = null
    private val prefManager: PrefManager? by lazy { PrefManager(context) }
    private val eventRepository: EventRepository by lazy {
        Retrofit.Builder()
            .client(getHttpClient(activity?.applicationContext))
            .baseUrl(API_ROOT)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(EventRepository::class.java)
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

            adapter = EventAdapter(prefManager?.userId!!)

            adapter?.onItemClick = {event ->
                startActivityForResult(EventDetailActivity.newIntent(context, event.id!!), INSPECT_EVENT)
            }

            recycler_view.adapter = adapter
        }

        if (savedInstanceState == null) {
            view.swipeContainer.isRefreshing = true
            loadEvents()
        }

        view.swipeContainer.setOnRefreshListener {
            loadEvents()
        }

        loadEvents()

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
        adapter?.submitList(events)
    }

    private fun loadEventsError(error: Throwable) {
        swipeContainer.isRefreshing = false
        Log.e(TAG, "Failed to load future events.", error)
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
}