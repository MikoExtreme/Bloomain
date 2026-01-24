package pt.ipt.bloomain

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PostMakerActivity : AppCompatActivity() {

    private var postBase64: String = ""
    private lateinit var imgPreview: ImageView

    /**
     * Gere a seleção de imagens da galeria e a sua preparação para upload.
     */
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imgPreview.visibility = View.VISIBLE
            imgPreview.setImageURI(it)

            val bytes = contentResolver.openInputStream(it)?.readBytes()
            if (bytes != null) {
                postBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        }
    }

    /**
     * Inicializa a interface de criação de posts e configura a lógica de publicação.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_maker)


        val userId = intent.getStringExtra("USER_ID") ?: ""

        imgPreview = findViewById(R.id.imgPostPreview)
        val btnAddImg = findViewById<ImageButton>(R.id.btnAddImage)
        val btnPublish = findViewById<Button>(R.id.btnPublishPost)
        val edtContent = findViewById<EditText>(R.id.edtPostContent)

        btnAddImg.setOnClickListener { pickImage.launch("image/*") }

        btnPublish.setOnClickListener {
            val description = edtContent.text.toString()

            if (postBase64.isEmpty()) {
                Toast.makeText(this, "Escolha uma foto primeiro!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val apiService = Retrofit.Builder()
                .baseUrl("http://192.168.1.211:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)


            val request = PostRequest(
                title = "Nova Publicação",
                description = description,
                postImage = postBase64,
                location = "Portugal",
                creatorId = userId
            )

            btnPublish.isEnabled = false

            apiService.createPost(request).enqueue(object : Callback<PostResponse> {
                override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@PostMakerActivity, "Publicado!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                    btnPublish.isEnabled = true
                    Toast.makeText(this@PostMakerActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}