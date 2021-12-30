package tasks

import elections.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var results = arrayListOf<String>()

suspend fun loadElectionChannels(
    numberOfNode: Int,
    updateResults: suspend (ArrayList<String>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val ring = Channel<Message>() // a shared table

        for (x in 0..numberOfNode) {
            launch(Dispatchers.Default) {
                log("Création noeud n°$x")
                node(x, ring, updateResults)
            }
        }
        results.add("Début de l'élection")
        ring.send(Message(MsgType.ELECTION, -1, "Début de l'élection")) // send the first message
    }
}

data class Message(var type: MsgType, var idNode: Int, var text: String)
enum class MsgType {
    ELECTION, ELECTED
}

suspend fun node(
    nodeId: Int,
    ring: Channel<Message>,
    updateResults: suspend (ArrayList<String>, completed: Boolean) -> Unit
) {
    for (message in ring) { // receive the message in the loop
        log("Node n°$nodeId")
        results.add("Node n°$nodeId  Message reçu : $message")
        updateResults(results, false)
        delay(300) // wait a bit

        if (message.type == MsgType.ELECTION) {
            if (message.idNode > nodeId) {
                message.text = "$nodeId fait suivre le msg sans MAJ"
                ring.send(message)
            }
            if (message.idNode < nodeId) {
                ring.send(Message(MsgType.ELECTION, nodeId, "$nodeId transfert le msg avec son propre id"))
            }
            if (message.idNode == nodeId) {
                ring.send(
                    Message(
                        MsgType.ELECTED,
                        nodeId,
                        " agit maintenant en tant que leader et envoie un msg ELECTED pour dire qu'il est le nouveau leader"
                    )
                )
            }
        } else {
            if (message.idNode == nodeId) {
                results.add("Le processus d'élection est terminé ! Le leader est le noeud n°$nodeId")
                updateResults(results, true)
                ring.cancel() // end of algorithm -> cancel channel
            }
            if (message.idNode != nodeId) {
                message.text = "Le noeud n°$nodeId a reconnu le noeud n°" + message.idNode + " comme leader"
                ring.send(message)
            }
        }
    }
}