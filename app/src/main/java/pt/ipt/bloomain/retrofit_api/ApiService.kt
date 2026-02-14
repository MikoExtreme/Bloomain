package pt.ipt.bloomain.retrofit_api

import retrofit2.Call
import retrofit2.http.*

// Dados do Perfil
data class ProfileData(val _id: String, val username: String, val bio: String, val profileImage: String, val stats: StatsData)

// Estatísticas do Utilizador
data class StatsData(val posts: Int, val followers: Int, val following: Int)

// Criação do Post
data class PostRequest(val title: String, val description: String, val postImage: String, val location: String, val creatorId: String)

// Resposta do Feed
data class PostItemResponse(val _id: String, val title: String, val description: String, val postImage: String, val location: String?, val creator: CreatorInfo, val likes: List<String>, val createdAt: String)

// Modelo do Comentário
data class Comment(val _id: String, val creator: CommentCreator, val postId: String, val description: String, val createdAt: String)

// Criador do Comentário
data class CommentCreator(val _id: String, val username: String, val profileImage: String)

// Pedido do Comentário
data class CommentRequest(val creatorId: String, val postId: String, val description: String)

// Resposta do Comentário
data class CommentResponse(val message: String, val commentId: String? = null)

// Dados de login
data class LoginRequest(val email: String, val password: String)

// Resposta do Login
data class LoginResponse(val message: String, val userId: String, val username: String)

// Dados de Registo
data class RegisterRequest(val username: String, val email: String, val password: String, val profileImage: String)

// Resposta do registo
data class RegisterResponse(val message: String)

// Confirmação do post
data class PostResponse(val message: String, val postId: String)

// Dados do Criador
data class CreatorInfo(val _id: String, val username: String, val profileImage: String)

// Resposta do Seguir
data class FollowResponse(val message: String, val isFollowing: Boolean)

data class LikeRequest(val userId: String)

data class DeleteRequest(val loggedInUserId: String)

data class FollowRequest(val followerId: String)

data class ProfileImageRequest(val profileImage: String, val loggedInUserId: String)

// Endpoints da API
interface ApiService {

    // Obter Perfil
    @GET("profile/{userId}")
    fun getProfile(@Path("userId") userId: String): Call<ProfileData>

    // Login
    @POST("login")
    fun login(@Body loginData: LoginRequest): Call<LoginResponse>

    // Criar Conta
    @POST("register")
    fun register(@Body registerData: RegisterRequest): Call<RegisterResponse>

    // Criar Post
    @POST("posts")
    fun createPost(@Body postData: PostRequest): Call<PostResponse>

    // Carregar Feed
    @GET("posts")
    fun getFeed(): Call<List<PostItemResponse>>

    // Criar Comentário
    @POST("comments")
    fun addComment(@Body commentData: CommentRequest): Call<CommentResponse>

    // Posts do Utilizador
    @GET("posts/user/{userId}")
    fun getUserPosts(@Path("userId") userId: String): Call<List<PostItemResponse>>

    // Atualizar dados do Utilizador
    @PATCH("users/{id}")
    fun updateUser(@Path("id") id: String, @Body data: Any): Call<ProfileData>

    // Gostar do Post
    @POST("posts/{postId}/like")
    fun toggleLike(@Path("postId") postId: String, @Body request: LikeRequest): Call<PostResponse>

    // Mostrar Comentários
    @GET("posts/{postId}/comments")
    fun getComments(@Path("postId") postId: String): Call<List<Comment>>

    // Seguir Utilizador
    @POST("users/{id}/follow")
    fun toggleFollow(@Path("id") userIdToFollow: String, @Body request: FollowRequest): Call<FollowResponse>

    // Eliminar Comentário
    @DELETE("comments/{id}")
    fun deleteComment(@Path("id") commentId: String): Call<PostResponse>

    // Eliminar publicação
    @HTTP(method = "DELETE", path = "posts/{postId}", hasBody = true)
    fun deletePost(@Path("postId") postId: String, @Body request: DeleteRequest): Call<PostResponse>

    // Eliminar conta
    @HTTP(method = "DELETE", path = "users/{userId}", hasBody = true)
    fun deleteAccount(@Path("userId") userId: String, @Body body: DeleteRequest): Call<PostResponse>

    // Obter Post
    @GET("posts/single/{postId}")
    fun getPostById(@Path("postId") postId: String): Call<PostItemResponse>

    // Pesquisar utilizadores
    @GET("users/search/{query}")
    fun searchUsers(@Path("query") query: String): Call<List<ProfileData>>
}