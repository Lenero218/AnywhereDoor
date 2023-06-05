package com.example.anywheredoor.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.anywheredoor.R
import com.example.anywheredoor.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.libraries.places.api.Places
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    lateinit var binding: FragmentMapBinding
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatLng: LatLng? = null


    companion object {
        private const val LOCAL_REQUEST_CODE = 1


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView?.onCreate(savedInstanceState)
        fusedLocationClient =
            activity?.let { LocationServices.getFusedLocationProviderClient(it) }!!
        binding.mapView?.getMapAsync {
            map = it
            map!!.setOnMarkerClickListener(this)
            map!!.uiSettings.isZoomControlsEnabled = true
            setupMap()
            //findPlaces("pubs")
            /* currentLatLng?.let{currentLatLng->
                findPlacesWithinRadius("hospitals")
            }*/


        }

    }

    private fun findPlacesWithinRadius(query: String) {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        Places.initialize(requireContext(), "AIzaSyDakygKUg6TWjtcShbKkOe3woExfG9Z4m8")
        val placesService: PlacesClient = Places.createClient(requireContext())
        var center: LatLng? = null
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) {

            currentLatLng = LatLng(it.latitude, it.longitude)
            center = LatLng(currentLatLng!!.latitude, currentLatLng!!.longitude)

            val radius = 30000
            val bounds = RectangularBounds.newInstance(
                LatLng(center!!.latitude - radius / 111000.0, center!!.longitude - radius / 111000.0),
                LatLng(center!!.latitude + radius / 111000.0, center!!.longitude + radius / 111000.0)
            )
            val request = FindAutocompletePredictionsRequest.builder()
                .setLocationRestriction(bounds)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setQuery(query)
                .build()

            // Perform the search
            placesService.findAutocompletePredictions(request)
                .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                    val predictions: List<AutocompletePrediction> = response.autocompletePredictions
                    // Process the search results here
                    for (prediction in predictions) {

                        val placeId = prediction.placeId
                        val placeName = prediction.getPrimaryText(null).toString()

                        val placeFields = listOf(Place.Field.LAT_LNG)
                        val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()



                        placesService.fetchPlace(fetchPlaceRequest)
                            .addOnSuccessListener { fetchPlaceResponse ->
                                val place = fetchPlaceResponse.place
                                val placeLatLng = place.latLng
                                Log.d("TAG", "Got lattitude and longi ${placeLatLng.toString()}")
                                if (placeLatLng != null) {
                                    // Add a marker for each hospital
                                    Log.d("TAG", placeLatLng.toString())

                                    placeMarkerOnMap(placeLatLng,placeName)
                                    /*map!!.addMarker(
                                        MarkerOptions()
                                            .position(placeLatLng)
                                            .title(placeName)
                                    )*/
                                }
                            }.addOnFailureListener { exception ->
                                // Handle any errors
                            }
                    }
                }.addOnFailureListener { exception: Exception ->
                    // Handle the failure
                }
        }


    }


   /*public fun findPlaces(query: String) {
        Places.initialize(requireContext(), "AIzaSyDakygKUg6TWjtcShbKkOe3woExfG9Z4m8")
        val placesClient = Places.createClient(requireContext())
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .setCountry("IN")
            .build()

        // Perform the search
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->

                val predictions = response.autocompletePredictions

                for (prediction in predictions) {

                    val placeId = prediction.placeId
                    val placeName = prediction.getPrimaryText(null).toString()

                    val placeFields = listOf(Place.Field.LAT_LNG)
                    val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()



                    placesClient.fetchPlace(fetchPlaceRequest)
                        .addOnSuccessListener { fetchPlaceResponse ->
                            val place = fetchPlaceResponse.place
                            val placeLatLng = place.latLng

                            if (placeLatLng != null) {
                                // Add a marker for each hospital
                                Log.d("TAG", placeLatLng.toString())

                                placeMarkerOnMap(placeLatLng)
                                /*map!!.addMarker(
                                    MarkerOptions()
                                        .position(placeLatLng)
                                        .title(placeName)
                                )*/
                            }
                        }.addOnFailureListener { exception ->
                        // Handle any errors
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occur during the search
            }
    }*/

    private fun setupMap() {
        setupLocationAccess()
    }

    private fun setupLocationAccess() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCAL_REQUEST_CODE
            )

        }
        map!!.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            currentLatLng = LatLng(location.latitude, location.longitude)
            placeMarkerOnMap(currentLatLng!!,currentLatLng.toString())
            map!!.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, 12f))

        }
        findPlacesWithinRadius("hospitals")
    }

    private fun placeMarkerOnMap(currentLatLng: LatLng, placeName : String) {

        val markerOptions = MarkerOptions().position(currentLatLng)
        markerOptions.title("${placeName}")
        map!!.addMarker(markerOptions)

    }

    override fun onResume() {
        super.onResume()
        binding.mapView?.onResume()

    }


    override fun onStart() {
        super.onStart()
        binding.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }


    override fun onMarkerClick(p0: Marker) = false
    override fun onMapReady(p0: GoogleMap) {
        // findPlacesWithinRadius("hospitals")
    }
}