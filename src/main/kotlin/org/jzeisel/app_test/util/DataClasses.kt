package org.jzeisel.app_test.util

data class BoxEntry(
    var name: String = "",
    var boxEntrySubList: List<BoxEntry>? = null
)