package pt.ipt.bloomain

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import pt.ipt.bloomain.databinding.ActivityCreatePostBinding
import pt.ipt.bloomain.retrofitpackage.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private var selectedLocation: String = "Não definida"
    private var imageBase64: String = ""
    private var userId: String = ""


    /**
     * Launcher para abrir o mapa e esperar pelo resultado
     */
    private val getTargetLocation = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            selectedLocation = data?.getStringExtra("SELECTED_LOCATION") ?: "Não definida"
            binding.btnOpenMap.text = "📍 Local: $selectedLocation"
        }
    }

    /**
     * Inicializa a Activity de criação de publicações.
     * Também define os ouvintes para:
     * 1. Capturar uma fotografia via CameraX.
     * 2. Abrir um mapa para seleção de localização geográfica.
     * 3. Publicar o post (imagem + legenda) para o servidor Node.js.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("USER_ID") ?: ""
        cameraExecutor = Executors.newSingleThreadExecutor()



        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        binding.btnCapture.setOnClickListener { takePhoto() }
        binding.btnPublish.setOnClickListener {
            val caption = binding.editCaption.text.toString()
            if (imageBase64.isEmpty()) {
                Toast.makeText(this, "Deves tirar uma foto primeiro!", Toast.LENGTH_SHORT).show()
            } else {
                publishPost(caption)
            }
        }

        findViewById<Button>(R.id.btnOpenMap).setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            getTargetLocation.launch(intent)
        }
    }

    /**
     * Configura e inicia o fluxo da câmara utilizando o CameraX.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraX", "Erro ao iniciar câmara", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Tirar uma fotografia
     */
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    // Converter ImageProxy para Base64
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    imageBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    image.close()
                    Toast.makeText(baseContext, "Foto capturada!", Toast.LENGTH_SHORT).show()
                }
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraX", "Erro na captura", exc)
                }
            }
        )
    }

    /**
     * Publicar Post
     */
    private fun publishPost(caption: String) {

        val postRequest = PostRequest(
            title = "Novo Post",
            description = caption,
            postImage = imageBase64,
            location = selectedLocation,
            creatorId = userId
        )

        RetrofitClient.instance.createPost(postRequest).enqueue(object : Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreatePostActivity, "Publicado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish() // Fecha a activity e volta ao Feed
                } else {
                    // Mensagem de erro adequada (Requisito 57 do enunciado)
                    Toast.makeText(this@CreatePostActivity, "Erro no servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                // Caso o teu servidor local esteja desligado (Requisito 57)
                Toast.makeText(this@CreatePostActivity, "Falha de rede: Servidor local inacessível", Toast.LENGTH_SHORT).show()
            }
        })
    }

  //Permissões na Criação dos Posts
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true) startCamera()
    }

    private fun requestPermissions() {

        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}