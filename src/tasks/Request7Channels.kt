package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: listOf()

        val usersChannel =  Channel<List<User>>()
        for (repo in repos) {
            launch {
                log("loading for: ${repo.name}")
                usersChannel.send(
                    service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }.bodyList()
                )
            }
        }
        val users = mutableListOf<User>()
        for (index in repos.indices) {
            users.addAll(usersChannel.receive())
            updateResults(users.aggregate(), index == repos.lastIndex)
//            updateResults(users.aggregate(), usersChannel.isEmpty)
        }
    }
}
