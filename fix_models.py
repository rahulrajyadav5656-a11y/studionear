with open("app/src/main/java/com/example/data/models/Models.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    # Remove duplicate description in StudioPackage
    if "val description: String = \"\"," in line and (i == 51 or i == 95 or i == 148):
        continue
    new_lines.append(line)

with open("app/src/main/java/com/example/data/models/Models.kt", "w") as f:
    f.writelines(new_lines)
