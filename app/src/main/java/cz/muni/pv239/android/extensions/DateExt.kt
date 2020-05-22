package cz.muni.pv239.android.extensions

import java.text.SimpleDateFormat
import java.util.*


fun Date.toReadableDate(): String {
    return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this)
}

fun Date.toReadableTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(this)
}

