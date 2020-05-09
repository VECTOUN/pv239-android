package cz.muni.pv239.android.ui.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import cz.muni.pv239.android.R
import cz.muni.pv239.android.extensions.toISO
import cz.muni.pv239.android.extensions.toPresentableDate
import cz.muni.pv239.android.extensions.toPresentableTime
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.model.CreateEventData
import cz.muni.pv239.android.model.Party
import cz.muni.pv239.android.repository.EventRepository
import cz.muni.pv239.android.repository.UserRepository
import cz.muni.pv239.android.util.PrefManager
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.create_event_form_items.view.*
import kotlinx.android.synthetic.main.fragment_create_event_form.view.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class CreateEventFormFragment : Fragment() {

    private val selectedDateTime = Calendar.getInstance()
    private var selectedParty : Party? = null

    private var dateSelected = false
    private var timeSelected = false
    private var compositeDisposable: CompositeDisposable? = null
    private var parties: List<Party>? = null

    private val prefManager: PrefManager? by lazy { PrefManager(context) }
    private val userRepository: UserRepository by lazy {
        Retrofit.Builder()
            .client(getHttpClient(activity?.applicationContext))
            .baseUrl(API_ROOT)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(UserRepository::class.java)
    }
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

        compositeDisposable?.add(
            userRepository.getParties(prefManager?.userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::getPartiesSuccess, this::getPartiesError)
        )
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_event_form, container, false)

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

        return view
    }

    companion object {
        private const val TAG = "CreateEventFormFragment"

        @JvmStatic
        fun newInstance() = CreateEventFormFragment()
    }

    private fun getPartiesSuccess(parties: List<Party>) {
        Log.i(TAG, "Loaded users parties: ${parties}.")
        this.parties = parties
        initFormData()
    }

    private fun getPartiesError(error: Throwable) {
        Log.e(TAG, "Failed to load users parties.", error)
    }

    private fun initFormData() {
        Log.d(TAG, "Parties: $parties")

        context?.let { context ->
            view!!.form_items.party_text_field
                ?.setAdapter(ArrayAdapter(context, android.R.layout.simple_list_item_1, parties!!))
            view!!.party_text_field.setOnItemClickListener { _, _, position, _ ->
                selectedParty = parties!![position]
                Log.d(TAG, "Selected party: $selectedParty")
                checkButton()
            }
            view!!.time_layout.setEndIconOnClickListener {
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
        }
    }

    private fun checkButton() {
        view!!.confirm_button.isEnabled = dateSelected && timeSelected && selectedParty != null &&
                view?.name_edit?.text?.trim()!!.isNotEmpty()
    }

    private fun parseEvent() = CreateEventData(
        name = view!!.name_edit.text.toString(),
        description = view!!.description_edit.text.toString(),
        capacity = null,
        dateTime = selectedDateTime.timeInMillis.toISO(),
        partyId = selectedParty!!.id!!
    )

    private fun partyCreatedSuccess(id : Long) {
        Toast
            .makeText(context, "Event created.", Toast.LENGTH_SHORT)
            .show()

        activity?.finish()
    }

    private fun partyCreatedError(error : Throwable) {
        view!!.confirm_button.isEnabled = true

        Log.e(TAG, "Failed to create event.", error)

        Toast
            .makeText(context, "Failed to create event.", Toast.LENGTH_LONG)
            .show()
    }
}
