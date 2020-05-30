package cz.muni.pv239.android.ui.fragments

import android.app.Activity
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import cz.muni.pv239.android.R
import cz.muni.pv239.android.extensions.toISO
import cz.muni.pv239.android.extensions.toPresentableDate
import cz.muni.pv239.android.extensions.toPresentableTime
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.model.CreateEventData
import cz.muni.pv239.android.model.Party
import cz.muni.pv239.android.repository.EventRepository
import cz.muni.pv239.android.util.NotifyWorker
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.create_event_form_items.view.*
import kotlinx.android.synthetic.main.fragment_create_event_form.*
import kotlinx.android.synthetic.main.fragment_create_event_form.view.*
import kotlinx.android.synthetic.main.fragment_create_event_form.view.confirm_button
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit


class CreateEventFormFragment(private val partyId: Long, private val partyName: String) : Fragment() {

    private val selectedDateTime = Calendar.getInstance()

    private var dateSelected = false
    private var timeSelected = false
    private var compositeDisposable: CompositeDisposable? = null

    private val eventRepository: EventRepository by lazy {
        Retrofit.Builder()
            .client(getHttpClient(activity?.applicationContext))
            .baseUrl(API_ROOT)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(EventRepository::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        compositeDisposable = CompositeDisposable()

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_event_form, container, false)

        view.party_label.text = getString(R.string.create_event_party_label, partyName)

        view.date_layout.setEndIconOnClickListener {

            val builder = MaterialDatePicker.Builder.datePicker()

            val picker = builder.build()
            picker.addOnPositiveButtonClickListener {
                val newDate = Calendar.getInstance()
                newDate.timeInMillis = it

                selectedDateTime.run {
                    newDate.timeInMillis = it

                    set(Calendar.DAY_OF_MONTH, newDate.get(Calendar.DAY_OF_MONTH))
                    set(Calendar.MONTH, newDate.get(Calendar.MONTH))
                    set(Calendar.YEAR, newDate.get(Calendar.YEAR))
                }

                dateSelected = true
                view.date_view.setText(selectedDateTime.timeInMillis.toPresentableDate())
                checkButton()
            }

            fragmentManager?.let {fm ->
                picker.show(fm, picker.toString())
            }
        }

        view.name_edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkButton()
            }
        })

        view.confirm_button.setOnClickListener {
            view.confirm_button.isEnabled = false
            compositeDisposable?.add(eventRepository.createEvent(parseEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::partyCreatedSuccess, this::partyCreatedError)
            )
        }

        view.time_layout.setEndIconOnClickListener {
            TimePickerDialog(
                context,
                TimePickerDialog.OnTimeSetListener {_, hourOfDay, minute ->
                    selectedDateTime.apply {
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                    }
                    view!!.time_view.setText(selectedDateTime.timeInMillis.toPresentableTime())
                    timeSelected = true
                    checkButton()
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true
            ).show()
        }

        return view
    }

    companion object {
        private const val TAG = "CreateEventFormFragment"

        @JvmStatic
        fun newInstance(partyId: Long, partyName: String) = CreateEventFormFragment(partyId, partyName)
    }

    private fun checkButton() {
        view!!.confirm_button.isEnabled = dateSelected && timeSelected &&
                view?.name_edit?.text?.trim()!!.isNotEmpty()
    }

    private fun parseEvent() = CreateEventData(
        name = view!!.name_edit.text.toString(),
        description = view!!.description_edit.text.toString(),
        capacity = null,
        dateTime = selectedDateTime.timeInMillis.toISO(),
        partyId = partyId
    )

    private fun partyCreatedSuccess(id : Long) {

        val inputData = workDataOf("arg_event_id" to  id,
            "arg_event_name" to view!!.name_edit.text.toString())
        val delay =  calculateDelay(selectedDateTime.timeInMillis)

        val notifyWork = delay?.let {
            OneTimeWorkRequestBuilder<NotifyWorker>()
                .setInitialDelay(it, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("$id")
                .build()
        }

        notifyWork?.let { context?.let { it1 -> WorkManager.getInstance(it1).enqueue(it) } }

        activity?.setResult(Activity.RESULT_OK)
        activity?.finish()
    }

    private fun partyCreatedError(error : Throwable) {
        view!!.confirm_button.isEnabled = true

        Log.e(TAG, "Failed to create event.", error)

        Snackbar
            .make(confirm_button, R.string.event_creation_failed, Snackbar.LENGTH_LONG)
            .show()
    }

    private fun calculateDelay(dateTime: Long?): Long? {
        val currentTime = Calendar.getInstance().timeInMillis
        val inAdvance = TimeUnit.MINUTES.toMillis(30)
        val triggerTime = dateTime?.minus(inAdvance)
        val delay = triggerTime?.minus(currentTime)


        if (delay!! > 0){
            return delay
        }
        return 0
    }
}
