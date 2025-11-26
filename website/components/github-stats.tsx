"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { Star, GitFork } from "lucide-react"
import { fetchGitHubStats, type GitHubStats, DEFAULT_STATS } from "@/lib/github-stats"

interface GitHubStatsProps {
  repo: string
}

export default function GitHubStatsDisplay({ repo }: GitHubStatsProps) {
  const [stats, setStats] = useState<GitHubStats>(DEFAULT_STATS)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    async function loadStats() {
      try {
        setIsLoading(true)
        const data = await fetchGitHubStats(repo)
        setStats(data)
      } catch (error) {
        console.error("Failed to load GitHub stats:", error)
        // Ensure we still set stats to defaults even if there's an error
        setStats(DEFAULT_STATS)
      } finally {
        setIsLoading(false)
      }
    }

    loadStats()
  }, [repo])

  if (isLoading) {
    return (
      <div className="flex items-center justify-center gap-6">
        <div className="bg-muted/50 px-4 py-2 rounded-full text-sm animate-pulse h-10 w-28"></div>
        <div className="bg-muted/50 px-4 py-2 rounded-full text-sm animate-pulse h-10 w-28"></div>
      </div>
    )
  }

  return (
    <div className="flex items-center justify-center gap-6">
      <Link
        href={`https://github.com/${repo}`}
        target="_blank"
        rel="noopener noreferrer"
        className="flex items-center gap-2 bg-muted/50 hover:bg-muted px-4 py-2 rounded-full text-sm transition-colors"
      >
        <Star className="size-4" fill="currentColor" />
        <span>{stats.stars.toLocaleString()} Stars</span>
      </Link>
      <Link
        href={`https://github.com/${repo}/fork`}
        target="_blank"
        rel="noopener noreferrer"
        className="flex items-center gap-2 bg-muted/50 hover:bg-muted px-4 py-2 rounded-full text-sm transition-colors"
      >
        <GitFork className="size-4" />
        <span>{stats.forks.toLocaleString()} Forks</span>
      </Link>
    </div>
  )
}
