package br.com.borges.meumedicamento;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.content.ContentValues;import android.graphics.Color;import android.view.*;import android.widget.*;import java.text.*;import java.util.*;import org.json.*;
public class MainActivity extends Activity{
 Db db;LinearLayout root;Calendar selectedDay=Calendar.getInstance();SimpleDateFormat fmt=new SimpleDateFormat("dd/MM HH:mm",new Locale("pt","BR"));
 public void onCreate(Bundle b){super.onCreate(b);db=new Db(this);if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},1);home();}
 TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(30,40,55));t.setPadding(12,12,12,12);return t;}
 void init(String title){ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setPadding(16,20,16,20);root.setOrientation(1);root.setBackgroundColor(Color.rgb(247,249,252));scroll.addView(root);setContentView(scroll);root.addView(text(title,24));}
 void button(String s,Runnable r){Button b=new Button(this);b.setText(s);root.addView(b);b.setOnClickListener(v->r.run());}
 EditText input(String hint,String val){EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(true);e.setText(val);root.addView(e);return e;}
 void home(){
  init("Meu Medicamento");
  root.addView(text("Seu controle diário de saúde",14));
  button("+ Novo tratamento",()->newMed());
  LinearLayout nav=new LinearLayout(this);root.addView(nav);
  Button prev=new Button(this);prev.setText("‹");nav.addView(prev);prev.setOnClickListener(v->{selectedDay.add(Calendar.DAY_OF_MONTH,-1);home();});
  Button date=new Button(this);date.setText(new SimpleDateFormat("dd/MM/yyyy",new Locale("pt","BR")).format(selectedDay.getTime()));nav.addView(date);
  date.setOnClickListener(v->new DatePickerDialog(this,(picker,y,m,d)->{selectedDay.set(y,m,d);home();},selectedDay.get(Calendar.YEAR),selectedDay.get(Calendar.MONTH),selectedDay.get(Calendar.DAY_OF_MONTH)).show());
  Button next=new Button(this);next.setText("›");nav.addView(next);next.setOnClickListener(v->{selectedDay.add(Calendar.DAY_OF_MONTH,1);home();});
  Calendar begin=(Calendar)selectedDay.clone();begin.set(Calendar.HOUR_OF_DAY,0);begin.set(Calendar.MINUTE,0);begin.set(Calendar.SECOND,0);begin.set(Calendar.MILLISECOND,0);
  Calendar end=(Calendar)begin.clone();end.add(Calendar.DAY_OF_MONTH,1);
  int total=0,done=0;
  LinearLayout dosesBox=new LinearLayout(this);dosesBox.setOrientation(1);
  try(Cursor c=db.query("SELECT doses.id,people.name,meds.name,meds.dose,doses.due,doses.status,doses.actual FROM doses JOIN meds ON meds.id=doses.med JOIN people ON people.id=meds.person WHERE doses.due>=? AND doses.due<? ORDER BY doses.due ASC",String.valueOf(begin.getTimeInMillis()),String.valueOf(end.getTimeInMillis())))){
   while(c.moveToNext()){
    total++;long id=c.getLong(0),due=c.getLong(4);int status=c.getInt(5);if(status==1)done++;
    LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(16,14,16,14);
    android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(22);bg.setStroke(2,Color.rgb(224,231,240));card.setBackground(bg);
    LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,6,0,12);dosesBox.addView(card,cp);
    TextView header=text(new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(due))+"  •  "+c.getString(2),19);header.setTypeface(null,1);card.addView(header);
    card.addView(text(c.getString(1)+"  ·  "+c.getString(3),14));
    if(status==1){card.addView(text("✓ Administrado às "+new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(c.getLong(6))),14));}
    else if(status==2){card.addView(text("✕ Não administrado",14));}
    else {card.addView(text("○ Pendente",14));}
    LinearLayout actions=new LinearLayout(this);card.addView(actions);
    Button yes=new Button(this);yes.setText(status==0?"✓ Tomei":"Alterar");actions.addView(yes);
    yes.setOnClickListener(v->{if(status==0){db.mark(id,1,System.currentTimeMillis());home();}else new AlertDialog.Builder(this).setTitle("Corrigir registro?").setPositiveButton("Voltar a pendente",(dialog,w)->{db.mark(id,0,0);home();}).setNegativeButton("Cancelar",null).show();});
    if(status==0){Button no=new Button(this);no.setText("Não tomei");actions.addView(no);no.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Confirmar dose não administrada?").setPositiveButton("Confirmar",(dialog,w)->{db.mark(id,2,0);home();}).setNegativeButton("Cancelar",null).show());}
   }
  }
  root.addView(text(done+" de "+total+" doses administradas",18));
  if(total==0)root.addView(text("Nenhuma dose programada para esta data.",15));
  root.addView(dosesBox);
  button("Hoje",()->{selectedDay=Calendar.getInstance();home();});
  button("Familiares",()->newPerson());
  button("Exportar histórico CSV",()->exportCsv());
  button("Fazer backup JSON",()->backup());
 }
 void newPerson(){init("Adicionar familiar");EditText n=input("Nome","");button("Salvar",()->{String s=n.getText().toString().trim();if(s.isEmpty())return;ContentValues v=new ContentValues();v.put("name",s);db.add("people",v);home();});button("Voltar",()->home());}
 void newMed(){init("Novo tratamento");ArrayList<Integer> ids=new ArrayList<>();ArrayList<String> names=new ArrayList<>();try(Cursor c=db.query("SELECT id,name FROM people ORDER BY name")){while(c.moveToNext()){ids.add(c.getInt(0));names.add(c.getString(1));}}Spinner people=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names);people.setAdapter(adapter);root.addView(text("Pessoa",16));root.addView(people);EditText med=input("Nome do medicamento","");EditText dose=input("Dose prescrita (ex.: 5 mL)","");Calendar startDate=Calendar.getInstance();startDate.set(Calendar.HOUR_OF_DAY,0);startDate.set(Calendar.MINUTE,0);startDate.set(Calendar.SECOND,0);startDate.set(Calendar.MILLISECOND,0);root.addView(text("Data de início do tratamento",16));Button chooseDate=new Button(this);chooseDate.setText(new SimpleDateFormat("dd/MM/yyyy",new Locale("pt","BR")).format(startDate.getTime()));root.addView(chooseDate);chooseDate.setOnClickListener(v->new DatePickerDialog(this,(picker,y,m,day)->{startDate.set(y,m,day);chooseDate.setText(new SimpleDateFormat("dd/MM/yyyy",new Locale("pt","BR")).format(startDate.getTime()));},startDate.get(Calendar.YEAR),startDate.get(Calendar.MONTH),startDate.get(Calendar.DAY_OF_MONTH)).show());EditText days=input("Duração em dias","5");days.setInputType(2);EditText hours=input("Horários diários (ex.: 08:00,20:00)","08:00,20:00");root.addView(text("Os horários são fixos por dia. Confirme-os com a prescrição. Não use para calcular doses.",13));button("Salvar tratamento e criar lembretes",()->{try{if(ids.isEmpty())throw new Exception("Cadastre uma pessoa");String name=med.getText().toString().trim(),d=dose.getText().toString().trim();if(name.isEmpty()||d.isEmpty())throw new Exception("Informe medicamento e dose");int count=Integer.parseInt(days.getText().toString());if(count<1||count>365)throw new Exception("Duração entre 1 e 365 dias");String[] times=hours.getText().toString().split(",");if(times.length<1||times.length>12)throw new Exception("Confira horários");Calendar start=(Calendar)startDate.clone();ArrayList<Long> due=new ArrayList<>();for(int day=0;day<count;day++)for(String time:times){String[] p=time.trim().split(":");if(p.length!=2)throw new Exception("Use HH:mm");int h=Integer.parseInt(p[0]),m=Integer.parseInt(p[1]);if(h>23||h<0||m>59||m<0)throw new Exception("Horário inválido");Calendar cal=(Calendar)start.clone();cal.add(Calendar.DAY_OF_YEAR,day);cal.set(Calendar.HOUR_OF_DAY,h);cal.set(Calendar.MINUTE,m);due.add(cal.getTimeInMillis());}ContentValues v=new ContentValues();v.put("person",ids.get(people.getSelectedItemPosition()));v.put("name",name);v.put("dose",d);v.put("start",start.getTimeInMillis());v.put("days",count);v.put("hours",hours.getText().toString());long mid=db.add("meds",v);for(long when:due){ContentValues dv=new ContentValues();dv.put("med",mid);dv.put("due",when);long id=db.add("doses",dv);ReminderReceiver.schedule(this,(int)id,when,names.get(people.getSelectedItemPosition())+": "+name+" ("+d+")");}home();}catch(Exception ex){Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();}});button("Cancelar",()->home());}
 void share(String name,String mime,String data){try{java.io.File f=new java.io.File(getCacheDir(),name);try(java.io.FileOutputStream out=new java.io.FileOutputStream(f)){out.write(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));}android.net.Uri uri=androidx.core.content.FileProvider.getUriForFile(this,getPackageName()+".files",f);Intent i=new Intent(Intent.ACTION_SEND);i.setType(mime);i.putExtra(Intent.EXTRA_STREAM,uri);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Exportar arquivo"));}catch(Exception e){Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();}}
 void exportCsv(){StringBuilder b=new StringBuilder("Pessoa;Medicamento;Dose;Previsto;Status;Administrado em\n");try(Cursor c=db.query("SELECT people.name,meds.name,meds.dose,doses.due,doses.status,doses.actual FROM doses JOIN meds ON meds.id=doses.med JOIN people ON people.id=meds.person ORDER BY doses.due")){while(c.moveToNext()){int status=c.getInt(4);b.append(c.getString(0)).append(";").append(c.getString(1)).append(";").append(c.getString(2)).append(";").append(fmt.format(new Date(c.getLong(3)))).append(";").append(status==1?"Administrado":status==2?"Não administrado":"Pendente").append(";").append(status==1?fmt.format(new Date(c.getLong(5))):"").append("\n");}}share("historico.csv","text/csv",b.toString());}
 void backup(){try{JSONObject out=new JSONObject();for(String table:new String[]{"people","meds","doses"}){JSONArray arr=new JSONArray();try(Cursor c=db.query("SELECT * FROM "+table)){while(c.moveToNext()){JSONObject item=new JSONObject();for(int i=0;i<c.getColumnCount();i++){String key=c.getColumnName(i);item.put(key,c.isNull(i)?JSONObject.NULL:c.getString(i));}arr.put(item);}}out.put(table,arr);}share("backup-medicamentos.json","application/json",out.toString(2));}catch(Exception e){Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();}}
}
