package cz.muni.pv239.android.repository

import cz.muni.pv239.android.model.Event
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET

interface EventRepository {

    @GET("future-events")
    fun getFutureEvents(): Observable<List<Event>>
}