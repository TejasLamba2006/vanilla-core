import React, { useState, useCallback, useMemo, useEffect } from 'react';
import Layout from '@theme/Layout';
import styles from './config-builder.module.css';

const CDN_BASE = 'https://smpcore.tejaslamba.com/cdn/config';
const MANIFEST_URL = 'https://smpcore.tejaslamba.com/cdn/manifest.json';
const FALLBACK_CDN = '/cdn/config';
const FALLBACK_MANIFEST = '/cdn/manifest.json';

const DEFAULT_CONFIG = {
    'config-version': 2,
    plugin: {
        name: 'Vanilla Core',
        prefix: '§8[§6SMP§8]§r',
        verbose: false
    }
};

async function fetchWithFallback(primaryUrl, fallbackUrl) {
    try {
        const response = await fetch(primaryUrl);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return await response.json();
    } catch (e) {
        console.warn(`Primary fetch failed (${primaryUrl}), trying fallback...`);
        const response = await fetch(fallbackUrl);
        if (!response.ok) throw new Error(`Fallback also failed: HTTP ${response.status}`);
        return await response.json();
    }
}

function setNestedValue(obj, path, value) {
    const keys = path.split('.');
    const lastKey = keys.pop();
    let current = obj;
    for (const key of keys) {
        if (!current[key]) current[key] = {};
        current = current[key];
    }
    current[lastKey] = value;
    return { ...obj };
}

function getNestedValue(obj, path) {
    return path.split('.').reduce((acc, key) => acc?.[key], obj);
}

function generateYAML(config, indent = 0) {
    const spaces = '  '.repeat(indent);
    let yaml = '';

    for (const [key, value] of Object.entries(config)) {
        if (value === null || value === undefined) continue;

        if (Array.isArray(value)) {
            yaml += `${spaces}${key}:\n`;
            if (value.length === 0) {
                yaml += `${spaces}  []\n`;
            } else {
                value.forEach(item => {
                    yaml += `${spaces}  - ${item}\n`;
                });
            }
        } else if (typeof value === 'object') {
            yaml += `${spaces}${key}:\n`;
            yaml += generateYAML(value, indent + 1);
        } else if (typeof value === 'string') {
            if (value.includes('§') || value.includes('{') || value.includes(':')) {
                yaml += `${spaces}${key}: "${value}"\n`;
            } else {
                yaml += `${spaces}${key}: ${value}\n`;
            }
        } else {
            yaml += `${spaces}${key}: ${value}\n`;
        }
    }

    return yaml;
}

function EnchantmentModal({ isOpen, onClose, enchantments, selectedLimits, onUpdateLimits }) {
    const [activeCategory, setActiveCategory] = useState('all');

    if (!isOpen) return null;

    const categories = ['all', ...new Set(enchantments.map(e => e.category))];
    const filtered = activeCategory === 'all'
        ? enchantments
        : enchantments.filter(e => e.category === activeCategory);

    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modal} onClick={e => e.stopPropagation()}>
                <div className={styles.modalHeader}>
                    <h3>Configure Enchantment Limits</h3>
                    <button className={styles.modalClose} onClick={onClose}>X</button>
                </div>
                <div className={styles.modalTabs}>
                    {categories.map(cat => (
                        <button
                            key={cat}
                            className={`${styles.modalTab} ${activeCategory === cat ? styles.modalTabActive : ''}`}
                            onClick={() => setActiveCategory(cat)}
                        >
                            {cat.charAt(0).toUpperCase() + cat.slice(1)}
                        </button>
                    ))}
                </div>
                <div className={styles.enchantmentGrid}>
                    {filtered.map(ench => (
                        <div key={ench.id} className={styles.enchantmentItem}>
                            <div className={styles.enchantmentInfo}>
                                <span className={styles.enchantmentName}>{ench.name}</span>
                                <span className={styles.enchantmentMax}>Max: {ench.maxLevel}</span>
                            </div>
                            <input
                                type="number"
                                min="0"
                                max={ench.maxLevel}
                                value={selectedLimits[ench.id] ?? ench.maxLevel}
                                onChange={(e) => onUpdateLimits(ench.id, parseInt(e.target.value) || 0)}
                                className={styles.enchantmentInput}
                            />
                        </div>
                    ))}
                </div>
                <div className={styles.modalFooter}>
                    <button className={styles.modalButton} onClick={onClose}>Done</button>
                </div>
            </div>
        </div>
    );
}

