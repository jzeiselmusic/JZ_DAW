package org.jzeisel.app_test.util

interface ObservableListener<DataType> {
    fun respondToChange(observable: Any, value: DataType, grow: Boolean = false)
}