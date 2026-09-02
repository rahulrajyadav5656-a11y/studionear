import re

with open('app/src/main/java/com/example/data/MockDataManager.kt', 'r') as f:
    content = f.read()

# Add missing imports if needed
if "import com.example.data.models.ChatMessage" not in content:
    content = content.replace("import com.example.data.models.Location", 
                              "import com.example.data.models.Location\nimport com.example.data.models.ChatMessage\nimport com.example.data.models.ChatThread\nimport com.example.data.models.BlockedDate")

# Add new StateFlows
new_flows = """
    private val _chatThreads = MutableStateFlow<List<ChatThread>>(emptyList())
    val chatThreads: StateFlow<List<ChatThread>> = _chatThreads.asStateFlow()
    
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()
    
    private val _blockedDates = MutableStateFlow<List<BlockedDate>>(emptyList())
    val blockedDates: StateFlow<List<BlockedDate>> = _blockedDates.asStateFlow()
"""
if "val chatThreads" not in content:
    content = content.replace("val portfolios: StateFlow<List<PortfolioItem>> = _portfolios.asStateFlow()",
                              "val portfolios: StateFlow<List<PortfolioItem>> = _portfolios.asStateFlow()\n" + new_flows)

# Add save/load logic
load_logic = """
        val chatThreadsJson = p.getString("chat_threads", null)
        if (chatThreadsJson != null) {
            val type = object : TypeToken<List<ChatThread>>() {}.type
            _chatThreads.value = gson.fromJson(chatThreadsJson, type)
        }
        val chatMsgsJson = p.getString("chat_messages", null)
        if (chatMsgsJson != null) {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            _chatMessages.value = gson.fromJson(chatMsgsJson, type)
        }
        val blockedDatesJson = p.getString("blocked_dates", null)
        if (blockedDatesJson != null) {
            val type = object : TypeToken<List<BlockedDate>>() {}.type
            _blockedDates.value = gson.fromJson(blockedDatesJson, type)
        }
"""
if "p.getString(\"chat_threads\"" not in content:
    content = content.replace("val reviewsJson = p.getString(\"reviews\", null)", load_logic + "\n        val reviewsJson = p.getString(\"reviews\", null)")

save_logic = """
            putString("chat_threads", gson.toJson(_chatThreads.value))
            putString("chat_messages", gson.toJson(_chatMessages.value))
            putString("blocked_dates", gson.toJson(_blockedDates.value))
"""
if "putString(\"chat_threads\"" not in content:
    content = content.replace("putString(\"portfolios\", gson.toJson(_portfolios.value))", "putString(\"portfolios\", gson.toJson(_portfolios.value))\n" + save_logic)

# Helper functions
helper_functions = """
    fun saveBlockedDate(blockedDate: BlockedDate) {
        val current = _blockedDates.value.toMutableList()
        current.add(blockedDate)
        _blockedDates.value = current
        saveData()
    }

    fun removeBlockedDate(id: String) {
        _blockedDates.value = _blockedDates.value.filterNot { it.id == id }
        saveData()
    }
    
    fun rejectBooking(bookingId: String, reason: String) {
        val current = _bookings.value.toMutableList()
        val index = current.indexOfFirst { it.id == bookingId }
        if (index >= 0) {
            current[index] = current[index].copy(status = com.example.data.models.BookingStatus.REJECTED, rejectionReason = reason)
            _bookings.value = current
            saveData()
        }
    }
    
    fun updateDeliveryNotes(bookingId: String, notes: String) {
        val current = _bookings.value.toMutableList()
        val index = current.indexOfFirst { it.id == bookingId }
        if (index >= 0) {
            current[index] = current[index].copy(deliveryNotes = notes)
            _bookings.value = current
            saveData()
        }
    }
    
    fun updateDeliveryStatus(bookingId: String, status: com.example.data.models.DeliveryStatus) {
        val current = _bookings.value.toMutableList()
        val index = current.indexOfFirst { it.id == bookingId }
        if (index >= 0) {
            current[index] = current[index].copy(deliveryStatus = status)
            _bookings.value = current
            saveData()
        }
    }
"""

if "fun saveBlockedDate" not in content:
    content = content.replace("fun updateBookingStatus", helper_functions + "\n    fun updateBookingStatus")

with open('app/src/main/java/com/example/data/MockDataManager.kt', 'w') as f:
    f.write(content)
