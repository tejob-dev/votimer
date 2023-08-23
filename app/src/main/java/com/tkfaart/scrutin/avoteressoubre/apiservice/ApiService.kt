package com.tkfaart.scrutin.avoteressoubre.apiservice

import com.tkfaart.scrutin.avoteressoubre.models.ImagePvModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiService {

    // region SYNCHRONIZATION
    @POST("exportpvonline")
    fun passImagePvOnline(@Body producteurModel: ImagePvModel): Call<ImagePvModel>

}
