// @ts-check
import { themes as prismThemes } from 'prism-react-renderer';

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'SMP Core',
    tagline: 'The Ultimate SMP Management Plugin for Minecraft',
    favicon: 'img/favicon.ico',

    url: 'https://smpcore.tejaslamba.com',
    baseUrl: '/',

    organizationName: 'TejasLamba2006',
    projectName: 'smp-core',
    trailingSlash: false,

    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',

    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },

    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    sidebarPath: './sidebars.js',
                },
                blog: false,
                theme: {
                    customCss: './src/css/custom.css',
                },
            }),
        ],
    ],

    themeConfig:
        /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            image: 'img/smp-core-social-card.png',
            navbar: {
                title: 'SMP Core',
                logo: {
                    alt: 'SMP Core Logo',
                    src: 'img/logo.png',
                },
                items: [
                    {
                        type: 'docSidebar',
                        sidebarId: 'tutorialSidebar',
                        position: 'left',
                        label: 'Documentation',
                    },
                    {
                        href: 'https://modrinth.com/plugin/smp-core',
                        label: 'Modrinth',
                        position: 'right',
                    },
                    {
                        href: 'https://discord.gg/7fQPG4Grwt?utm_source=smpcore.tejaslamba.com',
                        label: 'Discord',
                        position: 'right',
                    },
                ],
            },
            footer: {
                style: 'dark',
                links: [
                    {
                        title: 'Documentation',
                        items: [
                            {
                                label: 'Getting Started',
                                to: '/docs',
                            },
                            {
                                label: 'Features',
                                to: '/docs/category/features',
                            },
                            {
                                label: 'Configuration',
                                to: '/docs/configuration',
                            },
                        ],
                    },
                    {
                        title: 'Community',
                        items: [
                            {
                                label: 'Discord',
                                href: 'https://discord.gg/7fQPG4Grwt?utm_source=smpcore.tejaslamba.com',
                            },
                        ],
                    },
                    {
                        title: 'Download',
                        items: [
                            {
                                label: 'Modrinth',
                                href: 'https://modrinth.com/plugin/smp-core',
                            },
                        ],
                    },
                    {
                        title: 'Support',
                        items: [
                            {
                                label: 'Donate via PayPal',
                                href: 'https://paypal.me/tejaslamba',
                            },
                        ],
                    },
                ],
                copyright: `Copyright © ${new Date().getFullYear()} SMP Core. By Tejas Lamba`,
            },
            prism: {
                theme: prismThemes.github,
                darkTheme: prismThemes.dracula,
                additionalLanguages: ['java', 'yaml', 'bash'],
            },
            colorMode: {
                defaultMode: 'dark',
                disableSwitch: false,
                respectPrefersColorScheme: true,
            },
            announcementBar: {
                id: 'support_us',
                content:
                    'Support development and get early access to new features - <a target="_blank" rel="noopener noreferrer" href="https://paypal.me/tejaslamba">Donate via PayPal</a>',
                backgroundColor: '#6366f1',
                textColor: '#ffffff',
                isCloseable: true,
            },
        }),
};

export default config;
