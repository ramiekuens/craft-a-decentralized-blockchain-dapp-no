package io.craft.decentr.notifier

import kotlinx.coroutines.*
import org.web3j.abi.datatypes.Address
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService

class DecentrNotifier(private val web3j: Web3j) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val notificationListeners = mutableSetOf<(String) -> Unit>()

    init {
        scope.launch {
            web3j.blockFlowable().subscribe { block ->
                val blockHash = block.hash
                val blockNumber = block.number

                // Filter out irrelevant transactions
                val transactions = block.transactions.filter { it.from != null }
                transactions.forEach { transaction ->
                    val fromAddress = transaction.from
                    val toAddress = transaction.to

                    // Notify listeners about new transactions
                    notificationListeners.forEach { listener ->
                        listener.invoke("New transaction from $fromAddress to $toAddress (Block $blockNumber, Hash $blockHash)")
                    }
                }
            }
        }
    }

    fun addListener(listener: (String) -> Unit) {
        notificationListeners.add(listener)
    }

    fun removeListener(listener: (String) -> Unit) {
        notificationListeners.remove(listener)
    }
}

fun main() {
    val web3j = Web3j.build(HttpService("https://mainnet.infura.io/v3/YOUR_PROJECT_ID"))

    val notifier = DecentrNotifier(web3j)

    // Add a listener that prints notifications to the console
    notifier.addListener { message ->
        println(message)
    }

    // Start the notification loop
    runBlocking {
        while (true) {
            delay(1000)
        }
    }
}