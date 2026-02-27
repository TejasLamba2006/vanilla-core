package com.tejaslamba.vanillacore;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

class SectionSymbolMigrationTest {

    private static final char SECTION = '\u00A7';

    @Test
    void noSectionSymbolsInJavaSource() throws IOException {
        Path srcRoot = Paths.get("src/main/java");
        List<String> violations = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(srcRoot)) {
            paths.filter(p -> p.toString().endsWith(".java"))
                    .forEach(file -> {
                        try {
                            List<String> lines = Files.readAllLines(file);
                            String relativePath = srcRoot.relativize(file).toString();
                            for (int i = 0; i < lines.size(); i++) {
                                String line = lines.get(i);
                                if (line.indexOf(SECTION) == -1) {
                                    continue;
                                }
                                if (isIntentionalSectionCode(line)) {
                                    continue;
                                }
                                violations.add(relativePath + ":" + (i + 1) + " -> " + line.strip());
                            }
                        } catch (IOException e) {
                            violations.add("Could not read file: " + file);
                        }
                    });
        }

        if (!violations.isEmpty()) {
            fail("Found legacy § codes that should have been migrated to MiniMessage:\n"
                    + String.join("\n", violations));
        }
    }

    @Test
    void noSectionSymbolsInConfigYml() throws IOException {
        Path configFile = Paths.get("src/main/resources/config.yml");
        List<String> violations = new ArrayList<>();

        List<String> lines = Files.readAllLines(configFile);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.indexOf(SECTION) != -1) {
                violations.add("config.yml:" + (i + 1) + " -> " + line.strip());
            }
        }

        if (!violations.isEmpty()) {
            fail("Found legacy § codes in config.yml that should have been migrated to MiniMessage:\n"
                    + String.join("\n", violations));
        }
    }

    private boolean isIntentionalSectionCode(String line) {
        String trimmed = line.trim();
        if (trimmed.contains("XAEROS_")) {
            return true;
        }
        if (trimmed.contains("GUI_TITLE") || trimmed.contains("SCHEDULE_GUI_TITLE")
                || trimmed.contains("WORLD_SETTINGS_GUI_TITLE")) {
            return true;
        }
        if (trimmed.contains("WHITELIST_LORE")) {
            return true;
        }
        // Legacy § stripping utility — not outputting § as text, removing it from
        // legacy strings
        if (trimmed.contains("replaceAll(")) {
            return true;
        }
        return false;
    }
}
