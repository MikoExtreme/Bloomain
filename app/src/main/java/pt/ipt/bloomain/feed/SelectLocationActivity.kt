package pt.ipt.bloomain.feed

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import pt.ipt.bloomain.R

class SelectLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var tempLocation: String = ""

    /**
     * Activity para seleção de coordenadas geográficas através de um mapa interativo.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_location)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.btnConfirmLocation).setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("SELECTED_LOCATION", tempLocation)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    /**
     * Configura o comportamento do mapa assim que este estiver carregado e pronto a usar.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val tomar = LatLng(39.59955518526609, -8.389549138467965)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tomar, 15f))

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Local escolhido"))
            tempLocation = "${latLng.latitude}, ${latLng.longitude}"
        }
    }
}