package com.example.languageribbon_front

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MyApi {
    @Multipart
    @POST("uploadvoice/")
    fun uploadAudioFile(
        @Part userid: MultipartBody.Part,
        @Part audio: MultipartBody.Part,
        @Part lang: MultipartBody.Part
    ): Call<ServerResponse>
}
