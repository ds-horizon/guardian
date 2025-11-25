"use client"

import { motion } from "framer-motion"
import Link from "next/link"
import { ArrowRight } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import GitHubStatsDisplay from "@/components/github-stats"

export default function HeroSection() {
  return (
    <section className="w-full py-16 md:py-24 lg:py-32 overflow-hidden">
      <div className="container max-w-[1600px] px-4 md:px-6 relative">
        <div className="absolute inset-0 -z-10 h-full w-full bg-black bg-[linear-gradient(to_right,#1f1f1f_1px,transparent_1px),linear-gradient(to_bottom,#1f1f1f_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_110%)]"></div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="text-center max-w-5xl mx-auto mb-12"
        >
          <div className="h-8 md:h-10 lg:h-12"></div>
          <Badge className="mb-4 rounded-full px-4 py-1.5 text-sm font-medium" variant="secondary">
            Open Source
          </Badge>
          <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold tracking-tight mb-6 bg-clip-text text-transparent bg-gradient-to-r from-foreground to-foreground/70">
            Enterprise-grade Authentication & Authorization
          </h1>
          <p className="text-lg md:text-xl text-muted-foreground mb-8 max-w-3xl mx-auto">
            Guardian is a robust, open-source authentication and authorization solution designed for modern
            applications. It provides a comprehensive suite of authentication methods while giving you complete control
            over your user data.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button size="lg" className="rounded-full h-12 px-8 text-base" asChild>
              <Link href="/docs/quick-start">
                Get Started
                <ArrowRight className="ml-2 size-4" />
              </Link>
            </Button>
          </div>
          <div className="mt-8">
            <GitHubStatsDisplay repo="ds-horizon/guardian" />
          </div>
        </motion.div>
      </div>
    </section>
  )
}
