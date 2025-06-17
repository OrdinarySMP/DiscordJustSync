#!/bin/python3
import requests
from sys import argv
import re
import os
import shutil


if len(argv) <= 1:
    print("please provide game version")
    exit()
game_version = argv[1]


loader_versions = requests.get("https://meta.fabricmc.net/v2/versions/loader").json()
current_loader_version = loader_versions[0]["version"]

yarn_versions = requests.get("https://meta.fabricmc.net/v2/versions/yarn").json()
yarn_versions = [v for v in yarn_versions if v["gameVersion"] == game_version]
if len(yarn_versions) == 0:
    print("Invalid game verion!")
    exit()
yarn_version = yarn_versions[0]["version"]

api_versions = requests.get(
    "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml"
).text
api_versions = [
    m.group(1) for m in re.finditer(r"<version>(.+?)</version>", api_versions)
]
api_versions = [v for v in api_versions if v.endswith(game_version)]
api_version = api_versions[-1]

print(f"""
minecraft_version={game_version}
yarn_mappings={yarn_version}
loader_version={current_loader_version}

# Fabric API
fabric_version={api_version}
""")

os.system(f'./gradlew migrateMappings --mappings "{yarn_version}"')
shutil.rmtree("src/main/java")
shutil.move("remappedSrc", "src/main/java")

with open("gradle.properties", encoding="UTF-8") as file:
    current_properties = file.read().splitlines()
with open("gradle.properties", "w", encoding="UTF-8") as file:
    for line in current_properties:
        match line.split("=")[0]:
            case "minecraft_version":
                line = f"minecraft_version={game_version}"
            case "yarn_mappings":
                line = f"yarn_mappings={yarn_version}"
            case "loader_version":
                line = f"loader_version={current_loader_version}"
            case "fabric_version":
                line = f"fabric_version={api_version}"
        file.write(line + "\n")
