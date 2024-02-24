package org.jzeisel.app_test.util

class Observable<DataType>(initialValue: DataType) {
    private val listeners = mutableListOf<ObservableListener<DataType>>()
    private var value = initialValue
    private var performOnChange: () -> Unit = {}

    fun getValue(): DataType {
        return value
    }

    fun setValueAndNotify(newValue: DataType, broadcastType: BroadcastType) {
        val prevValue = value
        if (newValue != value) {
            value = newValue
            notifyListeners(prevValue, newValue, broadcastType)
        }
    }

    fun setValue(newValue: DataType) {
        if (newValue != value) {
            value = newValue
        }
    }

    fun addListener(listener: ObservableListener<DataType>) {
        listeners.add(listener)
    }

    fun removeListener(listener: ObservableListener<DataType>) {
        listeners.remove(listener)
    }

    private fun notifyListeners(oldValue: DataType, newValue: DataType, broadcastType: BroadcastType) {
        for (listener in listeners) {
            listener.respondToChange(broadcastType, oldValue, newValue)
        }
    }
}