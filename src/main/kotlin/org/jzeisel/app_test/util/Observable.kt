package org.jzeisel.app_test.util

class Observable<DataType>(initialValue: DataType) {
    private val listeners = mutableListOf<ObservableListener<DataType>>()
    private var value = initialValue
    private var performOnChange: () -> Unit = {}

    fun getValue(): DataType {
        return value
    }

    fun setValueAndNotify(newValue: DataType) {
        performOnChange()
        val prevValue = value
        if (newValue != value) {
            value = newValue
            notifyListeners(prevValue, newValue)
        }
    }

    fun setValue(newValue: DataType) {
        performOnChange()
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

    private fun notifyListeners(oldValue: DataType, newValue: DataType) {
        for (listener in listeners) {
            listener.respondToChange(this, oldValue, newValue)
        }
    }

    fun setPerformOnChange(func: ()->Unit) {
        performOnChange = func
    }
}