package pt.ipt.bloomain.authentication

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.bloomain.R


/**
* Activity responsável pela exibição as informações da aplicação e dos autores
* */
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        findViewById<Button>(R.id.btnBackAbout).setOnClickListener {
            finish()
        }
    }
}