import re

with open("app/src/main/java/com/example/ui/screens/SplashScreen.kt", "r") as f:
    content = f.read()

# Call syncProfileFromFirestore on splash
if "import com.example.data.AuthManager" not in content:
    content = content.replace("import kotlinx.coroutines.delay", "import kotlinx.coroutines.delay\nimport com.example.data.AuthManager")

if "AuthManager.syncProfileFromFirestore()" not in content:
    content = content.replace("delay(2000)", "delay(2000)\n        if(AuthManager.isLoggedIn()) {\n            AuthManager.syncProfileFromFirestore()\n        }")

with open("app/src/main/java/com/example/ui/screens/SplashScreen.kt", "w") as f:
    f.write(content)
