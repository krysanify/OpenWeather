package com.krysanify.openweather

import android.location.Location
import com.krysanify.openweather.BuildConfig.openweathermap_key
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object WeatherMap {

    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(Service::class.java)
    }

    fun currentByLocation(location: Location) {
        val call = service.getByLanLtg(location.latitude, location.longitude)
        call.enqueue(QueueCall())
    }

    class QueueCall : Callback<CurrentInfo> {
        override fun onFailure(call: Call<CurrentInfo>, t: Throwable) {
            TODO("not implemented")
        }

        override fun onResponse(call: Call<CurrentInfo>, response: Response<CurrentInfo>) {
            TODO("not implemented")
        }
    }

    interface Service {
        @GET("2.5/weather?appId=$openweathermap_key")
        fun getByLanLtg(@Query("lat") lat: Double, @Query("lon") lng: Double): Call<CurrentInfo>
    }
}