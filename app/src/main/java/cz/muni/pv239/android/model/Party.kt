package cz.muni.pv239.android.model

data class Party (
    val id: Long?,
    val name: String,
    val owner: User?,
    val members: List<User>?
) {
    override fun toString(): String {
        return "$name #$id, $owner, $members"
    }
}