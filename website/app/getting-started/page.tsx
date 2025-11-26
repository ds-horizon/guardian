"use client"

import { useState } from "react"
import { motion } from "framer-motion"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardFooter } from "@/components/ui/card"
import { Copy, ExternalLink } from "lucide-react"
import Layout from "@/components/layout"

export default function GettingStartedPage() {
  const [copiedIndex, setCopiedIndex] = useState<string | null>(null)

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text)
    setCopiedIndex(id)
    setTimeout(() => setCopiedIndex(null), 2000)
  }

  // Custom Docker icon
  const DockerIcon = () => (
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
      <path d="M22 12.5c0 1.4-1.1 2.5-2.5 2.5S17 13.9 17 12.5 18.1 10 19.5 10 22 11.1 22 12.5zM17.5 15H17c-1.7 0-3-1.3-3-3s1.3-3 3-3h.5" />
      <rect x="2" y="10" width="5" height="5" rx="1" />
      <rect x="9" y="10" width="5" height="5" rx="1" />
      <rect x="16" y="10" width="5" height="5" rx="1" />
      <rect x="2" y="3" width="5" height="5" rx="1" />
      <rect x="9" y="3" width="5" height="5" rx="1" />
    </svg>
  )

  // Custom Maven icon
  const MavenIcon = () => (
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
      <path d="M3 2v20l9-4 9 4V2L12 6z" />
    </svg>
  )

  // Check icon for copy button
  const CheckIcon = () => (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className="size-4"
    >
      <polyline points="20 6 9 17 4 12" />
    </svg>
  )

  return (
    <Layout showBackButton simplifiedHeader>
      <div className="container max-w-[1200px] px-4 py-12 md:py-16">
        <div className="mb-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="flex flex-col items-center justify-center space-y-4 text-center mb-12"
          >
            <h1 className="text-3xl md:text-4xl font-bold tracking-tight">Guardian Setup Guide</h1>
            <p className="max-w-[700px] text-muted-foreground md:text-lg">
              Follow these simple steps to get Guardian up and running.
            </p>
          </motion.div>
        </div>

        <div className="max-w-4xl mx-auto">
          {/* Step 1: Prerequisites */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
            className="mb-12"
          >
            <div className="flex items-start">
              <div className="flex-shrink-0 w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary mr-4 mt-1">
                <span className="text-lg font-bold">1</span>
              </div>
              <div className="flex-grow">
                <h2 className="text-xl font-bold mb-4">Prerequisites</h2>
                <Card className="border border-border/50 bg-card/50 backdrop-blur-sm">
                  <CardContent className="pt-6">
                    <ul className="space-y-4 text-muted-foreground">
                      <li className="flex items-center">
                        <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary mr-3">
                          <DockerIcon />
                        </div>
                        <span>Docker</span>
                      </li>
                      <li className="flex items-center">
                        <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary mr-3">
                          <MavenIcon />
                        </div>
                        <span>Maven</span>
                      </li>
                    </ul>
                  </CardContent>
                </Card>
              </div>
            </div>
          </motion.div>

          {/* Step 2: Clone Repository */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: 0.1 }}
            className="mb-12"
          >
            <div className="flex items-start">
              <div className="flex-shrink-0 w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary mr-4 mt-1">
                <span className="text-lg font-bold">2</span>
              </div>
              <div className="flex-grow">
                <h2 className="text-xl font-bold mb-4">Clone the Repository</h2>
                <Card className="border border-border/50 bg-card/50 backdrop-blur-sm overflow-hidden">
                  <CardContent className="p-0">
                    <div className="relative">
                      <pre className="p-4 text-sm overflow-x-auto bg-black/90 text-white font-mono">
                        <code>git clone https://github.com/ds-horizon/guardian.git cd guardian</code>
                      </pre>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="absolute top-2 right-2 h-8 w-8 text-white/70 hover:text-white hover:bg-white/10 rounded-full"
                        onClick={() =>
                          copyToClipboard(
                            "git clone https://github.com/ds-horizon/guardian.git\ncd guardian",
                            "clone",
                          )
                        }
                      >
                        {copiedIndex === "clone" ? <CheckIcon /> : <Copy className="size-4" />}
                        <span className="sr-only">Copy code</span>
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>
          </motion.div>

          {/* Step 3: Start Guardian */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: 0.2 }}
            className="mb-12"
          >
            <div className="flex items-start">
              <div className="flex-shrink-0 w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary mr-4 mt-1">
                <span className="text-lg font-bold">3</span>
              </div>
              <div className="flex-grow">
                <h2 className="text-xl font-bold mb-4">Start Guardian</h2>
                <Card className="border border-border/50 bg-card/50 backdrop-blur-sm overflow-hidden">
                  <CardContent className="p-0">
                    <div className="relative">
                      <pre className="p-4 text-sm overflow-x-auto bg-black/90 text-white font-mono">
                        <code>./quick-start.sh</code>
                      </pre>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="absolute top-2 right-2 h-8 w-8 text-white/70 hover:text-white hover:bg-white/10 rounded-full"
                        onClick={() => copyToClipboard("./quick-start.sh", "start")}
                      >
                        {copiedIndex === "start" ? <CheckIcon /> : <Copy className="size-4" />}
                        <span className="sr-only">Copy code</span>
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>
          </motion.div>

          {/* Step 4: Test the Setup */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: 0.3 }}
            className="mb-12"
          >
            <div className="flex items-start">
              <div className="flex-shrink-0 w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary mr-4 mt-1">
                <span className="text-lg font-bold">4</span>
              </div>
              <div className="flex-grow">
                <h2 className="text-xl font-bold mb-4">Test the Setup</h2>
                <p className="text-muted-foreground mb-4">
                  Test with a passwordless flow using the following curl commands:
                </p>
                <Card className="border border-border/50 bg-card/50 backdrop-blur-sm overflow-hidden mb-4">
                  <CardContent className="p-0">
                    <div className="relative">
                      <div className="bg-black/80 text-white/90 text-xs px-4 py-2 border-b border-white/10">
                        Initialize passwordless authentication
                      </div>
                      <pre className="p-4 text-sm overflow-x-auto bg-black/90 text-white font-mono">
                        <code>{`curl --location 'localhost:8080/v1/passwordless/init' \\
--header 'Content-Type: application/json' \\
--header 'tenant-id: tenant1' \\
--data '{
  "flow": "signinup",
  "responseType": "token",
  "contacts": [{
    "channel": "sms",
    "identifier": "9999999999"
  }],
  "metaInfo": {
    "ip": "127.0.0.1",
    "location": "localhost",
    "deviceName": "localhost",
    "source": "app"
  }
}'`}</code>
                      </pre>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="absolute top-2 right-2 h-8 w-8 text-white/70 hover:text-white hover:bg-white/10 rounded-full"
                        onClick={() =>
                          copyToClipboard(
                            `curl --location 'localhost:8080/v1/passwordless/init' \\
--header 'Content-Type: application/json' \\
--header 'tenant-id: tenant1' \\
--data '{
  "flow": "signinup",
  "responseType": "token",
  "contacts": [{
    "channel": "sms",
    "identifier": "9999999999"
  }],
  "metaInfo": {
    "ip": "127.0.0.1",
    "location": "localhost",
    "deviceName": "localhost",
    "source": "app"
  }
}'`,
                            "test1",
                          )
                        }
                      >
                        {copiedIndex === "test1" ? <CheckIcon /> : <Copy className="size-4" />}
                        <span className="sr-only">Copy code</span>
                      </Button>
                    </div>
                  </CardContent>
                </Card>

                <Card className="border border-border/50 bg-card/50 backdrop-blur-sm overflow-hidden">
                  <CardContent className="p-0">
                    <div className="relative">
                      <div className="bg-black/80 text-white/90 text-xs px-4 py-2 border-b border-white/10">
                        Complete authentication (using mock OTP)
                      </div>
                      <pre className="p-4 text-sm overflow-x-auto bg-black/90 text-white font-mono">
                        <code>{`curl --location 'localhost:8080/v1/passwordless/complete' \\
--header 'Content-Type: application/json' \\
--header 'tenant-id: tenant1' \\
--data '{
  "state": "<state-from-init-response>",
  "otp": "999999"
}'`}</code>
                      </pre>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="absolute top-2 right-2 h-8 w-8 text-white/70 hover:text-white hover:bg-white/10 rounded-full"
                        onClick={() =>
                          copyToClipboard(
                            `curl --location 'localhost:8080/v1/passwordless/complete' \\
--header 'Content-Type: application/json' \\
--header 'tenant-id: tenant1' \\
--data '{
  "state": "<state-from-init-response>",
  "otp": "999999"
}'`,
                            "test2",
                          )
                        }
                      >
                        {copiedIndex === "test2" ? <CheckIcon /> : <Copy className="size-4" />}
                        <span className="sr-only">Copy code</span>
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>
          </motion.div>

          {/* Configuration */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: 0.4 }}
            className="mb-12"
          >
            <div className="flex items-start">
              <div className="flex-shrink-0 w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary mr-4 mt-1">
                <span className="text-lg font-bold">5</span>
              </div>
              <div className="flex-grow">
                <h2 className="text-xl font-bold mb-4">Configuration</h2>
                <Card className="border border-border/50 bg-card/50 backdrop-blur-sm">
                  <CardContent className="pt-6">
                    <p className="text-muted-foreground">
                      Guardian offers extensive configuration options for authentication methods, database connections,
                      email settings, and more.
                    </p>
                  </CardContent>
                  <CardFooter className="pb-6">
                    <Button className="rounded-full" asChild>
                      <Link
                        href="https://github.com/ds-horizon/guardian/blob/main/docs/configuration.md"
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        View Configuration Documentation
                        <ExternalLink className="ml-2 size-4" />
                      </Link>
                    </Button>
                  </CardFooter>
                </Card>
              </div>
            </div>
          </motion.div>

          {/* Next Steps */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: 0.5 }}
            className="mt-16 text-center"
          >
            <h2 className="text-xl font-bold mb-4">Next Steps</h2>
            <p className="text-muted-foreground max-w-2xl mx-auto mb-6">
              Now that you have Guardian up and running, explore the documentation to learn more about its features and
              how to integrate it with your applications.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button className="rounded-full" asChild>
                <Link href="/api-docs">
                  API Documentation
                  <ExternalLink className="ml-2 size-4" />
                </Link>
              </Button>
            </div>
          </motion.div>
        </div>
      </div>
    </Layout>
  )
}
