import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import styles from './index.module.css';
import { useState, useEffect, useRef } from 'react';

import { LatestVersion, ModrinthStats } from '../components/ModrinthVersions';

const features = [
    {
        icon: (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M14.5 17.5L3 6V3h3l11.5 11.5M13 19l6-6M16 16l4 4M19 21l2-2" />
            </svg>
        ),
        title: 'Combat Balance',
        description: 'Control maces, enchantments, and netherite to prevent overpowered gear from ruining your SMP.',
    },
    {
        icon: (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="3" y="3" width="7" height="7" /><rect x="14" y="3" width="7" height="7" />
                <rect x="14" y="14" width="7" height="7" /><rect x="3" y="14" width="7" height="7" />
            </svg>
        ),
        title: 'Item Limiting',
        description: 'Set maximum quantities for any item per player. Perfect for limiting totems, gaps, and more.',
    },
    {
        icon: (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" /><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
            </svg>
        ),
        title: 'Dimension Control',
        description: 'Lock and unlock Nether and End with commands. Schedule automatic unlocks for server events.',
    },
    {
        icon: (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
        ),
        title: 'Mob Management',
        description: 'Control mob spawning in regions with WorldGuard integration. Perfect for spawn areas.',
    },
    {
        icon: (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
            </svg>
        ),
        title: 'Quality of Life',
        description: 'One-player sleep, infinite villager restocks, invisible kill messages, and more.',
    },
    {
        icon: (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="3" /><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
            </svg>
        ),
        title: 'In-Game GUI',
        description: 'Manage all features through an intuitive in-game menu. No config editing needed.',
    },
];

const stats = [
    { icon: 'download', label: 'Total Downloads' },
    { icon: 'server', label: 'Active Servers' },
    { icon: 'star', label: 'GitHub Stars' },
    //{ icon: 'discord', label: 'Discord Members' },
];

function FloatingParticles() {
    const [particles, setParticles] = useState([]);

    useEffect(() => {
        const colors = [
            'rgba(99, 102, 241, 0.6)',
            'rgba(139, 92, 246, 0.5)',
            'rgba(79, 70, 229, 0.5)',
            'rgba(124, 58, 237, 0.4)',
        ];
        const newParticles = Array.from({ length: 30 }, (_, i) => ({
            id: i,
            x: Math.random() * 100,
            y: Math.random() * 100,
            size: Math.random() * 5 + 2,
            delay: Math.random() * 5,
            duration: Math.random() * 10 + 8,
            color: colors[Math.floor(Math.random() * colors.length)],
        }));
        setParticles(newParticles);
    }, []);

    return (
        <div className={styles.particlesContainer}>
            {particles.map((p) => (
                <div
                    key={p.id}
                    className={styles.particle}
                    style={{
                        left: `${p.x}%`,
                        top: `${p.y}%`,
                        width: `${p.size}px`,
                        height: `${p.size}px`,
                        backgroundColor: p.color,
                        animationDelay: `${p.delay}s`,
                        animationDuration: `${p.duration}s`,
                    }}
                />
            ))}
        </div>
    );
}

function AnimatedCounter({ value, suffix = '', isVisible }) {
    const [count, setCount] = useState(0);

    useEffect(() => {
        if (!isVisible) return;
        const duration = 2000;
        const steps = 60;
        const stepValue = value / steps;
        const stepDuration = duration / steps;
        let current = 0;

        const timer = setInterval(() => {
            current += stepValue;
            if (current >= value) {
                setCount(value);
                clearInterval(timer);
            } else {
                setCount(Math.floor(current));
            }
        }, stepDuration);

        return () => clearInterval(timer);
    }, [value, isVisible]);

    const formatNumber = (num) => {
        if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M';
        if (num >= 1000) return (num / 1000).toFixed(num >= 10000 ? 0 : 1) + 'K';
        return num.toString();
    };

    return <span>{formatNumber(count)}{suffix}</span>;
}

