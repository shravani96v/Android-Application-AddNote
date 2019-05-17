package com.decoders.demoapp

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DataModel : RealmObject() {
    @PrimaryKey
    var id: Int = 0
    var note: String? = null
    var imagePath: String? = null
}
