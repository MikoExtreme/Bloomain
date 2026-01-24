package pt.ipt.bloomain

data class User(
    val _id: String,
    val username: String,
    val email: String,
    // A password normalmente não é devolvida em GETs por segurança,
    // mas se o teu login a devolve, podes mantê-la aqui.
    val profileImage: String? = "",
    val bio: String? = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val posts: List<String> = emptyList()
)