package pt.ipt.bloomain

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.bloomain.adapters.ProfilePostsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var currentLoadedUsername: String = ""

    private lateinit var tvFollowers: TextView
    private lateinit var tvFollowing: TextView
    private lateinit var btnFollow: Button

    // 1. Variável global que será usada pelo adaptador para evitar o crash
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
        enableEdgeToEdge()
        setContentView(R.layout.profile)

        // Inicialização de UI
        recyclerView = findViewById(R.id.postsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        tvFollowers = findViewById(R.id.followerCountText)
        tvFollowing = findViewById(R.id.followingCountText)
        btnFollow = findViewById(R.id.btnFollow)

        val profileUserId = intent.getStringExtra("USER_ID") ?: ""
        currentUserId = intent.getStringExtra("CURRENT_USER_ID") ?: ""



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. CORREÇÃO DO CRASH: Inicializamos a variável global
        val userId = intent.getStringExtra("USER_ID") ?: ""
        currentUserId = intent.getStringExtra("CURRENT_USER_ID") ?: ""

        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
        val fabCreatePost = findViewById<View>(R.id.fabCreatePost)

        // 3. Lógica do botão Seguir
        if (userId != currentUserId && currentUserId.isNotEmpty()) {
            btnFollow.visibility = View.VISIBLE
            btnEditProfile.visibility = View.GONE
            fabCreatePost.visibility = View.GONE

        } else {
            btnFollow.visibility = View.GONE
            btnEditProfile.visibility = View.VISIBLE
            fabCreatePost.visibility = View.VISIBLE
        }

        btnFollow.setOnClickListener {
            val body = mapOf("followerId" to currentUserId)
            apiService.toggleFollow(userId, body).enqueue(object : Callback<FollowResponse> {
                override fun onResponse(call: Call<FollowResponse>, response: Response<FollowResponse>) {
                    if (response.isSuccessful) {
                        val isFollowing = response.body()?.isFollowing ?: false
                        btnFollow.text = if (isFollowing) "A Seguir" else "Seguir"
                        loadProfileData(userId)
                    }
                }
                override fun onFailure(call: Call<FollowResponse>, t: Throwable) {}
            })
        }

        // Configuração de botões de navegação
        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("CURRENT_USERNAME", currentLoadedUsername)
            startActivity(intent)
        }

        if (userId.isNotEmpty()) {
            getProfile(userId)
        }
    }

    private fun getProfile(userId: String) {
        apiService.getProfile(userId).enqueue(object : Callback<ProfileData> {
            override fun onResponse(call: Call<ProfileData>, response: Response<ProfileData>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    currentLoadedUsername = profile?.username ?: ""
                    findViewById<TextView>(R.id.usernameTextView).text = currentLoadedUsername
                    findViewById<TextView>(R.id.bioTextView).text = profile?.bio ?: "Sem biografia"
                    findViewById<TextView>(R.id.postsCountText).text = profile?.stats?.posts.toString()
                    tvFollowers.text = profile?.stats?.followers.toString()
                    tvFollowing.text = profile?.stats?.following.toString()

                    profile?.profileImage?.let { base64 ->
                        if (base64.isNotEmpty()) {
                            try {
                                val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                findViewById<ImageView>(R.id.profileImage).setImageBitmap(bitmap)
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                    loadUserPosts(userId)
                }
            }
            override fun onFailure(call: Call<ProfileData>, t: Throwable) {}
        })
    }

    private fun loadUserPosts(userId: String) {
        apiService.getUserPosts(userId).enqueue(object : Callback<List<PostItemResponse>> {
            override fun onResponse(call: Call<List<PostItemResponse>>, response: Response<List<PostItemResponse>>) {
                if (response.isSuccessful) {
                    val userPosts = response.body() ?: emptyList()

                    // 4. CORREÇÃO DA PASSAGEM DE ARGUMENTOS:
                    // Passamos os dados e o ID do utilizador logado para o adaptador
                    recyclerView.adapter = ProfilePostsAdapter(userPosts, currentUserId) { post ->
                        Log.d("PROFILE", "Clicaste no post ${post._id}")
                    }
                }
            }
            override fun onFailure(call: Call<List<PostItemResponse>>, t: Throwable) {}
        })
    }

    private fun loadProfileData(userId: String) {
        apiService.getProfile(userId).enqueue(object : Callback<ProfileData> {
            override fun onResponse(call: Call<ProfileData>, response: Response<ProfileData>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    tvFollowers.text = profile?.stats?.followers.toString()
                    tvFollowing.text = profile?.stats?.following.toString()
                }
            }
            override fun onFailure(call: Call<ProfileData>, t: Throwable) {}
        })
    }

    override fun onResume() {
        super.onResume()
        val userId = intent.getStringExtra("USER_ID") ?: ""
        if (userId.isNotEmpty()) getProfile(userId)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}