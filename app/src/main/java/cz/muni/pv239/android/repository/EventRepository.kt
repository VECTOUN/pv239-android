package cz.muni.pv239.android.repository

import cz.muni.pv239.android.model.CreateEventData
import cz.muni.pv239.android.model.Event
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface EventRepository {

    @GET("user/{id}/future-events")
    fun getFutureEvents(@Path("id") userId : Long): Observable<List<Event>>

    @GET("user/{id}/events")
    fun getUserEvents(@Path("id") userId : Long): Observable<List<Event>>

    @PUT("event")
    fun createEvent(@Body event: CreateEventData) : Observable<Long>
}