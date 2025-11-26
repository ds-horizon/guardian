import type { ReactNode } from "react"
import Navbar from "@/components/navbar"
import Footer from "@/components/footer"

interface LayoutProps {
  children: ReactNode
  showBackButton?: boolean
  simplifiedHeader?: boolean
}

export default function Layout({ children, showBackButton = false, simplifiedHeader = false }: LayoutProps) {
  return (
    <div className="flex min-h-[100dvh] flex-col">
      <Navbar showBackButton={showBackButton} simplifiedHeader={simplifiedHeader} />
      <main className="flex-1">{children}</main>
      <Footer />
    </div>
  )
}
