package com.tkfaart.scrutin.avoteressoubre.models

data class RabatteurModel(
    var id: String? = null,
    var nomPrenoms: String? = null,
    var zone: Int? = 0
){

    override fun toString(): String{
        return "${id} - ${nomPrenoms}"
    }

}
