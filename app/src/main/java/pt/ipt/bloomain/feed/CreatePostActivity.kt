package pt.ipt.bloomain.feed

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import pt.ipt.bloomain.R
import pt.ipt.bloomain.feed.SelectLocationActivity
import pt.ipt.bloomain.databinding.ActivityCreatePostBinding
import pt.ipt.bloomain.retrofit_api.PostRequest
import pt.ipt.bloomain.retrofit_api.PostResponse
import pt.ipt.bloomain.retrofit_api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * Activity responsável pela criação de publicações
 * Integra a CameraX e o Google Maps
 */
class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private var selectedLocation: String = "Não definida"
    private var imageBase64: String = ""
    private var userId: String = ""

    // Permite pedir várias permissões
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true) startCamera()
    }
    // Permissões obrigatórias
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }


    /**
     * Launcher para abrir o mapa e esperar pelo resultado
     */
    private val getTargetLocation = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            selectedLocation = data?.getStringExtra("SELECTED_LOCATION") ?: "Não definida"
            binding.btnOpenMap.text = "Local: $selectedLocation"
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


        // Verificação das permissões da câmera
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
        val cameraProviderFuture = ProcessCameraProvider.Companion.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Configuração do fluxo de visualização em tempo real
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
                    // Converter ImageProxy para Bitmap
                    val bitmap = imageProxyToBitmap(image)

                    // Comprimir e converter para Base64
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    val bytes = outputStream.toByteArray()
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
     * Função utilitária que converte buffer de dados da CameraX num objeto Bitmap
     */
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
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

                    Toast.makeText(this@CreatePostActivity, "Erro no servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PostResponse>, t: Throwable) {

                Toast.makeText(this@CreatePostActivity, "Falha de rede: Servidor local inacessível", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Verifica se todas as permissões de hardware declaradas foram concedidas pelo utilizador
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }





    /**
     * Faz um pedido de permissão
     */
    private fun requestPermissions() {

        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    /**
     * Encerra a câmera, após a utilização
     */
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


}