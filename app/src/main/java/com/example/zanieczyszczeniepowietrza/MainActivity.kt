package com.example.zanieczyszczeniepowietrza

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.zanieczyszczeniepowietrza.ui.theme.ZanieczyszczeniePowietrzaTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.JsonArray
import java.text.Collator
import java.util.Locale
import android.content.Intent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.core.app.ActivityOptionsCompat

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: ZanieczyszczenieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        var isLoading by remember { mutableStateOf(false) }

        if (isLoading) {
            LoadingScreen()
        } else if (!isLoggedIn) {
            LogRegScreen(
                modifier = modifier,
                onLoginSuccess = {
                    isLoading = true
                    viewModel.firstApiQuerry(
                        this@LoginActivity,
                        onComplete = {
                            isLoggedIn = true
                            isLoading = false
                        }
                    )
                },
                onError = {
                    isLoading = false
                }
            )
        } else {
            navigateToMainScreen()
        }
    }
    @Composable
    fun LogRegScreen(
        modifier: Modifier,
        onLoginSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val context = LocalContext.current
        var isLoading by remember { mutableStateOf(false) }
        val auth = FirebaseAuth.getInstance()
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isRegistering by remember { mutableStateOf(false) }
        val annotatedTextLogin = buildAnnotatedString {
            append("Masz już konto? ")
            withStyle(style = SpanStyle(color = Color.White, textDecoration = TextDecoration.Underline)) {
                pushStringAnnotation(tag = "login", annotation = "login")
                append("Zaloguj")
                pop()
            }
        }
        val annotatedTextRegister = buildAnnotatedString {
            append("Nie masz jeszcze konta? ")
            withStyle(style = SpanStyle(color = Color.White, textDecoration = TextDecoration.Underline)) {
                pushStringAnnotation(tag = "register", annotation = "register")
                append("Zarejestruj")
                pop()
            }
        }

        Box(
            modifier = with(Modifier) {
                fillMaxSize()
                    .paint(
                        painterResource(id = R.drawable.background_bigger),
                        contentScale = ContentScale.FillHeight
                    )
            }
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email"
                )
                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Hasło",
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))

                FancyLoginButton(
                    text = if (isRegistering) "Zarejestruj" else "Zaloguj",
                    onClick = {
                        if (email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(context, "Proszę podać email i hasło", Toast.LENGTH_LONG).show()
                        } else {
                            isLoading = true
                            if (isRegistering) {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            onLoginSuccess()
                                        } else {
                                            isLoading = false
                                            onError()
                                            Toast.makeText(context, "Błąd rejestracji: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        onError()
                                        Toast.makeText(context, "Błąd rejestracji: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            onLoginSuccess()
                                        } else {
                                            isLoading = false
                                            onError()
                                            Toast.makeText(context, "Błąd logowania: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        onError()
                                        Toast.makeText(context, "Błąd logowania: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                    },
                    modifier = Modifier,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier)

                if(isRegistering){
                    ClickableText(
                        text = annotatedTextLogin,
                        onClick = { offset ->
                            annotatedTextLogin.getStringAnnotations(offset, offset).firstOrNull()?.let { annotation ->
                                when (annotation.tag) {
                                    "login" -> {
                                        isRegistering = !isRegistering
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .align(Alignment.CenterHorizontally)
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        style = TextStyle(Color.White)
                    )
                }else{
                    ClickableText(
                        text = annotatedTextRegister,
                        onClick = { offset ->
                            annotatedTextRegister.getStringAnnotations(offset, offset).firstOrNull()?.let { annotation ->
                                when (annotation.tag) {
                                    "register" -> {
                                        isRegistering = !isRegistering
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .align(Alignment.CenterHorizontally)
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        style = TextStyle(Color.White)
                    )
                }
            }
        }
    }


    private fun navigateToMainScreen() {
        val intent = Intent(this, MainScreenActivity::class.java)
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

val firaSansFamily = FontFamily(
    Font(R.font.firasans_regular)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    defaultIcon: ImageVector = Icons.Default.Email,
    trailingIcon: ImageVector = Icons.Default.Close,
    passwordIcon: ImageVector = Icons.Default.Lock
) {
    var leadingIcon: ImageVector
    if(visualTransformation != VisualTransformation.None){
        leadingIcon = passwordIcon
    }else{
        leadingIcon = defaultIcon
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp)
                )
        )


        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = placeholder, color = Color.White) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Wyczyść"
                        )
                    }
                }
            },
            visualTransformation = visualTransformation,
            textStyle = TextStyle(color = Color.White, fontFamily = firaSansFamily, fontSize = 18.sp),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
            ),
            singleLine = true
        )
    }
}

@Composable
fun FancyLoginButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = if (enabled) scale else 1f
                scaleY = if (enabled) scale else 1f
            }
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        if (enabled) Color(0xFFFAFFC5) else Color.Gray,
                        if (enabled) Color(0xFFA9BFA8) else Color.LightGray
                    )
                ),
                shape = RoundedCornerShape(50)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (enabled) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    },
                    onTap = { if (enabled) onClick() }
                )
            }
            .shadow(8.dp, RoundedCornerShape(50))
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

