package pt.ipt.bloomain.retrofit_api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton responsável por gerir a instância do cliente Retrofit
 */
object RetrofitClient {

    private const val BASE_URL = "http://192.168.1.211:3000/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}