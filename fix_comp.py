with open('app/src/main/java/com/example/ui/screens/owner/OwnerBookingsScreen.kt', 'r') as f:
    text = f.read()
text = text.replace("@Composable\n@Composable", "@Composable")
with open('app/src/main/java/com/example/ui/screens/owner/OwnerBookingsScreen.kt', 'w') as f:
    f.write(text)
