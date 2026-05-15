package com.example.shaalesmarttrack.ui.healthcheck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.shaalesmarttrack.data.local.AppDatabase
import com.example.shaalesmarttrack.data.local.HealthCheck
import com.example.shaalesmarttrack.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCheckScreen(
    db: AppDatabase,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var checks by remember { mutableStateOf<List<HealthCheck>>(emptyList()) }
    var assets by remember { mutableStateOf<List<com.example.shaalesmarttrack.data.local.Asset>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var passCount by remember { mutableStateOf(0) }
    var failCount by remember { mutableStateOf(0) }

    fun reload() {
        scope.launch {
            checks = db.appDao().getAllHealthChecks()
            assets = db.appDao().getAllAssets()
            passCount = db.appDao().getPassCount()
            failCount = db.appDao().getFailCount()
        }
    }

    LaunchedEffect(Unit) { reload() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd, Color.White)))
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text("Health Check", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary, modifier = Modifier.weight(1f))
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New Check", tint = GreenGrey40)
                }
            }

            // Summary
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryPill(modifier = Modifier.weight(1f), label = "Pass", count = passCount, color = StatusWorking)
                SummaryPill(modifier = Modifier.weight(1f), label = "Fail", count = failCount, color = SeverityHigh)
                SummaryPill(modifier = Modifier.weight(1f), label = "Total", count = checks.size, color = GreenGrey40)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (checks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No health checks yet", color = Color.Gray)
                        Text("Tap + to run a check", color = Color.LightGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(checks) { check ->
                        HealthCheckCard(check = check, onDelete = {
                            scope.launch { db.appDao().deleteHealthCheck(check); reload() }
                        })
                    }
                }
            }
        }

        if (showDialog) {
            AddHealthCheckDialog(
                assets = assets,
                onDismiss = { showDialog = false },
                onSave = { check ->
                    scope.launch { db.appDao().insertHealthCheck(check); reload() }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun SummaryPill(modifier: Modifier = Modifier, label: String, count: Int, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), fontWeight = FontWeight.ExtraBold, color = color, fontSize = 22.sp)
            Text(label, color = color, fontSize = 12.sp)
        }
    }
}

@Composable
fun HealthCheckCard(check: HealthCheck, onDelete: () -> Unit) {
    val color = when (check.result) {
        "Pass" -> StatusWorking
        "Fail" -> SeverityHigh
        else -> SeverityMedium
    }
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(check.assetName, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Checked by: ${check.checkedBy}", color = Color.Gray, fontSize = 12.sp)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.15f)) {
                    Text(check.result, color = color, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = SeverityHigh, modifier = Modifier.size(18.dp))
                }
            }
            if (check.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Note: ${check.notes}", color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(sdf.format(Date(check.checkedAt)), color = Color.LightGray, fontSize = 11.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHealthCheckDialog(
    assets: List<com.example.shaalesmarttrack.data.local.Asset>,
    onDismiss: () -> Unit,
    onSave: (HealthCheck) -> Unit
) {
    var selectedAsset by remember { mutableStateOf(assets.firstOrNull()) }
    var checkedBy by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Pass") }
    var notes by remember { mutableStateOf("") }
    var assetExpanded by remember { mutableStateOf(false) }
    var resultExpanded by remember { mutableStateOf(false) }
    val results = listOf("Pass", "Fail", "Needs Attention")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("New Health Check", fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))

                if (assets.isEmpty()) {
                    Text("Please add assets first.", color = SeverityHigh)
                } else {
                    ExposedDropdownMenuBox(expanded = assetExpanded, onExpandedChange = { assetExpanded = it }) {
                        OutlinedTextField(value = selectedAsset?.name ?: "", onValueChange = {}, readOnly = true, label = { Text("Select Asset") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assetExpanded) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenGrey40, unfocusedBorderColor = CardBorder), modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = assetExpanded, onDismissRequest = { assetExpanded = false }) {
                            assets.forEach { a -> DropdownMenuItem(text = { Text(a.name) }, onClick = { selectedAsset = a; assetExpanded = false }) }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = checkedBy, onValueChange = { checkedBy = it }, label = { Text("Checked By") }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenGrey40, unfocusedBorderColor = CardBorder, cursorColor = GreenGrey40, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(expanded = resultExpanded, onExpandedChange = { resultExpanded = it }) {
                        OutlinedTextField(value = result, onValueChange = {}, readOnly = true, label = { Text("Result") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = resultExpanded) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenGrey40, unfocusedBorderColor = CardBorder), modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = resultExpanded, onDismissRequest = { resultExpanded = false }) {
                            results.forEach { r -> DropdownMenuItem(text = { Text(r) }, onClick = { result = r; resultExpanded = false }) }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenGrey40, unfocusedBorderColor = CardBorder, cursorColor = GreenGrey40, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    if (assets.isNotEmpty()) {
                        Button(onClick = {
                            if (checkedBy.isNotBlank() && selectedAsset != null) {
                                onSave(HealthCheck(assetId = selectedAsset!!.id, assetName = selectedAsset!!.name, checkedBy = checkedBy, result = result, notes = notes))
                            }
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = GreenGrey40)) { Text("Save") }
                    }
                }
            }
        }
    }
}
