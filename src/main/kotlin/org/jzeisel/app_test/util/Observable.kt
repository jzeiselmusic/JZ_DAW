package org.jzeisel.app_test.util

class Observable<DataType>(initialValue: DataType) {
    private val listeners = mutableListOf<ObservableListener<DataType>>()
    private var value = initialValue

    fun getValue(): DataType {
        return value
    }

    fun setValueAndNotify(newValue: DataType, broadcastType: BroadcastType): Observable<DataType> {
        val prevValue = value
        if (newValue != value) {
            value = newValue
            notifyListeners(prevValue, newValue, broadcastType)
        }
        return this
    }

    fun setValue(newValue: DataType): Observable<DataType> {
        if (newValue != value) {
            value = newValue
        }
        return this
    }

    fun addListener(listener: ObservableListener<DataType>): Observable<DataType> {
        listeners.add(listener)
        return this
    }

    fun removeListener(listener: ObservableListener<DataType>): Observable<DataType> {
        listeners.remove(listener)
        return this
    }

    private fun notifyListeners(oldValue: DataType, newValue: DataType, broadcastType: BroadcastType) {
        for (listener in listeners) {
            listener.respondToChange(broadcastType, oldValue, newValue)
        }
    }
}