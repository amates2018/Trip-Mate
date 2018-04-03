package io.tripmate.util

class CenterItem(label: String) : AbstractItem(label) {


    override fun getType(): Int {
        return AbstractItem.TYPE_CENTER
    }

}
