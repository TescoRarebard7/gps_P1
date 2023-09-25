package com.example.gps

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.gps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() , LocationListener {
    private lateinit var binding: ActivityMainBinding

    private val TIEMPO_MIN: Long = 1000
    private val DISTANCE_MIN = 0f
    private val A = arrayOf("n/d", "preciso", "impreciso")
    private val E = arrayOf("fuera de servicio","temporalmente fuera de servicio","disponible")
    private val PERMISSION = 1000
    private lateinit var manejador:LocationManager
    private lateinit var provedor:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setup()
    }

    private fun setup() {

        manejador = getSystemService(LOCATION_SERVICE) as LocationManager

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION),PERMISSION)
        }else{
            startLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocation(){
        val criterio = Criteria()
        criterio.isCostAllowed = false
        criterio.isAltitudeRequired = false
        criterio.accuracy = Criteria.ACCURACY_FINE
        provedor = manejador.getBestProvider(criterio,true).toString()
        provedor.let {
            manejador.requestLocationUpdates(
                it,
                TIEMPO_MIN,
                DISTANCE_MIN,
                this
            )
        }

        val lastLocation = manejador.getLastKnownLocation(provedor)
        lastLocation(lastLocation)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permisos Concedidos", Toast.LENGTH_SHORT).show()
                startLocation()
            }
            else{
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()

            }
        }
    }

    override fun onLocationChanged(location: Location) {
        if (location == null){
            binding.data.setText("Localizacion desconocida")
        }
        mostrarlocalizacion(location)
        mostrarProvedoor()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        super.onStatusChanged(provider, status, extras)
        val text = "Cambio el estado del proveedor: $provider , estado = ${E[Math.max(0,status)]} " +
                ", extras = $extras"

        binding.status.text = text
    }

    override fun onProviderEnabled(provider: String) {
        super.onProviderEnabled(provider)
        val text = "Provedor habilitado +  $provider"
        binding.provedoor.text = text
    }

    override fun onProviderDisabled(provider: String) {
        super.onProviderDisabled(provider)
        val text = "Provedor deshabilitado +  $provider"
        binding.provedoor.text = text
    }

    fun mostrarlocalizacion(location: Location){
        val locationText = "Latitude = ${location.latitude}\n" +
                "longuitud = ${location.longitude}\n" +
                "Altitude = ${location.altitude}\n" +
                "Velocidad = ${location.speed}\n" +
                "Tiempo = ${location.time}"

        binding.data.text = locationText

        binding.provedoor.text = provedor.toString()


    }

    fun lastLocation(location: Location?){
        val lastLocationText = "Latitude = ${location?.latitude}\n" +
                "longuitud = ${location?.longitude}\n" +
                "Altitude = ${location?.altitude}\n" +
                "Velocidad = ${location?.speed}\n" +
                "Tiempo = ${location?.time}"

        binding.lastLocation.text = lastLocationText
    }

    fun mostrarProvedoor(){

        val info: LocationProvider? = manejador.getProvider(provedor)

        val text = "Name: ${info?.name} ," +
                "dispositivo habilitado: ${manejador.isProviderEnabled(provedor)} " +
                "Precision: ${A[Math.max(0, info!!.accuracy)]} " +
                "costo de servicio: ${info.hasMonetaryCost()} " +
                "Uso de satellite ${info.requiresSatellite()} " +
                "Uso de red ${info.requiresNetwork()} " +
                "Soporta altitud : ${info.supportsAltitude()} " +
                "Soporta Bearing : ${info.supportsBearing()} " +
                "soporta Velocidad: ${info.supportsSpeed()} "

        binding.status.text = text


    }



    override fun onPause() {
        super.onPause()
        manejador.removeUpdates(this)
    }

    override fun onResume() {
        super.onResume()
        startLocation()
    }

}