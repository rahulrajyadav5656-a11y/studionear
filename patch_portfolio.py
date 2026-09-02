import os

path = 'app/src/main/java/com/example/ui/screens/owner/OwnerSubScreens.kt'
if os.path.exists(path):
    with open(path, 'r') as f:
        content = f.read()

    # Remove the stubs for OwnerPortfolioScreen and OwnerCalendarScreen if they exist
    import re
    content = re.sub(r'@OptIn\(ExperimentalMaterial3Api::class\)\s*@Composable\s*fun OwnerPortfolioScreen\(\)\s*\{.*?(?=@OptIn|\Z)', '', content, flags=re.DOTALL)
    
    with open(path, 'w') as f:
        f.write(content)
