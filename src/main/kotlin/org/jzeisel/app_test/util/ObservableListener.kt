package org.jzeisel.app_test.util

interface ObservableListener<DataType> {
    fun respondToChange(observable: Observable<*>, old: DataType, new: DataType)
    fun registerForBroadcasts()
    fun unregisterForBroadcasts()
}