const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

// With JSDoc @type annotations, IDEs can provide config autocompletion
/** @type {import('@docusaurus/types').DocusaurusConfig} */
(module.exports = {
    title: 'Failgood',
    tagline: 'A simple and fast test runner for Kotlin',
    url: 'https://failgood.dev',
    baseUrl: '/',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',
    favicon: 'img/favicon.ico',
    organizationName: 'failgood',
    projectName: 'failgood',

    presets: [
        [
            '@docusaurus/preset-classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    sidebarPath: require.resolve('./sidebars.js'),
                    // Please change this to your repo.
                    editUrl: 'https://github.com/failgood/failgood/edit/main/docs/',
                },
                blog: false,
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }),
        ],
    ],

    themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
            ({
                navbar: {
                    title: 'My Site',
                    logo: {
                        alt: 'My Site Logo',
                        src: 'img/logo.svg',
                    },
                    items: [
                        {
                            type: 'doc',
                            docId: 'intro/index',
                            position: 'left',
                            label: 'Tutorial',
                        },
                        /*                        {to: '/blog', label: 'Blog', position: 'left'},
                                                {
                                                    href: 'https://github.com/failgood/failgood',
                                                    label: 'GitHub',
                                                    position: 'right',
                                                },*/
                    ],
                },
                footer: {
                    style: 'dark',
                    links: [
                        {
                            title: 'Docs',
                            items: [
                                {
                                    label: 'Tutorial',
                                    to: '/docs/intro/index',
                                },
                            ],
                        },
                        /*                        {
                                                    title: 'Community',
                                                    items: [
                                                        {
                                                            label: 'Stack Overflow',
                                                            href: 'https://stackoverflow.com/questions/tagged/docusaurus',
                                                        },
                                                        {
                                                            label: 'Discord',
                                                            href: 'https://discordapp.com/invite/docusaurus',
                                                        },
                                                        {
                                                            label: 'Twitter',
                                                            href: 'https://twitter.com/docusaurus',
                                                        },
                                                    ],
                                                },*/
                        {
                            title: 'More',
                            items: [
                                /*                                {
                                                                    label: 'Blog',
                                                                    to: '/blog',
                                                                },*/
                                {
                                    label: 'GitHub',
                                    href: 'https://github.com/failgood/failgood',
                                },
                            ],
                        },
                    ],
                    copyright: `Copyright © ${new Date().getFullYear()} Christoph Sturm. Built with Docusaurus.`,
                },
                prism: {
                    theme: lightCodeTheme,
                    darkTheme: darkCodeTheme,
                },
            }),
});
