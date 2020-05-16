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
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.model.Event
import cz.muni.pv239.android.repository.EventRepository
import cz.muni.pv239.android.ui.adapters.EventAdapter
import cz.muni.pv239.android.util.PrefManager
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_groups.*
import kotlinx.android.synthetic.main.fragment_home_page.view.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomePageFragment : Fragment() {

    private val adapter = EventAdapter()
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
        private const val TAG = "HomePageFragment"

        @JvmStatic
        fun newInstance() = HomePageFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true

        compositeDisposable = CompositeDisposable()

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val i = Intent(Intent.ACTION_MAIN)
                    i.addCategory(Intent.CATEGORY_HOME)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(i)
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home_page, container, false).apply {
            recycler_view.layoutManager = LinearLayoutManager(context)

            recycler_view.adapter = adapter
        }
        if (savedInstanceState == null) {
            view.swipeContainer.isRefreshing = true
            loadEvents()
        }

        view.swipeContainer.setOnRefreshListener {
            loadEvents()
        }

        return view
    }

    private fun loadEvents() {
        compositeDisposable?.add(
            eventRepository.getUserEvents(prefManager?.userId!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::loadEventsSuccess, this::loadEventsError)
        )
    }

    private fun loadEventsSuccess(events: List<Event>) {
        Log.i(TAG, "Loaded future events: ${events}.")
        swipeContainer.isRefreshing = false
        adapter.submitList(events)
    }

    private fun loadEventsError(error: Throwable) {
        swipeContainer.isRefreshing = false
        Log.e(TAG, "Failed to load future events.", error)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }
}