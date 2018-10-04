package com.krysanify.openweather

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
)

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
