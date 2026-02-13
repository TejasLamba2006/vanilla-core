import React, { useState, useEffect } from 'react';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import styles from './changelog.module.css';

const PROJECT_ID = 'GH4H8ndx';
const PROJECT_SLUG = 'vanillacorewastaken';
const API_BASE = 'https://api.modrinth.com/v2';

function formatDate(dateString) {
    const date = new Date(dateString);
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return `${months[date.getMonth()]} ${date.getFullYear()}`;
}

function parseChangelog(text) {
    if (!text) return [];
    const lines = text.split('\n').filter(line => line.trim());
    const changes = [];

    lines.forEach(line => {
        const trimmed = line.trim();
        if (trimmed.startsWith('- ') || trimmed.startsWith('* ')) {
            changes.push(trimmed.substring(2));
        } else if (trimmed.startsWith('• ')) {
            changes.push(trimmed.substring(2));
        } else if (!trimmed.startsWith('#') && !trimmed.startsWith('**') && trimmed.length > 0) {
            changes.push(trimmed);
        }
    });

    return changes.slice(0, 8);
}

function ChangelogPage() {
    const [versions, setVersions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [expandedVersions, setExpandedVersions] = useState([]);

    useEffect(() => {
        fetch(`${API_BASE}/project/${PROJECT_ID}/version`, {
            headers: { 'User-Agent': 'TejasLamba2006/vanilla-core-docs' }
        })
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    const versionData = data.map(v => ({
                        id: v.id,
                        version: v.version_number,
                        name: v.name,
                        date: formatDate(v.date_published),
                        rawDate: v.date_published,
                        type: v.version_type,
                        changelog: v.changelog,
                        changes: parseChangelog(v.changelog)
                    }));
                    setVersions(versionData);
                    if (versionData.length > 0) {
                        setExpandedVersions([versionData[0].version]);
                    }
                }
                setLoading(false);
            })
            .catch(() => setLoading(false));
    }, []);

    const toggleVersion = (version) => {
        setExpandedVersions(prev =>
            prev.includes(version) ? prev.filter(v => v !== version) : [...prev, version]
        );
    };

    const typeColors = {
        release: styles.typeRelease,
        beta: styles.typeBeta,
        alpha: styles.typeAlpha
    };

    return (
        <Layout title="Changelog" description="Track all updates and improvements to Vanilla Core">
            <section className={styles.changelogSection}>
                <div className={styles.bgGradient} />
                <div className={styles.bgGlow} />

                <div className="container">
                    <div className={styles.header}>
                        <h1 className={styles.title}>Changelog</h1>
                        <p className={styles.subtitle}>Track all updates and improvements to Vanilla Core.</p>
                    </div>

                    {loading ? (
                        <div className={styles.loading}>Loading changelog...</div>
                    ) : (
                        <div className={styles.timeline}>
                            <div className={styles.timelineLine} />

                            {versions.map((entry, index) => {
                                const isExpanded = expandedVersions.includes(entry.version);

                                return (
                                    <div key={entry.id} className={styles.timelineItem}>
                                        <div className={`${styles.timelineNode} ${index === 0 ? styles.timelineNodeActive : ''} ${typeColors[entry.type]}`} />

                                        <div className={styles.timelineCard}>
                                            <button
                                                onClick={() => toggleVersion(entry.version)}
                                                className={styles.timelineHeader}
                                            >
                                                <div className={styles.timelineInfo}>
                                                    <span className={`${styles.versionBadge} ${typeColors[entry.type]}`}>
                                                        {entry.version}
                                                    </span>
                                                    <span className={styles.timelineDate}>{entry.date}</span>
                                                    <span className={`${styles.typeBadge} ${typeColors[entry.type]}`}>
                                                        {entry.type}
                                                    </span>
                                                </div>
                                                <svg
                                                    className={`${styles.chevron} ${isExpanded ? styles.chevronOpen : ''}`}
                                                    width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
                                                >
                                                    <path d="M6 9l6 6 6-6" />
                                                </svg>
                                            </button>

                                            <div className={`${styles.timelineContent} ${isExpanded ? styles.timelineContentOpen : ''}`}>
                                                {entry.changes.length > 0 ? (
                                                    <ul className={styles.changesList}>
                                                        {entry.changes.map((change, i) => (
                                                            <li key={i}>
                                                                <span className={styles.bullet}>•</span>
                                                                {change}
                                                            </li>
                                                        ))}
                                                    </ul>
                                                ) : (
                                                    <p className={styles.noChanges}>No changelog provided for this version.</p>
                                                )}
                                                <a
                                                    href={`https://modrinth.com/plugin/${PROJECT_SLUG}/version/${entry.id}`}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    className={styles.modrinthLink}
                                                >
                                                    View on Modrinth
                                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                                        <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6M15 3h6v6M10 14L21 3" />
                                                    </svg>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            </section>
        </Layout>
    );
}

export default ChangelogPage;
