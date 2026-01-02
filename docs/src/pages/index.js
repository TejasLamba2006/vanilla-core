import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';
import styles from './index.module.css';

import { GiCrossedSwords, GiAnvil, GiMapleLeaf, GiSkeletonKey } from 'react-icons/gi';
import { MdOutlineInventory2, MdOutlineSettings } from 'react-icons/md';
import { LatestVersion, ModrinthStats } from '../components/ModrinthVersions';

const features = [
    {
        title: 'Combat Balance',
        image: '/img/features/combat-balance.svg',
        icon: GiCrossedSwords,
        description: 'Control maces, enchantments, and netherite to prevent overpowered gear from ruining your SMP experience.',
    },
    {
        title: 'Item Limiting',
        image: '/img/features/item-limiter.svg',
        icon: MdOutlineInventory2,
        description: 'Set maximum quantities for any item per player. Perfect for limiting totems, gaps, and other powerful items.',
    },
    {
        title: 'Dimension Control',
        image: '/img/features/dimension-control.svg',
        icon: GiMapleLeaf,
        description: 'Lock and unlock the Nether and End dimensions with commands. Schedule automatic unlocks for your server events.',
    },
    {
        title: 'Mob Management',
        image: '/img/features/mob-management.svg',
        icon: GiSkeletonKey,
        description: 'Control mob spawning in specific regions with WorldGuard integration. Perfect for spawn areas and events.',
    },
    {
        title: 'Quality of Life',
        image: '/img/features/qol.svg',
        icon: GiAnvil,
        description: 'One-player sleep, infinite villager restocks, and invisible kill messages for a smoother experience.',
    },
    {
        title: 'In-Game GUI',
        image: '/img/features/gui.svg',
        icon: MdOutlineSettings,
        description: 'Manage all features through an intuitive in-game menu. No need to edit config files manually.',
    },
];

function Feature({ title, icon: IconComponent, image, description }) {
    return (
        <div className={clsx('col col--4')}>
            <div className="feature-card margin-bottom--lg">
                <div className="feature-icon">
                    {image ? (
                        <img src={image} alt={`${title} icon`} className="feature-image" />
                    ) : (
                        <IconComponent size={34} />
                    )}
                </div>
                <Heading as="h3" className="text--center">{title}</Heading>
                <p className="text--center">{description}</p>
            </div>
        </div>
    );
}

function HomepageHeader() {
    const { siteConfig } = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <Heading as="h1" className="hero__title">
                    {siteConfig.title}
                </Heading>
                <p className="hero__subtitle">{siteConfig.tagline}</p>
                <ModrinthStats />
                <div className={styles.heroRow}>
                    <div className={styles.heroLeft}>
                        <div className={styles.buttons}>
                            <Link
                                className="button button--secondary button--lg"
                                to="/docs">
                                Get Started
                            </Link>
                            <Link
                                className="button button--outline button--lg margin-left--md"
                                style={{ color: 'white', borderColor: 'white' }}
                                href="https://modrinth.com/plugin/smpcore">
                                Download on Modrinth
                            </Link>
                        </div>
                    </div>
                    <div className={styles.heroRight}>
                        <img src="/gifs/main-gui.gif" alt="Main GUI demo" className={styles.heroGif} />
                    </div>
                </div>
            </div>
        </header>
    );
}

export default function Home() {
    const { siteConfig } = useDocusaurusContext();
    return (
        <Layout
            title="Home"
            description={siteConfig.tagline}>
            <HomepageHeader />
            <main>
                <section className="margin-vert--xl">
                    <div className="container">
                        <div className="row">
                            {features.map((props, idx) => (
                                <Feature key={idx} {...props} />
                            ))}
                        </div>
                    </div>
                </section>

                <section className="margin-vert--xl" style={{ background: 'var(--ifm-background-surface-color)', padding: '4rem 0' }}>
                    <div className="container">
                        <div className="row">
                            <div className="col col--6">
                                <Heading as="h2">Why SMP Core?</Heading>
                                <p style={{ fontSize: '1.1rem' }}>
                                    Running an SMP server shouldn't require 20 different plugins. SMP Core combines
                                    all the essential features you need into one lightweight, well-optimized package.
                                </p>
                                <ul style={{ fontSize: '1.1rem' }}>
                                    <li>Single JAR, no dependencies required</li>
                                    <li>Optimized for performance</li>
                                    <li>100% configurable</li>
                                    <li>In-game GUI management</li>
                                    <li>Active development and support</li>
                                </ul>
                            </div>
                            <div className="col col--6">
                                <Heading as="h2">Latest Release</Heading>
                                <LatestVersion />
                            </div>
                        </div>
                    </div>
                </section>

                <section className="margin-vert--xl" style={{ padding: '4rem 0' }}>
                    <div className="container text--center">
                        <Heading as="h2">Support Development</Heading>
                        <p style={{ fontSize: '1.1rem', maxWidth: '600px', margin: '0 auto 2rem' }}>
                            SMP Core is free and open source. If you find it useful, consider supporting development
                            to get early access to new features and priority feature requests.
                        </p>
                        <Link
                            className="button button--primary button--lg"
                            href="https://paypal.me/tejaslamba">
                            Donate via PayPal
                        </Link>
                    </div>
                </section>
            </main>
        </Layout>
    );
}
