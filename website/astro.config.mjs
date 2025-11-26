import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import react from '@astrojs/react';

// https://astro.build/config
export default defineConfig({
  srcDir: './docs',
  integrations: [
    react(),
    starlight({
      title: 'Guardian Docs',
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
          ],
        },
        {
          label: 'Configuration',
          items: [
            { label: 'Configuration', link: '/configuration/configuration' },
            { label: 'User Configuration', link: '/configuration/user-configuration' },
            { label: 'SMS/Email Configuration', link: 'configuration/sms-email-configuration' },
          ],
        },
        {
            label: 'Features',
            items: [
              { label: 'Passwordless Authentication', link: '/features/passwordless-authentication' },
              { label: 'Username/Password Authentication', link: '/features/username-password-authentication' },
              { label: 'Social Authentication', link: '/features/social-authentication' },
              { label: 'Google Authentication', link: '/features/google-authentication' },
              { label: 'Facebook Authentication', link: '/features/facebook-authentication' },
              { label: 'Post Authentication', link: '/features/post-authentication' },
             
            ],
          },
          {
            label: 'Deployment',
            items: [
              { label: 'Deployment', link: '/deployment/deployment' }
            ],
          }
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
  outDir: './dist',
  base: '/guardian/docs',
});

