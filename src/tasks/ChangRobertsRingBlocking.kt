package tasks

import elections.log
import java.lang.Thread.sleep

fun loadElectionsBlocking(numberOfNodes: Int): ArrayList<String> {

    log("starting election")
    val results = arrayListOf("Début de l'élection")
    startElection(createNodes(numberOfNodes)[0], results)

    return results
}

fun createNodes(numberOfNodes: Int): MutableList<Node> {
    val nodes = emptyList<Node>().toMutableList()
    nodes.add(Node(0, null))

    val numbers = ArrayList<Int>()
    for (x in 1..numberOfNodes) {
        numbers.add(x)
    }
    numbers.shuffle()
    for (x in numbers) {
        val n = Node(x, null)
        val i = nodes.size - 1
        nodes[i].nextNode = n
        nodes.add(n)
    }
    nodes[nodes.size - 1].nextNode = nodes[0]

    return nodes
}

data class Node(var id: Int, var nextNode: Node?)

fun startElection(node: Node, results: ArrayList<String>) {
    forwardMsg(node, Message(MsgType.ELECTION, node.id, node.id.toString() + " commence l'élection."), results)
}

fun forwardMsg(node: Node, msg: Message, results: ArrayList<String>) {
    node.nextNode?.let { checkMsg(it, msg, results) }
}

fun checkMsg(node: Node, msg: Message, results: ArrayList<String>) {
    results.add("Noeud n°" + node.id + ", message reçu : " + msg)
    sleep(500)
    if (msg.type == MsgType.ELECTION) {
        if (msg.idNode > node.id) {
            msg.text = node.id.toString() + " fait suivre le msg sans MAJ"
            forwardMsg(node, msg, results)
        }
        if (msg.idNode < node.id) {
            forwardMsg(
                node,
                Message(MsgType.ELECTION, node.id, node.id.toString() + " transfert le msg avec son propre id"),
                results
            )
        }
        if (msg.idNode == node.id) {
            forwardMsg(
                node,
                Message(
                    MsgType.ELECTED,
                    node.id,
                    node.id.toString() + " agit maintenant en tant que leader et envoie un msg ELECTED pour dire qu'il est le nouveau leader"
                ),
                results
            )
        }
    }
    if (msg.type == MsgType.ELECTED) {
        if (msg.idNode == node.id) {
            results.add("le processus d'élection est terminé")
        }
        if (msg.idNode != node.id) {
            msg.text = node.id.toString() + " a reconnu le noeud n°" + msg.idNode + " comme leader."
            forwardMsg(node, msg, results)
        }
    }
}

