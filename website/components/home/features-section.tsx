"use client"

import { motion } from "framer-motion"
import { Lock, Layers, Clock, Code, Users, Github } from "lucide-react"
import SectionHeader from "@/components/ui/section-header"
import FeatureCard from "@/components/feature-card"

const features = [
  {
    title: "Enterprise-Grade Security",
    description: "Built with security best practices and regular security audits to keep your users' data safe.",
    icon: <Lock className="size-5" />,
  },
  {
    title: "Flexible Integration",
    description: "Works seamlessly with your existing user service and tech stack without vendor lock-in.",
    icon: <Layers className="size-5" />,
  },
  {
    title: "Quick Implementation",
    description: "Get up and running in minutes with our comprehensive documentation and starter templates.",
    icon: <Clock className="size-5" />,
  },
  {
    title: "Multi-Platform Support",
    description: "Native support for web, mobile, and API authentication across all your applications.",
    icon: <Code className="size-5" />,
  },
  {
    title: "OpenID Provider",
    description: "Enable single sign-on capabilities for your entire ecosystem of applications.",
    icon: <Users className="size-5" />,
  },
  {
    title: "100% Open Source",
    description: "Full transparency with MIT license. Inspect, modify, and contribute to the codebase.",
    icon: <Github className="size-5" />,
  },
]

export default function FeaturesSection() {
  const container = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1,
      },
    },
  }

  const item = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0 },
  }

  return (
    <section id="features" className="w-full py-12 md:py-20">
      <div className="container max-w-[1600px] px-4 md:px-6">
        <SectionHeader
          badge="Features"
          title="Why Choose Guardian?"
          description="Our comprehensive authentication solution provides all the tools you need to secure your applications while maintaining full control over your user data."
        />

        <motion.div
          variants={container}
          initial="hidden"
          whileInView="show"
          viewport={{ once: true }}
          className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3"
        >
          {features.map((feature, i) => (
            <motion.div key={i} variants={item}>
              <FeatureCard icon={feature.icon} title={feature.title} description={feature.description} />
            </motion.div>
          ))}
        </motion.div>
      </div>
    </section>
  )
}
