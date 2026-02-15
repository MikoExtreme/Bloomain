package pt.ipt.bloomain.feed

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import pt.ipt.bloomain.R

/**
 * Activity responsável pela visualização associada a uma publicação
 * Implementa [OnMapReadyCallback] para o processamento da exibição do ponto exato no mapa da Google Maps
 */
class ViewLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_location)

        // Recebe a string "lat,long" enviada pelo Adapter
        val coords = intent.getStringExtra("COORDINATES") ?: ""
        if (coords.contains(",")) {
            val parts = coords.split(",")
            latitude = parts[0].toDoubleOrNull() ?: 0.0
            longitude = parts[1].toDoubleOrNull() ?: 0.0
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Configura o mapa e posiciona o marcador visual nas coordenadas fornecidas
     */
    override fun onMapReady(googleMap: GoogleMap) {
        val location = LatLng(latitude, longitude)

        // Adiciona um marcador no local exato
        googleMap.addMarker(MarkerOptions().position(location).title("Local do Post"))

        // Move a câmara para lá com um zoom de 15 (nível de rua)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

        // Ativa botões de zoom para melhor usabilidade
        googleMap.uiSettings.isZoomControlsEnabled = true
    }
}