package pt.ipt.bloomain.feed

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.bloomain.R
import pt.ipt.bloomain.feed.UserSearchAdapter
import pt.ipt.bloomain.retrofit_api.ProfileData
import pt.ipt.bloomain.retrofit_api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearchQuery: EditText
    private lateinit var currentUserId: String



    /**
     * Gere a funcionalidade de pesquisa de utilizadores em tempo real.
     */
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
                    recyclerView.adapter = UserSearchAdapter(emptyList(), currentUserId)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Executa a chamada à API para filtrar utilizadores com base numa string de consulta.
     */
    private fun performSearch(query: String) {
        RetrofitClient.instance.searchUsers(query).enqueue(object : Callback<List<ProfileData>> {
            override fun onResponse(call: Call<List<ProfileData>>, response: Response<List<ProfileData>>) {
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    recyclerView.adapter = UserSearchAdapter(users, currentUserId)
                } else {
                    // Requisito 57: Mensagem de erro adequada
                    Toast.makeText(this@SearchActivity, "Erro na procura", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ProfileData>>, t: Throwable) {
                // Requisito 57: Feedback de rede
                Toast.makeText(this@SearchActivity, "Sem ligação ao servidor local", Toast.LENGTH_SHORT).show()
            }
        })
    }
}