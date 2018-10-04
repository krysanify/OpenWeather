package com.krysanify.openweather

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

private val icons = mapOf(
    "01d" to R.drawable.ic_01d,
    "01n" to R.drawable.ic_01n,
    "02d" to R.drawable.ic_02d,
    "02n" to R.drawable.ic_02n,
    "03d" to R.drawable.ic_03d,
    "03n" to R.drawable.ic_03n,
    "04d" to R.drawable.ic_04d,
    "04n" to R.drawable.ic_04n,
    "09d" to R.drawable.ic_09d,
    "09n" to R.drawable.ic_09n,
    "10d" to R.drawable.ic_10d,
    "10n" to R.drawable.ic_10n,
    "11d" to R.drawable.ic_11d,
    "11n" to R.drawable.ic_11n,
    "13d" to R.drawable.ic_13d,
    "13n" to R.drawable.ic_13n,
    "50d" to R.drawable.ic_50d,
    "50n" to R.drawable.ic_50n
)

private fun String.fromResource() = BitmapDescriptorFactory.fromResource(icons[this]!!)

data class CurrentInfo(
    val coord: LonLat,
    val weather: List<Weather>,
    val base: String,
    val main: MainInfo,
    val wind: Wind,
    val clouds: Clouds,
    val rain: Rain,
    val snow: Snow,
    val dt: Long,
    val sys: SysInfo,
    val id: Int,
    val name: String,
    val cod: Int
) {
    fun coordinate() = LatLng(coord.lat, coord.lon)
    fun toMarker(): MarkerOptions {
        val coordinate = LatLng(coord.lat, coord.lon)
        val desc = weather[0].description
        val bmp = weather[0].icon.fromResource()
        return MarkerOptions()
            .position(coordinate)
            .title(name)
            .snippet(desc)
            .icon(bmp)
    }
}

data class LonLat(val lon: Double, val lat: Double)

data class Weather(val id: Int, val main: String, val description: String, val icon: String)

data class MainInfo(
    val temp: Double,
    val pressure: Double,
    val humidity: Double,
    val temp_min: Double,
    val temp_max: Double,
    val sea_level: Double,
    val grnd_level: Double
)

data class Wind(val speed: Double, val deg: Double)

data class Clouds(val all: Double)

data class Rain(val `3h`: Double)

data class Snow(val `3h`: Double)

data class SysInfo(
    val type: Int,
    val id: Int,
    val message: Double,
    val country: String,
    val sunrise: Long,
    val sunset: Long
)
