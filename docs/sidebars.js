/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
    tutorialSidebar: [
        'intro',
        'installation',
        'commands',
        'permissions',
        {
            type: 'category',
            label: 'Features',
            link: {
                type: 'generated-index',
                title: 'Features',
                description: 'All Vanilla Core features explained in detail.',
                slug: '/category/features',
            },
            items: [
                {
                    type: 'category',
                    label: 'Items & Combat',
                    items: [
                        'features/enchantment-limiter',
                        'features/item-limiter',
                        'features/ender-chest-item-limiter',
                        'features/mace-limiter',
                        'features/combat-restrictions',
                        'features/item-cooldowns',
                        'features/potion-bans',
                        'features/netherite-disabler',
                        'features/shield-mechanics',
                    ],
                },
                {
                    type: 'category',
                    label: 'World & Dimensions',
                    items: [
                        'features/dimension-locks',
                        'features/mob-manager',
                        'features/one-player-sleep',
                    ],
                },
                {
                    type: 'category',
                    label: 'Miscellaneous',
                    items: [
                        'features/ritual',
                        'features/infinite-restock',
                        'features/invisible-kills',
                        'features/item-explosion-immunity',
                        'features/minimap-control',
                        'features/faster-happy-ghasts',
                        'features/spectator-on-death',
                        'features/server-restart',
                    ],
                },
            ],
        },
        'configuration',
        'gui-reference',
        'faq',
    ],
};

export default sidebars;
