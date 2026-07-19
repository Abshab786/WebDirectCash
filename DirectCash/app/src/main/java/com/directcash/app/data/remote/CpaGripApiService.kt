package com.directcash.app.data.remote

import okhttp3.OkHttpClient
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi

interface CpaGripApiService {
    @GET("common/offer_feed_json.php")
    suspend fun getOffers(
        @Query("user_id") userId: String = "2539830",
        @Query("pubkey") pubKey: String = "8e626368619ce46a337494d71f5b55ab"
    ): CpaGripResponse

    companion object {
        private const val BASE_URL = "https://www.cpagrip.com/"

        fun create(): CpaGripApiService {
            val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36")
                        .header("Accept", "application/json")
                        .build()
                    chain.proceed(request)
                }
                .build()

            val moshi = Moshi.Builder()
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(CpaGripApiService::class.java)
        }
    }
}
