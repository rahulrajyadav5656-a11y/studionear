import re

with open('app/src/main/java/com/example/ui/screens/owner/OwnerBookingsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    """fun OwnerBookingCard(
    booking: Booking,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit
)""",
    """import androidx.compose.foundation.clickable

@Composable
fun OwnerBookingCard(
    booking: Booking,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    onClick: () -> Unit
)""")

content = content.replace("modifier = Modifier.fillMaxWidth(),", "modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),")

content = content.replace(
"""                        OwnerBookingCard(
                            booking = booking,
                            onAccept = { MockDataManager.updateBookingStatus(booking.id, BookingStatus.ACCEPTED) },
                            onReject = { MockDataManager.updateBookingStatus(booking.id, BookingStatus.CANCELLED) },
                            onComplete = { MockDataManager.updateBookingStatus(booking.id, BookingStatus.COMPLETED) }
                        )""",
"""                        OwnerBookingCard(
                            booking = booking,
                            onAccept = { MockDataManager.updateBookingStatus(booking.id, BookingStatus.ACCEPTED) },
                            onReject = { MockDataManager.updateBookingStatus(booking.id, BookingStatus.CANCELLED) },
                            onComplete = { MockDataManager.updateBookingStatus(booking.id, BookingStatus.COMPLETED) },
                            onClick = { onNavigateToBooking(booking.id) }
                        )""")

with open('app/src/main/java/com/example/ui/screens/owner/OwnerBookingsScreen.kt', 'w') as f:
    f.write(content)
