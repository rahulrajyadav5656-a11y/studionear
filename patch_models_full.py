import re

with open('app/src/main/java/com/example/data/models/Models.kt', 'r') as f:
    content = f.read()

# Add missing models for full functionality
new_models = """

data class ChatMessage(
    val id: String = "",
    val threadId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class ChatThread(
    val id: String = "",
    val clientId: String = "",
    val studioId: String = "",
    val bookingId: String? = null,
    val lastMessage: String = "",
    val lastTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)

data class BlockedDate(
    val id: String = "",
    val studioId: String = "",
    val date: Long = 0,
    val reason: String = ""
)
"""

if "data class ChatMessage" not in content:
    content += new_models

# Add new fields to Studio
if "val coverPhoto: String? = null" not in content:
    content = content.replace("val portfolioThumbnails: List<String> = emptyList(),",
                              "val coverPhoto: String? = null,\n    val phone: String = \"\",\n    val email: String = \"\",\n    val city: String = \"\",\n    val experienceYears: Int = 0,\n    val travelRadiusKm: Int = 0,\n    val languages: List<String> = emptyList(),\n    val equipment: String = \"\",\n    val teamSize: Int = 1,\n    val verificationReason: String? = null,\n    val followersCount: Int = 0,\n    val portfolioThumbnails: List<String> = emptyList(),")

# Add new fields to Booking
if "val rejectionReason: String? = null" not in content:
    content = content.replace("val totalAmount: Double = 0.0,",
                              "val rejectionReason: String? = null,\n    val deliveryNotes: String? = null,\n    val totalAmount: Double = 0.0,")

with open('app/src/main/java/com/example/data/models/Models.kt', 'w') as f:
    f.write(content)
