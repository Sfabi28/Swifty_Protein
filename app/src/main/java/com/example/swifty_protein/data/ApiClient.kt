package com.example.swifty_protein.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit


interface RcsbApiService {
    @GET("ligands/view/{ligand}.cif")
    suspend fun getLigandCif(@Path("ligand") ligandId: String): Response<String>
}
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

abstract class BaseApiClient {
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Failed to parse ligand data. The file may be corrupted.")
                }
            } else {
                if (response.code() == 404){
                    Resource.Error("Ligand not found (404). This ligand may not exist in the database")
                } else {
                    Resource.Error("Server error: ${response.code()}")
                }
            }
        } catch (e: SocketTimeoutException) {
            Resource.Error("Request timeout. Please try again.")
        } catch (e: IOException) {
            Resource.Error("Network error. Please check your internet connection.")
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.message}")
        }
    }
}

object ApiClient {
    private val BASE_URL = "https://files.rcsb.org/"
    private val loggingInterceptor = HttpLoggingInterceptor().apply{
        level = HttpLoggingInterceptor.Level.BASIC
    }
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }
    val rcsbService: RcsbApiService by lazy {
        retrofit.create(RcsbApiService::class.java)
    }

}