package com.tejaslamba.smpcore.manager;

import com.tejaslamba.smpcore.Main;
import com.tejaslamba.smpcore.feature.Feature;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FeatureManager {

    private final Main plugin;
    private final Map<String, Feature> features = new LinkedHashMap<>();
    private final Map<String, Feature> featuresByConfigPath = new HashMap<>();
    private final Map<Class<?>, Feature> featuresByClass = new HashMap<>();

    public FeatureManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadFeatures() {
        features.clear();
        featuresByConfigPath.clear();
        featuresByClass.clear();
        boolean verbose = plugin.isVerbose();

        try {
            String packageName = "com.tejaslamba.smpcore.features";
            Class<?>[] featureClasses = getClasses(packageName);

            if (verbose) {
                plugin.getLogger().info("[VERBOSE] Scanning for features in package: " + packageName);
                plugin.getLogger().info("[VERBOSE] Found " + featureClasses.length + " classes to check");
            }

            for (Class<?> clazz : featureClasses) {
                if (Feature.class.isAssignableFrom(clazz) && !clazz.isInterface()
                        && !Modifier.isAbstract(clazz.getModifiers())) {
                    try {
                        Feature feature = (Feature) clazz.getDeclaredConstructor().newInstance();
                        features.put(feature.getName(), feature);
                        featuresByConfigPath.put(feature.getConfigPath(), feature);
                        featuresByClass.put(clazz, feature);
                        feature.onEnable(plugin);
                        plugin.getLogger().info("Loaded feature: " + feature.getName());

                        if (verbose) {
                            plugin.getLogger().info("[VERBOSE] Feature '" + feature.getName() + "' registered");
                            plugin.getLogger().info("[VERBOSE]   - Config Path: " + feature.getConfigPath());
                            plugin.getLogger().info("[VERBOSE]   - Enabled: " + feature.isEnabled());
                            plugin.getLogger().info("[VERBOSE]   - Has Listener: " + (feature.getListener() != null));
                        }
                    } catch (Exception e) {
                        plugin.getLogger()
                                .warning("Failed to load feature: " + clazz.getName() + " - " + e.getMessage());
                        if (verbose) {
                            plugin.getLogger().warning("[VERBOSE] Stack trace:");
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (verbose) {
                plugin.getLogger().info("[VERBOSE] Total features loaded: " + features.size());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to scan features: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
        }
    }

    public boolean isFeatureRemotelyDisabled(String featureId) {
        CDNManager cdnManager = plugin.getCDNManager();
        if (cdnManager == null)
            return false;
        return cdnManager.isFeatureDisabled(featureId);
    }

    public boolean isMaintenanceMode() {
        CDNManager cdnManager = plugin.getCDNManager();
        if (cdnManager == null)
            return false;
        return cdnManager.isMaintenanceMode();
    }

    public void disableAll() {
        for (Feature feature : features.values()) {
            feature.onDisable();
        }
        features.clear();
        featuresByConfigPath.clear();
        featuresByClass.clear();
    }

    public Collection<Feature> getFeatures() {
        return features.values();
    }

    public Feature getFeature(String name) {
        return features.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T extends Feature> T getFeature(Class<T> featureClass) {
        return (T) featuresByClass.get(featureClass);
    }

    public Feature getFeatureByConfigPath(String configPath) {
        return featuresByConfigPath.get(configPath);
    }

    public List<ItemStack> getMenuItems() {
        List<Feature> sortedFeatures = new ArrayList<>(features.values());
        sortedFeatures.sort(Comparator.comparingInt(Feature::getDisplayOrder));

        List<ItemStack> items = new ArrayList<>();
        for (Feature feature : sortedFeatures) {
            ItemStack item = feature.getMenuItem();
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    private Class<?>[] getClasses(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());

        List<Class<?>> classes = new ArrayList<>();

        if (jarFile.isFile()) {
            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith(path) && name.endsWith(".class")) {
                        String className = name.substring(0, name.length() - 6).replace('/', '.');
                        try {
                            classes.add(Class.forName(className));
                        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                            // Skip classes that can't be loaded
                        }
                    }
                }
            }
        } else {
            URL resource = classLoader.getResource(path);
            if (resource != null) {
                File directory = new File(resource.toURI());
                if (directory.exists()) {
                    for (File file : directory.listFiles()) {
                        if (file.getName().endsWith(".class")) {
                            String className = packageName + '.'
                                    + file.getName().substring(0, file.getName().length() - 6);
                            try {
                                classes.add(Class.forName(className));
                            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                                // Skip classes that can't be loaded
                            }
                        }
                    }
                }
            }
        }

        return classes.toArray(new Class<?>[0]);
    }
}
