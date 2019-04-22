package com.example.data.network

import com.example.data.core.Failure
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by jsmirabal on 4/21/2019.
 */
object Service {

    data class Params(
        val startDate: String,
        val endDate: String,
        val base: String,
        val symbols: String
    )

    fun fetchHistory(params: Params) = withRetrofit(params)

    private fun withRetrofit(params: Params) =
        RetrofitService.fetchHistory(params)


    private object RetrofitService {
        private val retrofit: Retrofit by lazy {
            val createClient = {
                val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
                okHttpClientBuilder.build()
            }
            val gson = GsonBuilder()
                .registerTypeAdapter(HistoryResponse::class.java, HistoryDeserializer())
                .create()

            Retrofit.Builder()
                .baseUrl("https://api.exchangeratesapi.io/")
                .client(createClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        private val currencyService = retrofit.create(CurrencyApi::class.java)

        fun fetchHistory(params: Params): Pair<Failure?, HistoryResponse?> =
            currencyService
                .fetchHistory(params.startDate, params.endDate, params.base, params.symbols)
                .execute().run {
                    when {
                        isSuccessful -> body()?.run { Pair(null, this) } ?: Pair(Failure.ApiFailure("Empty body"), null)
                        else -> Pair(Failure.ApiFailure(errorBody()?.string() ?: "Unknown API Error"), null)
                    }
                }
    }
}