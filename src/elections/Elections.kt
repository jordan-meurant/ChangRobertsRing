package elections

import elections.Elections.LoadingStatus.*
import elections.Variant.BLOCKING
import elections.Variant.CHANNELS
import kotlinx.coroutines.*
import tasks.loadContributorsBlocking
import tasks.loadElectionChannels
import java.awt.event.ActionListener
import kotlin.coroutines.CoroutineContext

enum class Variant {
    BLOCKING,           // ChangRobertsRingBlocking
    CHANNELS          // ChangRobertsRingChannels
}

interface Elections : CoroutineScope {

    val job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    fun init() {
        // Start a new loading on 'load' click
        addLoadListener {
            loadElections()
        }

        // Save preferences and exit on closing the window
        addOnWindowClosingListener {
            job.cancel()
            System.exit(0)
        }

        // Load stored params (user & password values)
        loadInitialParams()
    }

    fun loadElections() {
        val (numberOfNodes, _) = getParams()

        clearResults()
        // val service = createGitHubService(req.username, req.password)

        val startTime = System.currentTimeMillis()
        when (getSelectedVariant()) {
            BLOCKING -> { // Blocking UI thread
                val result = loadContributorsBlocking(numberOfNodes)
                updateResults(result, startTime)
            }
            CHANNELS -> {  // Performing requests concurrently and showing progress
                clearResults()
                launch(Dispatchers.Default) {
                    loadElectionChannels(numberOfNodes) { data, completed ->
                        withContext(Dispatchers.Main) {
                            updateResults(data, startTime, completed)
                        }
                    }
                }.setUpCancellation()
            }
        }
    }

    private enum class LoadingStatus { COMPLETED, CANCELED, IN_PROGRESS }

    private fun clearResults() {
        updateResult(arrayListOf())
        updateLoadingStatus(IN_PROGRESS)
        setActionsStatus(newLoadingEnabled = false)
    }

    private fun updateResults(
        result: ArrayList<String>,
        startTime: Long,
        completed: Boolean = true
    ) {
        //updateContributors(users)
        updateResult(result)
        updateLoadingStatus(if (completed) COMPLETED else IN_PROGRESS, startTime)
        if (completed) {
            setActionsStatus(newLoadingEnabled = true)
        }
    }

    private fun updateLoadingStatus(
        status: LoadingStatus,
        startTime: Long? = null
    ) {
        val time = if (startTime != null) {
            val time = System.currentTimeMillis() - startTime
            "${(time / 1000)}.${time % 1000 / 100} sec"
        } else ""

        val text = "Loading status: " +
                when (status) {
                    COMPLETED -> "completed in $time"
                    IN_PROGRESS -> "in progress $time"
                    CANCELED -> "canceled"
                }
        setLoadingStatus(text, status == IN_PROGRESS)
    }

    private fun Job.setUpCancellation() {
        // make active the 'cancel' button
        setActionsStatus(newLoadingEnabled = false, cancellationEnabled = true)

        val loadingJob = this

        // cancel the loading job if the 'cancel' button was clicked
        val listener = ActionListener {
            loadingJob.cancel()
            updateLoadingStatus(CANCELED)
        }
        addCancelListener(listener)

        // update the status and remove the listener after the loading job is completed
        launch {
            loadingJob.join()
            setActionsStatus(newLoadingEnabled = true)
            removeCancelListener(listener)
        }
    }

    fun loadInitialParams() {
        setParams(loadStoredParams())
    }


    fun getSelectedVariant(): Variant

    fun updateResult(result: ArrayList<String>)

    fun setLoadingStatus(text: String, iconRunning: Boolean)

    fun setActionsStatus(newLoadingEnabled: Boolean, cancellationEnabled: Boolean = false)

    fun addCancelListener(listener: ActionListener)

    fun removeCancelListener(listener: ActionListener)

    fun addLoadListener(listener: () -> Unit)

    fun addOnWindowClosingListener(listener: () -> Unit)

    fun setParams(params: Params)

    fun getParams(): Params

}
