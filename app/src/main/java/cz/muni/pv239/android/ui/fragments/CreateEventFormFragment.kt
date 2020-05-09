package cz.muni.pv239.android.ui.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import cz.muni.pv239.android.R
import cz.muni.pv239.android.extensions.toPresentableDate
import cz.muni.pv239.android.extensions.toPresentableTime
import kotlinx.android.synthetic.main.fragment_create_event_form.view.*
import java.util.*


class CreateEventFormFragment : Fragment() {

    private val selectedDateTime = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_event_form, container, false)

        val partyArray = arrayOf("Tykuni", "Perun")
        context?.let { context ->
            view.party_text_field
                ?.setAdapter(ArrayAdapter(context, android.R.layout.simple_list_item_1, partyArray))
            view.time_layout.setEndIconOnClickListener {
                TimePickerDialog(
                    context,
                    TimePickerDialog.OnTimeSetListener {_, hourOfDay, minute ->
                        selectedDateTime.apply {
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                        }
                        view.time_view.setText(selectedDateTime.timeInMillis.toPresentableTime())
                    },
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE),
                    true
                ).show()
            }
        }

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

                view.date_view.setText(selectedDateTime.timeInMillis.toPresentableDate())
            }

            fragmentManager?.let {fm ->
                picker.show(fm, picker.toString())
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = CreateEventFormFragment()
    }
}
