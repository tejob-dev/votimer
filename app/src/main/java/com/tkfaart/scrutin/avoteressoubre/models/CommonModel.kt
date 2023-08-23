package com.tkfaart.scrutin.avoteressoubre.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
public final data class CommonModel(
    var id: String? = null,
    var libel: String? = null,
    var otherId: String? = "0",
):Parcelable{

    override fun toString(): String{
        return "${id} - ${libel}"
    }

}
