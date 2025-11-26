"use client"

import { motion } from "framer-motion"
import { Badge } from "@/components/ui/badge"

interface SectionHeaderProps {
  badge?: string
  title: string
  description?: string
  className?: string
}

export default function SectionHeader({ badge, title, description, className = "" }: SectionHeaderProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true }}
      transition={{ duration: 0.5 }}
      className={`flex flex-col items-center justify-center space-y-4 text-center mb-12 ${className}`}
    >
      {badge && (
        <Badge className="rounded-full px-4 py-1.5 text-sm font-medium" variant="secondary">
          {badge}
        </Badge>
      )}
      <h2 className="text-3xl md:text-4xl font-bold tracking-tight">{title}</h2>
      {description && <p className="max-w-[800px] text-muted-foreground md:text-lg">{description}</p>}
    </motion.div>
  )
}
