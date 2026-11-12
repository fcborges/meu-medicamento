package br.com.borges.meumedicamento;
import android.content.*;import android.database.*;
public class BootReceiver extends BroadcastReceiver{public void onReceive(Context c,Intent i){Db db=new Db(c);try(Cursor q=db.query("SELECT doses.id,doses.due,meds.name,people.name FROM doses JOIN meds ON meds.id=doses.med JOIN people ON people.id=meds.person WHERE doses.status=0 AND doses.due>?",String.valueOf(System.currentTimeMillis()))){while(q.moveToNext())ReminderReceiver.schedule(c,q.getInt(0),q.getLong(1),q.getString(3)+": "+q.getString(2));}}}
}