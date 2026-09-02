import re

with open('app/src/main/java/com/example/ui/screens/owner/OwnerDashboardScreen.kt', 'r') as f:
    content = f.read()

verify_ui = """
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Verification Status", fontWeight = FontWeight.Bold)
                            Text(if (studio.isVerified) "Verified" else "Not Verified", color = if (studio.isVerified) ThemePrimary else MaterialTheme.colorScheme.error)
                        }
                        if (!studio.isVerified) {
                            var showVerifyDialog by remember { mutableStateOf(false) }
                            Button(onClick = { showVerifyDialog = true }) {
                                Text("Submit")
                            }
                            if (showVerifyDialog) {
                                AlertDialog(
                                    onDismissRequest = { showVerifyDialog = false },
                                    title = { Text("Submit for Verification") },
                                    text = { Text("Submit your studio documents (Aadhar, PAN, Portfolio) to gain the Verified badge. For this demo, this will simulate submission.") },
                                    confirmButton = {
                                        Button(onClick = { showVerifyDialog = false }) { Text("Submit Now") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showVerifyDialog = false }) { Text("Cancel") }
                                    }
                                )
                            }
                        }
                    }
"""

content = re.sub(r'Text\("Verification functionality coming soon\.", color = ThemeOnSurfaceVariant\)', verify_ui, content)

# Also fix the Quick Actions to navigate correctly
content = content.replace('onClick = { /* Add Package */ }', 'onClick = { onNavigate("owner_packages") }')
content = content.replace('onClick = { /* Check Earnings */ }', 'onClick = { onNavigate("owner_earnings") }')
content = content.replace('onClick = { /* Edit Studio Profile */ }', 'onClick = { onNavigate("owner_profile") }')
content = content.replace('onClick = { /* View Bookings */ }', 'onClick = { onNavigate("owner_bookings") }')
content = content.replace('onClick = { /* Add Portfolio */ }', 'onClick = { onNavigate("owner_portfolio") }')

# Replace the fake pending bookings stat with real one
stats_ui = """
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val allBookings by MockDataManager.bookings.collectAsState()
                    val studioBookings = allBookings.filter { it.studioId == studio.id }
                    
                    val pending = studioBookings.count { it.status == com.example.data.models.BookingStatus.PENDING }
                    val upcoming = studioBookings.count { it.status == com.example.data.models.BookingStatus.ACCEPTED }
                    val completed = studioBookings.count { it.status == com.example.data.models.BookingStatus.COMPLETED }
                    
                    DashboardStatCard("Pending", "$pending", Modifier.weight(1f))
                    DashboardStatCard("Upcoming", "$upcoming", Modifier.weight(1f))
                    DashboardStatCard("Completed", "$completed", Modifier.weight(1f))
                }
"""

content = re.sub(r'Row\(horizontalArrangement = Arrangement\.spacedBy\(16\.dp\)\)\s*\{\s*DashboardStatCard\("Pending", "2", Modifier\.weight\(1f\)\)\s*DashboardStatCard\("Upcoming", "5", Modifier\.weight\(1f\)\)\s*DashboardStatCard\("Completed", "12", Modifier\.weight\(1f\)\)\s*\}', stats_ui, content)

with open('app/src/main/java/com/example/ui/screens/owner/OwnerDashboardScreen.kt', 'w') as f:
    f.write(content)
