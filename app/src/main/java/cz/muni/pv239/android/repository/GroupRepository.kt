package cz.muni.pv239.android.repository

import cz.muni.pv239.android.model.Party
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.*

interface GroupRepository {

    @PUT("party")
    fun createGroup(@Body name: Party) : Observable<Long>

    @POST("party/{id}/join")
    fun joinGroup(@Path("id") id : Long) : Observable<Response<Void>>

    @POST("party/{id}/leave")
    fun leaveGroup(@Path("id") id : Long) : Observable<Response<Void>>

    @GET("party/{id}")
    fun getGroup(@Path("id") id: Long) : Observable<Party>
}