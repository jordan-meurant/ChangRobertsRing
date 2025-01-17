import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class Message(var type: MsgType, var idNode: Int, var text: String)
enum class MsgType {
    ELECTION, ELECTED
}

fun main() = runBlocking {
    val ring = Channel<Message>() // a shared table

    for (x in 0..100) {
        launch(Dispatchers.Default) {
            println("Default: I'm working in thread ${Thread.currentThread().name} - Noeud n° $x")
            node(x, ring)

        }
    }
    ring.send(Message(MsgType.ELECTION, -1, "Début de l'élection")) // serve the ball
}

suspend fun node(nodeId: Int, ring: Channel<Message>) {
    for (message in ring) {
        println("Node n°$nodeId  Message reçu : $message")
        delay(300)

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
                println("Le processus d'élection est terminé ! Le leader est le noeud n°$nodeId")
                ring.cancel()
            }
            if (message.idNode != nodeId) {
                message.text = "Le noeud n°$nodeId a reconnu le noeud n°" + message.idNode + " comme leader"
                ring.send(message)
            }
        }
    }
}
