package com.luxon.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luxon.app.ui.theme.LuxonTheme

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var sensorEventListener: SensorEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        setContent {
            LuxonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var luxValue by remember { mutableStateOf(0f) }
                    var showRecommendations by remember { mutableStateOf(false) }
                    var showInformation by remember { mutableStateOf(false) }
                    var isTestMode by remember { mutableStateOf(false) }
                    var testLuxValue by remember { mutableStateOf(300f) }
                    var showTestMode by remember { mutableStateOf(false) }

                    // Register sensor listener
                    LaunchedEffect(isTestMode) {
                        if (!isTestMode) {
                            sensorEventListener = object : SensorEventListener {
                                override fun onSensorChanged(event: SensorEvent?) {
                                    event?.let {
                                        if (it.sensor.type == Sensor.TYPE_LIGHT) {
                                            luxValue = it.values[0]
                                        }
                                    }
                                }

                                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                            }
                            lightSensor?.let {
                                sensorManager.registerListener(
                                    sensorEventListener,
                                    it,
                                    SensorManager.SENSOR_DELAY_NORMAL
                                )
                            }
                        } else {
                            sensorEventListener?.let {
                                sensorManager.unregisterListener(it)
                            }
                        }
                    }
                    
                    // Update lux value based on mode
                    val currentLuxValue = if (isTestMode) testLuxValue else luxValue

                    // Cleanup on dispose
                    DisposableEffect(Unit) {
                        onDispose {
                            sensorEventListener?.let {
                                sensorManager.unregisterListener(it)
                            }
                        }
                    }

                    MainScreen(
                        luxValue = currentLuxValue,
                        isTestMode = isTestMode,
                        onRecommendationsClick = { showRecommendations = true },
                        onInformationClick = { showInformation = true },
                        onTestModeClick = { showTestMode = true },
                        onExitClick = { finish() }
                    )

                    if (showRecommendations) {
                        RecommendationsDialog(
                            onDismiss = { showRecommendations = false },
                            luxValue = currentLuxValue
                        )
                    }

                    if (showInformation) {
                        InformationDialog(
                            onDismiss = { showInformation = false }
                        )
                    }
                    
                    if (showTestMode) {
                        TestModeDialog(
                            isTestMode = isTestMode,
                            testLuxValue = testLuxValue,
                            onTestModeToggle = { 
                                isTestMode = it
                                if (it) {
                                    luxValue = testLuxValue
                                }
                            },
                            onLuxValueChange = { 
                                testLuxValue = it
                                if (isTestMode) {
                                    luxValue = it
                                }
                            },
                            onDismiss = { showTestMode = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    luxValue: Float,
    isTestMode: Boolean,
    onRecommendationsClick: () -> Unit,
    onInformationClick: () -> Unit,
    onTestModeClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val lightStatus = getLightStatus(luxValue)
    val animatedProgress by animateFloatAsState(
        targetValue = calculateProgress(luxValue),
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Logo and Test Mode Indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color(0xFFD4AF63))) {
                        append("LUX")
                    }
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.Black)) {
                        append("ON")
                    }
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            if (isTestMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = Color(0xFFD4AF63),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Test Modu",
                        fontSize = 12.sp,
                        color = Color(0xFFD4AF63),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Content Card with Gauge
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Gauge with Desk Icon
                Box(
                    modifier = Modifier.size(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularGauge(
                        progress = animatedProgress,
                        color = lightStatus.color,
                        luxValue = luxValue.toInt()
                    )
                    
                    // Desk lamp icon button - white circle with black icon
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier
                            .size(48.dp)
                            .offset(x = 100.dp, y = (-20).dp)
                            .shadow(2.dp, CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Status Message
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFD4AF63),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = lightStatus.message,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = lightStatus.textColor,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = lightStatus.description,
                    fontSize = 14.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    icon = Icons.Default.Lightbulb,
                    text = "Çalışma masasının yerini ayarla",
                    onClick = { /* TODO: Implement desk position adjustment */ }
                )
                
                ActionButton(
                    icon = Icons.Default.Lightbulb,
                    text = "Tavsiyeler",
                    onClick = onRecommendationsClick
                )
                
                ActionButton(
                    icon = Icons.Default.Info,
                    text = "Bilgilendirme",
                    onClick = onInformationClick
                )
                
                ActionButton(
                    icon = if (isTestMode) Icons.Default.Science else Icons.Default.Settings,
                    text = if (isTestMode) "Test Modu: Açık" else "Test Modu",
                    onClick = onTestModeClick
                )
                
                ActionButton(
                    icon = Icons.Default.ExitToApp,
                    text = "Çıkış",
                    onClick = onExitClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CircularGauge(
    progress: Float,
    color: Color,
    luxValue: Int
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 24.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Background circle
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            val sweepAngle = 360f * progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$luxValue",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "lux",
                fontSize = 20.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.Black
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = Color(0xFFD4AF63)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun RecommendationsDialog(
    onDismiss: () -> Unit,
    luxValue: Float
) {
    val lightStatus = getLightStatus(luxValue)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tavsiyeler",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Mevcut ışık seviyesi: ${luxValue.toInt()} lux",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        luxValue < 150 -> "• Işığı artırmak için pencereyi açın veya ek bir ışık kaynağı ekleyin\n" +
                                "• Göz yorgunluğunu önlemek için yeterli aydınlatma önemlidir\n" +
                                "• Çalışma masanızı daha aydınlık bir alana taşımayı düşünün"
                        luxValue > 600 -> "• Işığı azaltmak için perdeleri kapatın veya ışık kaynaklarını kapatın\n" +
                                "• Çok parlak ışık göz yorgunluğuna neden olabilir\n" +
                                "• Çalışma masanızı daha az aydınlık bir alana taşımayı düşünün"
                        else -> "• Mevcut aydınlatma seviyeniz idealdir\n" +
                                "• Bu seviyeyi korumaya çalışın\n" +
                                "• Düzenli olarak ışık seviyesini kontrol edin"
                    },
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tamam")
            }
        }
    )
}

@Composable
fun InformationDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Bilgilendirme",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "LUXON - Smart Light Assistant",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "LUXON, çalışma ortamınızdaki ışık seviyesini ölçerek göz sağlığınızı korumanıza yardımcı olur.\n\n" +
                            "İdeal ışık seviyeleri:\n" +
                            "• Çalışma için: 300-500 lux\n" +
                            "• Yetersiz: 150 lux altı\n" +
                            "• Çok parlak: 600 lux üstü\n\n" +
                            "Uygulama, cihazınızın ışık sensörünü kullanarak gerçek zamanlı ölçüm yapar.",
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tamam")
            }
        }
    )
}

