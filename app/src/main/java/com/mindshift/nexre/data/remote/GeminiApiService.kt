package com.mindshift.nexre.data.remote

import com.mindshift.nexre.data.remote.model.GeminiRequest
import com.mindshift.nexre.data.remote.model.GeminiResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface GeminiApiService {

    @POST
    suspend fun generateContent(
        @Url url: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest,
    ): Response<GeminiResponse>

    @GET("v1beta/models")
    suspend fun listModels(
        @Query("key") apiKey: String,
    ): Response<ResponseBody>
}
