"use client"

import { useEffect, useState } from "react"
import Layout from "@/components/layout"
import HeroSection from "@/components/home/hero-section"
import FeaturesSection from "@/components/home/features-section"
import FaqSection from "@/components/home/faq-section"
import ContactSection from "@/components/home/contact-section"
import ScrollToTop from "@/components/scroll-to-top"

export default function LandingPage() {
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
  }, [])

  return (
    <Layout>
      <HeroSection />
      <FeaturesSection />
      <FaqSection />
      <ContactSection />
      <ScrollToTop />
    </Layout>
  )
}
