package com.example.shaalesmarttrack.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shaalesmarttrack.data.local.AppDatabase
import com.example.shaalesmarttrack.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    db: AppDatabase,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var totalAssets by remember { mutableStateOf(0) }
    var workingCount by remember { mutableStateOf(0) }
    var needsRepairCount by remember { mutableStateOf(0) }
    var damagedCount by remember { mutableStateOf(0) }
    var openIssues by remember { mutableStateOf(0) }
    var activeRepairs by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            totalAssets = db.appDao().getTotalAssets()
            workingCount = db.appDao().getWorkingCount()
            needsRepairCount = db.appDao().getNeedsRepairCount()
            damagedCount = db.appDao().getDamagedCount()
            openIssues = db.appDao().getOpenIssueCount()
            activeRepairs = db.appDao().getActiveRepairCount()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgGradientStart, BgGradientEnd, Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Text(
                text = "Namma-Shaale",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Inventory Tracker",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Summary Row
            Text("Overview", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Assets",
                    value = totalAssets.toString(),
                    icon = Icons.Default.Inventory,
                    color = Green40
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Working",
                    value = workingCount.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = StatusWorking
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Needs Repair",
                    value = needsRepairCount.toString(),
                    icon = Icons.Default.Build,
                    color = StatusRepair
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Open Issues",
                    value = openIssues.toString(),
                    icon = Icons.Default.Warning,
                    color = SeverityHigh
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions
            Text("Quick Actions", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))

            QuickActionCard(
                icon = Icons.Default.Inventory,
                title = "Manage Assets",
                subtitle = "Add, view or update school assets",
                color = Green40,
                onClick = { onNavigate("assets") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            QuickActionCard(
                icon = Icons.Default.HealthAndSafety,
                title = "Health Check",
                subtitle = "Run equipment health inspections",
                color = GreenGrey40,
                onClick = { onNavigate("health_check") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            QuickActionCard(
                icon = Icons.Default.ReportProblem,
                title = "Log an Issue",
                subtitle = "Report broken or missing equipment",
                color = SeverityMedium,
                onClick = { onNavigate("issues") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            QuickActionCard(
                icon = Icons.Default.Build,
                title = "Track Repairs",
                subtitle = "Monitor ongoing repair status",
                color = AccentOrange,
                onClick = { onNavigate("repairs") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            QuickActionCard(
                icon = Icons.Default.Assessment,
                title = "View Reports",
                subtitle = "Generate summary reports",
                color = Color(0xFF5C6BC0),
                onClick = { onNavigate("reports") }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = color)
            Text(label, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
