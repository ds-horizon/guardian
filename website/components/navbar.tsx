"use client"

import type React from "react"

import { useState, useEffect } from "react"
import Link from "next/link"
import { motion } from "framer-motion"
import { Menu, X, ArrowLeft, Shield, Github } from "lucide-react"
import { Button } from "@/components/ui/button"
import { usePathname } from "next/navigation"

interface NavbarProps {
  showBackButton?: boolean
  simplifiedHeader?: boolean
}

export default function Navbar({ showBackButton = false, simplifiedHeader = false }: NavbarProps) {
  const [isScrolled, setIsScrolled] = useState(false)
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const pathname = usePathname()

  useEffect(() => {
    const handleScroll = () => {
      if (window.scrollY > 10) {
        setIsScrolled(true)
      } else {
        setIsScrolled(false)
      }
    }

    window.addEventListener("scroll", handleScroll)
    return () => window.removeEventListener("scroll", handleScroll)
  }, [])

  // Define navigation items to avoid repetition
  const navItems = [
    {
      label: "Features",
      href: "/#features",
      isAnchor: true,
    },
    {
      label: "FAQ",
      href: "/#faq",
      isAnchor: true,
    },
    {
      label: "Documentation",
      href: "/docs",
      external: true,
    },
    {
      label: "API Reference",
      href: "/docs/api-docs",
    },
  ]

  const handleNavigation = (item: any, e?: React.MouseEvent) => {
    if (item.isAnchor && pathname === "/") {
      e?.preventDefault()
      const element = document.getElementById(item.href.replace("/#", ""))
      if (element) {
        const yOffset = -80
        const y = element.getBoundingClientRect().top + window.pageYOffset + yOffset
        window.scrollTo({ top: y, behavior: "smooth" })
      }
    }

    if (mobileMenuOpen) {
      setMobileMenuOpen(false)
    }
  }

  return (
    <header
      className={`sticky top-0 z-50 w-full transition-all duration-300 ${
        isScrolled ? "bg-background/95 backdrop-blur-md shadow-sm" : "bg-background/50 backdrop-blur-sm"
      }`}
    >
      <div className="container max-w-[1600px] py-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-8">
            <Link href="/" className="flex items-center gap-2 font-bold">
              <div className="size-9 rounded-lg bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center text-primary-foreground">
                <Shield className="size-5" />
              </div>
              <span className="text-lg">Guardian</span>
            </Link>

            {showBackButton && (
              <Button variant="ghost" size="sm" asChild>
                <Link href="/">
                  <ArrowLeft className="mr-2 size-4" />
                  Back to Home
                </Link>
              </Button>
            )}

            {!simplifiedHeader && (
              <nav className="hidden md:flex items-center gap-1">
                {navItems.map((item, index) => (
                  <Link
                    key={index}
                    href={item.href}
                    target={item.external ? "_blank" : undefined}
                    rel={item.external ? "noopener noreferrer" : undefined}
                    onClick={(e) => handleNavigation(item, e)}
                    className="px-4 py-2 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted/50 transition-colors"
                  >
                    {item.label}
                  </Link>
                ))}
              </nav>
            )}
          </div>

          {!simplifiedHeader && (
            <div className="flex items-center gap-3">
              <Link
                href="https://github.com/ds-horizon/guardian"
                target="_blank"
                rel="noopener noreferrer"
                className="hidden md:flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted/50 transition-colors"
              >
                <Github className="size-4" />
                <span>GitHub</span>
              </Link>

              <Button
                variant="ghost"
                size="icon"
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                className="md:hidden"
              >
                {mobileMenuOpen ? <X className="size-5" /> : <Menu className="size-5" />}
                <span className="sr-only">Toggle menu</span>
              </Button>
            </div>
          )}
        </div>
      </div>

      {/* Mobile menu - only show if not simplified */}
      {!simplifiedHeader && mobileMenuOpen && (
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: "auto" }}
          exit={{ opacity: 0, height: 0 }}
          className="md:hidden bg-background border-b"
        >
          <div className="container py-4 flex flex-col gap-2">
            {navItems.map((item, index) => (
              <Link
                key={index}
                href={item.href}
                target={item.external ? "_blank" : undefined}
                rel={item.external ? "noopener noreferrer" : undefined}
                onClick={(e) => handleNavigation(item, e)}
                className="px-4 py-3 rounded-md text-sm font-medium hover:bg-muted/50 transition-colors text-left"
              >
                {item.label}
              </Link>
            ))}
            <Link
              href="https://github.com/ds-horizon/guardian"
              target="_blank"
              rel="noopener noreferrer"
              className="px-4 py-3 rounded-md text-sm font-medium hover:bg-muted/50 transition-colors flex items-center gap-2"
              onClick={() => setMobileMenuOpen(false)}
            >
              <Github className="size-4" />
              GitHub
            </Link>
          </div>
        </motion.div>
      )}
    </header>
  )
}
