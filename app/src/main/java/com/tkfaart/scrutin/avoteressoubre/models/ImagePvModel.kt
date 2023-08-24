package com.tkfaart.scrutin.avoteressoubre.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize

@Parcelize
public final data class ImagePvModel(
    @Expose var lieuVoteId: String? = null,
    @Expose var bureauVoteId: String? = null,
    @Expose var imageContent: String? = null,
    @Expose var type: String? = "PV",

):Parcelable{

}
