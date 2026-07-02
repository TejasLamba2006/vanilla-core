import os
import re

root_dir = 'c:\\Users\\Tejas\\Desktop\\Source-Codes\\MinecraftSTUFF\\vanilla-core'

ignore_dirs = {'.git', '.idea', 'target', 'build', 'node_modules', 'paper-test-server', 'other-plugins', '.docusaurus'}

def replace_in_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception:
        return

    orig_content = content

    # Specific URL overrides
    content = content.replace('https://modrinth.com/plugin/vanillacorewastaken', 'https://modrinth.com/plugin/vanillacorewastaken')
    content = content.replace('https://discord.gg/7fQPG4Grwt?utm_source=vanillacore', 'https://discord.gg/7fQPG4Grwt?utm_source=vanillacore')
    
    # Specific SLUG variables
    content = content.replace(\"'vanillacorewastaken'\", \"'vanillacorewastaken'\")
    content = content.replace('\"smpwatchdog\"', '\"vanillacorewastaken\"')

    # General replaces
    content = content.replace('Vanilla Core', 'Vanilla Core')
    content = content.replace('VanillaCore', 'VanillaCore')

    # Update Modrinth ID in configs if it exists
    content = content.replace('GH4H8ndx', 'GH4H8ndx') # If Modrinth ID stays same? wait user said vanillacorewastaken is slug, but ID might change. Let's keep ID same unless told otherwise.

    if orig_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f'Updated {filepath}')

for root, dirs, files in os.walk(root_dir):
    dirs[:] = [d for d in dirs if d not in ignore_dirs]
    for file in files:
        if file.endswith(('.jar', '.png', '.gif', '.zip', '.tar.gz', '.class', '.ico', '.woff', '.woff2')):
            continue
        replace_in_file(os.path.join(root, file))

print('Done!')
