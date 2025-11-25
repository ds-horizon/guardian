"use client"
import Link from "next/link"
import { Github, MessageSquare, GitPullRequest, BookOpen, ExternalLink } from "lucide-react"
import { Card, CardContent } from "@/components/ui/card"
import SectionHeader from "@/components/ui/section-header"

export default function ContactSection() {
  return (
    <section id="contact" className="w-full py-12 md:py-20">
      <div className="container max-w-[1600px] px-4 md:px-6">
        <SectionHeader
          badge="Contact"
          title="Get in Touch"
          description="Have questions or need help with Guardian? We're here to help."
        />

        <div className="grid md:grid-cols-2 gap-8 max-w-4xl mx-auto">
          <Card className="overflow-hidden border-border/40 bg-gradient-to-b from-background to-muted/10 backdrop-blur">
            <CardContent className="p-6">
              <h3 className="text-xl font-bold mb-4">Community Support</h3>
              <ul className="space-y-4">
                <li className="flex items-start gap-3">
                  <Github className="size-5 text-primary mt-0.5" />
                  <div>
                    <p className="font-medium">GitHub Discussions</p>
                    <p className="text-sm text-muted-foreground">Ask questions and share ideas with the community</p>
                    <Link
                      href="https://github.com/ds-horizon/guardian/discussions"
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-sm text-primary hover:underline inline-flex items-center gap-1 mt-1"
                    >
                      Join the discussion
                      <ExternalLink className="size-3" />
                    </Link>
                  </div>
                </li>
                <li className="flex items-start gap-3">
                  <MessageSquare className="size-5 text-primary mt-0.5" />
                  <div>
                    <p className="font-medium">GitHub Issues</p>
                    <p className="text-sm text-muted-foreground">Report bugs or request features</p>
                    <Link
                      href="https://github.com/ds-horizon/guardian/issues"
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-sm text-primary hover:underline inline-flex items-center gap-1 mt-1"
                    >
                      Open an issue
                      <ExternalLink className="size-3" />
                    </Link>
                  </div>
                </li>
                <li className="flex items-start gap-3">
                  <GitPullRequest className="size-5 text-primary mt-0.5" />
                  <div>
                    <p className="font-medium">Pull Requests</p>
                    <p className="text-sm text-muted-foreground">Contribute code or documentation improvements</p>
                    <Link
                      href="https://github.com/ds-horizon/guardian/pulls"
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-sm text-primary hover:underline inline-flex items-center gap-1 mt-1"
                    >
                      Create a pull request
                      <ExternalLink className="size-3" />
                    </Link>
                  </div>
                </li>
                <li className="flex items-start gap-3">
                  <BookOpen className="size-5 text-primary mt-0.5" />
                  <div>
                    <p className="font-medium">Documentation</p>
                    <p className="text-sm text-muted-foreground">Comprehensive guides and API references</p>
                    <Link
                      href="/docs/quick-start"
                      rel="noopener noreferrer"
                      className="text-sm text-primary hover:underline inline-flex items-center gap-1 mt-1"
                    >
                      Read the docs
                      <ExternalLink className="size-3" />
                    </Link>
                  </div>
                </li>
              </ul>
            </CardContent>
          </Card>

          <Card className="overflow-hidden border-border/40 bg-gradient-to-b from-background to-muted/10 backdrop-blur">
            <CardContent className="p-6 flex flex-col items-center justify-center text-center h-full">
              <h3 className="text-xl font-bold mb-4">Contact Us</h3>
              <p className="text-muted-foreground mb-6">
                Have questions or need assistance with Guardian? Feel free to reach out to our team directly.
              </p>
              <div className="flex items-center justify-center gap-2 bg-muted/50 px-6 py-4 rounded-lg">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className="size-5"
                >
                  <rect width="20" height="16" x="2" y="4" rx="2" />
                  <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
                </svg>
                <Link href="mailto:info@dreamsportslabs.com" className="text-primary font-medium hover:underline">
                  info@dreamsportslabs.com
                </Link>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </section>
  )
}
