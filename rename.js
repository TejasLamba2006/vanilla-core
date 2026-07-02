const fs = require('fs');
const path = require('path');

const rootDir = 'c:\\Users\\Tejas\\Desktop\\Source-Codes\\MinecraftSTUFF\\vanilla-core';
const ignoreDirs = new Set(['.git', '.idea', 'target', 'build', 'node_modules', 'paper-test-server', 'other-plugins', '.docusaurus']);

function walk(dir) {
    let results = [];
    const list = fs.readdirSync(dir);
    list.forEach(function(file) {
        if (ignoreDirs.has(file)) return;
        const filePath = path.join(dir, file);
        const stat = fs.statSync(filePath);
        if (stat && stat.isDirectory()) {
            results = results.concat(walk(filePath));
        } else {
            if (!file.endsWith('.jar') && !file.endsWith('.png') && !file.endsWith('.gif') && !file.endsWith('.zip') && !file.endsWith('.tar.gz') && !file.endsWith('.class') && !file.endsWith('.ico')) {
                results.push(filePath);
            }
        }
    });
    return results;
}

const files = walk(rootDir);

files.forEach(filePath => {
    try {
        const origContent = fs.readFileSync(filePath, 'utf8');
        let content = origContent;
        
        content = content.replace(/https:\/\/modrinth\.com\/plugin\/smpwatchdog/g, 'https://modrinth.com/plugin/vanillacorewastaken');
        content = content.replace(/https:\/\/discord\.gg\/7fQPG4Grwt\?utm_source=smpwatchdog/g, 'https://discord.gg/7fQPG4Grwt?utm_source=vanillacore');
        content = content.replace(/'vanillacorewastaken'/g, "'vanillacorewastaken'");
        content = content.replace(/"vanillacorewastaken"/g, '"vanillacorewastaken"');
        content = content.replace(/Vanilla Core/g, 'Vanilla Core');
        content = content.replace(/VanillaCore/g, 'VanillaCore');

        if (content !== origContent) {
            fs.writeFileSync(filePath, content, 'utf8');
            console.log('Updated ' + filePath);
        }
    } catch (e) {}
});
console.log('Done!');
