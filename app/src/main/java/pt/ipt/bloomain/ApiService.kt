package pt.ipt.bloomain

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path

// --- MODELOS DE DADOS (FORA DA INTERFACE) ---

// Perfil
data class ProfileData(
    val username: String,
    val bio: String,
    val profileImage: String,
    val stats: StatsData
)

data class StatsData(
    val posts: Int,
    val followers: Int,
    val following: Int
)

// Login
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val message: String, val userId: String, val username: String)

data class RegisterRequest(val username: String, val email: String, val password: String, val profileImage: String)

data class RegisterResponse(val message: String)
// --- INTERFACE DE COMUNICAÇÃO ---

interface ApiService {

    @GET("profile/{userId}")
    fun getProfile(@Path("userId") userId: String): Call<ProfileData>

    @POST("login")
    fun login(@Body loginData: LoginRequest): Call<LoginResponse>

    @POST("register") // Nova rota para o registo
    fun register(@Body registerData: RegisterRequest): Call<RegisterResponse>
}