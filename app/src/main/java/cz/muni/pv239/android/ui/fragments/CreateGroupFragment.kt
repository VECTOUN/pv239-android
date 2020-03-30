package cz.muni.pv239.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cz.muni.pv239.android.R

class CreateGroupFragment : Fragment(){

    companion object {
        @JvmStatic
        fun newInstance() = CreateGroupFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_group, container, false)
    }
}