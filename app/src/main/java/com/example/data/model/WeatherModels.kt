package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "current") val current: OpenMeteoCurrent? = null,
    @Json(name = "daily") val daily: OpenMeteoDaily? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    @Json(name = "temperature_2m") val temperature2m: Double? = null,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
    @Json(name = "is_day") val isDay: Int? = null,
    @Json(name = "precipitation") val precipitation: Double? = null,
    @Json(name = "weather_code") val weatherCode: Int? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>? = null,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>? = null,
    @Json(name = "sunrise") val sunrise: List<String>? = null,
    @Json(name = "sunset") val sunset: List<String>? = null
)

data class WeatherInfo(
    val cityName: String = "ঢাকা",
    val latitude: Double = 23.8103,
    val longitude: Double = 90.4125,
    val temperature: Double = 31.0,
    val feelsLike: Double = 34.0,
    val minTemp: Double = 26.5,
    val maxTemp: Double = 33.5,
    val humidity: Int = 74,
    val windSpeed: Double = 10.5,
    val precipitation: Double = 0.0,
    val weatherCode: Int = 1,
    val conditionTextBn: String = "আংশিক মেঘলা",
    val conditionTextEn: String = "Partly Cloudy",
    val conditionEmoji: String = "⛅",
    val isDay: Boolean = true,
    val isLiveGps: Boolean = false,
    val adviceTextBn: String = "আবহাওয়া মনোরম, দিনটি সুন্দরভাবে উপভোগ করুন।",
    val lastUpdatedTime: String = "এইমাত্র"
)

data class CityLocation(
    val nameBn: String,
    val nameEn: String,
    val lat: Double,
    val lon: Double
)

object BangladeshCities {
    val defaultCity = CityLocation("ঢাকা", "Dhaka", 23.8103, 90.4125)

    val list = listOf(
        CityLocation("ঢাকা", "Dhaka", 23.8103, 90.4125),
        CityLocation("চট্টগ্রাম", "Chattogram", 22.3569, 91.7832),
        CityLocation("সিলেট", "Sylhet", 24.8949, 91.8687),
        CityLocation("রাজশাহী", "Rajshahi", 24.3745, 88.6042),
        CityLocation("খুলনা", "Khulna", 22.8456, 89.5403),
        CityLocation("বরিশাল", "Barishal", 22.7010, 90.3535),
        CityLocation("রংপুর", "Rangpur", 25.7439, 89.2752),
        CityLocation("ময়মনসিংহ", "Mymensingh", 24.7471, 90.4203),
        CityLocation("কক্সবাজার", "Cox's Bazar", 21.4272, 92.0058),
        CityLocation("কুমিল্লা", "Cumilla", 23.4607, 91.1809),
        CityLocation("বগুড়া", "Bogura", 24.8465, 89.3777),
        CityLocation("যশোর", "Jashore", 23.1664, 89.2081),
        CityLocation("দিনাজপুর", "Dinajpur", 25.6217, 88.6355),
        CityLocation("পাবনা", "Pabna", 24.0064, 89.2372),
        CityLocation("গাজীপুর", "Gazipur", 23.9999, 90.4203),
        CityLocation("নারায়ণগঞ্জ", "Narayanganj", 23.6238, 90.5000)
    )
}
