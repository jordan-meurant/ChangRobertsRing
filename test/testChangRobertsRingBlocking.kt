fun main() = run {
    println("-----------------------")
    println("CHANG ROBERTS ALGORITHM")
    println("-----------------------")

    startElection(createNodes()[0])
}

fun createNodes(): MutableList<Node> {
    val nodes = emptyList<Node>().toMutableList()
    nodes.add(Node(0, null))

    val numbers = ArrayList<Int>()
    for (x in 1..99) {
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
    for (node in nodes) println(node)

    return nodes
}

data class Node(var id: Int, var nextNode: Node?) {
    override fun toString(): String {
        return "Noeud : $id, voisin :" + (nextNode?.id ?: "null")
    }
}

fun startElection(node: Node) {
    forwardMsg(node, Message(MsgType.ELECTION, node.id, node.id.toString() + "commence l'élection."))
}

fun forwardMsg(node: Node, msg: Message) {
    node.nextNode?.let { checkMsg(it, msg) }
}

fun checkMsg(node: Node, msg: Message) {
    // print(msg.type.toString() + ": ")
    print("Noeud n°" + node.id + ", message reçu : ")
    println(msg)
    if (msg.type == MsgType.ELECTION) {
        if (msg.idNode > node.id) {
            msg.text = node.id.toString() + " fait suivre le msg sans MAJ"
            forwardMsg(node, msg)
        }
        if (msg.idNode < node.id) {
            forwardMsg(
                node,
                Message(MsgType.ELECTION, node.id, node.id.toString() + " transfert le msg avec son propre id")
            )
        }
        if (msg.idNode == node.id) {
            forwardMsg(
                node,
                Message(
                    MsgType.ELECTED,
                    node.id,
                    node.id.toString() + " agit maintenant en tant que leader et envoie un msg ELECTED pour dire qu'il est le nouveau leader"
                )
            )
        }
    }
    if (msg.type == MsgType.ELECTED) {
        if (msg.idNode == node.id) {
            println("le processus d'élection est terminé")
        }
        if (msg.idNode != node.id) {
            msg.text = node.id.toString() + " a reconnu le noeud n°" + msg.idNode + " comme leader."
            forwardMsg(node, msg)
        }
    }
}


//val log: Logger = LoggerFactory.getLogger("Contributors")
//fun log(msg: String?) {
//    log.info(msg)
//}


//fun CoroutineScope.produceNodes(): ReceiveChannel<Node> = produce {
//    for (x in nodesIdList()) {
//        send(Node(x, null))
//    }
//}
//
//fun nodesIdList(): List<Int> {
//    val nodesId = emptyList<Int>().toMutableList()
//    for (x in 1..5) {
//        nodesId.add(x)
//    }
//    nodesId.shuffle()
//    return nodesId
//}
//
//
//fun main() = runBlocking<Unit> {
//    val nodes = produceNodes()
//    val channel = Channel<Node>()
//    nodes.consumeEach {// replace the "for" loop on the consumer side
//
//
//        println(it)
//
//        launch { channel.send(it) }
//    }
//    println("-----------------------")
//
//
//    for (node in nodes) {
//
//        println(node.id)
//        println(nodes)
//    }
//
//    nodes.consumeEach {
//        print(it.id)
//    }
