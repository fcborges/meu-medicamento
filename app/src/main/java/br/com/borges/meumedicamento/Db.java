package br.com.borges.meumedicamento;
import android.content.*;import android.database.*;import android.database.sqlite.*;
public class Db extends SQLiteOpenHelper {
 Db(Context c){super(c,"medicamentos.db",null,1);}
 public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE people(id INTEGER PRIMARY KEY,name TEXT UNIQUE)");d.execSQL("CREATE TABLE meds(id INTEGER PRIMARY KEY,person INTEGER,name TEXT,dose TEXT,start INTEGER,days INTEGER,hours TEXT)");d.execSQL("CREATE TABLE doses(id INTEGER PRIMARY KEY,med INTEGER,due INTEGER,status INTEGER DEFAULT 0,actual INTEGER)");for(String n:new String[]{"Fátima","Matteo","Lorenzo"}){ContentValues v=new ContentValues();v.put("name",n);d.insert("people",null,v);}}
 public void onUpgrade(SQLiteDatabase d,int a,int b){}
 long add(String table,ContentValues v){return getWritableDatabase().insert(table,null,v);}
 Cursor query(String sql,String...args){return getReadableDatabase().rawQuery(sql,args);}
 void mark(long id,int status,long actual){ContentValues v=new ContentValues();v.put("status",status);if(actual>0)v.put("actual",actual);else v.putNull("actual");getWritableDatabase().update("doses",v,"id=?",new String[]{""+id});}
}