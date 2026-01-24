package pt.ipt.bloomain

data class Post(
    val _id: String,
    val title: String,
    val description: String? = "",
    val postImage: String, // String Base64
    val location: String? = "",
    val createdAt: String, // O Date do JS chega como String (ISO)
    val creator: String,   // ID do utilizador que criou
    val likes: List<String> = emptyList(),
    val comments: List<String> = emptyList()
)