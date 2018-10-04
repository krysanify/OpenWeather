package com.krysanify.openweather

import android.location.Location
import com.krysanify.openweather.BuildConfig.openweathermap_key
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.ref.WeakReference

object WeatherMap {

    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(Service::class.java)
    }

    fun currentByLocation(location: Location, callback: Callback) {
        service.getByLanLtg(location.latitude, location.longitude)
            .enqueue(QueueCall(callback))
    }

    interface Callback {
        fun onCurrentWeather(info: CurrentInfo)
    }

    class QueueCall(callback: Callback) : retrofit2.Callback<CurrentInfo> {
        private val _callbackRef = WeakReference(callback)

        override fun onFailure(call: Call<CurrentInfo>, t: Throwable) {
            t.printStackTrace()
        }

        override fun onResponse(call: Call<CurrentInfo>, response: Response<CurrentInfo>) {
            val body = response.body() ?: return
            val callback = _callbackRef.get()
            _callbackRef.clear()
            callback?.onCurrentWeather(body)
        }
    }

    interface Service {
        @GET("2.5/weather?appId=$openweathermap_key")
        fun getByLanLtg(@Query("lat") lat: Double, @Query("lon") lng: Double): Call<CurrentInfo>
    }
}