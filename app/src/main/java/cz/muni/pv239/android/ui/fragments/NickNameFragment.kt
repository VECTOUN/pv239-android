package cz.muni.pv239.android.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import cz.muni.pv239.android.R
import kotlinx.android.synthetic.main.fragment_nick_name.*

class NickNameFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = NickNameFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_nick_name, container, false)

        val editText = view.findViewById<EditText>(R.id.nick_name_edit);
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                confirm_button.isEnabled = s.toString().trim().isNotEmpty()
            }
        })

        return view
    }
}
