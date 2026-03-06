package com.example.swifty_protein.model

class User {
    var userName = ""
    var idLeg = mutableListOf<Number>()

    fun getUserName(): String{
        return userName
    }

    fun getIdLeg(): List<Number>{
        return idLeg
    }

    fun addIdLeg(id: Number){
        idLeg.add(id)
    }

    fun removeIdLeg(id: Number){
        idLeg.remove(id)
    }

}

