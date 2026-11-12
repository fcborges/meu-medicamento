package br.com.borges.meumedicamento;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        Db db = new Db(context);
        try (Cursor cursor = db.query(
                "SELECT doses.id,doses.due,meds.name,people.name FROM doses " +
                "JOIN meds ON meds.id=doses.med " +
                "JOIN people ON people.id=meds.person " +
                "WHERE doses.status=0 AND doses.due>?",
                String.valueOf(System.currentTimeMillis()))) {
            while (cursor.moveToNext()) {
                ReminderReceiver.schedule(
                    context, cursor.getInt(0), cursor.getLong(1),
                    cursor.getString(3) + ": " + cursor.getString(2));
            }
        }
    }
}
