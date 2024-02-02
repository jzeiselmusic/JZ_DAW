package org.jzeisel.app_test.util

interface ObservableListener<DataType> {
    fun respondToChange(newValue: DataType)
}