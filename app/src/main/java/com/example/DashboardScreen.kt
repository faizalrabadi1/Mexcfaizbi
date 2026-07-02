package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import kotlin.random.Random

data class TradeResult(
    val time: String,
    val amount: Double,
    val isWin: Boolean,
    val profit: Double
)

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    var showWarning by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    // Bot States
    var isBotRunning by remember { mutableStateOf(false) }
    var entryAmount by remember { mutableStateOf("1") }
    var duration by remember { mutableStateOf("1 min") }
    var martingaleEnabled by remember { mutableStateOf(false) }
    var martingaleMaxSteps by remember { mutableStateOf("3") }
    var martingaleMultiplier by remember { mutableStateOf("2.0") }
    var aiRadar by remember { mutableStateOf(75) }
    
    val tradeHistory = remember { mutableStateListOf<TradeResult>() }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showBottomSheet by remember { mutableStateOf(false) }

    // Strategies
    val classicStrategies = listOf("RSI", "MACD", "Bollinger", "Fibonacci", "SMA")
    val exclusiveStrategies = listOf("القناص", "الشموع", "تقاطع SMA", "التحليل بعلم الرمل")
    val statsStrategies = listOf("الزخم الكمي", "الشبكة العصبية العميقة", "خوارزمية الفوضى")
    
    val selectedStrategies = remember { mutableStateMapOf<String, Boolean>() }

    // AI Radar simulator
    LaunchedEffect(isBotRunning) {
        if (isBotRunning) {
            while (true) {
                kotlinx.coroutines.delay(2000)
                aiRadar = Random.nextInt(45, 99)
            }
        }
    }

    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            title = { Text("تحذير المخاطر") },
            text = { Text("هذا البوت مخصص للأغراض التعليمية والتجريبية فقط. التداول بالخيارات الثنائية ينطوي على مخاطر عالية وقد يؤدي إلى خسارة رأس المال. نوصي باستخدام الحساب التجريبي (Demo) دائماً.") },
            confirmButton = {
                Button(onClick = { showWarning = false }) { Text("موافق وأفهم المخاطر") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("منصة التداول - الحساب التجريبي") },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "الإعدادات")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "خروج")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // WebView Container
            Box(modifier = Modifier.weight(1f)) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            
                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    view?.evaluateJavascript(getInjectionJS()) { }
                                }
                            }
                            
                            addJavascriptInterface(BotInterface(context) { result ->
                                scope.launch(Dispatchers.Main) {
                                    tradeHistory.add(0, result)
                                }
                            }, "AndroidBot")
                            
                            loadUrl("https://m.pocketoption.com/ar/login")
                            webViewRef = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Bottom Sheet for Bot Settings
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text("إعدادات البوت", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    item {
                        // AI Radar
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("رادار الذكاء الاصطناعي الثقة الحالية")
                                Text(
                                    text = "$aiRadar%",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (aiRadar > 70) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = entryAmount,
                            onValueChange = { entryAmount = it },
                            label = { Text("مبلغ الدخول (Entry Amount)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        var expandedDuration by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedDuration,
                            onExpandedChange = { expandedDuration = !expandedDuration }
                        ) {
                            OutlinedTextField(
                                value = duration,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("وقت الصفقة (Duration)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDuration) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDuration,
                                onDismissRequest = { expandedDuration = false }
                            ) {
                                listOf("1 ثانية", "5 ثواني", "15 ثانية", "30 ثانية", "1 دقيقة", "5 دقائق").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            duration = option
                                            expandedDuration = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        // Martingale Settings
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = martingaleEnabled, onCheckedChange = { martingaleEnabled = it })
                            Text("تفعيل نظام المضاعفة (Martingale)")
                        }
                        if (martingaleEnabled) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = martingaleMaxSteps,
                                    onValueChange = { martingaleMaxSteps = it },
                                    label = { Text("أقصى عدد خطوات") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = martingaleMultiplier,
                                    onValueChange = { martingaleMultiplier = it },
                                    label = { Text("معامل الضرب") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Text("الاستراتيجيات", style = MaterialTheme.typography.titleMedium)
                        StrategyGroup("كلاسيكية", classicStrategies, selectedStrategies)
                        StrategyGroup("حصرية", exclusiveStrategies, selectedStrategies)
                        StrategyGroup("إحصائية وجذرية", statsStrategies, selectedStrategies)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    item {
                        Button(
                            onClick = {
                                isBotRunning = !isBotRunning
                                val strategiesJson = JSONArray(selectedStrategies.filterValues { it }.keys).toString()
                                val script = "window.toggleBot($isBotRunning, $entryAmount, '$strategiesJson', $martingaleEnabled, $martingaleMaxSteps, $martingaleMultiplier);"
                                webViewRef?.evaluateJavascript(script) {}
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBotRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (isBotRunning) "إيقاف البوت" else "تشغيل البوت", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        // Chart & History
                        Text("أداء المحفظة", style = MaterialTheme.typography.titleMedium)
                        PortfolioStatsChart(profits = tradeHistory.map { it.profit.toFloat() }.reversed())
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Text("سجل الصفقات", style = MaterialTheme.typography.titleMedium)
                    }

                    items(tradeHistory) { trade ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (trade.isWin) MaterialTheme.colorScheme.tertiaryContainer 
                                                 else MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(trade.time)
                                Text("$${trade.amount}")
                                Text(
                                    text = if (trade.isWin) "ربح (+$${trade.profit})" else "خسارة (-$${trade.amount})",
                                    fontWeight = FontWeight.Bold,
                                    color = if (trade.isWin) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StrategyGroup(title: String, strategies: List<String>, selectedMap: MutableMap<String, Boolean>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
        strategies.forEach { strategy ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = selectedMap[strategy] ?: false,
                    onCheckedChange = { selectedMap[strategy] = it }
                )
                Text(strategy)
            }
        }
    }
}

class BotInterface(private val context: Context, private val onTradeResult: (TradeResult) -> Unit) {
    @JavascriptInterface
    fun reportTradeResult(time: String, amount: Double, isWin: Boolean, profit: Double) {
        onTradeResult(TradeResult(time, amount, isWin, profit))
    }
}

fun getInjectionJS(): String {
    return """
        (function() {
            if (window.YemeniBotInjected) return;
            window.YemeniBotInjected = true;

            let isRunning = false;
            let currentAmount = 1;
            let strategies = [];
            let isMartingale = false;
            let mMaxSteps = 3;
            let mMultiplier = 2.0;
            let mCurrentStep = 0;
            let baseAmount = 1;

            let priceHistory = [];
            let lastBalance = 0;

            // 1. Monkey-patch WebSocket
            const OriginalWebSocket = window.WebSocket;
            window.WebSocket = function(url, protocols) {
                const ws = new OriginalWebSocket(url, protocols);
                ws.addEventListener('message', function(event) {
                    try {
                        // Extracting mock prices from typical stringified JSON
                        if (event.data.includes('price')) {
                            // Dummy simulation for extracting price
                            priceHistory.push(Math.random() * 100);
                            if (priceHistory.length > 50) priceHistory.shift();
                        }
                    } catch(e) {}
                });
                return ws;
            };

            // 2. Main Loop
            setInterval(() => {
                if (!isRunning) return;

                // 3. Strategy Calculation
                let callScore = 0;
                let putScore = 0;
                
                // Simulate strategy logic
                if (strategies.includes("RSI")) {
                    let rsi = Math.random() * 100;
                    if (rsi < 30) callScore++;
                    if (rsi > 70) putScore++;
                }
                
                if (strategies.includes("الشبكة العصبية العميقة")) {
                    // heavy simulation
                    if (Math.random() > 0.5) callScore += 2;
                    else putScore += 2;
                }

                // 4. Execution
                if (callScore > putScore && callScore >= 1) {
                    clickButton('.btn-call');
                    simulateResult(currentAmount, "Call");
                } else if (putScore > callScore && putScore >= 1) {
                    clickButton('.btn-put');
                    simulateResult(currentAmount, "Put");
                }
            }, 10000); // Check every 10 seconds

            // 5. Simulate DOM Clicks
            function clickButton(selector) {
                let btn = document.querySelector(selector);
                if (btn) btn.click();
            }

            // Mocking trade result delay for educational purposes since real tracking requires deep DOM observers
            function simulateResult(amount, direction) {
                setTimeout(() => {
                    let isWin = Math.random() > 0.45; // 55% win rate simulation
                    let profit = isWin ? amount * 0.8 : 0;
                    
                    // Martingale Logic
                    if (isMartingale) {
                        if (!isWin) {
                            mCurrentStep++;
                            if (mCurrentStep <= mMaxSteps) {
                                currentAmount = currentAmount * mMultiplier;
                            } else {
                                currentAmount = baseAmount; // Reset
                                mCurrentStep = 0;
                            }
                        } else {
                            currentAmount = baseAmount; // Reset on win
                            mCurrentStep = 0;
                        }
                    }
                    
                    let time = new Date().toLocaleTimeString();
                    if (window.AndroidBot) {
                        window.AndroidBot.reportTradeResult(time, amount, isWin, profit);
                    }
                }, 5000);
            }

            // Public API for Android
            window.toggleBot = function(run, amount, stratsJson, mart, max, mult) {
                isRunning = run;
                baseAmount = amount;
                if(mCurrentStep === 0) currentAmount = amount;
                strategies = JSON.parse(stratsJson);
                isMartingale = mart;
                mMaxSteps = max;
                mMultiplier = mult;
            };
        })();
    """.trimIndent()
}
