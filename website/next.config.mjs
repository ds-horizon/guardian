/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'export',
  basePath: '/guardian',
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: true,
  },
  images: {
    unoptimized: true,
  },
}

export default nextConfig
