package cz.muni.pv239.android.repository

import cz.muni.pv239.android.model.Party
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.PUT

interface GroupRepository {

    @PUT("party")
    fun createGroup(@Body name: Party) : Observable<Long>

}