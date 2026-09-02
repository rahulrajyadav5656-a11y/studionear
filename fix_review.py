with open('app/src/main/java/com/example/data/MockDataManager.kt', 'r') as f:
    text = f.read()

if 'fun replyToReview' not in text:
    func = """
    fun replyToReview(reviewId: String, reply: String) {
        val current = _reviews.value.toMutableList()
        val index = current.indexOfFirst { it.id == reviewId }
        if (index >= 0) {
            current[index] = current[index].copy(ownerReply = reply)
            _reviews.value = current
            saveData()
        }
    }
"""
    # Find last brace and insert
    idx = text.rfind('}')
    if idx != -1:
        text = text[:idx] + func + text[idx:]

with open('app/src/main/java/com/example/data/MockDataManager.kt', 'w') as f:
    f.write(text)
