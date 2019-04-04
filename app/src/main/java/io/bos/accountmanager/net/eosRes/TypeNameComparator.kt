package com.hconline.base.eos.eosRes

/**
 * Created by haichecker on 18-6-28.
 */
class TypeNameComparator : Comparator<Row> {
    override fun compare(o1: Row?, o2: Row?): Int {
        if (o1 == null)
            return -1
        if (o1 == o2) {
            return 0
        }

        for (i in 0..11) {
            val o1Chat = o1.owner[i]
            val o2Chat = o2!!.owner[i]
            val d = o1Chat.compareTo(o2Chat)
            if (d == 0) {
                continue
            }
            return d
        }

        return 0

    }
}