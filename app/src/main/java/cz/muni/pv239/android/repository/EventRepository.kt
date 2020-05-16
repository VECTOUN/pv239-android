package cz.muni.pv239.android.repository

import cz.muni.pv239.android.model.CreateEventData
import cz.muni.pv239.android.model.Event
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.*

interface EventRepository {

    @GET("user/{id}/future-events")
    fun getFutureEvents(@Path("id") userId : Long): Observable<List<Event>>

    @GET("user/{id}/events")
    fun getUserEvents(@Path("id") userId : Long): Observable<List<Event>>

    @GET("event/{id}")
    fun getEvent(@Path("id") eventId : Long) : Observable<Event>

    @POST("event/{id}/leave")
    fun leaveEvent(@Path("id") eventId : Long) : Observable<Response<Void>>

    @POST("event/{id}/join")
    fun joinEvent(@Path("id") eventId : Long) : Observable<Response<Void>>

    @PUT("event")
    fun createEvent(@Body event: CreateEventData) : Observable<Long>
}