data class LightStatus(
    val message: String,
    val description: String,
    val color: Color,
    val textColor: Color
)

fun getLightStatus(luxValue: Float): LightStatus {
    return when {
        luxValue < 150 -> LightStatus(
            message = "Ortam ışığı yetersiz",
            description = "Daha iyi bir çalışma ortamı için ışığı artırın.",
            color = Color(0xFFC56A67), // Reddish-orange
            textColor = Color(0xFFC56A67)
        )
        luxValue > 600 -> LightStatus(
            message = "Ortam ışığı fazla parlak",
            description = "Göz konforu için ışığı azaltmayı düşünün.",
            color = Color(0xFFD4AF63), // Golden yellow
            textColor = Color(0xFFD4AF63)
        )
        else -> LightStatus(
            message = "Ortam ışığı ideal",
            description = "Göz konforu için mükemmel denge.",
            color = Color(0xFF548D6F), // Green
            textColor = Color(0xFF548D6F)
        )
    }
}

fun calculateProgress(luxValue: Float): Float {
    // Normalize lux value to 0-1 range
    // Ideal range is around 300-500, so we'll map:
    // 0-1000 lux -> 0-1 progress
    return (luxValue / 1000f).coerceIn(0f, 1f)
}

@Composable
fun TestModeDialog(
    isTestMode: Boolean,
    testLuxValue: Float,
    onTestModeToggle: (Boolean) -> Unit,
    onLuxValueChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Test Modu",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Test Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Test Modunu Aç",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isTestMode,
                        onCheckedChange = onTestModeToggle
                    )
                }
                
                if (isTestMode) {
                    Divider()
                    
                    // Current Value Display
                    Text(
                        text = "Işık Seviyesi: ${testLuxValue.toInt()} lux",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = getLightStatus(testLuxValue).color
                    )
                    
                    // Slider
                    Column {
                        Text(
                            text = "Lux Değeri: ${testLuxValue.toInt()}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Slider(
                            value = testLuxValue,
                            onValueChange = onLuxValueChange,
                            valueRange = 0f..1000f,
                            steps = 99
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0", fontSize = 12.sp, color = Color.Gray)
                            Text("1000", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    
                    // Preset Buttons
                    Text(
                        text = "Hızlı Test Değerleri:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetButton("Düşük (100)", 100f, onLuxValueChange, Modifier.weight(1f))
                        PresetButton("İdeal (300)", 300f, onLuxValueChange, Modifier.weight(1f))
                        PresetButton("Yüksek (600)", 600f, onLuxValueChange, Modifier.weight(1f))
                    }
                    
                    Text(
                        text = "Test modunda sensör devre dışı bırakılır ve manuel olarak ışık seviyesi ayarlanabilir.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text(
                        text = "Test modunu açarak farklı ışık seviyelerini test edebilirsiniz. Sensör otomatik olarak devre dışı kalacaktır.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tamam")
            }
        }
    )
}

@Composable
fun PresetButton(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onValueChange(value) },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD4AF63),
            contentColor = Color.White
        )
    ) {
        Text(label, fontSize = 12.sp)
    }
}

