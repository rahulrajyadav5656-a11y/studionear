with open('app/src/main/java/com/example/ui/screens/owner/OwnerBookingsScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.strip() == "@Composable" and len(new_lines) > 0 and new_lines[-1].strip() == "@Composable":
        continue # skip duplicate
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/owner/OwnerBookingsScreen.kt', 'w') as f:
    f.writelines(new_lines)
