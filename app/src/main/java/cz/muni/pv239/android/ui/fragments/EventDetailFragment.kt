package cz.muni.pv239.android.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import cz.muni.pv239.android.R
import cz.muni.pv239.android.extensions.toReadableDate
import cz.muni.pv239.android.extensions.toReadableTime
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.model.Event
import cz.muni.pv239.android.model.User
import cz.muni.pv239.android.repository.EventRepository
import cz.muni.pv239.android.ui.activities.EventDetailActivity.Companion.JOINED_RESULT
import cz.muni.pv239.android.ui.activities.EventDetailActivity.Companion.LEFT_RESULT
import cz.muni.pv239.android.util.PrefManager
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_event_detail.*
import kotlinx.android.synthetic.main.fragment_event_detail.view.*
import kotlinx.android.synthetic.main.fragment_loading.progressBar
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.function.Consumer


class EventDetailFragment(private val eventId: Long) : Fragment() {

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
        private const val TAG = "EventDetailFragment"

        fun newInstance(eventId : Long) = EventDetailFragment(eventId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        retainInstance = true

        val view = inflater.inflate(R.layout.fragment_event_detail, container, false)

        view.leave_button.setOnClickListener {
            leaveEvent()
        }

        view.join_button.setOnClickListener {
            joinEvent()
        }

        if (savedInstanceState == null) {
            compositeDisposable = CompositeDisposable()
            loadEvent()
        }
        loadEvent()

        return view
    }

    private fun joinEvent() {
        compositeDisposable?.add(eventRepository.joinEvent(eventId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(this::onJoinEventResponse))
    }

    private fun leaveEvent() {
        compositeDisposable?.add(eventRepository.leaveEvent(eventId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(this::onLeaveEventResponse))
    }

    private fun onLeaveEventResponse(response : Response<Void>) {
        if (response.isSuccessful) {
            activity?.setResult(LEFT_RESULT)
            activity?.finish()
        }
    }

    private fun onJoinEventResponse(response : Response<Void>) {
        if (response.isSuccessful) {
            activity?.setResult(JOINED_RESULT)
            activity?.finish()
        }
    }

    private fun loadEvent() {
        compositeDisposable?.add(eventRepository.getEvent(eventId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(this::onEventLoaded, this::onEventLoadError))
    }

    private fun onEventLoaded(event : Event) {
        event_detail.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        if (event.participants.map(User::id).contains(prefManager?.userId)) {
            leave_button.visibility = View.VISIBLE
        } else {
            join_button.visibility = View.VISIBLE
        }
        event_name.text = event.name
        party_name.text = event.party.name
        date_label.text = getString(R.string.event_detail_date, event.dateTime.toReadableDate())
        time_label.text = getString(R.string.event_detail_time, event.dateTime.toReadableTime())
        owner_label.text = getString(R.string.event_detail_owner, event.owner.nick)
        participants_label.text =
            getString(R.string.event_detail_participants, event.participants.size)
        var participans = ""
        for (participant in event.participants) {
            if (participans.isNotEmpty()) {
                participans += "\n"
            }
            participans += participant.nick
        }
        participants_names.text = participans
        description_text.text = event.description
    }

    private fun onEventLoadError(error: Throwable) {
        progressBar.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }
}
