package com.decoders.demoapp

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }

    companion object {

        private var realm: Realm? = null

        val realmDatabaseConfiguration: RealmConfiguration
            get() = RealmConfiguration.Builder().name("demoapp.realm").deleteRealmIfMigrationNeeded().build()

        val realmDatabaseInstance: Realm
            get() = Realm.getInstance(realmDatabaseConfiguration)
    }
}
