package com.example.shaalesmarttrack.ui.settings

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
import com.example.shaalesmarttrack.ui.theme.*

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    var schoolName by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd, Color.White)))) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary) }
                Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            }

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {

                // Profile section
                SettingsSectionHeader("School Profile")
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = schoolName,
                            onValueChange = { schoolName = it },
                            label = { Text("School Name") },
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = Green40) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green40, unfocusedBorderColor = CardBorder, cursorColor = Green40, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = teacherName,
                            onValueChange = { teacherName = it },
                            label = { Text("Teacher / In-charge Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Green40) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green40, unfocusedBorderColor = CardBorder, cursorColor = Green40, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { /* save profile */ }, colors = ButtonDefaults.buttonColors(containerColor = Green40), modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Profile")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Preferences
                SettingsSectionHeader("Preferences")
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingsToggleRow(
                            icon = Icons.Default.Notifications,
                            label = "Notifications",
                            subtitle = "Enable issue & repair alerts",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                        HorizontalDivider(color = CardBorder, modifier = Modifier.padding(horizontal = 8.dp))
                        SettingsToggleRow(
                            icon = Icons.Default.DarkMode,
                            label = "Dark Mode",
                            subtitle = "Switch to dark theme",
                            checked = darkMode,
                            onCheckedChange = { darkMode = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // About
                SettingsSectionHeader("About")
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingsInfoRow(icon = Icons.Default.Info, label = "App Version", value = "1.0.0")
                        HorizontalDivider(color = CardBorder, modifier = Modifier.padding(horizontal = 8.dp))
                        SettingsInfoRow(icon = Icons.Default.School, label = "App", value = "Namma-Shaale Inventory")
                        HorizontalDivider(color = CardBorder, modifier = Modifier.padding(horizontal = 8.dp))
                        SettingsInfoRow(icon = Icons.Default.Description, label = "Purpose", value = "Public school asset tracker")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(title, fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp, top = 4.dp))
}

@Composable
fun SettingsToggleRow(icon: ImageVector, label: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Green40, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 14.sp)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Green40))
    }
}

@Composable
fun SettingsInfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Green40, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = Color.Gray, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 14.sp)
    }
}
