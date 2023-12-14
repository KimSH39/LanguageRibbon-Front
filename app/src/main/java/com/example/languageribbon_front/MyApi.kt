package com.example.languageribbon_front

import okhttp3.MultipartBody
import okhttp3.ResponseBody
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

    @Multipart
    @POST("translate/to_voice")
    fun translate(
        @Part lang: MultipartBody.Part,
        @Part targetlang: MultipartBody.Part,
        @Part userid: MultipartBody.Part,
        @Part audio: MultipartBody.Part
    ):  Call<ResponseBody>
}
