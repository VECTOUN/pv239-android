package cz.muni.pv239.android.model

data class CreateEventData (
    val name: String,
    val description: String,
    val capacity: Int?,
    val dateTime: String,
    val partyId: Long
)