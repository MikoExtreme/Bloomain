package pt.ipt.bloomain.retrofitpackage

import pt.ipt.bloomain.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    // REGRAS IMPORTANTES:
    // 1. Usa o IP da tua máquina (visto no ipconfig) em vez de localhost
    // 2. Garante que termina com "/"
    private const val BASE_URL = "http://192.168.1.XX:3000/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}