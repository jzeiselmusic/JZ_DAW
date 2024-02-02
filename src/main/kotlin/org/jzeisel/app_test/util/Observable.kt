package org.jzeisel.app_test.util

class Observable<DataType>(initialValue: DataType) {
    private val listeners = mutableListOf<ObservableListener<DataType>>()
    private var value = initialValue

    fun getValue(): DataType {
        return value
    }

    fun setValueAndNotify(newValue: DataType) {
        if (newValue != value) {
            value = newValue
            notifyListeners(newValue)
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

    private fun notifyListeners(newValue: DataType) {
        for (listener in listeners) {
            listener.respondToChange(newValue)
        }
    }
}