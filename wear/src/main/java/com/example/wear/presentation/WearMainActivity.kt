package com.example.wear.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WearMainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "WearMainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            SOSButton { sendSOSMessage() }
        }
    }

    private fun sendSOSMessage() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching connected nodes...")
                val nodes = getConnectedNodes()

                if (nodes.isEmpty()) {
                    Log.w(TAG, "No connected nodes found!")
                } else {
                    Log.d(TAG, "Connected nodes: ${nodes.map { it.id }}")
                }

                for (node in nodes) {
                    Log.d(TAG, "Sending SOS message to node: ${node.id}")
                    sendMessageToNode(node.id, "/sos", "SOS")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending SOS message", e)
            }
        }
    }

    private suspend fun getConnectedNodes(): List<Node> {
        return try {
            val nodeClient = Wearable.getNodeClient(this@WearMainActivity)
            val task = nodeClient.connectedNodes
            val nodes = Tasks.await(task) // Ensures proper execution
            Log.d(TAG, "Found ${nodes.size} connected nodes.")
            nodes
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching connected nodes", e)
            emptyList()
        }
    }

    private suspend fun sendMessageToNode(nodeId: String, path: String, message: String) {
        try {
            val messageClient = Wearable.getMessageClient(this@WearMainActivity)
            val task = messageClient.sendMessage(nodeId, path, message.toByteArray())
            Tasks.await(task)
            Log.d(TAG, "Message sent successfully to node: $nodeId")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message to node: $nodeId", e)
        }
    }
}
