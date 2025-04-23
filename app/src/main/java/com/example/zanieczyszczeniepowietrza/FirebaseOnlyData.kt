package com.example.zanieczyszczeniepowietrza

data class FirebaseOnlyData(
    var province: String = "",
    var city: String = ""
) {
    constructor():this("","")
}
