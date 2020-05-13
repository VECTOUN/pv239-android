package cz.muni.pv239.android.ui.fragments

import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.repository.GroupRepository
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_join_group.view.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class JoinGroupDialogFragment : DialogFragment() {

    private var compositeDisposable: CompositeDisposable? = null
    private val groupRepository: GroupRepository by lazy {
        Retrofit.Builder()
            .client(getHttpClient(activity?.applicationContext))
            .baseUrl(API_ROOT)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GroupRepository::class.java)
    }

    private var onGroupJoinedListener: OnGroupJoinedListener? = null;

    companion object {
        private const val TAG = "JoinGroupDialogFragment"

        @JvmStatic
        fun newInstance() = JoinGroupDialogFragment()
        @JvmStatic
        fun newInstance(listener: OnGroupJoinedListener) = JoinGroupDialogFragment().apply {
            onGroupJoinedListener = listener
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_join_group, container, false)
        compositeDisposable = CompositeDisposable()


        view.group_join_button.isEnabled = false

        view.edit_group_id.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                view.group_join_button.isEnabled = s.toString().trim().isNotEmpty()
            }
        })

        view.group_join_button.setOnClickListener(){
            val id = view.edit_group_id.text.toString().trim()
            view.group_join_button.isEnabled = false
            joinGroup(id.toLong())
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

    private fun joinGroup(id : Long) {
        Log.d(TAG, "joinGroup called with id: $id")

        compositeDisposable?.add(
            groupRepository.joinGroup(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::joinGroupResponse)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }

    private fun joinGroupResponse(response : Response<Void>) {
        if (response.isSuccessful) {
            Log.i(TAG, "Joined group.")
            dismiss()
            onGroupJoinedListener?.groupJoined()
        } else {
            Log.e(TAG, "Failed to join group.")

            dismiss()
            onGroupJoinedListener?.groupJoinFailed()
        }
    }
    interface OnGroupJoinedListener {
        fun groupJoined()
        fun groupJoinFailed()
    }
}