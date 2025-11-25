"use client"

import { useState, useEffect } from "react"
import { ChevronDown, ChevronRight, Code, Copy, Lock, MessageSquare, RefreshCw, Send, User, Search } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Input } from "@/components/ui/input"
import { toast } from "@/hooks/use-toast"
import Layout from "@/components/layout"
import SectionHeader from "@/components/ui/section-header"

// Extract API spec to a separate file to reduce page size
import { apiSpec } from "@/lib/api-spec"

// Helper function to get the icon for a tag
const getTagIcon = (tag: string) => {
  switch (tag) {
    case "Passwordless":
      return <MessageSquare className="size-5" />
    case "Password":
      return <Lock className="size-5" />
    case "Social":
      return <User className="size-5" />
    case "Session Management":
      return <RefreshCw className="size-5" />
    default:
      return <Code className="size-5" />
  }
}

// Helper function to get the method color
const getMethodColor = (method: string) => {
  switch (method.toLowerCase()) {
    case "get":
      return "bg-blue-600"
    case "post":
      return "bg-green-600"
    case "put":
      return "bg-amber-600"
    case "delete":
      return "bg-red-600"
    case "patch":
      return "bg-purple-600"
    default:
      return "bg-gray-600"
  }
}

// Helper function to resolve schema references
const resolveRef = (ref: string, components: any) => {
  const path = ref.replace("#/components/", "").split("/")
  let result = components
  for (const segment of path) {
    result = result[segment]
  }
  return result
}

// Helper function to format JSON
const formatJSON = (obj: any) => {
  return JSON.stringify(obj, null, 2)
}

