package cz.muni.pv239.android.repository

import cz.muni.pv239.android.model.User
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserRepository {

    @GET("user-exists")
    fun userExists() : Observable<Boolean>

    @PUT("user")
    fun createUser(@Body nick: User) : Observable<Long>
}