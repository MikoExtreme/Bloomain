package pt.ipt.bloomain.profile

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
import pt.ipt.bloomain.R
import pt.ipt.bloomain.profile.SettingsActivity
import pt.ipt.bloomain.adapters.ProfilePostsAdapter
import pt.ipt.bloomain.retrofit_api.FollowRequest
import pt.ipt.bloomain.retrofit_api.FollowResponse
import pt.ipt.bloomain.retrofit_api.PostItemResponse
import pt.ipt.bloomain.retrofit_api.ProfileData
import pt.ipt.bloomain.retrofit_api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity que gere a visualização dos detalhes do perfil de um utilizador
 * Implementa a lógica para o perfil do utilizador autenticado e para outros utilizadores
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var currentLoadedUsername: String = ""

    private lateinit var tvFollowers: TextView
    private lateinit var tvFollowing: TextView
    private lateinit var btnFollow: Button

    private lateinit var currentUserId: String



    /**
     * Gere a visualização e interação com o perfil de um utilizador.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile)


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

        val userId = intent.getStringExtra("USER_ID") ?: ""
        currentUserId = intent.getStringExtra("CURRENT_USER_ID") ?: ""

        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)

        // Lógica da interface dinâmica
        // É diferente consoante o utilizador autenticado e o perfil em exibição
        if (userId != currentUserId && currentUserId.isNotEmpty()) {
            btnFollow.visibility = View.VISIBLE
            btnEditProfile.visibility = View.GONE

        } else {
            btnFollow.visibility = View.GONE
            btnEditProfile.visibility = View.VISIBLE
        }

        btnFollow.setOnClickListener {
            val request = FollowRequest(followerId = currentUserId)

            RetrofitClient.instance.toggleFollow(profileUserId, request).enqueue(object :
                Callback<FollowResponse> {
                override fun onResponse(call: Call<FollowResponse>, response: Response<FollowResponse>) {
                    if (response.isSuccessful) {
                        val isFollowing = response.body()?.isFollowing ?: false
                        btnFollow.text = if (isFollowing) "A Seguir" else "Seguir"
                        getProfile(profileUserId) // Atualiza contadores
                    }
                }
                override fun onFailure(call: Call<FollowResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Erro ao processar follow", Toast.LENGTH_SHORT).show()
                }
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
    /**
     * Procura e exibe os detalhes biográficos e estatísticos do utilizador.
     * Realiza o carregamento da imagem de perfil (Base64), nome de utilizador e bio.
     * Após o sucesso, dispara o [loadUserPosts] para preencher a grelha de fotografias.
     */
    private fun getProfile(userId: String) {
        RetrofitClient.instance.getProfile(userId).enqueue(object : Callback<ProfileData> {
            override fun onResponse(call: Call<ProfileData>, response: Response<ProfileData>) {
                if (response.isSuccessful) {
                    val profile = response.body()

                    // Atualiza textos e bio
                    findViewById<TextView>(R.id.usernameTextView).text = profile?.username ?: ""
                    findViewById<TextView>(R.id.bioTextView).text = profile?.bio ?: "Sem biografia"

                    // Atualiza estatísticas (publicações, seguidores, a seguir)
                    findViewById<TextView>(R.id.postsCountText).text = profile?.stats?.posts.toString()
                    tvFollowers.text = profile?.stats?.followers.toString()
                    tvFollowing.text = profile?.stats?.following.toString()

                    // Atualiza imagem de perfil
                    profile?.profileImage?.let { base64 ->
                        if (base64.isNotEmpty()) {
                            try {
                                val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                findViewById<ImageView>(R.id.profileImage).setImageBitmap(bitmap)
                            } catch (e: Exception) { Log.e("PROFILE", "Erro ao processar imagem") }
                        }
                    }

                    // Carrega a grelha de fotos após ter os dados do perfil
                    loadUserPosts(userId)
                }
            }

            override fun onFailure(call: Call<ProfileData>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Erro ao ligar ao servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }
    /**
     * Carrega a coleção de publicações pertencentes ao utilizador do perfil.
     * * Configura o [ProfilePostsAdapter] para organizar as imagens em grelha.
     */
    private fun loadUserPosts(userId: String) {
        RetrofitClient.instance.getUserPosts(userId).enqueue(object :
            Callback<List<PostItemResponse>> {
            override fun onResponse(call: Call<List<PostItemResponse>>, response: Response<List<PostItemResponse>>) {
                if (response.isSuccessful) {
                    val userPosts = response.body() ?: emptyList()
                    recyclerView.adapter = ProfilePostsAdapter(userPosts, currentUserId) { post ->
                    }
                }
            }
            override fun onFailure(call: Call<List<PostItemResponse>>, t: Throwable) {}
        })
    }

    /**
     * Garante que o utilizador tem sempre os dados atualizados com acessa a página de perfil
     */
    override fun onResume() {
        super.onResume()
        val userId = intent.getStringExtra("USER_ID") ?: ""
        if (userId.isNotEmpty()) getProfile(userId)
    }


    /**
     * Gere a propagação de eventos de toque para fechar o teclado automaticamente.
     */
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