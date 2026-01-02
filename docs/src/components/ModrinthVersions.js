import React, { useState, useEffect } from 'react';

const PROJECT_ID = 'GH4H8ndx';
const API_BASE = 'https://api.modrinth.com/v2';

export function ModrinthStats() {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch(`${API_BASE}/project/${PROJECT_ID}`, {
            headers: { 'User-Agent': 'TejasLamba2006/smp-core-docs' }
        })
            .then(res => res.json())
            .then(data => {
                setStats({
                    downloads: data.downloads,
                    followers: data.followers,
                    updated: new Date(data.updated).toLocaleDateString()
                });
                setLoading(false);
            })
            .catch(() => setLoading(false));
    }, []);

    if (loading) return <span>Loading...</span>;
    if (!stats) return null;

    return (
        <div className="modrinth-stats">
            <span className="stat">{stats.downloads.toLocaleString()} downloads</span>
            <span className="stat">{stats.followers} followers</span>
            <span className="stat">Updated {stats.updated}</span>
        </div>
    );
}

export function LatestVersion({ showDownloadButton = true }) {
    const [version, setVersion] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch(`${API_BASE}/project/${PROJECT_ID}/version`, {
            headers: { 'User-Agent': 'TejasLamba2006/smp-core-docs' }
        })
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    const latest = data[0];
                    setVersion({
                        number: latest.version_number,
                        name: latest.name,
                        date: new Date(latest.date_published).toLocaleDateString(),
                        downloads: latest.downloads,
                        gameVersions: latest.game_versions,
                        downloadUrl: latest.files[0]?.url,
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
            {showDownloadButton && version.downloadUrl && (
                <a
                    href={version.downloadUrl}
                    className="download-button"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    Download {version.filename}
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
            headers: { 'User-Agent': 'TejasLamba2006/smp-core-docs' }
        })
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    setVersions(data.map(v => ({
                        number: v.version_number,
                        name: v.name,
                        date: new Date(v.date_published).toLocaleDateString(),
                        downloads: v.downloads,
                        type: v.version_type,
                        gameVersions: v.game_versions,
                        downloadUrl: v.files[0]?.url
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
                    <tr key={v.number}>
                        <td>
                            <span className={`version-type ${v.type}`}>{v.number}</span>
                            {i === 0 && <span className="latest-badge">Latest</span>}
                        </td>
                        <td>{v.date}</td>
                        <td>{v.gameVersions[0]} - {v.gameVersions[v.gameVersions.length - 1]}</td>
                        <td>{v.downloads.toLocaleString()}</td>
                        <td>
                            {v.downloadUrl && (
                                <a href={v.downloadUrl} target="_blank" rel="noopener noreferrer">
                                    Download
                                </a>
                            )}
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
            headers: { 'User-Agent': 'TejasLamba2006/smp-core-docs' }
        })
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    setVersions(data.map(v => ({
                        number: v.version_number,
                        name: v.name,
                        date: new Date(v.date_published).toLocaleDateString(),
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
                <div key={v.number} className="changelog-entry">
                    <div className="changelog-header">
                        <span className="version-badge">v{v.number}</span>
                        <span className="version-date">{v.date}</span>
                        <span className={`version-type-badge ${v.type}`}>{v.type}</span>
                    </div>
                    <div className="changelog-content">
                        <pre style={{ whiteSpace: 'pre-wrap', fontFamily: 'inherit', margin: 0 }}>
                            {v.changelog}
                        </pre>
                    </div>
                </div>
            ))}
        </div>
    );
}

export default { ModrinthStats, LatestVersion, VersionHistory, VersionChangelog };
