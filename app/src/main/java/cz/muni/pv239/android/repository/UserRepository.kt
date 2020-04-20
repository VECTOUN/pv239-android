package cz.muni.pv239.android.repository

import cz.muni.pv239.android.model.Party
import cz.muni.pv239.android.model.User
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*


interface UserRepository {

    @GET("user-exists")
    fun userExists() : Observable<Boolean>

    @PUT("user")
    fun createUser(@Body nick: User) : Observable<Long>

    @GET("logged-user")
    fun getUserInfo() : Observable<User>

    @GET("user/{id}/owned-parties")
    fun getOwnedParties(@Path("id") id : Long?) : Observable<List<Party>>

    @GET("user/{id}/member-parties")
    fun getMemberParties(@Path("id") id : Long?) : Observable<List<Party>>
}