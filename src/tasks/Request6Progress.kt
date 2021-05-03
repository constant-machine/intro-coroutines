package tasks

import contributors.*

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: listOf()

    val users = mutableListOf<User>()

    for (index in repos.indices) {
        users.addAll(
            service
                .getRepoContributors(req.org, repos[index].name)
                .also { logUsers(repos[index], it) }
                .bodyList()
        )
        updateResults(users.aggregate(), index == repos.lastIndex)
    }
}
