package com.example.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.data.model.BangladeshCities
import com.example.data.model.CityLocation
import com.example.data.model.OpenMeteoResponse
import com.example.data.model.WeatherInfo
import com.example.data.network.WeatherApiService
import com.example.util.CalendarUtils
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository(
    private val context: Context,
    private val apiService: WeatherApiService = WeatherApiService.create()
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _weatherState = MutableStateFlow(WeatherInfo())
    val weatherState: StateFlow<WeatherInfo> = _weatherState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentSelectedCity: CityLocation = BangladeshCities.defaultCity

    init {
        // Initial fetch with default city
        refreshWeather()
    }

    fun selectCity(city: CityLocation) {
        currentSelectedCity = city
        fetchWeather(city.lat, city.lon, city.nameBn, isLiveGps = false)
    }

    fun refreshWeather() {
        fetchWeather(
            currentSelectedCity.lat,
            currentSelectedCity.lon,
            currentSelectedCity.nameBn,
            isLiveGps = _weatherState.value.isLiveGps
        )
    }

    fun fetchWeather(
        lat: Double,
        lon: Double,
        cityName: String,
        isLiveGps: Boolean
    ) {
        repositoryScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getForecast(latitude = lat, longitude = lon)
                val mappedInfo = mapResponseToWeatherInfo(response, cityName, lat, lon, isLiveGps)
                _weatherState.value = mappedInfo
            } catch (e: Exception) {
                // If network fails, maintain sensible fallback data with updated time
                val current = _weatherState.value
                val fallbackCondition = mapWeatherCodeToCondition(current.weatherCode, current.temperature, current.isDay)
                _weatherState.value = current.copy(
                    cityName = cityName,
                    latitude = lat,
                    longitude = lon,
                    isLiveGps = isLiveGps,
                    conditionTextBn = fallbackCondition.textBn,
                    conditionEmoji = fallbackCondition.emoji,
                    adviceTextBn = fallbackCondition.adviceBn,
                    lastUpdatedTime = formatCurrentTime()
                )
                _errorMessage.value = "আবহাওয়া আপডেট করতে সমস্যা হয়েছে (${e.localizedMessage ?: "অফলাইন"})"
            } finally {
                _isLoading.value = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentGpsLocationWeather(): Boolean = withContext(Dispatchers.IO) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return@withContext false
        }

        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val detectedCityName = getCityNameFromCoordinates(lat, lon) ?: "বর্তমান অবস্থান"
                    fetchWeather(lat, lon, detectedCityName, isLiveGps = true)
                } else {
                    // Fallback to LocationManager
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    val gpsLoc = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

                    if (gpsLoc != null) {
                        val lat = gpsLoc.latitude
                        val lon = gpsLoc.longitude
                        val detectedCityName = getCityNameFromCoordinates(lat, lon) ?: "বর্তমান অবস্থান"
                        fetchWeather(lat, lon, detectedCityName, isLiveGps = true)
                    } else {
                        // Keep current city
                        refreshWeather()
                    }
                }
            }
            return@withContext true
        } catch (e: Exception) {
            return@withContext false
        }
    }

    private fun getCityNameFromCoordinates(lat: Double, lon: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale("bn", "BD"))
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "বর্তমান অবস্থান"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun mapResponseToWeatherInfo(
        response: OpenMeteoResponse,
        cityName: String,
        lat: Double,
        lon: Double,
        isLiveGps: Boolean
    ): WeatherInfo {
        val current = response.current
        val daily = response.daily

        val temp = current?.temperature2m ?: 30.5
        val feelsLike = current?.apparentTemperature ?: (temp + 3.0)
        val humidity = current?.relativeHumidity2m ?: 75
        val windSpeed = current?.windSpeed10m ?: 10.0
        val precipitation = current?.precipitation ?: 0.0
        val weatherCode = current?.weatherCode ?: 1
        val isDay = (current?.isDay ?: 1) == 1

        val maxTemp = daily?.temperature2mMax?.firstOrNull() ?: (temp + 3.0)
        val minTemp = daily?.temperature2mMin?.firstOrNull() ?: (temp - 4.5)

        val condition = mapWeatherCodeToCondition(weatherCode, temp, isDay)

        return WeatherInfo(
            cityName = cityName,
            latitude = lat,
            longitude = lon,
            temperature = temp,
            feelsLike = feelsLike,
            minTemp = minTemp,
            maxTemp = maxTemp,
            humidity = humidity,
            windSpeed = windSpeed,
            precipitation = precipitation,
            weatherCode = weatherCode,
            conditionTextBn = condition.textBn,
            conditionTextEn = condition.textEn,
            conditionEmoji = condition.emoji,
            isDay = isDay,
            isLiveGps = isLiveGps,
            adviceTextBn = condition.adviceBn,
            lastUpdatedTime = formatCurrentTime()
        )
    }

    private data class WeatherConditionData(
        val textBn: String,
        val textEn: String,
        val emoji: String,
        val adviceBn: String
    )

    private fun mapWeatherCodeToCondition(code: Int, temp: Double, isDay: Boolean): WeatherConditionData {
        return when (code) {
            0 -> WeatherConditionData(
                textBn = if (isDay) "পরিষ্কার রোদঝলমলে" else "পরিষ্কার রাত",
                textEn = "Clear Sky",
                emoji = if (isDay) "☀️" else "🌙",
                adviceBn = if (temp > 33.0) "আজ বেশ রোদ ও গরম থাকতে পারে, প্রচুর পানি পান করুন।" else "পরিষ্কার আকাশ, দিনটি দারুণ কাটুক!"
            )
            1, 2, 3 -> WeatherConditionData(
                textBn = "আংশিক মেঘলা",
                textEn = "Partly Cloudy",
                emoji = if (isDay) "⛅" else "☁️",
                adviceBn = "আবহাওয়া মনোরম ও মৃদু মেঘলা থাকতে পারে।"
            )
            45, 48 -> WeatherConditionData(
                textBn = "কুয়াশাচ্ছন্ন",
                textEn = "Foggy",
                emoji = "🌫️",
                adviceBn = "চারপাশে কুয়াশা থাকতে পারে, চলাচলের সময় খেয়াল রাখুন।"
            )
            51, 53, 55 -> WeatherConditionData(
                textBn = "গুঁড়ি গুঁড়ি বৃষ্টি",
                textEn = "Drizzle",
                emoji = "🌦️",
                adviceBn = "হালকা গুঁড়ি গুঁড়ি বৃষ্টি হতে পারে, ছাতা সাথে রাখলে ভালো হবে।"
            )
            61, 63, 65 -> WeatherConditionData(
                textBn = if (code == 65) "ভারী বৃষ্টিপাত" else "বৃষ্টিপাত",
                textEn = "Rainy",
                emoji = "🌧️",
                adviceBn = "বৃষ্টির সম্ভাবনা রয়েছে, বাইরে বের হলে অবশ্যই ছাতা সাথে রাখুন।"
            )
            71, 73, 75, 77 -> WeatherConditionData(
                textBn = "তুষারপাত / শৈত্যপ্রবাহ",
                textEn = "Snow",
                emoji = "❄️",
                adviceBn = "শীতল আবহাওয়া বিরাজ করছে, গরম কাপড় পরিধান করুন।"
            )
            80, 81, 82 -> WeatherConditionData(
                textBn = "বিক্ষিপ্ত বৃষ্টি",
                textEn = "Rain Showers",
                emoji = "🌦️",
                adviceBn = "হঠাৎ পশলা বৃষ্টি হতে পারে, সতর্ক থাকুন।"
            )
            95, 96, 99 -> WeatherConditionData(
                textBn = "বজ্রঝড় ও বৃষ্টি",
                textEn = "Thunderstorm",
                emoji = "⛈️",
                adviceBn = "বজ্রসহ বৃষ্টির সম্ভাবনা রয়েছে, নিরাপদ স্থানে অবস্থান করুন।"
            )
            else -> WeatherConditionData(
                textBn = "স্বাভাবিক আবহাওয়া",
                textEn = "Normal",
                emoji = "🌤️",
                adviceBn = "আবহাওয়া স্বাভাবিক থাকবে।"
            )
        }
    }

    private fun formatCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}