export default function ApiDocsPage() {
  const [activeEndpoint, setActiveEndpoint] = useState<string | null>(null)
  const [expandedTags, setExpandedTags] = useState<Record<string, boolean>>({})
  const [copiedText, setCopiedText] = useState<string | null>(null)
  const [searchQuery, setSearchQuery] = useState("")
  const [apiUrl, setApiUrl] = useState("https://guardian-api.dreamsportslabs.com")
  const [requestHeaders, setRequestHeaders] = useState<Record<string, string>>({
    "Content-Type": "application/json",
    "tenant-id": "tenant1",
  })
  const [requestBody, setRequestBody] = useState<string>("")
  const [responseData, setResponseData] = useState<any>(null)
  const [responseStatus, setResponseStatus] = useState<number | null>(null)
  const [responseHeaders, setResponseHeaders] = useState<Record<string, string>>({})
  const [isLoading, setIsLoading] = useState(false)
  const [responseTime, setResponseTime] = useState<number | null>(null)

  // Group endpoints by tags
  const endpointsByTag: Record<string, { path: string; method: string; operation: any }[]> = {}

  Object.entries(apiSpec.paths).forEach(([path, pathItem]: [string, any]) => {
    Object.entries(pathItem).forEach(([method, operation]: [string, any]) => {
      const tags = operation.tags || ["default"]
      tags.forEach((tag: string) => {
        if (!endpointsByTag[tag]) {
          endpointsByTag[tag] = []
        }
        endpointsByTag[tag].push({ path, method, operation })
      })
    })
  })

  // Toggle tag expansion
  const toggleTag = (tag: string) => {
    setExpandedTags((prev) => ({
      ...prev,
      [tag]: !prev[tag],
    }))
  }

  // Copy to clipboard function
  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text)
    setCopiedText(id)
    setTimeout(() => setCopiedText(null), 2000)
  }

  // Filter endpoints based on search query
  const filteredEndpoints = Object.entries(endpointsByTag).reduce(
    (acc: Record<string, { path: string; method: string; operation: any }[]>, [tag, endpoints]) => {
      const filtered = endpoints.filter(
        (endpoint) =>
          endpoint.path.toLowerCase().includes(searchQuery.toLowerCase()) ||
          endpoint.operation.summary.toLowerCase().includes(searchQuery.toLowerCase()) ||
          tag.toLowerCase().includes(searchQuery.toLowerCase()),
      )
      if (filtered.length > 0) {
        acc[tag] = filtered
      }
      return acc
    },
    {},
  )

  const sendRequest = async (path: string, method: string) => {
    try {
      setIsLoading(true)
      setResponseData(null)
      setResponseStatus(null)
      setResponseHeaders({})
      setResponseTime(null)

      const startTime = Date.now()

      // In a real implementation, we would use the actual API URL
      // For demo purposes, we'll simulate a response
      const fullUrl = `${apiUrl}${path}`

      // This is a simulated API call for demonstration
      // In production, you would use the actual fetch call:
      /*
      const response = await fetch(fullUrl, {
        method: method.toUpperCase(),
        headers: requestHeaders,
        body: method !== 'get' && method !== 'head' ? requestBody : undefined
      });
      */

      // Simulate network delay
      await new Promise((resolve) => setTimeout(resolve, 1000))

      // Simulate a response based on the endpoint
      let simulatedResponse
      let simulatedStatus

      if (path.includes("passwordless/init")) {
        simulatedResponse = {
          state: "abc123xyz456",
          tries: 1,
          retriesLeft: 4,
          resends: 1,
          resendsLeft: 4,
          resendAfter: 30,
          isNewUser: true,
        }
        simulatedStatus = 200
      } else if (path.includes("signin") || path.includes("signup") || path.includes("auth")) {
        simulatedResponse = {
          accessToken: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
          refreshToken: "rt.abc123xyz456",
          idToken: "id.abc123xyz456",
          tokenType: "Bearer",
          expiresIn: 3600,
        }
        simulatedStatus = 200
      } else if (path.includes("logout")) {
        simulatedResponse = {}
        simulatedStatus = 204
      } else {
        simulatedResponse = {
          error: {
            code: "not_implemented",
            message: "This endpoint is not implemented in the demo.",
          },
        }
        simulatedStatus = 501
      }

      const endTime = Date.now()

      setResponseTime(endTime - startTime)
      setResponseData(simulatedResponse)
      setResponseStatus(simulatedStatus)
      setResponseHeaders({
        "content-type": "application/json",
        "x-request-id": "req_" + Math.random().toString(36).substring(2, 15),
        date: new Date().toUTCString(),
      })

      toast({
        title: `${simulatedStatus} Response`,
        description: `Request completed in ${endTime - startTime}ms`,
        variant: simulatedStatus >= 400 ? "destructive" : "default",
      })
    } catch (error) {
      console.error("Error sending request:", error)
      toast({
        title: "Request Failed",
        description: error instanceof Error ? error.message : "An unknown error occurred",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  // Add this function to update the request body when an endpoint is selected
  const updateRequestBodyForEndpoint = (path: string, method: string, operation: any) => {
    if (
      operation.requestBody &&
      operation.requestBody.content &&
      operation.requestBody.content["application/json"].schema
    ) {
      const schema = operation.requestBody.content["application/json"].schema
      let exampleObj = {}

      if (schema.$ref) {
        const resolvedSchema = resolveRef(schema.$ref, apiSpec.components)
        exampleObj = Object.entries(resolvedSchema.properties || {}).reduce((acc, [key, value]: [string, any]) => {
          acc[key] = value.example || ""
          return acc
        }, {})
      }

      setRequestBody(JSON.stringify(exampleObj, null, 2))
    } else {
      setRequestBody("")
    }
  }

  // Initialize expanded tags
  useEffect(() => {
    const initialExpandedTags: Record<string, boolean> = {}
    Object.keys(endpointsByTag).forEach((tag) => {
      initialExpandedTags[tag] = true
    })
    setExpandedTags(initialExpandedTags)

    // Set the first endpoint as active by default
    const firstTag = Object.keys(endpointsByTag)[0]
    if (firstTag && endpointsByTag[firstTag].length > 0) {
      const firstEndpoint = endpointsByTag[firstTag][0]
      const endpointKey = `${firstEndpoint.path}-${firstEndpoint.method}`
      setActiveEndpoint(endpointKey)
      updateRequestBodyForEndpoint(firstEndpoint.path, firstEndpoint.method, firstEndpoint.operation)
    }
  }, [])

  return (
    <Layout showBackButton simplifiedHeader>
      <div className="container max-w-[1600px] py-6">
        <div className="mb-8">
          <SectionHeader
            title="Guardian API Reference"
            description="Explore and test the Guardian authentication and authorization APIs."
          />
        </div>

        <div className="relative mb-6">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search endpoints..."
            className="pl-10 bg-muted/30"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="grid grid-cols-12 gap-6">
          {/* API Categories */}
          <div className="col-span-12 md:col-span-3">
            <div className="sticky top-24 space-y-4">
              <div className="space-y-2">
                {Object.entries(filteredEndpoints).map(([tag, endpoints]) => (
                  <div key={tag} className="border border-border/40 rounded-lg overflow-hidden">
                    <button
                      onClick={() => toggleTag(tag)}
                      className="w-full flex items-center justify-between p-3 bg-muted/30 hover:bg-muted/50 transition-colors"
                    >
                      <div className="flex items-center gap-2">
                        {getTagIcon(tag)}
                        <span className="font-medium">{tag}</span>
                      </div>
                      {expandedTags[tag] ? (
                        <ChevronDown className="size-4 text-muted-foreground" />
                      ) : (
                        <ChevronRight className="size-4 text-muted-foreground" />
                      )}
                    </button>
                    {expandedTags[tag] && (
                      <div className="border-t border-border/40">
                        {endpoints.map((endpoint, index) => (
                          <button
                            key={`${endpoint.path}-${endpoint.method}-${index}`}
                            onClick={() => {
                              const endpointKey = `${endpoint.path}-${endpoint.method}`
                              setActiveEndpoint(endpointKey)
                              updateRequestBodyForEndpoint(endpoint.path, endpoint.method, endpoint.operation)
                            }}
                            className={`w-full text-left p-3 hover:bg-muted/30 transition-colors flex items-start gap-2 ${
                              activeEndpoint === `${endpoint.path}-${endpoint.method}`
                                ? "bg-muted/30 border-l-2 border-primary"
                                : ""
                            }`}
                          >
                            <span
                              className={`text-xs font-bold px-2 py-1 rounded uppercase text-white ${getMethodColor(
                                endpoint.method,
                              )}`}
                            >
                              {endpoint.method}
                            </span>
                            <div className="flex-1">
                              <p className="text-sm font-medium truncate">{endpoint.path}</p>
                              <p className="text-xs text-muted-foreground mt-1">{endpoint.operation.summary}</p>
                            </div>
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Endpoint Details */}
          <div className="col-span-12 md:col-span-9">
            {activeEndpoint ? (
              (() => {
                const [path, method] = activeEndpoint.split("-")
                const operation = apiSpec.paths[path][method]

                return (
                  <div className="p-6 space-y-8">
                    <div className="space-y-4">
                      <div className="flex items-center gap-3">
                        <span
                          className={`text-xs font-bold px-2 py-1 rounded uppercase text-white ${getMethodColor(
                            method,
                          )}`}
                        >
                          {method}
                        </span>
                        <h2 className="text-xl font-bold">{path}</h2>
                      </div>
                      <p className="text-muted-foreground whitespace-pre-line">{operation.description}</p>
                    </div>

                    <Tabs defaultValue="request" className="w-full">
                      <TabsList className="w-full grid grid-cols-3">
                        <TabsTrigger value="request">Request</TabsTrigger>
                        <TabsTrigger value="response">Response</TabsTrigger>
                        <TabsTrigger value="example">Example</TabsTrigger>
                      </TabsList>
                      <TabsContent value="request" className="space-y-6 pt-4">
                        <div className="space-y-4">
                          <h3 className="text-lg font-medium">Headers</h3>
                          <Card>
                            <CardContent className="p-4">
                              <div className="space-y-2">
                                <div className="grid grid-cols-3 gap-4 py-2 border-b border-border/40">
                                  <div className="font-medium">Name</div>
                                  <div className="font-medium">Description</div>
                                  <div className="font-medium">Required</div>
                                </div>
                                <div className="grid grid-cols-3 gap-4 py-2">
                                  <div className="text-sm">tenant-id</div>
                                  <div className="text-sm text-muted-foreground">
                                    tenant-id of the client integrating with guardian
                                  </div>
                                  <div className="text-sm">
                                    <Badge variant="outline" className="bg-red-500/10 text-red-500 border-red-500/20">
                                      Required
                                    </Badge>
                                  </div>
                                </div>
                              </div>
                            </CardContent>
                          </Card>
                        </div>

                        {operation.requestBody && (
                          <div className="space-y-4">
                            <h3 className="text-lg font-medium">Request Body</h3>
                            <Card>
                              <CardContent className="p-4">
                                <div className="relative">
                                  <pre className="text-sm overflow-x-auto p-4 bg-muted/30 rounded-lg">
                                    {formatJSON(
                                      operation.requestBody.content["application/json"].schema.$ref
                                        ? resolveRef(
                                            operation.requestBody.content["application/json"].schema.$ref,
                                            apiSpec.components,
                                          )
                                        : operation.requestBody.content["application/json"].schema,
                                    )}
                                  </pre>
                                  <Button
                                    variant="ghost"
                                    size="icon"
                                    className="absolute top-2 right-2 h-8 w-8 hover:bg-muted rounded-full"
                                    onClick={() =>
                                      copyToClipboard(
                                        formatJSON(
                                          operation.requestBody.content["application/json"].schema.$ref
                                            ? resolveRef(
                                                operation.requestBody.content["application/json"].schema.$ref,
                                                apiSpec.components,
                                              )
                                            : operation.requestBody.content["application/json"].schema,
                                        ),
                                        "request-body",
                                      )
                                    }
                                  >
                                    {copiedText === "request-body" ? (
                                      <span className="text-green-500">✓</span>
                                    ) : (
                                      <Copy className="size-4" />
                                    )}
                                    <span className="sr-only">Copy code</span>
                                  </Button>
                                </div>
                              </CardContent>
                            </Card>
                          </div>
                        )}
                      </TabsContent>

                      <TabsContent value="response" className="space-y-6 pt-4">
                        <div className="space-y-4">
                          <h3 className="text-lg font-medium">Response Codes</h3>
                          <Card>
                            <CardContent className="p-4">
                              <div className="space-y-2">
                                <div className="grid grid-cols-2 gap-4 py-2 border-b border-border/40">
                                  <div className="font-medium">Status</div>
                                  <div className="font-medium">Description</div>
                                </div>
                                {Object.entries(operation.responses).map(([status, response]: [string, any]) => (
                                  <div key={status} className="grid grid-cols-2 gap-4 py-2">
                                    <div className="text-sm">
                                      <Badge
                                        variant="outline"
                                        className={`${
                                          status.startsWith("2")
                                            ? "bg-green-500/10 text-green-500 border-green-500/20"
                                            : status.startsWith("4")
                                              ? "bg-amber-500/10 text-amber-500 border-amber-500/20"
                                              : "bg-red-500/10 text-red-500 border-red-500/20"
                                        }`}
                                      >
                                        {status}
                                      </Badge>
                                    </div>
                                    <div className="text-sm text-muted-foreground">{response.description}</div>
                                  </div>
                                ))}
                              </div>
                            </CardContent>
                          </Card>
                        </div>

                        {operation.responses["200"] && operation.responses["200"].content && (
                          <div className="space-y-4">
                            <h3 className="text-lg font-medium">Success Response (200)</h3>
                            <Card>
                              <CardContent className="p-4">
                                <div className="relative">
                                  <pre className="text-sm overflow-x-auto p-4 bg-muted/30 rounded-lg">
                                    {formatJSON(
                                      operation.responses["200"].content["application/json"].schema.oneOf
                                        ? resolveRef(
                                            operation.responses["200"].content["application/json"].schema.oneOf[0].$ref,
                                            apiSpec.components,
                                          )
                                        : operation.responses["200"].content["application/json"].schema,
                                    )}
                                  </pre>
                                  <Button
                                    variant="ghost"
                                    size="icon"
                                    className="absolute top-2 right-2 h-8 w-8 hover:bg-muted rounded-full"
                                    onClick={() =>
                                      copyToClipboard(
                                        formatJSON(
                                          operation.responses["200"].content["application/json"].schema.oneOf
                                            ? resolveRef(
                                                operation.responses["200"].content["application/json"].schema.oneOf[0]
                                                  .$ref,
                                                apiSpec.components,
                                              )
                                            : operation.responses["200"].content["application/json"].schema,
                                        ),
                                        "response-body",
                                      )
                                    }
                                  >
                                    {copiedText === "response-body" ? (
                                      <span className="text-green-500">✓</span>
                                    ) : (
                                      <Copy className="size-4" />
                                    )}
                                    <span className="sr-only">Copy code</span>
                                  </Button>
                                </div>
                              </CardContent>
                            </Card>
                          </div>
                        )}

                        <div className="space-y-4">
                          <h3 className="text-lg font-medium">Error Response</h3>
                          <Card>
                            <CardContent className="p-4">
                              <div className="relative">
                                <pre className="text-sm overflow-x-auto p-4 bg-muted/30 rounded-lg">
                                  {formatJSON(resolveRef("#/components/schemas/ErrorResponse", apiSpec.components))}
                                </pre>
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  className="absolute top-2 right-2 h-8 w-8 hover:bg-muted rounded-full"
                                  onClick={() =>
                                    copyToClipboard(
                                      formatJSON(resolveRef("#/components/schemas/ErrorResponse", apiSpec.components)),
                                      "error-body",
                                    )
                                  }
                                >
                                  {copiedText === "error-body" ? (
                                    <span className="text-green-500">✓</span>
                                  ) : (
                                    <Copy className="size-4" />
                                  )}
                                  <span className="sr-only">Copy code</span>
                                </Button>
                              </div>
                            </CardContent>
                          </Card>
                        </div>
                      </TabsContent>

                      <TabsContent value="example" className="space-y-6 pt-4">
                        <div className="space-y-4">
                          <h3 className="text-lg font-medium">Example Request</h3>
                          <Card>
                            <CardContent className="p-4">
                              <div className="relative">
                                <pre className="text-sm overflow-x-auto p-4 bg-muted/30 rounded-lg">
                                  {`curl --location '${path}' \\
--header 'Content-Type: application/json' \\
--header 'tenant-id: tenant1' \\
--data '${formatJSON(
                                    operation.requestBody && operation.requestBody.content
                                      ? operation.requestBody.content["application/json"].schema.$ref
                                        ? (() => {
                                            const schema = resolveRef(
                                              operation.requestBody.content["application/json"].schema.$ref,
                                              apiSpec.components,
                                            )
                                            const example = {}
                                            Object.entries(schema.properties || {}).forEach(
                                              ([key, value]: [string, any]) => {
                                                example[key] = value.example || ""
                                              },
                                            )
                                            return example
                                          })()
                                        : {}
                                      : {},
                                  )}'`}
                                </pre>
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  className="absolute top-2 right-2 h-8 w-8 hover:bg-muted rounded-full"
                                  onClick={() =>
                                    copyToClipboard(
                                      `curl --location '${path}' \\
--header 'Content-Type: application/json' \\
--header 'tenant-id: tenant1' \\
--data '${formatJSON(
                                        operation.requestBody && operation.requestBody.content
                                          ? operation.requestBody.content["application/json"].schema.$ref
                                            ? (() => {
                                                const schema = resolveRef(
                                                  operation.requestBody.content["application/json"].schema.$ref,
                                                  apiSpec.components,
                                                )
                                                const example = {}
                                                Object.entries(schema.properties || {}).forEach(
                                                  ([key, value]: [string, any]) => {
                                                    example[key] = value.example || ""
                                                  },
                                                )
                                                return example
                                              })()
                                            : {}
                                          : {},
                                      )}'`,
                                      "example-request",
                                    )
                                  }
                                >
                                  {copiedText === "example-request" ? (
                                    <span className="text-green-500">✓</span>
                                  ) : (
                                    <Copy className="size-4" />
                                  )}
                                  <span className="sr-only">Copy code</span>
                                </Button>
                              </div>
                            </CardContent>
                          </Card>
                        </div>

                        {operation.responses["200"] && operation.responses["200"].content && (
                          <div className="space-y-4">
                            <h3 className="text-lg font-medium">Example Response</h3>
                            <Card>
                              <CardContent className="p-4">
                                <div className="relative">
                                  <pre className="text-sm overflow-x-auto p-4 bg-muted/30 rounded-lg">
                                    {formatJSON(
                                      operation.responses["200"].content["application/json"].schema.oneOf
                                        ? (() => {
                                            const schema = resolveRef(
                                              operation.responses["200"].content["application/json"].schema.oneOf[0]
                                                .$ref,
                                              apiSpec.components,
                                            )
                                            const example = {}
                                            Object.entries(schema.properties || {}).forEach(
                                              ([key, value]: [string, any]) => {
                                                example[key] = value.example || ""
                                              },
                                            )
                                            return example
                                          })()
                                        : {},
                                    )}
                                  </pre>
                                  <Button
                                    variant="ghost"
                                    size="icon"
                                    className="absolute top-2 right-2 h-8 w-8 hover:bg-muted rounded-full"
                                    onClick={() =>
                                      copyToClipboard(
                                        formatJSON(
                                          operation.responses["200"].content["application/json"].schema.oneOf
                                            ? (() => {
                                                const schema = resolveRef(
                                                  operation.responses["200"].content["application/json"].schema.oneOf[0]
                                                    .$ref,
                                                  apiSpec.components,
                                                )
                                                const example = {}
                                                Object.entries(schema.properties || {}).forEach(
                                                  ([key, value]: [string, any]) => {
                                                    example[key] = value.example || ""
                                                  },
                                                )
                                                return example
                                              })()
                                            : {},
                                        ),
                                        "example-response",
                                      )
                                    }
                                  >
                                    {copiedText === "example-response" ? (
                                      <span className="text-green-500">✓</span>
                                    ) : (
                                      <Copy className="size-4" />
                                    )}
                                    <span className="sr-only">Copy code</span>
                                  </Button>
                                </div>
                              </CardContent>
                            </Card>
                          </div>
                        )}
                      </TabsContent>
                    </Tabs>
                  </div>
                )
              })()
            ) : (
              <div className="flex flex-col items-center justify-center h-[60vh] text-center p-6">
                <div className="size-16 rounded-full bg-muted/50 flex items-center justify-center mb-4">
                  <Send className="size-8 text-muted-foreground" />
                </div>
                <h2 className="text-2xl font-bold mb-2">Select an API Endpoint</h2>
                <p className="text-muted-foreground max-w-md">
                  Choose an endpoint from the sidebar to view detailed documentation, request/response formats, and
                  examples.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  )
}
