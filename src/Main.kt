import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

class ChatServer(port: Int) : WebSocketServer(InetSocketAddress(port)) {

    private var nextId = 0

    override fun onOpen(connection: WebSocket, handshake: ClientHandshake?) {
        val id = ++nextId

        val message = "#$id connected"
        sendToAll(message)
        println(message)
        connection.setAttachment(id)
    }

    override fun onClose(connection: WebSocket, code: Int, reason: String?, remote: Boolean) {
        val message = "#${connection.getAttachment<Int>()} disconnected"
        sendToAll(message)
        println(message)
    }

    override fun onMessage(connection: WebSocket, message: String) {
        val toClientMessage = "${connection.getAttachment<Int>()}: $message"
        sendToAll(toClientMessage)
    }

    override fun onStart() {
        println("server starts on port $port")
    }

    override fun onError(connection: WebSocket, e: Exception?) {
        println("error: $e")
    }

    private fun sendToAll(message: String) {
        connections.filter(WebSocket::isOpen).forEach { openedConnection ->
            try {
                openedConnection.send(message)
            } catch (t: Throwable) {
            }
        }
    }
}

object Main {

    @JvmStatic
    fun main(vararg args: String) {
        val server = ChatServer(DEFAULT_PORT).apply {
            start()
        }

        Runtime.getRuntime().addShutdownHook(object : Thread() {

            override fun run() {
                server.stop()
            }
        })
    }

    private const val DEFAULT_PORT = 8885
}
