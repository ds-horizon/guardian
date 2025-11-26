export interface GitHubStats {
  stars: number
  forks: number
}

export const DEFAULT_STATS: GitHubStats = {
  stars: 0,
  forks: 0,
}

export async function fetchGitHubStats(repo: string): Promise<GitHubStats> {
  // Create an AbortController for the fetch request
  const controller = new AbortController()
  const { signal } = controller

  // Set up a timeout that we can clear later
  let timeoutId: NodeJS.Timeout | null = null

  try {
    // Set a timeout that will abort the fetch after 5 seconds
    timeoutId = setTimeout(() => controller.abort(), 5000)

    const response = await fetch(`https://api.github.com/repos/${repo}`, {
      signal,
      headers: {
        "User-Agent": "Guardian-Website",
      },
    })

    // Clear the timeout since the request completed
    if (timeoutId) clearTimeout(timeoutId)

    if (!response.ok) {
      console.log(`GitHub API responded with status: ${response.status}`)
      return DEFAULT_STATS
    }

    const data = await response.json()
    return {
      stars: data?.stargazers_count ?? DEFAULT_STATS.stars,
      forks: data?.forks_count ?? DEFAULT_STATS.forks,
    }
  } catch (error) {
    // Clear the timeout if it's still active
    if (timeoutId) clearTimeout(timeoutId)

    // Check if the error is an AbortError (timeout)
    if (error instanceof Error && error.name === "AbortError") {
      console.log("GitHub stats fetch timed out after 5 seconds")
    } else {
      console.error("Error fetching GitHub stats:", error)
    }

    // Return default stats in any error case
    return DEFAULT_STATS
  }
}
