package com.example.shaalesmarttrack.ui.issues

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
import com.example.shaalesmarttrack.data.local.Issue
import com.example.shaalesmarttrack.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuesScreen(
    db: AppDatabase,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var issues by remember { mutableStateOf<List<Issue>>(emptyList()) }
    var assets by remember { mutableStateOf<List<com.example.shaalesmarttrack.data.local.Asset>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var filterStatus by remember { mutableStateOf("All") }

    fun reload() {
        scope.launch {
            issues = db.appDao().getAllIssues()
            assets = db.appDao().getAllAssets()
        }
    }
    LaunchedEffect(Unit) { reload() }

    val filtered = if (filterStatus == "All") issues else issues.filter { it.status == filterStatus }

    Box(modifier = modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd, Color.White)))) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary) }
                Text("Issues", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary, modifier = Modifier.weight(1f))
                IconButton(onClick = { showDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Report Issue", tint = SeverityHigh) }
            }

            // Filter chips
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Open", "In Progress", "Resolved").forEach { s ->
                    FilterChip(selected = filterStatus == s, onClick = { filterStatus = s }, label = { Text(s, fontSize = 12.sp) })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Text("No issues found", color = Color.Gray)
                        Text("Tap + to report one", color = Color.LightGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(filtered) { issue ->
                        IssueCard(issue = issue,
                            onDelete = { scope.launch { db.appDao().deleteIssue(issue); reload() } },
                            onResolve = { scope.launch { db.appDao().updateIssue(issue.copy(status = "Resolved", resolvedAt = System.currentTimeMillis())); reload() } }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddIssueDialog(assets = assets, onDismiss = { showDialog = false }, onSave = { issue ->
                scope.launch { db.appDao().insertIssue(issue); reload() }
                showDialog = false
            })
        }
    }
}

@Composable
fun IssueCard(issue: Issue, onDelete: () -> Unit, onResolve: () -> Unit) {
    val severityColor = when (issue.severity) { "High" -> SeverityHigh; "Medium" -> SeverityMedium; else -> SeverityLow }
    val statusColor = when (issue.status) { "Open" -> SeverityHigh; "In Progress" -> SeverityMedium; else -> StatusWorking }
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(issue.assetName, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(issue.description, color = Color.Gray, fontSize = 12.sp, maxLines = 2)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(shape = RoundedCornerShape(20.dp), color = severityColor.copy(alpha = 0.15f)) {
                        Text(issue.severity, color = severityColor, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Text(issue.status, color = statusColor, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(sdf.format(Date(issue.reportedAt)), color = Color.LightGray, fontSize = 11.sp)
                Row {
                    if (issue.status != "Resolved") {
                        TextButton(onClick = onResolve, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("Resolve", color = StatusWorking, fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = SeverityHigh, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIssueDialog(assets: List<com.example.shaalesmarttrack.data.local.Asset>, onDismiss: () -> Unit, onSave: (Issue) -> Unit) {
    var selectedAsset by remember { mutableStateOf(assets.firstOrNull()) }
    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("Medium") }
    var assetExpanded by remember { mutableStateOf(false) }
    var severityExpanded by remember { mutableStateOf(false) }
    val severities = listOf("Low", "Medium", "High")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("Report Issue", fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))

                if (assets.isEmpty()) {
                    Text("Please add assets first.", color = SeverityHigh)
                } else {
                    ExposedDropdownMenuBox(expanded = assetExpanded, onExpandedChange = { assetExpanded = it }) {
                        OutlinedTextField(value = selectedAsset?.name ?: "", onValueChange = {}, readOnly = true, label = { Text("Select Asset") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assetExpanded) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SeverityHigh, unfocusedBorderColor = CardBorder), modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = assetExpanded, onDismissRequest = { assetExpanded = false }) {
                            assets.forEach { a -> DropdownMenuItem(text = { Text(a.name) }, onClick = { selectedAsset = a; assetExpanded = false }) }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Issue Description") }, minLines = 2, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SeverityHigh, unfocusedBorderColor = CardBorder, cursorColor = SeverityHigh, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(expanded = severityExpanded, onExpandedChange = { severityExpanded = it }) {
                        OutlinedTextField(value = severity, onValueChange = {}, readOnly = true, label = { Text("Severity") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = severityExpanded) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SeverityHigh, unfocusedBorderColor = CardBorder), modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = severityExpanded, onDismissRequest = { severityExpanded = false }) {
                            severities.forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { severity = s; severityExpanded = false }) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    if (assets.isNotEmpty()) {
                        Button(onClick = {
                            if (description.isNotBlank() && selectedAsset != null) {
                                onSave(Issue(assetId = selectedAsset!!.id, assetName = selectedAsset!!.name, description = description, severity = severity, status = "Open"))
                            }
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = SeverityHigh)) { Text("Report") }
                    }
                }
            }
        }
    }
}
