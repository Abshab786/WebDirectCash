package com.directcash.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import com.directcash.app.ui.components.TaskItemComponent
import com.directcash.app.ui.viewmodel.TaskViewModel
import com.directcash.app.ui.viewmodel.FirebaseViewModel

@Composable
fun TasksScreen(
    taskViewModel: TaskViewModel,
    firebaseViewModel: FirebaseViewModel,
    onTaskClick: (Long) -> Unit
) {
    val tasks by firebaseViewModel.tasks.collectAsState()
    val loadingTasks by firebaseViewModel.loadingTasks.collectAsState()
    val taskError by firebaseViewModel.taskError.collectAsState()
    val offerwallSettings by firebaseViewModel.offerwallSettings.collectAsState()
    val userId = firebaseViewModel.userId ?: "guest"

    val manualTasks = tasks // Since CPAGrip native parsing is removed

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Premium Offerwalls",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = Color.Black
            )
        }

        // CPAGrip
        if (offerwallSettings.isCpagripEnabled) {
            val cpaUrl = offerwallSettings.cpagripUrl.ifEmpty { "https://filestrue.com/script_include.php?id=1903788" }
            item {
                OfferwallCard(
                    name = "CPAGrip",
                    url = cpaUrl,
                    color = Color(0xFFE8F5E9)
                )
            }
        }

        // PubScale
        if (offerwallSettings.isPubscaleEnabled && offerwallSettings.pubscaleAppId.isNotEmpty()) {
            item {
                OfferwallCard(
                    name = "PubScale",
                    url = "https://sdk.pubscale.com/v1/offerwall?app_id=${offerwallSettings.pubscaleAppId}&user_id=$userId",
                    color = Color(0xFFE3F2FD)
                )
            }
        }

        // CPAlead
        if (offerwallSettings.isCpaleadEnabled) {
            item {
                OfferwallCard(
                    name = "CPAlead",
                    url = "https://www.cpalead.com/mobile/locker.php?pub=${offerwallSettings.cpaleadSourceId}&subid=$userId",
                    color = Color(0xFFFFF3E0)
                )
            }
        }

        // Monlix
        if (offerwallSettings.isMonlixEnabled) {
            item {
                OfferwallCard(
                    name = "Monlix",
                    url = "https://offers.monlix.com/?appid=${offerwallSettings.monlixApiKey}&userid=$userId",
                    color = Color(0xFFF3E5F5)
                )
            }
        }

        // Notik
        if (offerwallSettings.isNotikEnabled && !offerwallSettings.pubscaleAppId.contains("YOUR_APP_ID")) {
            item {
                OfferwallCard(
                    name = "Notik",
                    url = "https://notik.me/offers?pubid=${offerwallSettings.cpagripUserId}&app=${offerwallSettings.pubscaleAppId}&user=$userId",
                    color = Color(0xFFE0F2F1)
                )
            }
        }

        // Adsterra
        if (offerwallSettings.isAdsterraEnabled) {
            item {
                OfferwallCard(
                    name = "Adsterra",
                    // Adsterra CPA usually uses a token or direct link. 
                    // Using a placeholder structure similar to others.
                    url = "https://publishers.adsterra.com/preview/${offerwallSettings.adsterraCpaToken}?subid=$userId",
                    color = Color(0xFFFCE4EC)
                )
            }
        }

        if (!offerwallSettings.isCpagripEnabled && !offerwallSettings.isPubscaleEnabled && 
            !offerwallSettings.isCpaleadEnabled && !offerwallSettings.isMonlixEnabled && 
            !offerwallSettings.isNotikEnabled && !offerwallSettings.isAdsterraEnabled) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No offerwalls available. Check back soon!", color = Color.Gray)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Manual Tasks",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = Color.Black
            )
        }

        if (loadingTasks) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00C853))
                }
            }
        } else if (taskError != null) {
             item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(taskError ?: "An error occurred", color = Color.Red)
                }
            }
        } else if (manualTasks.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No manual tasks available.", color = Color.Gray)
                }
            }
        } else {
            items(manualTasks) { task ->
                TaskItemComponent(
                    task = task,
                    onStartClick = { onTaskClick(task.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun OfferwallCard(name: String, url: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "$name Offerwall",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(16.dp)
            )
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        
                        settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                        
                        webViewClient = WebViewClient()
                        webChromeClient = object : android.webkit.WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                android.util.Log.d("WEBVIEW_DEBUG", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                                return true
                            }
                        }
                        
                        val scriptUrl = url.ifEmpty { "https://filestrue.com/script_include.php?id=1903788" }
                        val htmlData = """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>
                                <style>
                                    body, html { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background-color: transparent; }
                                    #offer-container { width: 100%; height: 100%; }
                                </style>
                            </head>
                            <body>
                                <div id="offer-container">
                                    <script type='text/javascript' src='$scriptUrl'></script>
                                </div>
                            </body>
                            </html>
                        """.trimIndent()
                        
                        loadDataWithBaseURL("https://filestrue.com", htmlData, "text/html", "UTF-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}
