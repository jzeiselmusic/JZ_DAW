package org.jzeisel.app_test.util

class ObservableList<DataType>(override var size: Int): MutableList<DataType> {
    /* observable list notifies listeners when size of list changes */
    private val listeners = mutableListOf<ObservableListener<Int>>()
    private var list = mutableListOf<DataType>()

    override fun get(index: Int): DataType {
        return list[index]
    }

    fun addAndNotify(element: DataType): Boolean {
        val ret = list.add(element)
        if (ret) {
            size += 1
            notifyListeners(list.indexOf(element), true)
            return true
        }
        return false
    }

    fun addAndNotify(index: Int, element: DataType) {
        list.add(index, element)
        size += 1
        notifyListeners(list.indexOf(element), true)
    }

    override fun add(index: Int, element: DataType) {
        list.add(index, element)
        size += 1
    }

    override fun add(element: DataType): Boolean {
        val ret = list.add(element)
        if (ret) {
            size += 1
        }
        return ret
    }

    override fun removeAt(index: Int): DataType {
        val ret = list.removeAt(index)
        size -= 1
        return ret
    }

    fun removeAtAndNotify(index: Int) {
        list.removeAt(index)
        size -= 1
        notifyListeners(index, false)
    }

    override fun remove(element: DataType): Boolean {
        val ret = list.remove(element)
        if (ret) {
            size -= 1
        }
        return ret
    }

    fun removeAndNotify(element: DataType) {
        val idx = list.indexOf(element)
        list.remove(element)
        size -= 1
        notifyListeners(idx, false)
    }

    fun addListener(listener: ObservableListener<Int>) {
        listeners.add(listener)
    }

    fun removeListener(listener: ObservableListener<Int>) {
        listeners.remove(listener)
    }

    private fun notifyListeners(index: Int, grow: Boolean) {
        for (listener in listeners) {
            /* index will be null if value added is last in the list */
            listener.respondToChange(this, index, grow)
        }
    }

    override fun contains(element: DataType): Boolean {
        return list.contains(element)
    }

    override fun containsAll(elements: Collection<DataType>): Boolean {
        return list.containsAll(elements)
    }

    override fun addAll(index: Int, elements: Collection<DataType>): Boolean {
        return list.addAll(index, elements)
    }

    override fun addAll(elements: Collection<DataType>): Boolean {
        return list.addAll(elements)
    }

    override fun clear() {
        list.clear()
    }

    override fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    override fun iterator(): MutableIterator<DataType> {
        return list.iterator()
    }

    override fun listIterator(): MutableListIterator<DataType> {
        return list.listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<DataType> {
        return list.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<DataType> {
        return list.subList(fromIndex, toIndex)
    }

    override fun set(index: Int, element: DataType): DataType {
        return list.set(index, element)
    }

    override fun retainAll(elements: Collection<DataType>): Boolean {
        return retainAll(elements)
    }

    override fun removeAll(elements: Collection<DataType>): Boolean {
        return list.removeAll(elements)
    }

    override fun lastIndexOf(element: DataType): Int {
        return list.lastIndexOf(element)
    }

    override fun indexOf(element: DataType): Int {
        return list.indexOf(element)
    }
}