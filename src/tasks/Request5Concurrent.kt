package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: listOf()

    val usersDeferred: List<Deferred<List<User>>> = repos.map { repo ->
        async {
            log("loading for: ${repo.name}")
            delay(3000)
            service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }.bodyList()
        }
    }
    usersDeferred.awaitAll().flatten().aggregate()
}