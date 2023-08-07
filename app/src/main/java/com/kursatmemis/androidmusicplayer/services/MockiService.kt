package com.kursatmemis.androidmusicplayer.services

import com.kursatmemis.androidmusicplayer.models.MockiResponse
import retrofit2.Call
import retrofit2.http.GET

interface MockiService {

    @GET("v1/f27fbefc-d775-4aee-8d65-30f76f1f7109")
    fun getResponse(): Call<MockiResponse>

}