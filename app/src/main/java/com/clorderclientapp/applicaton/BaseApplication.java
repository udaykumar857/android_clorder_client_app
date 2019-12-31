package com.clorderclientapp.applicaton;

import android.app.Application;
import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class BaseApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        RealmMigration realmMigration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                Log.d("Older Version", "" + oldVersion);
                Log.d("New Version", "" + newVersion);
                RealmSchema schema = realm.getSchema();
                if (oldVersion == 1) {// keep previous version here..... *presently update playStore database version is 1;
                    RealmObjectSchema realmObjectSchema = schema.get("CartModel");
                    realmObjectSchema.addField("orderId", int.class);
                    realmObjectSchema.addField("orderPlacedTime", String.class);
                    realmObjectSchema.addField("orderPlacedDate", String.class);
                    realmObjectSchema.addField("isFutureOrder", boolean.class);
                    realmObjectSchema.addField("discountAmount", float.class);
                    realmObjectSchema.addField("discountCode", String.class);
                    realmObjectSchema.addField("paymentType", String.class);
                    realmObjectSchema.addField("validForOrderType", int.class);
                    realmObjectSchema.addField("discountType", int.class);
                }
            }
        };
        RealmConfiguration configuration = new RealmConfiguration.Builder(this)
                .schemaVersion(2)//change version num when new keys added in database....
                .migration(realmMigration)
                .build();
        Realm.setDefaultConfiguration(configuration);
    }

}
