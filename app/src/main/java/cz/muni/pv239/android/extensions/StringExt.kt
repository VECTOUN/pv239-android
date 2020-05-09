package cz.muni.pv239.android.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Long.toPresentableDate(): String {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return dateFormat.format(this)
}

fun Long.toPresentableTime(): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(this)
}