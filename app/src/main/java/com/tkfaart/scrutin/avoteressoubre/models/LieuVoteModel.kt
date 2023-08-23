package com.tkfaart.scrutin.avoteressoubre.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
public final data class LieuVoteModel(
    var id: String? = null,
    var libel: String? = null
):Parcelable{

    override fun toString(): String{
        return "${id} - ${libel}"
    }

}
