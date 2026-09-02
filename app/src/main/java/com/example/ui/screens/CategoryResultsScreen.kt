package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Studio
import com.example.di.ServiceLocator
import com.example.ui.components.StudioCard
import com.example.ui.theme.ThemeBackground
import com.example.ui.theme.ThemeOnSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryResultsScreen(
    categoryName: String,
    onBack: () -> Unit,
    onStudioClick: (String) -> Unit
) {
    val studiosFlow = remember(categoryName) {
        ServiceLocator.studioRepository.getStudios(serviceCategory = categoryName)
    }
    val studios by studiosFlow.collectAsState(initial = emptyList())
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(categoryName) {
        isLoading = true
        kotlinx.coroutines.delay(300)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        containerColor = ThemeBackground
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (studios.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No studios found for $categoryName",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Studios offering $categoryName services in Prayagraj will appear here.",
                        fontSize = 13.sp,
                        color = ThemeOnSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                items(studios, key = { it.id }) { studio ->
                    StudioCard(
                        studio = studio,
                        onClick = { onStudioClick(studio.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