function FeatureCard({ icon, title, description, index, isVisible }) {
    return (
        <div
            className={clsx(styles.featureCard, isVisible && styles.featureCardVisible)}
            style={{ animationDelay: `${index * 100}ms` }}
        >
            <div className={styles.featureIcon}>{icon}</div>
            <h3 className={styles.featureTitle}>{title}</h3>
            <p className={styles.featureDescription}>{description}</p>
        </div>
    );
}

function HomepageHeader() {
    const { siteConfig } = useDocusaurusContext();
    return (
        <header className={styles.heroSection}>
            <div className={styles.heroBg} />
            <div className={styles.heroGrid} />
            <FloatingParticles />
            <div className={styles.glowGreen} />
            <div className={styles.glowPurple} />

            <div className={styles.heroContent}>
                <div className={styles.heroRow}>
                    <div className={styles.heroLeft}>
                        <h1 className={styles.heroTitle}>Vanilla Core</h1>
                        <p className={styles.heroSubtitle}>{siteConfig.tagline}</p>
                        <ModrinthStats />
                        <div className={styles.heroButtons}>
                            <Link className={styles.btnPrimary} to="/docs">
                                Get Started
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M5 12h14M12 5l7 7-7 7" />
                                </svg>
                            </Link>
                            <Link className={styles.btnOutline} href="https://modrinth.com/plugin/Vanilla Core">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6M15 3h6v6M10 14L21 3" />
                                </svg>
                                Download on Modrinth
                            </Link>
                        </div>
                    </div>

                    <div className={styles.heroRight}>
                        <div className={styles.screenshotWrapper}>
                            <div className={styles.screenshotCard}>
                                <img
                                    src="/gifs/main-gui.gif"
                                    alt="Vanilla Core Minecraft GUI"
                                    className={styles.screenshotImg}
                                />
                                <div className={styles.screenshotOverlay} />
                            </div>
                            <div className={styles.screenshotGlow} />
                            <div className={styles.badgeVersion}>v1.2.1</div>
                            <div className={styles.badgeMc}>1.21.1+</div>
                        </div>
                    </div>
                </div>
            </div>

            <div className={styles.heroFade} />
        </header>
    );
}

function FeaturesSection() {
    const [isVisible, setIsVisible] = useState(false);
    const sectionRef = useRef(null);

    useEffect(() => {
        const observer = new IntersectionObserver(
            ([entry]) => {
                if (entry.isIntersecting) {
                    setIsVisible(true);
                    observer.disconnect();
                }
            },
            { threshold: 0.1 }
        );
        if (sectionRef.current) observer.observe(sectionRef.current);
        return () => observer.disconnect();
    }, []);

    return (
        <section ref={sectionRef} className={styles.featuresSection}>
            <div className={styles.sectionGlow} />
            <div className="container">
                <div className={styles.sectionHeader}>
                    <h2 className={styles.sectionTitle}>Powerful Features</h2>
                    <p className={styles.sectionSubtitle}>
                        Everything you need to run a successful SMP server, all in one plugin.
                    </p>
                </div>
                <div className={styles.featuresGrid}>
                    {features.map((feature, index) => (
                        <FeatureCard
                            key={feature.title}
                            {...feature}
                            index={index}
                            isVisible={isVisible}
                        />
                    ))}
                </div>
            </div>
        </section>
    );
}

