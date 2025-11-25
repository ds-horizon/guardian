import { defineCollection } from 'astro:content';
import { docsSchema } from '@astrojs/starlight/schema';
import { z } from 'zod';

export const collections = {
  docs: defineCollection({ 
    schema: (context) => {
      const baseSchema = docsSchema()(context);
      return baseSchema.omit({ title: true }).extend({
        title: z.string().optional(),
        description: z.string().optional(),
      });
    },
  }),
};

