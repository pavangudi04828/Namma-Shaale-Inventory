package com.example.shaalesmarttrack.ui.repairs

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
import com.example.shaalesmarttrack.data.local.Repair
import com.example.shaalesmarttrack.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairsScreen(
    db: AppDatabase,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var repairs by remember { mutableStateOf<List<Repair>>(emptyList()) }
    var assets by remember { mutableStateOf<List<com.example.shaalesmarttrack.data.local.Asset>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var filterStatus by remember { mutableStateOf("All") }

    fun reload() { scope.launch { repairs = db.appDao().getAllRepairs(); assets = db.appDao().getAllAssets() } }
    LaunchedEffect(Unit) { reload() }

    val filtered = if (filterStatus == "All") repairs else repairs.filter { it.status == filterStatus }

    Box(modifier = modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd, Color.White)))) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary) }
                Text("Repairs", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary, modifier = Modifier.weight(1f))
                IconButton(onClick = { showDialog = true }) { Icon(Icons.Default.Add, contentDescription = "New Repair", tint = AccentOrange) }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Pending", "In Progress", "Completed").forEach { s ->
                    FilterChip(selected = filterStatus == s, onClick = { filterStatus = s }, label = { Text(s, fontSize = 12.sp) })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Text("No repairs found", color = Color.Gray)
                        Text("Tap + to add one", color = Color.LightGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(filtered) { repair ->
                        RepairCard(repair = repair,
                            onDelete = { scope.launch { db.appDao().deleteRepair(repair); reload() } },
                            onComplete = { scope.launch { db.appDao().updateRepair(repair.copy(status = "Completed", completedAt = System.currentTimeMillis())); reload() } }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddRepairDialog(assets = assets, onDismiss = { showDialog = false }, onSave = { repair ->
                scope.launch { db.appDao().insertRepair(repair); reload() }
                showDialog = false
            })
        }
    }
}

@Composable
fun RepairCard(repair: Repair, onDelete: () -> Unit, onComplete: () -> Unit) {
    val statusColor = when (repair.status) { "Completed" -> StatusWorking; "In Progress" -> SeverityMedium; else -> Color.Gray }
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(repair.assetName, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(repair.description, color = Color.Gray, fontSize = 12.sp, maxLines = 2)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(repair.status, color = statusColor, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            if (repair.technicianName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(repair.technicianName, color = Color.Gray, fontSize = 12.sp)
                    if (repair.cost > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                        Text("%.0f".format(repair.cost), color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(sdf.format(Date(repair.startedAt)), color = Color.LightGray, fontSize = 11.sp)
                Row {
                    if (repair.status != "Completed") {
                        TextButton(onClick = onComplete, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text("Complete", color = StatusWorking, fontSize = 12.sp) }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, contentDescription = null, tint = SeverityHigh, modifier = Modifier.size(16.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRepairDialog(assets: List<com.example.shaalesmarttrack.data.local.Asset>, onDismiss: () -> Unit, onSave: (Repair) -> Unit) {
    var selectedAsset by remember { mutableStateOf(assets.firstOrNull()) }
    var description by remember { mutableStateOf("") }
    var technicianName by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Pending") }
    var assetExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    val statuses = listOf("Pending", "In Progress", "Completed")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("New Repair", fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                if (assets.isEmpty()) {
                    Text("Please add assets first.", color = SeverityHigh)
                } else {
                    ExposedDropdownMenuBox(expanded = assetExpanded, onExpandedChange = { assetExpanded = it }) {
                        OutlinedTextField(value = selectedAsset?.name ?: "", onValueChange = {}, readOnly = true, label = { Text("Select Asset") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assetExpanded) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange, unfocusedBorderColor = CardBorder), modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = assetExpanded, onDismissRequest = { assetExpanded = false }) {
                            assets.forEach { a -> DropdownMenuItem(text = { Text(a.name) }, onClick = { selectedAsset = a; assetExpanded = false }) }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Repair Description") }, minLines = 2, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange, unfocusedBorderColor = CardBorder, cursorColor = AccentOrange, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = technicianName, onValueChange = { technicianName = it }, label = { Text("Technician Name") }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange, unfocusedBorderColor = CardBorder, cursorColor = AccentOrange, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost (₹)") }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange, unfocusedBorderColor = CardBorder, cursorColor = AccentOrange, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                        OutlinedTextField(value = status, onValueChange = {}, readOnly = true, label = { Text("Status") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange, unfocusedBorderColor = CardBorder), modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            statuses.forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { status = s; statusExpanded = false }) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    if (assets.isNotEmpty()) {
                        Button(onClick = {
                            if (description.isNotBlank() && selectedAsset != null) {
                                onSave(Repair(assetId = selectedAsset!!.id, assetName = selectedAsset!!.name, description = description, status = status, cost = cost.toDoubleOrNull() ?: 0.0, technicianName = technicianName))
                            }
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)) { Text("Save") }
                    }
                }
            }
        }
    }
}
