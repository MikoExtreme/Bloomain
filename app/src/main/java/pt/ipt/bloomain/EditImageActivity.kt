package pt.ipt.bloomain

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EditImageActivity : AppCompatActivity() {

    private lateinit var previewImage: ImageView
    private lateinit var btnSave: Button
    private var newBase64: String = ""
    private var userId: String = ""

    /**
     * Abrir a galeria e processar a imagem selecionada.
     */
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inputStream = contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                newBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                previewImage.setImageBitmap(bitmap)
                btnSave.isEnabled = true
            }
        }
    }

    /**
     * Inicializa a Activity de edição da imagem de perfil.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_image)

        previewImage = findViewById(R.id.previewImageView)
        btnSave = findViewById(R.id.btnSave)
        userId = intent.getStringExtra("USER_ID") ?: ""


        val currentImageBase64 = intent.getStringExtra("CURRENT_IMAGE")
        if (!currentImageBase64.isNullOrEmpty()) {
            val bytes = Base64.decode(currentImageBase64, Base64.DEFAULT)
            previewImage.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        }

        findViewById<Button>(R.id.btnSelectNew).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            uploadImage()
        }

        findViewById<Button>(R.id.btnCancel).setOnClickListener { finish() }
    }

    /**
     * Realiza o upload da nova imagem de perfil para o Utilizador
     */
    private fun uploadImage() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.211:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        val updateData = mapOf(
            "profileImage" to newBase64,
            "loggedInUserId" to userId
        )

        apiService.updateUser(userId, updateData).enqueue(object : Callback<ProfileData> {
            override fun onResponse(call: Call<ProfileData>, response: Response<ProfileData>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditImageActivity, "Foto atualizada!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditImageActivity, "Não autorizado ou erro", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ProfileData>, t: Throwable) {
                Toast.makeText(this@EditImageActivity, "Erro de ligação", Toast.LENGTH_SHORT).show()
            }
        })
    }
}