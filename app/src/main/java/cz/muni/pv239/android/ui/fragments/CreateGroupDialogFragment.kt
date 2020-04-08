package cz.muni.pv239.android.ui.fragments

import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.model.Party
import cz.muni.pv239.android.repository.GroupRepository
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_create_group.view.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class CreateGroupDialogFragment : DialogFragment() {


    private var compositeDisposable: CompositeDisposable? = null
    private val groupRepository: GroupRepository by lazy {
        Retrofit.Builder()
            .client(getHttpClient(activity?.applicationContext))
            .baseUrl(API_ROOT)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GroupRepository::class.java)
    }

    companion object {
        private const val TAG = "CreateGroupFragment"

        @JvmStatic
        fun newInstance() = CreateGroupDialogFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_group, container, false)
        compositeDisposable = CompositeDisposable()


        view.group_create_button.isEnabled = false

        view.edit_group_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                view.group_create_button.isEnabled = s.toString().trim().isNotEmpty()
            }
        })

        view.group_create_button.setOnClickListener(){
            val name = view.edit_group_name.text.toString().trim()
            createGroup(name)
        }

        view.group_cancel_button.setOnClickListener(){
            dismiss()
        }

        return view
    }


    override fun onResume() {
        super.onResume()
        val window = dialog!!.window
        val size = Point()
        val display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        val width: Int = size.x
        window.setLayout((width * 0.95).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
    }

    private fun createGroup(name : String) {
        Log.d(TAG, "createGroup called")

        compositeDisposable?.add(
            groupRepository.createGroup(Party(null, name))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::createGroupSuccess, this::createGroupError)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }

    private fun createGroupSuccess(id: Long) {
        Log.i(TAG, "Created group with id: $id")

        Toast.makeText(activity?.applicationContext, "Created group with id: $id", Toast.LENGTH_LONG)
            .show()

        dismiss()
    }

    private fun createGroupError(error: Throwable) {
        Log.e(TAG, "Failed to create group.", error)

        Toast.makeText(activity?.applicationContext, "Failed to create group", Toast.LENGTH_LONG)
            .show()

        dismiss()
    }
}