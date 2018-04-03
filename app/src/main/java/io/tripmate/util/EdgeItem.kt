package io.tripmate.util

class EdgeItem(label: String) : AbstractItem(label) {


    override fun getType(): Int {
        return AbstractItem.TYPE_EDGE
    }

}
