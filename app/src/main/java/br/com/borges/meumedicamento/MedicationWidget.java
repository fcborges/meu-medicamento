package br.com.borges.meumedicamento;
import android.app.*;import android.appwidget.*;import android.content.*;import android.widget.*;
public class MedicationWidget extends AppWidgetProvider{public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids){RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget);PendingIntent p=PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);v.setOnClickPendingIntent(R.id.widget_title,p);v.setOnClickPendingIntent(R.id.widget_info,p);m.updateAppWidget(id,v);}}}
