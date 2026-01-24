package pt.ipt.bloomain

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.Path

//Perfil
data class ProfileData(
    val _id: String,
    val username: String,
    val bio: String,
    val profileImage: String,
    val stats: StatsData
)
//Status do Utilizador
data class StatsData(
    val posts: Int,
    val followers: Int,
    val following: Int
)
//Detalhes do Post
data class PostRequest(
    val title: String,
    val description: String,
    val postImage: String,
    val location: String,
    val creatorId: String
)

data class PostItemResponse(
    val _id: String,
    val title: String,
    val description: String,
    val postImage: String,
    val creator: CreatorInfo,
    val likes: List<String>,
    val createdAt: String
)

data class Comment(
    val _id: String,
    val creator: CommentCreator,
    val postId: String,
    val description: String,
    val createdAt: String
)

data class CommentCreator(
    val _id: String,
    val username: String,
    val profileImage: String
)

data class CommentRequest(
    val creatorId: String,
    val postId: String,
    val description: String
)

data class CommentResponse(
    val message: String,
    val commentId: String? = null
)



// Login
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val message: String, val userId: String, val username: String)

data class RegisterRequest(val username: String, val email: String, val password: String, val profileImage: String)

data class RegisterResponse(val message: String)

data class PostResponse(val message: String, val postId: String)

data class CreatorInfo(val _id: String, val username: String, val profileImage: String)

data class FollowResponse(val message: String, val isFollowing: Boolean)





interface ApiService {

    @GET("profile/{userId}")
    fun getProfile(@Path("userId") userId: String): Call<ProfileData>

    @POST("login")
    fun login(@Body loginData: LoginRequest): Call<LoginResponse>

    @POST("register")
    fun register(@Body registerData: RegisterRequest): Call<RegisterResponse>

    @POST("posts")
    fun createPost(@Body postData: PostRequest): Call<PostResponse>

    @GET("posts")
    fun getFeed(): Call<List<PostItemResponse>>

    @POST("comments")
    fun addComment(@Body commentData: CommentRequest): Call<CommentResponse>

    @GET("posts/user/{userId}")
    fun getUserPosts(@Path("userId") userId: String): Call<List<PostItemResponse>>

    @PATCH("users/{id}")
    fun updateUser(@Path("id") id: String, @Body data: Map<String, String>): Call<User>


    @POST("posts/{postId}/like")
    fun toggleLike(
        @Path("postId") postId: String,
        @Body request: Map<String, String>
    ): Call<PostResponse>




    @GET("posts/{postId}/comments")
    fun getComments(@Path("postId") postId: String): Call<List<Comment>>


    @PATCH("users/{id}/password")
    fun updatePassword(
        @Path("id") id: String,
        @Body data: Map<String, String>
    ): Call<PostResponse>


    @PATCH("users/{id}")
    fun updateUserInfo(
        @Path("id") id: String,
        @Body data: Map<String, String>
    ): Call<User>


    @POST("users/{id}/follow")
    fun toggleFollow(
        @Path("id") userToFollowId: String,
        @Body body: Map<String, String>
    ): Call<FollowResponse>


    @DELETE("comments/{id}")
    fun deleteComment(@Path("id") commentId: String): Call<PostResponse>

    @DELETE("posts/{id}")
    fun deletePost(@Path("id") postId: String): Call<PostResponse>

    @DELETE("users/{id}")
    fun deleteAccount(@Path("id") userId: String): Call<PostResponse>

    @GET("posts/single/{postId}")
    fun getPostById(@Path("postId") postId: String): Call<PostItemResponse>


    @GET("users/search/{query}")
    fun searchUsers(@Path("query") query: String): Call<List<ProfileData>>


}