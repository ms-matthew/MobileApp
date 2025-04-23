package com.example.zanieczyszczeniepowietrza

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import com.example.zanieczyszczeniepowietrza.instances.RestIndex
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.example.zanieczyszczeniepowietrza.ui.theme.ZanieczyszczeniePowietrzaTheme
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.SetOptions
import kotlin.math.roundToInt
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

class MainScreenActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: ZanieczyszczenieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, NotificationService::class.java)
        startService(intent)
        auth = FirebaseAuth.getInstance()
        viewModel = ZanieczyszczenieViewModel()
        setContent {
            ZanieczyszczeniePowietrzaTheme {
                AppContent(modifier = Modifier)
            }
        }
    }
    @Composable
    fun AppContent(modifier: Modifier) {
        var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }

        if (!isLoggedIn) {
            navigateToLogin()
        } else {
            MainScreen(
                modifier = modifier,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    isLoggedIn = false
                }
            )
        }
    }
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @Composable
    fun MainScreen(modifier: Modifier, onLogout: () -> Unit) {
        var permissionGranted by remember { mutableStateOf(checkNotificationPermission()) }
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            permissionGranted = isGranted
            if (!isGranted) {
                Toast.makeText(
                    this,
                    "Brak zezwolenia ! Nie można wyświetlić powiadomienia.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val viewmodel : ZanieczyszczenieViewModel = viewModel()
        val zanieczyszczenia by viewmodel.zanieczyszczenia.collectAsState()
        val context = LocalContext.current
        val test = remember { ZanieczyszczenieViewModel() }
        val (savedProvince, savedCity) = test.getSavedSelection(context)
        val filejsonList = test.readJsonFromFile(context, "stations.json") ?: return
        val firestore = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val gson = Gson()
        val provincesToChoose = remember(filejsonList) {
            sortPolish(extractProvinceNames(filejsonList, gson, "provinceName"))
        }
        var expandedStateProvince by remember { mutableStateOf(false) }
        var expandedStateCity by remember { mutableStateOf(false) }
        var selectedProvince by remember { mutableStateOf(savedProvince) }
        var selectedCity by remember { mutableStateOf(savedCity) }
        val scrollToMap = rememberScrollState()
        var Mapa by remember { mutableStateOf(true) }
        var pixels = 120.dp
        var TekstButtonMapa = "Mapa"
        if(Mapa){
            pixels = 550.dp
        }else{
            pixels = 120.dp
            TekstButtonMapa = "Stacje"
        }
        val citiesToChoose = remember(selectedProvince, filejsonList) {
            if (selectedProvince != "Wybierz województwo") {
                extractCitiesForProvince(filejsonList, gson, selectedProvince).let { cities ->
                    if (cities.isEmpty()) {
                        listOf("Brak dostępnych miast")
                    } else {
                        sortPolish(cities)
                    }
                }
            } else {
                emptyList()
            }
        }
        LaunchedEffect(selectedProvince) {
            println("Selected Province: $selectedProvince")
            println("Available cities: ${citiesToChoose.joinToString()}")

        }
        LaunchedEffect(Unit){
            if(!permissionGranted){
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            viewmodel.fetchZanieczyszczenia()
        }

        LaunchedEffect(zanieczyszczenia) {
            if(zanieczyszczenia.city != ""){
                selectedCity = zanieczyszczenia.city
                selectedProvince = zanieczyszczenia.province
            }
        }
        fun parseStation(stationString: String): Station {
            val parts = stationString.split("|")
            return Station(parts[0], parts[1].toDouble(), parts[2].toDouble())
        }

        val stations = remember(selectedProvince, selectedCity, filejsonList) {
            extractStations(filejsonList, gson, selectedProvince, selectedCity).map {
                parseStation(it)
            }
        }
        val stationsID = remember(selectedProvince, selectedCity, filejsonList) {
            extractStationID(filejsonList, gson, selectedProvince, selectedCity)
        }
        LaunchedEffect(Unit) {
            auth.currentUser?.let { user ->
                firestore.collection("users")
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            selectedProvince = document.getString("province") ?: "Wybierz województwo"
                            selectedCity = document.getString("city") ?: "Wybierz miasto"
                        }
                    }
            }
        }
        fun saveToFirebase(province: String, city: String) {
            auth.currentUser?.let { user ->
                val userData = hashMapOf(
                    "province" to province,
                    "city" to city
                )
                firestore.collection("users")
                    .document(user.uid).collection("zanieczyszczenie").document(user.uid)
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener {
                    }
            }
        }


            Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF123524).copy(alpha = 0.8f),
                            Color(0xFF6E8E59).copy(alpha = 1f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                CustomDropdown(
                    text = selectedProvince,
                    onClick = { expandedStateProvince = true },
                    dropdownItems = provincesToChoose,
                    onItemSelected = { province ->
                        selectedProvince = province
                        selectedCity = "Wybierz miasto"
                        saveToFirebase(province, "Wybierz miasto")
                    },
                    expanded = expandedStateProvince,
                    onDismiss = { expandedStateProvince = false }
                )

                CustomDropdown(
                    text = selectedCity,
                    onClick = { expandedStateCity = true },
                    dropdownItems = citiesToChoose,
                    onItemSelected = { city ->
                        selectedCity = city
                        saveToFirebase(selectedProvince, city)
                    },
                    expanded = expandedStateCity,
                    onDismiss = { expandedStateCity = false }
                )

                if (selectedProvince != "Wybierz województwo" && selectedCity != "Wybierz miasto") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .height(pixels)
                                .verticalScroll(scrollToMap),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                "Stacje pomiarowe",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            if (selectedProvince != "Wybierz województwo" && selectedCity != "Wybierz miasto") {
                                if (stations.isEmpty()) {
                                    Text("Brak stacji dla wybranego województwa i miasta")
                                }
                                stations.zip(stationsID).forEachIndexed { index, (station, stationID) ->
                                    var stationStats by remember { mutableStateOf<RestIndex?>(null) }
                                    val isVisible = remember { mutableStateOf(false) }

                                    LaunchedEffect(index) {
                                        kotlinx.coroutines.delay(index * 100L)
                                        isVisible.value = true
                                    }

                                    AnimatedVisibility(
                                        visible = isVisible.value,
                                        enter = slideInHorizontally(
                                            initialOffsetX = { -it },
                                            animationSpec = tween(durationMillis = 500)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = station.stationName,
                                                color = Color.White,
                                                fontFamily = firaSansFamily,
                                                fontSize = 20.sp
                                            )

                                            LaunchedEffect(stationID) {
                                                stationStats = null
                                                test.showStationStats(stationID) { stats ->
                                                    stationStats = stats
                                                }
                                            }

                                            stationStats?.stIL?.let { indexLevel ->
                                                Text(
                                                    "Indeks stacji: ${indexLevel.indexLevelName ?: "N/A"}",
                                                    color = Color.White,
                                                    modifier = Modifier.padding(top = 4.dp),
                                                    fontFamily = firaSansFamily,
                                                    fontSize = 15.sp
                                                )
                                                AirQualityIndexBar(indexLevel.indexLevelName)
                                            } ?: run {
                                                ShimmerPlaceholder(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(50.dp)
                                                        .padding(vertical = 8.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }

                    }
                    if(!Mapa){
                        Row(
                            modifier = Modifier.height(415.dp)
                        ) {
                            LeafletMapView(stations = stations)
                        }
                    }
                    if (selectedProvince != "Wybierz województwo" && selectedCity != "Wybierz miasto") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FancyLoginButton(
                                text = "Wyloguj",
                                onClick = onLogout,
                                modifier = Modifier.weight(1f)
                            )
                            FancyLoginButton(
                                text = TekstButtonMapa,
                                onClick = {
                                    Mapa = !Mapa

                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }


            }
        }
    }

    @SuppressLint("UnusedBoxWithConstraintsScope")
    @Composable
    fun AirQualityIndexBar(indexLevelName: String?) {
        val indicatorPosition = when (indexLevelName) {
            "Bardzo dobry" -> 90f
            "Dobry" -> 270f
            "Umiarkowany" -> 450f
            "Dostateczny" -> 630f
            "Zły" -> 810f
            "Bardzo zły" -> 900f
            else -> -1f
        }
        val offsetX = remember { Animatable(0f) }
        if(indicatorPosition != -1f){
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(12.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50),
                                    Color(0xFFFFEB3B),
                                    Color(0xFFF44336)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
                Card(
                    colors = CardColors(Color.Black, Color.Blue, Color.Blue, Color.Blue),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                ){
                    LaunchedEffect(indicatorPosition) { offsetX.animateTo(indicatorPosition, animationSpec = tween(500)) }
                    Log.d("OffsetX: ", offsetX.value.toString())
                }
            }
        }
    }


    @Composable
    fun CustomDropdown(
        text: String,
        onClick: () -> Unit,
        dropdownItems: List<String>,
        onItemSelected: (String) -> Unit,
        expanded: Boolean,
        onDismiss: () -> Unit,
        enabled: Boolean = true
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            FancyLoginButton(
                text = text,
                onClick = onClick,
                enabled = enabled
            )

            DropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = onDismiss
            ) {
                dropdownItems.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onItemSelected(item)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            this,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        startActivity(intent, options.toBundle())
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }
}

private fun updateStationMarkers(webView: WebView, stations: List<Station>) {
    val stationsJson = stations.map { station ->
        """
        {
            "lat": ${station.gegrLat},
            "lon": ${station.gegrLon},
            "name": "${station.stationName}"
        }
        """.trimIndent()
    }.joinToString(",", "[", "]")
    webView.evaluateJavascript(
        "updateMarkers($stationsJson);",
        null
    )
}

@Composable
fun LeafletMapView(
    stations: List<Station>,
    modifier: Modifier = Modifier
) {
    var isMapLoaded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            updateStationMarkers(this@apply, stations)
                            isMapLoaded = true
                        }
                    }
                    loadUrl("file:///android_asset/map.html")
                }
            },
            update = { webView ->
                updateStationMarkers(webView, stations)
            }
        )

        AnimatedVisibility(
            visible = !isMapLoaded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LoadingScreen()
        }
    }
}

@Composable
fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val translateAnimation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.LightGray.copy(alpha = 0.4f),
            Color.Gray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.4f)
        ),
        start = Offset(translateAnimation.value * 300f, 0f),
        end = Offset(translateAnimation.value * 300f + 300f, 0f),
        tileMode = TileMode.Mirror

    )

    Box(
        modifier = modifier
            .background(brush = shimmerBrush, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    )
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF123524).copy(alpha = 0.8f),
                        Color(0xFF6E8E59).copy(alpha = 1f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Ładowanie danych...",
                color = Color.White,
                fontSize = 20.sp,
                fontFamily = firaSansFamily
            )
        }
    }
}