package com.krysanify.openweather

import android.content.Context
import android.location.Location
import com.krysanify.openweather.BuildConfig.openweathermap_key
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.io.File
import java.lang.ref.WeakReference

object WeatherMap {
    private const val cacheSize = 10L * 1024 * 1024

    private val service by lazy {
        val client = OkHttpClient.Builder()
            .cache(Cache(cacheDir, cacheSize))
            .build()

        Retrofit.Builder()
            .client(client)
            .baseUrl("https://api.openweathermap.org/data/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(Service::class.java)
    }

    private lateinit var cacheDir: File

    fun init(context: Context) {
        cacheDir = context.cacheDir
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
        @Headers("Cache-Control: private, max-age=600, max-stale=600")
        fun getByLanLtg(@Query("lat") lat: Double, @Query("lon") lng: Double): Call<CurrentInfo>
    }
}