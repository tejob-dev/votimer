package com.tkfaart.scrutin.avoteressoubre.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
public final data class ElectoratModel(
    var id: String? = null,
    var nomPrenoms: String? = null,
    var zone: Int? = 0
):Parcelable{

    override fun toString(): String{
        return "${id!!.split("-").get(1)?:"??"} - ${nomPrenoms}"
    }

}
