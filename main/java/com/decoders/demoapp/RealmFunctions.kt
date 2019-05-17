package com.decoders.demoapp

import android.util.Log
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

import java.util.ArrayList

object RealmFunctions {

    val allData: ArrayList<DataModel>
        get() {
            val realm = MyApp.realmDatabaseInstance
            val realmResults = realm.where(DataModel::class.java).findAllAsync().sort("id", Sort.DESCENDING)
            val list = ArrayList<DataModel>()
            list.addAll(realmResults)
            return list
        }

    private val lastId: Int
        get() {
            val realm = MyApp.realmDatabaseInstance
            if (realm.where(DataModel::class.java).max("id") != null) {
                val maxId = realm.where(DataModel::class.java).max("id")!!.toInt()
                Log.e("tag", "maxId: $maxId")
                return maxId
            }
            return 0
        }

    fun getData(id: Int): DataModel? {
        val realm = MyApp.realmDatabaseInstance
        val model = realm.where(DataModel::class.java).equalTo("id", id).findFirst()
        return model
    }

    fun storeData(model: DataModel?) {
        if (model == null)
            return
        val realm = beginTransaction()
        var maxId = lastId
        maxId++
        model.id = maxId
        realm.copyToRealmOrUpdate(model)
        realm.commitTransaction()
    }

    fun checkExist(model: DataModel): Boolean {
        val realm = MyApp.realmDatabaseInstance
        val rows = realm.where(DataModel::class.java).equalTo("id", model.id).findAll()
        return if (rows.size > 0) {
            true
        } else false
    }

    fun deleteData(id: Int) {
        val realm = beginTransaction()
        val toDelete = realm.where(DataModel::class.java).equalTo("id", id).findFirst()
        toDelete?.deleteFromRealm()
        realm.commitTransaction()
    }

    fun deleteAllData() {
        val realm = beginTransaction()
        val toDelete = realm.where(DataModel::class.java).findAll()
        toDelete?.deleteAllFromRealm()
        realm.commitTransaction()
    }

    fun beginTransaction(): Realm {
        val realm = MyApp.realmDatabaseInstance
        if (realm.isInTransaction)
            realm.cancelTransaction()
        realm.beginTransaction()
        return realm
    }

}