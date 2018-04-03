package io.tripmate.util

class EmptyItem(label: String) : AbstractItem(label) {


    override fun getType(): Int {
        return AbstractItem.TYPE_EMPTY
    }

}
