package pt.ipt.bloomain

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val registerButton = findViewById<Button>(R.id.registerButton)

        // 1.2. Configurar o que acontece quando o botão é clicado
        registerButton.setOnClickListener {
            // 1.3. Criar a Intent: Diz ao Android para ir de MainActivity para RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)

            // 1.4. Iniciar o novo Activity
            startActivity(intent)
        }

        // Exemplo: Lógica para o botão de Login (que levaria ao Perfil/Lobby)
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            // Em uma aplicação real, aqui você faria a validação de email/password

            // Se o login for bem-sucedido, navegue para ProfileActivity
            val intent = Intent(this, ProfileActivity::class.java)

            // OPÇÃO: Se esta for a última tela da sessão (o utilizador não deve poder voltar ao Login)
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }


}

