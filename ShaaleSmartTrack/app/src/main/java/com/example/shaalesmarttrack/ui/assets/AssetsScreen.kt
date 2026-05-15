package com.example.shaalesmarttrack.ui.assets

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
import com.example.shaalesmarttrack.data.local.Asset
import com.example.shaalesmarttrack.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    db: AppDatabase,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var assets by remember { mutableStateOf<List<Asset>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    fun reload() {
        scope.launch { assets = db.appDao().getAllAssets() }
    }

    LaunchedEffect(Unit) { reload() }

    val filtered = assets.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.category.contains(searchQuery, ignoreCase = true) ||
        it.location.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd, Color.White)))
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    "Assets",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Asset", tint = Green40)
                }
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search assets...", color = TextHint) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Green40) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Green40)
                        }
                    }
                },
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green40,
                    unfocusedBorderColor = CardBorder,
                    cursorColor = Green40,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(55.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No assets found", color = Color.Gray, fontSize = 16.sp)
                        Text("Tap + to add one", color = Color.LightGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filtered) { asset ->
                        AssetCard(asset = asset, onDelete = {
                            scope.launch { db.appDao().deleteAsset(asset); reload() }
                        })
                    }
                }
            }
        }

        if (showAddDialog) {
            AddAssetDialog(
                onDismiss = { showAddDialog = false },
                onSave = { asset ->
                    scope.launch { db.appDao().insertAsset(asset); reload() }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AssetCard(asset: Asset, onDelete: () -> Unit) {
    val statusColor = when (asset.status) {
        "Working" -> StatusWorking
        "Needs Repair" -> StatusRepair
        "Damaged" -> StatusDamaged
        else -> StatusMissing
    }
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(asset.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    Text(asset.category, color = Color.Gray, fontSize = 12.sp)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        asset.status,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SeverityHigh, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoChip(Icons.Default.LocationOn, asset.location)
                Spacer(modifier = Modifier.width(8.dp))
                InfoChip(Icons.Default.Tag, asset.assetTag)
                Spacer(modifier = Modifier.weight(1f))
                Text(sdf.format(Date(asset.lastChecked)), color = Color.LightGray, fontSize = 11.sp)
            }
            if (asset.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Note: ${asset.notes}", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(2.dp))
        Text(text, color = Color.Gray, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetDialog(onDismiss: () -> Unit, onSave: (Asset) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var assetTag by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Working") }
    var condition by remember { mutableStateOf("Good") }
    var notes by remember { mutableStateOf("") }

    val statuses = listOf("Working", "Needs Repair", "Damaged", "Missing")
    val conditions = listOf("Good", "Fair", "Poor")
    var statusExpanded by remember { mutableStateOf(false) }
    var conditionExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("Add New Asset", fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(value = name, onValueChange = { name = it }, label = "Asset Name")
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = category, onValueChange = { category = it }, label = "Category (e.g. Computer)")
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = assetTag, onValueChange = { assetTag = it }, label = "Asset Tag / ID")
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = location, onValueChange = { location = it }, label = "Location (e.g. Class 5-A)")
                Spacer(modifier = Modifier.height(8.dp))

                // Status dropdown
                ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green40, unfocusedBorderColor = CardBorder),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        statuses.forEach { s ->
                            DropdownMenuItem(text = { Text(s) }, onClick = { status = s; statusExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Condition dropdown
                ExposedDropdownMenuBox(expanded = conditionExpanded, onExpandedChange = { conditionExpanded = it }) {
                    OutlinedTextField(
                        value = condition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Condition") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green40, unfocusedBorderColor = CardBorder),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = conditionExpanded, onDismissRequest = { conditionExpanded = false }) {
                        conditions.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { condition = c; conditionExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = notes, onValueChange = { notes = it }, label = "Notes (optional)")
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && category.isNotBlank() && location.isNotBlank()) {
                                onSave(Asset(name = name, category = category, assetTag = assetTag, location = location, status = status, condition = condition, notes = notes))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Green40)
                    ) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun AppTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green40, unfocusedBorderColor = CardBorder, cursorColor = Green40, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
        modifier = Modifier.fillMaxWidth()
    )
}
