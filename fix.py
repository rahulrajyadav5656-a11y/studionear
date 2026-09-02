with open('app/src/main/java/com/example/data/MockDataManager.kt', 'r') as f:
    content = f.read()

# I appended those three functions and a closing brace. I just need to remove the trailing duplicates.
# Let's just find the last "}" before the appended stuff and cut there.
index = content.rfind("fun rejectBooking(bookingId: String, reason: String)")
if index > 0:
    content = content[:index]

if not content.strip().endswith('}'):
    content += "\n}\n"

with open('app/src/main/java/com/example/data/MockDataManager.kt', 'w') as f:
    f.write(content)
