package com.example.zanieczyszczeniepowietrza

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zanieczyszczeniepowietrza.instances.RestIndex
import com.example.zanieczyszczeniepowietrza.instances.Zanieczyszczenie
import com.example.zanieczyszczeniepowietrza.instances.ZanieczyszczenieRepo
import com.example.zanieczyszczeniepowietrza.instances.RetrofitInstance
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileNotFoundException

class ZanieczyszczenieViewModel : ViewModel() {
    private val repository = ZanieczyszczenieRepo()
    private val _isLoading = MutableStateFlow(true)
    private val _zanieczyszczenieList = MutableStateFlow<List<Zanieczyszczenie>>(emptyList())
    private val _zanieczyszczenia = MutableStateFlow<FirebaseOnlyData>(FirebaseOnlyData())
    val zanieczyszczenia = _zanieczyszczenia.asStateFlow()

    fun getZanieczyszczenie() {
        viewModelScope.launch {
            _zanieczyszczenieList.value = repository.getZanieczyszczenie()
        }
    }

    fun saveJsonToFile(context: Context, fileName: String, stationList: List<Zanieczyszczenie>) {
        try {
            val gson = Gson()
            val jsonString = gson.toJson(stationList)
            val file = File(context.filesDir, fileName)
            file.writeText(jsonString)
            println("Zapisano dane do pliku: ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Błąd zapisu pliku JSON: ${e.message}")
        }
    }

    fun readJsonFromFile(context: Context, fileName: String): String? {
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                file.readText()
            } else {
                println("Plik nie istnieje.")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun firstApiQuerry(context: Context, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                withTimeout(30000) {
                    val response = RetrofitInstance.api.searchZanieczyszczenie()
                    saveJsonToFile(context, "stations.json", response)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    "Błąd pobierania danych: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                _isLoading.value = false
                onComplete?.invoke()
            }
        }
    }

    fun showStationStats(id: String, onResult: (RestIndex?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.ZanieczyszczeniePoID(id)
                onResult(response)
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                onResult(null)
            }
        }
    }


    fun getSavedSelection(context: Context): Pair<String, String> {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val selectedProvince = sharedPreferences.getString("selectedProvince", "Wybierz województwo") ?: "Wybierz województwo"
        val selectedCity = sharedPreferences.getString("selectedCity", "Wybierz miasto") ?: "Wybierz miasto"
        return Pair(selectedProvince, selectedCity)
    }

    suspend fun getFirebaseData() : FirebaseOnlyData {
        val firestore = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        var zanieczyszczenia = FirebaseOnlyData()
        try {
            if(user != null){
                val snapshot = firestore.collection("users")
                    .document(user.uid)
                    .collection("zanieczyszczenie")
                    .document(user.uid)
                    .get()
                    .await()
                zanieczyszczenia = snapshot.toObject(FirebaseOnlyData::class.java)!!
                println("ZANIECZYSZCZENIA ${zanieczyszczenia}")
            }
            return zanieczyszczenia
        }catch (e: Exception){
            return zanieczyszczenia
        }
    }

    fun fetchZanieczyszczenia(){
        viewModelScope.launch {
            val result = getFirebaseData()
            _zanieczyszczenia.value = result
        }
    }
}
