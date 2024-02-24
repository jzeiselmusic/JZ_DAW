package org.jzeisel.app_test.util

interface ObservableListener<DataType> {
    fun respondToChange(broadcastType: BroadcastType, old: DataType, new: DataType)
    fun registerForBroadcasts()
    fun unregisterForBroadcasts()
}