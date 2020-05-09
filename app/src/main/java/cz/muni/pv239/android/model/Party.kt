package cz.muni.pv239.android.model

data class Party (
    val id: Long?,
    val name: String
) {
    override fun toString(): String {
        return "$name #$id"
    }
}