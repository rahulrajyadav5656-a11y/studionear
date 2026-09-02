package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.FavoritesManager
import com.example.data.models.Studio
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun StudioCard(
    studio: Studio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteToggle: ((Boolean) -> Unit)? = null
) {
    val favoriteIds by FavoritesManager.favoriteStudioIds.collectAsState()
    val isFavorite = favoriteIds.contains(studio.id)
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clickable { onClick() }
            .testTag("studio_card_${studio.id}"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
        border = BorderStroke(1.dp, ThemeOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Image Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
                    .background(ThemePrimaryContainer)
            ) {
                val imageUrl = when {
                    studio.coverImageUrl.isNotBlank() -> studio.coverImageUrl
                    !studio.coverPhoto.isNullOrBlank() -> studio.coverPhoto
                    studio.portfolioUrls.isNotEmpty() -> studio.portfolioUrls.first()
                    studio.portfolioThumbnails.isNotEmpty() -> studio.portfolioThumbnails.first()
                    else -> ""
                }
                
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Studio thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                startY = 80f
                            )
                        )
                )

                // Top Badges Row (Verified, Sponsored & Favorite Bookmark)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (studio.isVerifiedActive) {
                            Row(
                                modifier = Modifier
                                    .background(ThemeSurface.copy(alpha = 0.92f), RoundedCornerShape(50))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Studio",
                                    tint = VerifiedBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "VERIFIED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ThemeOnSurface,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        if (studio.isSponsoredActive) {
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFFE5A93C).copy(alpha = 0.95f), RoundedCornerShape(50))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Sponsored",
                                    tint = Color.Black,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SPONSORED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    // Bookmark / Favorite Heart Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = if (isFavorite) ThemeSurface.copy(alpha = 0.95f) else Color.Black.copy(alpha = 0.45f),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isFavorite) ThemePrimary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable {
                                scope.launch {
                                    val newStatus = FavoritesManager.toggleFavorite(studio.id)
                                    onFavoriteToggle?.invoke(newStatus)
                                }
                            }
                            .testTag("favorite_button_${studio.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFE53935) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Bottom Info in Image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = studio.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = StarColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${studio.rating} (${studio.reviewCount} reviews)",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = " • ",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                        val dist = if (studio.distanceKm > 0.0) studio.distanceKm else studio.distance
                        val distanceText = if (dist != null && dist > 0.0) {
                            "${String.format("%.1f", dist)} km away"
                        } else if (studio.area.isNotBlank()) {
                            studio.area
                        } else {
                            studio.city
                        }
                        Text(
                            text = distanceText,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Bottom Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "STARTING PACKAGE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ThemeOnSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            val priceVal = if (studio.startingPrice > 0.0) studio.startingPrice.toInt() else studio.startingPackagePrice.toInt()
                            Text(
                                text = "₹$priceVal",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeOnBackground
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "/ Day",
                                fontSize = 14.sp,
                                color = GrayText,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .background(SuccessBgColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "FAST DELIVERY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${studio.completedBookings}+ Bookings",
                            fontSize = 11.sp,
                            color = ThemeOnSurfaceVariant
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onClick() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Check Availability",
                            color = ThemeOnPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    val context = androidx.compose.ui.platform.LocalContext.current

                    // Call Button
                    IconButton(
                        onClick = { com.example.ui.utils.ContactUtils.openDialer(context, studio.phone) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(ThemePrimaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .border(1.dp, ThemePrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call Studio",
                            tint = ThemePrimary
                        )
                    }

                    // WhatsApp Button
                    IconButton(
                        onClick = { com.example.ui.utils.ContactUtils.openWhatsApp(context, studio.phone) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF25D366).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF25D366).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp Studio",
                            tint = Color(0xFF25D366)
                        )
                    }
                }
            }
        }
    }
}
