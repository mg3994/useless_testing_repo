fun main() {
    initSocket()
}

private fun initSocket() {
    try {
        val options = IO.Options().apply {
            transports = arrayOf("websocket")
            forceNew = true
            reconnection = true
            // Add this to specify EIO3 protocol explicitly if needed
            // This might help if supported by the client library
            // query = "EIO=3"
        }

        socket = IO.socket("http://185.48.228.171:21741", options)

        socket?.on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "Socket connected")
            socket?.emit("subscribe", "consumer")
        }

        socket?.on(Socket.EVENT_CONNECTING) { Log.d(TAG, "Socket connecting") }
        socket?.on(Socket.EVENT_CONNECT_TIMEOUT) { Log.d(TAG, "Socket connect timeout") }
        socket?.on(Socket.EVENT_RECONNECT) { Log.d(TAG, "Socket reconnect") }
        socket?.on(Socket.EVENT_RECONNECTING) { Log.d(TAG, "Socket reconnecting") }
        socket?.on(Socket.EVENT_RECONNECT_FAILED) { Log.d(TAG, "Socket reconnect failed") }
        socket?.on(Socket.EVENT_ERROR) { args -> Log.e(TAG, "Socket error: ${args.joinToString()}") }
        socket?.on(Socket.EVENT_DISCONNECT) { Log.d(TAG, "Socket disconnected") }

        socket?.on("events") { args ->
            Log.d(TAG, "Received 'events' event with args: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                val data = args[0]
                runOnUiThread {
                    try {
                        val jsonArray = when (data) {
                            is String -> JSONArray(data)
                            is JSONArray -> data
                            else -> null
                        }

                        if (jsonArray == null || jsonArray.length() == 0) {
                            Log.d(TAG, "Empty or null JSON array")
                            showEmpty()
                        } else {
                            events.clear()
                            for (i in 0 until jsonArray.length()) {
                                val jsonObj = jsonArray.getJSONObject(i)
                                Log.d(TAG, "Event JSON object: $jsonObj")
                                events.add(ConsumerModel.fromJson(jsonObj))
                            }
                            adapter.notifyDataSetChanged()
                            showContent()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception parsing events JSON", e)
                        showEmpty()
                    }
                }
            } else {
                Log.d(TAG, "'events' received with empty args")
                runOnUiThread { showEmpty() }
            }
        }

        socket?.connect()

    } catch (e: URISyntaxException) {
        Log.e(TAG, "Socket URI syntax error", e)
        showEmpty()
    }
}