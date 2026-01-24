package pt.ipt.bloomain

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.bloomain.UserSearchAdapter // Vamos criar este a seguir
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearchQuery: EditText
    private lateinit var currentUserId: String

    private val apiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.1.211:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        currentUserId = intent.getStringExtra("CURRENT_USER_ID") ?: ""

        etSearchQuery = findViewById(R.id.etSearchQuery)
        recyclerView = findViewById(R.id.searchRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length >= 2) {
                    performSearch(query)
                } else {
                    // Limpa a lista se houver menos de 2 letras
                    recyclerView.adapter = UserSearchAdapter(emptyList(), currentUserId)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun performSearch(query: String) {
        apiService.searchUsers(query).enqueue(object : Callback<List<ProfileData>> {
            override fun onResponse(call: Call<List<ProfileData>>, response: Response<List<ProfileData>>) {
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    recyclerView.adapter = UserSearchAdapter(users, currentUserId)
                }
            }
            override fun onFailure(call: Call<List<ProfileData>>, t: Throwable) {}
        })
    }
}