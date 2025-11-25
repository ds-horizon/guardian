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
            { label: 'Quick Start', link: '/quick-start/' },
            { label: 'API Reference', link: '/api-reference/' },
          ],
        },
        {
          label: 'Configuration',
          items: [
            { label: 'Configuration', link: '/configuration/configuration' },
            { label: 'User Configuration', link: '/configuration/user-configuration' },
          
            { label: 'SMS Configuration', link: 'configuration/sms-configuration' },
          ],
        },
        {
            label: 'Features',
            items: [
              { label: 'Username/Password Authentication', link: '/features/username-password-authentication' },
              { label: 'Social Authentication', link: '/features/social-authentication' },
              { label: 'Google Authentication', link: '/features/google-authentication' },
              { label: 'Facebook Authentication', link: '/features/facebook-authentication' },
              { label: 'Post Authentication', link: '/features/post-authentication' },
             
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

