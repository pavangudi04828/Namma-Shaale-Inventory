package com.example.shaalesmarttrack.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shaalesmarttrack.data.local.AppDatabase
import com.example.shaalesmarttrack.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ReportsScreen(
    db: AppDatabase,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var totalAssets by remember { mutableStateOf(0) }
    var workingCount by remember { mutableStateOf(0) }
    var needsRepairCount by remember { mutableStateOf(0) }
    var damagedCount by remember { mutableStateOf(0) }
    var openIssues by remember { mutableStateOf(0) }
    var activeRepairs by remember { mutableStateOf(0) }
    var passChecks by remember { mutableStateOf(0) }
    var failChecks by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            totalAssets = db.appDao().getTotalAssets()
            workingCount = db.appDao().getWorkingCount()
            needsRepairCount = db.appDao().getNeedsRepairCount()
            damagedCount = db.appDao().getDamagedCount()
            openIssues = db.appDao().getOpenIssueCount()
            activeRepairs = db.appDao().getActiveRepairCount()
            passChecks = db.appDao().getPassCount()
            failChecks = db.appDao().getFailCount()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd, Color.White)))) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary) }
                Text("Reports", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            }

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {

                ReportSection(title = "Asset Summary", icon = Icons.Default.Inventory, color = Green40) {
                    ReportRow("Total Assets", totalAssets.toString(), Green40)
                    ReportRow("Working", workingCount.toString(), StatusWorking)
                    ReportRow("Needs Repair", needsRepairCount.toString(), StatusRepair)
                    ReportRow("Damaged", damagedCount.toString(), StatusDamaged)
                }

                Spacer(modifier = Modifier.height(16.dp))

                ReportSection(title = "Issues Summary", icon = Icons.Default.ReportProblem, color = SeverityHigh) {
                    ReportRow("Open Issues", openIssues.toString(), SeverityHigh)
                }

                Spacer(modifier = Modifier.height(16.dp))

                ReportSection(title = "Repairs Summary", icon = Icons.Default.Build, color = AccentOrange) {
                    ReportRow("Active Repairs", activeRepairs.toString(), AccentOrange)
                }

                Spacer(modifier = Modifier.height(16.dp))

                ReportSection(title = "Health Check Summary", icon = Icons.Default.HealthAndSafety, color = GreenGrey40) {
                    ReportRow("Total Checks", (passChecks + failChecks).toString(), GreenGrey40)
                    ReportRow("Passed", passChecks.toString(), StatusWorking)
                    ReportRow("Failed", failChecks.toString(), SeverityHigh)
                    if ((passChecks + failChecks) > 0) {
                        val pct = (passChecks * 100 / (passChecks + failChecks))
                        ReportRow("Pass Rate", "$pct%", if (pct >= 70) StatusWorking else SeverityHigh)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Overall health score card
                val score = when {
                    totalAssets == 0 -> 0
                    else -> ((workingCount.toFloat() / totalAssets) * 100).toInt()
                }
                val scoreColor = when {
                    score >= 80 -> StatusWorking
                    score >= 50 -> SeverityMedium
                    else -> SeverityHigh
                }
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = scoreColor.copy(alpha = 0.1f)), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = scoreColor, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Overall Asset Health", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$score%", fontWeight = FontWeight.ExtraBold, color = scoreColor, fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(progress = { score / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp), color = scoreColor, trackColor = scoreColor.copy(alpha = 0.2f))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ReportSection(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CardBorder)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun ReportRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, color = valueColor, fontSize = 14.sp)
    }
}
