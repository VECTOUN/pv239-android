package cz.muni.pv239.android.ui.fragments


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import cz.muni.pv239.android.R
import cz.muni.pv239.android.model.API_ROOT
import cz.muni.pv239.android.model.Party
import cz.muni.pv239.android.repository.GroupRepository
import cz.muni.pv239.android.ui.activities.CreateEventActivity
import cz.muni.pv239.android.ui.activities.CreateEventActivity.Companion.CREATE_EVENT
import cz.muni.pv239.android.ui.activities.GroupDetailActivity.Companion.LEFT_RESULT
import cz.muni.pv239.android.util.PrefManager
import cz.muni.pv239.android.util.getHttpClient
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_event_detail.view.leave_button
import kotlinx.android.synthetic.main.fragment_events.view.*
import kotlinx.android.synthetic.main.fragment_events.view.swipeContainer
import kotlinx.android.synthetic.main.fragment_group_detail.*
import kotlinx.android.synthetic.main.fragment_group_detail.view.*
import kotlinx.android.synthetic.main.fragment_groups.*
import kotlinx.android.synthetic.main.fragment_loading.progressBar
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class GroupDetailFragment(private val groupId: Long) : Fragment(){

    private var party: Party? = null
    private var compositeDisposable: CompositeDisposable? = null
    private val prefManager: PrefManager? by lazy { PrefManager(context) }
    private val groupRepository: GroupRepository by lazy {
        Retrofit.Builder()
            .client(getHttpClient(activity?.applicationContext))
            .baseUrl(API_ROOT)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GroupRepository::class.java)
    }

    companion object {
        private const val TAG = "GroupDetailFragment"

        fun newInstance(groupId : Long) = GroupDetailFragment(groupId)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        val view = inflater.inflate(R.layout.fragment_group_detail, container, false)

        view.leave_button.setOnClickListener {
            leaveGroup()
        }

        view.create_event_button.setOnClickListener {
            createEvent()
        }

        if (savedInstanceState == null) {
            compositeDisposable = CompositeDisposable()
        }

        loadGroup()

        return view
    }

    private fun createEvent() {
        startActivityForResult(CreateEventActivity.newIntent(context!!, party!!), CREATE_EVENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CREATE_EVENT) {
            when(resultCode) {
                Activity.RESULT_OK -> {
                    Snackbar
                        .make(view!!, R.string.event_created, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun leaveGroup(){
        Log.i(TAG, "User id ${prefManager?.userId!!} is leaving group with id: $groupId")
        compositeDisposable?.add(groupRepository.leaveGroup(groupId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(this::onLeaveGroupResponse))
    }


    private fun loadGroup(){
        compositeDisposable?.add(groupRepository.getGroup(groupId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(this::onGroupLoaded, this::onGroupLoadError))
    }

    private fun onLeaveGroupResponse(response : Response<Void>){
        if(response.isSuccessful){
            activity?.setResult(LEFT_RESULT)
            activity?.finish()
        }
    }

    private fun onGroupLoaded(party: Party){
        this.party = party

        group_detail.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        group_name.text = party.name
        owner_label.text = getString(R.string.event_detail_owner, party.owner?.nick)
        members_label.text = getString(R.string.group_detail_members,
            party.members?.size.toString())
        var members = ""
        for (member in party.members!!) {
            if (members.isNotEmpty()) {
                members += "\n"
            }
            members += member.nick
        }
        members_names.text = members
    }

    private fun onGroupLoadError(error: Throwable) {
        progressBar.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.clear()
    }
}