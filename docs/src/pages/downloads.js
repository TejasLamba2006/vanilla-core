import React, { useState, useEffect } from 'react';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import styles from './downloads.module.css';

const PROJECT_ID = 'GH4H8ndx';
const PROJECT_SLUG = 'vanillacorewastaken';
const API_BASE = 'https://api.modrinth.com/v2';

function formatDate(dateString) {
    const date = new Date(dateString);
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return `${months[date.getMonth()]} ${date.getFullYear()}`;
}

function DownloadsPage() {
    const [versions, setVersions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('all');
    const [search, setSearch] = useState('');
    const [totalDownloads, setTotalDownloads] = useState(0);

    useEffect(() => {
        Promise.all([
            fetch(`${API_BASE}/project/${PROJECT_ID}`, {
                headers: { 'User-Agent': 'TejasLamba2006/vanilla-core-docs' }
            }).then(res => res.json()),
            fetch(`${API_BASE}/project/${PROJECT_ID}/version`, {
                headers: { 'User-Agent': 'TejasLamba2006/vanilla-core-docs' }
            }).then(res => res.json())
        ])
            .then(([project, versionData]) => {
                setTotalDownloads(project.downloads || 0);
                if (versionData && versionData.length > 0) {
                    setVersions(versionData.map(v => ({
                        id: v.id,
                        version: v.version_number,
                        name: v.name,
                        date: formatDate(v.date_published),
                        downloads: v.downloads,
                        type: v.version_type,
                        gameVersions: v.game_versions,
                        changelog: v.changelog,
                        files: v.files
                    })));
                }
                setLoading(false);
            })
            .catch(() => setLoading(false));
    }, []);

    const filteredVersions = versions.filter(v => {
        const matchesFilter = filter === 'all' || v.type === filter;
        const matchesSearch = v.version.toLowerCase().includes(search.toLowerCase()) ||
            v.gameVersions.some(mc => mc.includes(search));
        return matchesFilter && matchesSearch;
    });

    const typeColors = {
        release: styles.typeRelease,
        beta: styles.typeBeta,
        alpha: styles.typeAlpha
    };

    return (
        <Layout title="Downloads" description="Download Vanilla Core for your Minecraft server">
            <section className={styles.downloadsSection}>
                <div className={styles.bgGradient} />
                <div className={styles.bgGlow} />

                <div className="container">
                    <div className={styles.header}>
                        <h1 className={styles.title}>Downloads</h1>
                        <div className={styles.totalBadge}>
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" />
                            </svg>
                            <span>{totalDownloads.toLocaleString()} total downloads</span>
                        </div>
                    </div>

                    <div className={styles.controls}>
                        <div className={styles.filterTabs}>
                            {['all', 'release', 'beta'].map(tab => (
                                <button
                                    key={tab}
                                    onClick={() => setFilter(tab)}
                                    className={`${styles.filterTab} ${filter === tab ? styles.filterTabActive : ''}`}
                                >
                                    {tab === 'all' ? 'All' : tab === 'release' ? 'Releases' : 'Betas'}
                                </button>
                            ))}
                        </div>

                        <div className={styles.searchWrapper}>
                            <svg className={styles.searchIcon} width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <circle cx="11" cy="11" r="8" />
                                <line x1="21" y1="21" x2="16.65" y2="16.65" />
                            </svg>
                            <input
                                type="text"
                                placeholder="Search versions..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                className={styles.searchInput}
                            />
                        </div>
                    </div>

                    {loading ? (
                        <div className={styles.loading}>Loading versions...</div>
                    ) : (
                        <div className={styles.versionsGrid}>
                            {filteredVersions.map((version, index) => (
                                <div
                                    key={version.id}
                                    className={styles.versionCard}
                                    style={{ animationDelay: `${index * 50}ms` }}
                                >
                                    <div className={styles.cardGlow} />

                                    <div className={styles.cardHeader}>
                                        <div>
                                            <h3 className={styles.versionNumber}>{version.version}</h3>
                                            <div className={styles.versionDate}>
                                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                                                    <line x1="16" y1="2" x2="16" y2="6" />
                                                    <line x1="8" y1="2" x2="8" y2="6" />
                                                    <line x1="3" y1="10" x2="21" y2="10" />
                                                </svg>
                                                {version.date}
                                            </div>
                                        </div>
                                        <span className={`${styles.typeBadge} ${typeColors[version.type]}`}>
                                            {version.type}
                                        </span>
                                    </div>

                                    <div className={styles.mcVersions}>
                                        {version.gameVersions.slice(0, 3).map(mc => (
                                            <span key={mc} className={styles.mcBadge}>
                                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                                    <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
                                                </svg>
                                                {mc}
                                            </span>
                                        ))}
                                        {version.gameVersions.length > 3 && (
                                            <span className={styles.mcBadge}>+{version.gameVersions.length - 3}</span>
                                        )}
                                    </div>

                                    <div className={styles.cardFooter}>
                                        <span className={styles.downloadCount}>
                                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" />
                                            </svg>
                                            {version.downloads.toLocaleString()}
                                        </span>
                                        <a
                                            href={`https://modrinth.com/plugin/${PROJECT_SLUG}/version/${version.id}`}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className={styles.downloadBtn}
                                        >
                                            Download
                                        </a>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}

                    {filteredVersions.length === 0 && !loading && (
                        <div className={styles.noResults}>No versions found matching your search.</div>
                    )}

                    <div className={styles.requirementsSection}>
                        <h2 className={styles.requirementsTitle}>Requirements</h2>
                        <div className={styles.requirementsGrid}>
                            <div className={styles.requirementCard}>
                                <div className={styles.requirementIcon}>
                                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                        <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
                                    </svg>
                                </div>
                                <h3>Minecraft</h3>
                                <p>1.21.1+</p>
                            </div>
                            <div className={styles.requirementCard}>
                                <div className={styles.requirementIcon}>
                                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                        <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
                                        <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
                                        <line x1="6" y1="6" x2="6.01" y2="6" />
                                        <line x1="6" y1="18" x2="6.01" y2="18" />
                                    </svg>
                                </div>
                                <h3>Server</h3>
                                <p>Paper or Spigot</p>
                            </div>
                            <div className={styles.requirementCard}>
                                <div className={styles.requirementIcon}>
                                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                                        <circle cx="9" cy="7" r="4" />
                                        <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                                        <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                                    </svg>
                                </div>
                                <h3>Java</h3>
                                <p>21+</p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </Layout>
    );
}

export default DownloadsPage;