function StatsSection() {
    const [isVisible, setIsVisible] = useState(false);
    const [stats, setStats] = useState({ downloads: 0, servers: 0, followers: 0 });
    const sectionRef = useRef(null);

    useEffect(() => {
        fetch('https://api.modrinth.com/v2/project/GH4H8ndx', {
            headers: { 'User-Agent': 'TejasLamba2006/Vanilla Core-docs' }
        })
            .then(res => res.json())
            .then(data => {
                setStats(prev => ({
                    ...prev,
                    downloads: data.downloads || 0,
                    followers: data.followers || 0
                }));
            })
            .catch(() => { });

        const BSTATS_PLUGIN_ID = 28654;
        if (BSTATS_PLUGIN_ID) {
            fetch(`https://bstats.org/api/v1/plugins/${BSTATS_PLUGIN_ID}/charts/servers/data/?maxElements=1`)
                .then(res => res.json())
                .then(data => {
                    if (data && data.length > 0) {
                        setStats(prev => ({ ...prev, servers: data[0][1] || 0 }));
                    }
                })
                .catch(() => { });
        }
    }, []);

    useEffect(() => {
        const observer = new IntersectionObserver(
            ([entry]) => {
                if (entry.isIntersecting) {
                    setIsVisible(true);
                    observer.disconnect();
                }
            },
            { threshold: 0.3 }
        );
        if (sectionRef.current) observer.observe(sectionRef.current);
        return () => observer.disconnect();
    }, []);

    const statItems = [
        { value: stats.downloads, suffix: '+', label: 'Total Downloads', icon: 'download' },
        { value: stats.servers, suffix: '', label: 'Active Servers', icon: 'server' },
        { value: stats.followers, suffix: '', label: 'Followers', icon: 'users' },
    ];

    return (
        <section ref={sectionRef} className={styles.statsSection}>
            <div className={styles.statsSectionGlow} />
            <div className="container">
                <div className={styles.sectionHeader}>
                    <h2 className={styles.sectionTitle}>Trusted by Server Owners</h2>
                    <p className={styles.sectionSubtitle}>Join the growing community using Vanilla Core.</p>
                </div>
                <div className={styles.statsGrid}>
                    {statItems.map((stat, index) => (
                        <div
                            key={stat.label}
                            className={styles.statCard}
                            style={{ animationDelay: `${index * 150}ms` }}
                        >
                            <div className={styles.statIcon}>
                                {stat.icon === 'download' && (
                                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" />
                                    </svg>
                                )}
                                {stat.icon === 'server' && (
                                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                        <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
                                        <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
                                        <line x1="6" y1="6" x2="6.01" y2="6" />
                                        <line x1="6" y1="18" x2="6.01" y2="18" />
                                    </svg>
                                )}
                                {stat.icon === 'users' && (
                                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 7a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
                                    </svg>
                                )}
                            </div>
                            <div className={styles.statValue}>
                                <AnimatedCounter value={stat.value} suffix={stat.suffix} isVisible={isVisible} />
                            </div>
                            <div className={styles.statLabel}>{stat.label}</div>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
}

function WhySection() {
    return (
        <section className={styles.whySection}>
            <div className="container">
                <div className={styles.whyGrid}>
                    <div className={styles.whyContent}>
                        <h2 className={styles.sectionTitle}>Why Vanilla Core?</h2>
                        <p className={styles.whyText}>
                            Running an SMP server shouldn't require 20 different plugins. Vanilla Core combines
                            all the essential features you need into one lightweight, well-optimized package.
                        </p>
                        <ul className={styles.whyList}>
                            <li>
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M20 6L9 17l-5-5" />
                                </svg>
                                Single JAR, no dependencies required
                            </li>
                            <li>
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M20 6L9 17l-5-5" />
                                </svg>
                                Optimized for performance
                            </li>
                            <li>
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M20 6L9 17l-5-5" />
                                </svg>
                                100% configurable
                            </li>
                            <li>
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M20 6L9 17l-5-5" />
                                </svg>
                                In-game GUI management
                            </li>
                            <li>
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M20 6L9 17l-5-5" />
                                </svg>
                                Active development and support
                            </li>
                        </ul>
                    </div>
                    <div className={styles.whyRelease}>
                        <h2 className={styles.sectionTitle}>Latest Release</h2>
                        <LatestVersion />
                    </div>
                </div>
            </div>
        </section>
    );
}

function CTASection() {
    return (
        <section className={styles.ctaSection}>
            <div className={styles.ctaGlow} />
            <div className="container">
                <div className={styles.ctaContent}>
                    <h2 className={styles.ctaTitle}>Support Development</h2>
                    <p className={styles.ctaText}>
                        Vanilla Core is free. If you find it useful, consider supporting development
                        to get early access to new features and priority feature requests.
                    </p>
                    <Link className={styles.ctaButton} href="https://paypal.me/tejaslamba">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
                        </svg>
                        Donate via PayPal
                    </Link>
                </div>
            </div>
        </section>
    );
}

const faqs = [
    {
        question: "What versions does Vanilla Core support?",
        answer: "Vanilla Core supports Paper/Spigot 1.21.1+ with Java 21+. Paper is recommended for better performance.",
        category: "general"
    },
    {
        question: "Is Vanilla Core free?",
        answer: "Yes! Vanilla Core is completely free. You can download it from Modrinth.",
        category: "general"
    },
    {
        question: "Can I use only some features?",
        answer: "Absolutely! Every feature can be individually enabled or disabled through the config or in-game GUI.",
        category: "features"
    },
    {
        question: "Does it conflict with other plugins?",
        answer: "Vanilla Core is designed to be compatible with most plugins including EssentialsX, LuckPerms, WorldGuard, and Vault.",
        category: "features"
    },
    {
        question: "Will this lag my server?",
        answer: "No! Vanilla Core is optimized for performance with efficient event listeners, cooldown systems, and minimal memory usage (typically under 10MB).",
        category: "performance"
    },
    {
        question: "How do I report bugs or suggest features?",
        answer: "You can report bugs or suggest features on our Discord server or GitHub issues page.",
        category: "support"
    }
];

function FAQSection() {
    const [openItems, setOpenItems] = useState([0]);

    const toggleItem = (index) => {
        setOpenItems(prev =>
            prev.includes(index) ? prev.filter(i => i !== index) : [...prev, index]
        );
    };

    return (
        <section className={styles.faqSection}>
            <div className="container">
                <div className={styles.sectionHeader}>
                    <h2 className={styles.sectionTitle}>Frequently Asked Questions</h2>
                    <p className={styles.sectionSubtitle}>Quick answers to common questions about Vanilla Core.</p>
                </div>
                <div className={styles.faqContainer}>
                    {faqs.map((faq, index) => {
                        const isOpen = openItems.includes(index);
                        return (
                            <div key={index} className={`${styles.faqItem} ${isOpen ? styles.faqItemOpen : ''}`}>
                                <button
                                    onClick={() => toggleItem(index)}
                                    className={styles.faqQuestion}
                                >
                                    <span>{faq.question}</span>
                                    <div className={`${styles.faqIcon} ${isOpen ? styles.faqIconOpen : ''}`}>
                                        {isOpen ? (
                                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                                <line x1="5" y1="12" x2="19" y2="12" />
                                            </svg>
                                        ) : (
                                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                                <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
                                            </svg>
                                        )}
                                    </div>
                                </button>
                                <div className={`${styles.faqAnswer} ${isOpen ? styles.faqAnswerOpen : ''}`}>
                                    <p>{faq.answer}</p>
                                </div>
                            </div>
                        );
                    })}
                </div>
                <div className={styles.faqMore}>
                    <Link to="/docs/faq" className={styles.faqMoreLink}>
                        View all FAQs
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M5 12h14M12 5l7 7-7 7" />
                        </svg>
                    </Link>
                </div>
            </div>
        </section>
    );
}

export default function Home() {
    const { siteConfig } = useDocusaurusContext();
    return (
        <Layout title="Home" description={siteConfig.tagline}>
            <HomepageHeader />
            <main>
                <FeaturesSection />
                <StatsSection />
                <WhySection />
                <FAQSection />
                <CTASection />
            </main>
        </Layout>
    );
}
