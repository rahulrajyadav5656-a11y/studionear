with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

if 'UserRepository.init' not in text:
    text = text.replace('MockDataManager.init(applicationContext)', 'MockDataManager.init(applicationContext)\n        com.example.data.UserRepository.init(applicationContext)')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)
