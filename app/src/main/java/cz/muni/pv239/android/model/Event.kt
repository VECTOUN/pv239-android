package cz.muni.pv239.android.model

import java.util.Date

data class Event (
    val id: Long,
    val name: String,
    val description: String,
    val capacity: Int?,
    val dateTime: Date,
    val owner: User,
    val party: Party,
    val participants: List<User>
)