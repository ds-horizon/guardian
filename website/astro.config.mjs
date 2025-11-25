import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
  srcDir: './docs',
  integrations: [
    starlight({
      title: 'Docs',
      description: 'Authentication and Authorization Service Documentation',
      social: [
        {
          label: 'GitHub',
          icon: 'github',
          href: 'https://github.com/ds-horizon/guardian',
        }
      ],
      sidebar: [
        {
          label: 'Introduction',
          items: [
            { label: 'Getting Started', link: '/getting-started/' },
            { label: 'Quick Start', link: '/quick-start/' },
          ],
        },
        {
          label: 'Core Concepts',
          items: [
            { label: 'Authentication Flows', link: '/authentication/' },
            { label: 'Configuration', link: '/configuration/' },
            { label: 'Security', link: '/security/' },
            { label: 'Performance', link: '/performance/' },
            { label: 'SMS', link: '/sms-email-configuration/' },
          ],
        },
      ],
      customCss: [
        './styles/starlight-custom.css',
      ],
      defaultLocale: 'root',
      locales: {
        root: {
          label: 'English',
          lang: 'en',
        },
      },
      // Enable search
      components: {
        // Use built-in search
      },
    }),
  ],
  output: 'static',
  outDir: './docs-dist',
  base: '/docs',
});