fun extractProvinceNames(jsonString: String, gson: Gson, search: String): List<String> {
    val provinces = mutableSetOf<String>()

    fun searchRecursively(element: Any?) {
        when (element) {
            is Map<*, *> -> {
                element.forEach { (key, value) ->
                    if (key == search && value is String) {
                        provinces.add(value.toString())
                    }
                    if (value is Map<*, *> || value is List<*>) {
                        searchRecursively(value)
                    }
                }
            }
            is List<*> -> {
                element.forEach { searchRecursively(it) }
            }
        }
    }

    val jsonElement = gson.fromJson(jsonString, Any::class.java)
    searchRecursively(jsonElement)

    return provinces.toList()
}

fun extractCitiesForProvince(
    jsonString: String,
    gson: Gson,
    selectedProvince: String
): List<String> {
    val cities = mutableSetOf<String>()

    try {
        val jsonArray = gson.fromJson(jsonString, JsonArray::class.java)

        jsonArray.forEach { element ->
            val stationJson = element.asJsonObject
            val cityJson = stationJson.getAsJsonObject("city")
            val communeJson = cityJson?.getAsJsonObject("commune")

            val provinceName = communeJson?.get("provinceName")?.asString
            val cityName = cityJson?.get("name")?.asString

            if (provinceName == selectedProvince && cityName != null) {
                cities.add(cityName)
            }
        }
        println("Found ${cities.size} cities for province $selectedProvince")

    } catch (e: Exception) {
        println("Error parsing cities: ${e.message}")
        e.printStackTrace()
    }

    return cities.toList()
}

fun sortPolish(list: List<String>): List<String> {
    val collator = Collator.getInstance(Locale("pl", "PL"))
    return list.sortedWith(collator)
}
fun extractStations(
    jsonString: String,
    gson: Gson,
    selectedProvince: String,
    selectedCity: String
): List<String> {
    val stations = mutableListOf<String>()
    val jsonObject = gson.fromJson(jsonString, JsonArray::class.java)

    jsonObject.forEach { element ->
        val stationJson = element.asJsonObject
        val cityJson = stationJson.getAsJsonObject("city")
        val communeJson = cityJson?.getAsJsonObject("commune")

        val provinceName = communeJson?.get("provinceName")?.asString
        val cityName = cityJson?.get("name")?.asString
        val stationName = stationJson.get("stationName")?.asString
        val gegrLat = stationJson.get("gegrLat")?.asDouble
        val gegrLon = stationJson.get("gegrLon")?.asDouble
        val index = stationJson.get("index")?.asString

        if (provinceName == selectedProvince && cityName == selectedCity && stationName != null && gegrLat != null && gegrLon != null) {
            stations.add("$stationName|$gegrLat|$gegrLon|${index ?: "N/A"}")
        }
    }
    return stations
}


fun extractStationID(
    jsonString: String,
    gson: Gson,
    selectedProvince: String,
    selectedCity: String
): List<String> {
    val stationsID = mutableSetOf<String>()
    val jsonObject = gson.fromJson(jsonString, JsonArray::class.java)

    jsonObject.forEach { element ->
        val stationJson = element.asJsonObject
        val cityJson = stationJson.getAsJsonObject("city")
        val communeJson = cityJson?.getAsJsonObject("commune")

        val provinceName = communeJson?.get("provinceName")?.asString
        val cityName = cityJson?.get("name")?.asString
        val stationID = stationJson.get("id")?.asString

        if (provinceName == selectedProvince && cityName == selectedCity && stationID != null) {
            stationsID.add(stationID)
        }
    }
    return stationsID.toList()
}

