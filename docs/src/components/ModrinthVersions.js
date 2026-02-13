import React, { useState, useEffect } from 'react';

const PROJECT_ID = 'GH4H8ndx';
const PROJECT_SLUG = 'Vanilla Core';
const API_BASE = 'https://api.modrinth.com/v2';
const MODRINTH_BASE = 'https://modrinth.com/plugin';
const WIKI_DOWNLOADS = 'https://smpcore.tejaslamba.com/downloads';

function parseMarkdown(text) {
    if (!text) return '';
    return text
        .replace(/^### (.+)$/gm, '<h4>$1</h4>')
        .replace(/^## (.+)$/gm, '<h3>$1</h3>')
        .replace(/^# (.+)$/gm, '<h2>$1</h2>')
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.+?)\*/g, '<em>$1</em>')
        .replace(/`(.+?)`/g, '<code>$1</code>')
        .replace(/^- (.+)$/gm, '<li>$1</li>')
        .replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
        .replace(/\n\n/g, '</p><p>')
        .replace(/\n/g, '<br/>');
}

function formatDate(dateString) {
    const date = new Date(dateString);
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return `${months[date.getMonth()]} ${date.getFullYear()}`;
}

export function ModrinthStats() {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch(`${API_BASE}/project/${PROJECT_ID}`, {
            headers: { 'User-Agent': 'TejasLamba2006/Vanilla Core-docs' }
        })
            .then(res => res.json())
            .then(data => {
                setStats({
                    downloads: data.downloads,
                    followers: data.followers,
                    updated: formatDate(data.updated)
                });
                setLoading(false);
            })
            .catch(() => setLoading(false));
    }, []);

    if (loading) return <div className="modrinth-stats"><span className="stat">Loading...</span></div>;
    if (!stats) return null;

    return (
        <div className="modrinth-stats">
            <div className="stat">
                <svg className="stat-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" />
                </svg>
                <span className="stat-value">{stats.downloads.toLocaleString()}</span>
                <span className="stat-label">downloads</span>
            </div>
            <div className="stat">
                <svg className="stat-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 7a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
                </svg>
                <span className="stat-value">{stats.followers}</span>
                <span className="stat-label">followers</span>
            </div>
            <div className="stat">
                <svg className="stat-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                    <line x1="16" y1="2" x2="16" y2="6" />
                    <line x1="8" y1="2" x2="8" y2="6" />
                    <line x1="3" y1="10" x2="21" y2="10" />
                </svg>
                <span className="stat-value">{stats.updated}</span>
                <span className="stat-label">updated</span>
            </div>
        </div>
    );
}

export function LatestVersion({ showDownloadButton = true }) {
    const [version, setVersion] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch(`${API_BASE}/project/${PROJECT_ID}/version`, {
            headers: { 'User-Agent': 'TejasLamba2006/Vanilla Core-docs' }
        })
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    const latest = data[0];
                    setVersion({
                        id: latest.id,
                        number: latest.version_number,
                        name: latest.name,
                        date: formatDate(latest.date_published),
                        downloads: latest.downloads,
                        gameVersions: latest.game_versions,
                        filename: latest.files[0]?.filename,
                        size: latest.files[0]?.size
                    });
                }
                setLoading(false);
            })
            .catch(() => setLoading(false));
    }, []);

    if (loading) {
        return (
            <div className="version-card loading">
                <div className="version-skeleton"></div>
            </div>
        );
    }

    if (!version) return null;

    const formatSize = (bytes) => {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    };

    const versionPageUrl = `${MODRINTH_BASE}/${PROJECT_SLUG}/version/${version.id}`;

    return (
        <div className="version-card">
            <div className="version-header">
                <span className="version-badge">v{version.number}</span>
                <span className="version-date">{version.date}</span>
            </div>
            <div className="version-info">
                <p><strong>Minecraft:</strong> {version.gameVersions[0]} - {version.gameVersions[version.gameVersions.length - 1]}</p>
                <p><strong>Size:</strong> {formatSize(version.size)}</p>
                <p><strong>Downloads:</strong> {version.downloads.toLocaleString()}</p>
            </div>
            {showDownloadButton && (
                <a
                    href={versionPageUrl}
                    className="download-button"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    Download on Modrinth
                </a>
            )}
        </div>
    );
}

export function VersionHistory() {
    const [versions, setVersions] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch(`${API_BASE}/project/${PROJECT_ID}/version`, {
            headers: { 'User-Agent': 'TejasLamba2006/Vanilla Core-docs' }
        })
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    setVersions(data.map(v => ({
                        id: v.id,
                        number: v.version_number,
                        name: v.name,
                        date: formatDate(v.date_published),
                        downloads: v.downloads,
                        type: v.version_type,
                        gameVersions: v.game_versions
                    })));
                }
                setLoading(false);
            })
            .catch(() => setLoading(false));
    }, []);

    if (loading) return <p>Loading version history...</p>;
    if (versions.length === 0) return null;

    return (
        <table className="version-table">
            <thead>
                <tr>
                    <th>Version</th>
                    <th>Release Date</th>
                    <th>Minecraft</th>
                    <th>Downloads</th>
                    <th>Download</th>
                </tr>
            </thead>
            <tbody>
                {versions.map((v, i) => (
                    <tr key={v.id}>
                        <td>
                            <span className={`version-type ${v.type}`}>{v.number}</span>
                            {i === 0 && <span className="latest-badge">Latest</span>}
                        </td>
                        <td>{v.date}</td>
                        <td>{v.gameVersions[0]} - {v.gameVersions[v.gameVersions.length - 1]}</td>
                        <td>{v.downloads.toLocaleString()}</td>
                        <td>
                            <a
                                href={`${MODRINTH_BASE}/${PROJECT_SLUG}/version/${v.id}`}
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                                View on Modrinth
                            </a>
                        </td>
                    </tr>
                ))}
            </tbody>
        </table>
    );
}

export function VersionChangelog() {
    const [versions, setVersions] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch(`${API_BASE}/project/${PROJECT_ID}/version`, {
            headers: { 'User-Agent': 'TejasLamba2006/Vanilla Core-docs' }
        })
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    setVersions(data.map(v => ({
                        id: v.id,
                        number: v.version_number,
                        name: v.name,
                        date: formatDate(v.date_published),
                        changelog: v.changelog || 'No changelog provided.',
                        type: v.version_type
                    })));
                }
                setLoading(false);
            })
            .catch(() => setLoading(false));
    }, []);

    if (loading) return <p>Loading changelog...</p>;
    if (versions.length === 0) return null;

    return (
        <div className="changelog-list">
            {versions.map((v) => (
                <div key={v.id} className="changelog-entry">
                    <div className="changelog-header">
                        <span className="version-badge">v{v.number}</span>
                        <span className="version-date">{v.date}</span>
                        <span className={`version-type-badge ${v.type}`}>{v.type}</span>
                        <a
                            href={`${MODRINTH_BASE}/${PROJECT_SLUG}/version/${v.id}`}
                            className="changelog-link"
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            View on Modrinth
                        </a>
                    </div>
                    <div
                        className="changelog-content"
                        dangerouslySetInnerHTML={{ __html: parseMarkdown(v.changelog) }}
                    />
                </div>
            ))}
        </div>
    );
}

export default { ModrinthStats, LatestVersion, VersionHistory, VersionChangelog };