function ConfigBuilder() {
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [manifest, setManifest] = useState(null);
    const [featuresData, setFeaturesData] = useState({ features: [], presets: [] });
    const [enchantmentsData, setEnchantmentsData] = useState({ enchantments: [] });
    const [enabledFeatures, setEnabledFeatures] = useState(new Set());
    const [featureConfigs, setFeatureConfigs] = useState({});
    const [copied, setCopied] = useState(false);
    const [selectedCategory, setSelectedCategory] = useState('All');
    const [enchantModalOpen, setEnchantModalOpen] = useState(false);
    const [enchantLimits, setEnchantLimits] = useState({});

    useEffect(() => {
        async function loadData() {
            try {
                setLoading(true);
                setError(null);

                const manifestData = await fetchWithFallback(MANIFEST_URL, FALLBACK_MANIFEST);
                setManifest(manifestData);

                const version = manifestData.latestVersion || '1.1.0';
                const primaryBase = `${CDN_BASE}/${version}`;
                const fallbackBase = `${FALLBACK_CDN}/${version}`;

                const [features, enchantments] = await Promise.all([
                    fetchWithFallback(`${primaryBase}/features.json`, `${fallbackBase}/features.json`),
                    fetchWithFallback(`${primaryBase}/enchantments.json`, `${fallbackBase}/enchantments.json`)
                ]);

                setFeaturesData(features);
                setEnchantmentsData(enchantments);

                const defaultLimits = {};
                enchantments.enchantments?.forEach(e => {
                    defaultLimits[e.id] = e.maxLevel;
                });
                setEnchantLimits(defaultLimits);

            } catch (e) {
                console.error('Failed to load config data:', e);
                setError('Failed to load configuration data. Please try again later.');
            } finally {
                setLoading(false);
            }
        }

        loadData();
    }, []);

    const FEATURES = featuresData.features || [];
    const PRESETS = featuresData.presets || [];

    const categoryMap = useMemo(() => {
        const map = {};
        (featuresData.categories || []).forEach(cat => {
            map[cat.id] = cat.name;
        });
        return map;
    }, [featuresData.categories]);

    const categories = useMemo(() => {
        const cats = new Set(['All']);
        FEATURES.forEach(f => {
            const displayName = categoryMap[f.category] || f.category;
            cats.add(displayName);
        });
        return Array.from(cats);
    }, [FEATURES, categoryMap]);

    const filteredFeatures = useMemo(() => {
        if (selectedCategory === 'All') return FEATURES;
        return FEATURES.filter(f => {
            const displayName = categoryMap[f.category] || f.category;
            return displayName === selectedCategory;
        });
    }, [selectedCategory, FEATURES, categoryMap]);

    const toggleFeature = useCallback((featureId) => {
        setEnabledFeatures(prev => {
            const newSet = new Set(prev);
            if (newSet.has(featureId)) {
                newSet.delete(featureId);
            } else {
                newSet.add(featureId);
            }
            return newSet;
        });
    }, []);

    const updateFeatureConfig = useCallback((featureId, key, value) => {
        setFeatureConfigs(prev => {
            const feature = FEATURES.find(f => f.id === featureId);
            const currentConfig = prev[featureId] || JSON.parse(JSON.stringify(feature?.defaultConfig || {}));
            const newConfig = setNestedValue(currentConfig, key, value);

            if (key === 'enabled') {
                if (value) {
                    setEnabledFeatures(p => new Set([...p, featureId]));
                } else {
                    setEnabledFeatures(p => {
                        const newSet = new Set(p);
                        newSet.delete(featureId);
                        return newSet;
                    });
                }
            }

            return { ...prev, [featureId]: newConfig };
        });
    }, [FEATURES]);

    const applyPreset = useCallback((preset) => {
        const newEnabled = new Set(preset.features);
        setEnabledFeatures(newEnabled);

        const newConfigs = {};
        preset.features.forEach(featureId => {
            const feature = FEATURES.find(f => f.id === featureId);
            if (feature) {
                newConfigs[featureId] = { ...JSON.parse(JSON.stringify(feature.defaultConfig || {})), enabled: true };
            }
        });
        setFeatureConfigs(newConfigs);
    }, [FEATURES]);

    const updateEnchantLimit = useCallback((enchId, value) => {
        setEnchantLimits(prev => ({ ...prev, [enchId]: value }));
    }, []);

    const generatedConfig = useMemo(() => {
        const config = JSON.parse(JSON.stringify(DEFAULT_CONFIG));
        config.features = {};

        FEATURES.forEach(feature => {
            const isEnabled = enabledFeatures.has(feature.id);
            const customConfig = featureConfigs[feature.id];

            if (feature.id === 'enchantment-limiter' && isEnabled) {
                const enchantConfig = customConfig || JSON.parse(JSON.stringify(feature.defaultConfig || {}));
                enchantConfig.enabled = true;
                enchantConfig.limits = { ...enchantLimits };
                config.features[feature.id] = enchantConfig;
            } else if (customConfig) {
                config.features[feature.id] = { ...customConfig, enabled: isEnabled };
            } else if (isEnabled) {
                config.features[feature.id] = { ...JSON.parse(JSON.stringify(feature.defaultConfig || {})), enabled: true };
            }
        });

        return config;
    }, [enabledFeatures, featureConfigs, enchantLimits, FEATURES]);

    const yamlOutput = useMemo(() => {
        return `# ============================================
# Vanilla Core Configuration
# Generated by Config Builder
# https://smpcore.tejaslamba.com/config-builder
# Version: ${manifest?.latestVersion || '1.1.0'}
# ============================================

${generateYAML(generatedConfig)}`;
    }, [generatedConfig, manifest]);

    const copyToClipboard = useCallback(() => {
        navigator.clipboard.writeText(yamlOutput).then(() => {
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        });
    }, [yamlOutput]);

    const downloadConfig = useCallback(() => {
        const blob = new Blob([yamlOutput], { type: 'text/yaml' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'config.yml';
        a.click();
        URL.revokeObjectURL(url);
    }, [yamlOutput]);

    if (loading) {
        return (
            <Layout title="Config Builder" description="Interactive configuration generator for Vanilla Core">
                <section className={styles.builderSection}>
                    <div className={styles.bgGradient} />
                    <div className={styles.bgGlow} />
                    <div className="container">
                        <div className={styles.loadingContainer}>
                            <div className={styles.loadingSpinner} />
                            <p>Loading configuration data...</p>
                        </div>
                    </div>
                </section>
            </Layout>
        );
    }

    if (error) {
        return (
            <Layout title="Config Builder" description="Interactive configuration generator for Vanilla Core">
                <section className={styles.builderSection}>
                    <div className={styles.bgGradient} />
                    <div className={styles.bgGlow} />
                    <div className="container">
                        <div className={styles.errorContainer}>
                            <h2>Error Loading Config</h2>
                            <p>{error}</p>
                            <button onClick={() => window.location.reload()} className={styles.actionButtonPrimary}>
                                Retry
                            </button>
                        </div>
                    </div>
                </section>
            </Layout>
        );
    }

    return (
        <Layout title="Config Builder" description="Interactive configuration generator for Vanilla Core">
            <section className={styles.builderSection}>
                <div className={styles.bgGradient} />
                <div className={styles.bgGlow} />

                <div className="container">
                    <div className={styles.header}>
                        <h1 className={styles.title}>Config Builder</h1>
                        <p className={styles.subtitle}>
                            Build your perfect Vanilla Core configuration interactively
                        </p>
                        {manifest && (
                            <span className={styles.versionBadge}>v{manifest.latestVersion}</span>
                        )}
                    </div>

                    {/* Presets */}
                    <div className={styles.presetsSection}>
                        <h2 className={styles.sectionTitle}>Quick Presets</h2>
                        <div className={styles.presetGrid}>
                            {PRESETS.map(preset => (
                                <button
                                    key={preset.id}
                                    className={styles.presetCard}
                                    onClick={() => applyPreset(preset)}
                                >
                                    <div className={styles.presetInfo}>
                                        <h3>{preset.name}</h3>
                                        <p>{preset.description}</p>
                                    </div>
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className={styles.mainLayout}>
                        {/* Features Panel */}
                        <div className={styles.featuresPanel}>
                            <div className={styles.categoryTabs}>
                                {categories.map(cat => (
                                    <button
                                        key={cat}
                                        className={`${styles.categoryTab} ${selectedCategory === cat ? styles.categoryTabActive : ''}`}
                                        onClick={() => setSelectedCategory(cat)}
                                    >
                                        {cat}
                                    </button>
                                ))}
                            </div>

                            <div className={styles.featuresList}>
                                {filteredFeatures.map(feature => {
                                    const isEnabled = enabledFeatures.has(feature.id);
                                    const config = featureConfigs[feature.id] || feature.defaultConfig || {};

                                    return (
                                        <div
                                            key={feature.id}
                                            className={`${styles.featureCard} ${isEnabled ? styles.featureCardEnabled : ''}`}
                                        >
                                            <div className={styles.featureHeader}>
                                                <button
                                                    className={styles.featureToggle}
                                                    onClick={() => {
                                                        toggleFeature(feature.id);
                                                        updateFeatureConfig(feature.id, 'enabled', !isEnabled);
                                                    }}
                                                >
                                                    <div className={styles.featureInfo}>
                                                        <h3>
                                                            {feature.name}
                                                            {feature.popular && <span className={styles.popularBadge}>Popular</span>}
                                                        </h3>
                                                        <p>{feature.description}</p>
                                                    </div>
                                                    <div className={`${styles.toggleSwitch} ${isEnabled ? styles.toggleSwitchOn : ''}`}>
                                                        <div className={styles.toggleKnob} />
                                                    </div>
                                                </button>
                                            </div>

                                            {isEnabled && feature.id === 'enchantment-limiter' && (
                                                <div className={styles.featureOptions}>
                                                    <button
                                                        className={styles.configureButton}
                                                        onClick={() => setEnchantModalOpen(true)}
                                                    >
                                                        Configure Enchantments
                                                    </button>
                                                </div>
                                            )}

                                            {isEnabled && feature.options && feature.options.length > 1 && feature.id !== 'enchantment-limiter' && (
                                                <div className={styles.featureOptions}>
                                                    {feature.options.filter(opt => opt.key !== 'enabled').map(opt => (
                                                        <div key={opt.key} className={styles.optionRow}>
                                                            <label>{opt.label}</label>
                                                            {opt.type === 'boolean' && (
                                                                <button
                                                                    className={`${styles.optionToggle} ${getNestedValue(config, opt.key) ? styles.optionToggleOn : ''}`}
                                                                    onClick={() => updateFeatureConfig(feature.id, opt.key, !getNestedValue(config, opt.key))}
                                                                >
                                                                    {getNestedValue(config, opt.key) ? 'ON' : 'OFF'}
                                                                </button>
                                                            )}
                                                            {opt.type === 'number' && (
                                                                <input
                                                                    type="number"
                                                                    min={opt.min}
                                                                    max={opt.max}
                                                                    value={getNestedValue(config, opt.key) ?? opt.default}
                                                                    onChange={(e) => updateFeatureConfig(feature.id, opt.key, parseInt(e.target.value) || 0)}
                                                                    className={styles.optionInput}
                                                                />
                                                            )}
                                                            {opt.type === 'text' && (
                                                                <input
                                                                    type="text"
                                                                    value={getNestedValue(config, opt.key) ?? opt.default}
                                                                    onChange={(e) => updateFeatureConfig(feature.id, opt.key, e.target.value)}
                                                                    className={styles.optionInputText}
                                                                />
                                                            )}
                                                            {opt.type === 'select' && (
                                                                <select
                                                                    value={getNestedValue(config, opt.key) ?? opt.default}
                                                                    onChange={(e) => updateFeatureConfig(feature.id, opt.key, e.target.value)}
                                                                    className={styles.optionSelect}
                                                                >
                                                                    {opt.options.map(o => (
                                                                        <option key={o} value={o}>{o}</option>
                                                                    ))}
                                                                </select>
                                                            )}
                                                        </div>
                                                    ))}
                                                </div>
                                            )}

                                            {feature.docsUrl && (
                                                <a
                                                    href={feature.docsUrl}
                                                    className={styles.docsLink}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                >
                                                    View Documentation
                                                </a>
                                            )}
                                        </div>
                                    );
                                })}
                            </div>
                        </div>

                        {/* Output Panel */}
                        <div className={styles.outputPanel}>
                            <div className={styles.outputHeader}>
                                <h2>Generated config.yml</h2>
                                <div className={styles.outputActions}>
                                    <button onClick={copyToClipboard} className={styles.actionButton}>
                                        {copied ? 'Copied!' : 'Copy'}
                                    </button>
                                    <button onClick={downloadConfig} className={styles.actionButtonPrimary}>
                                        Download
                                    </button>
                                </div>
                            </div>
                            <div className={styles.codeBlock}>
                                <pre>{yamlOutput}</pre>
                            </div>
                            <div className={styles.outputTip}>
                                <strong>Tip:</strong> Place this file in your server's <code>plugins/Vanilla Core/</code> folder, then run <code>/smp reload</code>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <EnchantmentModal
                isOpen={enchantModalOpen}
                onClose={() => setEnchantModalOpen(false)}
                enchantments={enchantmentsData.enchantments || []}
                selectedLimits={enchantLimits}
                onUpdateLimits={updateEnchantLimit}
            />
        </Layout>
    );
}

export default ConfigBuilder;
