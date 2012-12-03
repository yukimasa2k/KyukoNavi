package jp.thicklab.kyukonavi;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class CsvImporterActivity extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			//ファイルディレクトリの取得
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_PICK);
			Uri startDir = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()));
			intent.setDataAndType(startDir, "vnd.android.cursor.dir/lysesoft.andexplorer.file");

			intent.putExtra("explorer_title", "時間割ファイルを選択");
			intent.putExtra("browser_filter_extension_whitelist", "*.csv");
			intent.putExtra("browser_title_background_color", "440000AA");
			intent.putExtra("browser_title_foreground_color", "FFFFFFFF");
			intent.putExtra("browser_list_background_color", "66000000");
			intent.putExtra("browser_list_fontscale", "120%");
			intent.putExtra("browser_list_layout", "2");
			startActivityForResult(intent, 0);
		} catch(ActivityNotFoundException e) {
			Toast.makeText(this, "AndExplorerをインストールしてください", Toast.LENGTH_LONG).show();
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://market.android.com/details?id=lysesoft.andexplorer&hl=ja")));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(intent != null) {
			if(intent.getData() != null) {
				try {
					readcsv(this, intent.getData().toString().replace("file://", ""));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			if(resultCode == 0) finish();
		}
	}

	public void readcsv(Activity activity, String filepath) throws Exception {
		//データベースオブジェクトの取得
		DBHelper dbHelper = new DBHelper(activity);
		SQLiteDatabase db = dbHelper.getWritableDatabase();


		int season = 0,day = 0,time = 0,x = 0;
		int get = 0,id = 0;
		FileInputStream in = null;
		ByteArrayOutputStream out = null;
		ByteArrayOutputStream outmeta = null;

		try {

			//ファイル入力ストリームのオープン(6)
			in = new FileInputStream(filepath);
			out = new ByteArrayOutputStream();
			outmeta = new ByteArrayOutputStream();

			while(true) {
				out.reset();
				outmeta.reset();
				int tmp;

				while(true) {
					tmp = in.read();

					if(tmp == -1) {
						out.close();
						outmeta.close();
						in.close();
						db.close();
						dbHelper.close();
						return;
					}

					if(tmp == 0x22) { //"
						if(get == 0) {
							get = 1;
							x = 0;
							day++;
							id++;
							break;
						} else if(get == 1) {
							get = 0;
							x++;
							break;
						}
					}

					if(tmp == 0x2c) break;//,

					if(tmp == 0x0a) {
						if(get == 1) x++; //\n
						break;
					}

					if(get == 1) out.write(tmp);
					else outmeta.write(tmp);
				}

				if(outmeta.size() > 0) {
					String meta = outmeta.toString("Shift_JIS");
					if(meta.equals("春学期")) season = 0;
					else if(meta.equals("秋学期")) season = 1;
					else if(meta.equals("集中科目・時間制科目")) {
						season = 2;
						time = 0;
						day = 0;
					}
					if(meta.equals("１"))		time = 0;
					else if(meta.equals("２"))	time = 1;
					else if(meta.equals("３"))	time = 2;
					else if(meta.equals("４"))	time = 3;
					else if(meta.equals("５"))	time = 4;
					else if(meta.equals("６"))	time = 5;
					else if(meta.equals("７"))	time = 6;
					day=0;
				}
				if(out.size() > 0) {
					String strout = out.toString("Shift_JIS");
					if(strout.equals("【先行登録】")) {
						x--;
						continue;
					}

					ContentValues values = new ContentValues();

					values.put("season", season+"");
					values.put("day", (day-1)+"");
					values.put("time", time+"");
					switch(x) {
					case 1:
						values.put("place", strout);
						break;
					case 2:
						values.put("name", strout);
						break;
					case 3:
						values.put("data1", strout);
						break;
					case 4:
						values.put("data2", strout);
						break;
					default:
					}
					values.put("id", (id-1)+"");
					int colNum = db.update("DoshishaJikanwari", values, "id = ?", new String[]{(id-1)+""});
					if(colNum == 0) {
						db.insert("DoshishaJikanwari", "", values);
					}
					db.close();
					Builder dlg = new Builder(this);
					dlg.setTitle("休講ナビ");
					dlg.setMessage("春or秋学期のどちらを\n読み込みますか？");

					dlg.setPositiveButton("春学期", new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int which) {viewdb("0");}
					});

					dlg.setNeutralButton("秋学期", new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int which) {viewdb("1");}
					});

					dlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int which) {}
					});
					dlg.create();
					dlg.show();
				}
			}
		} catch(FileNotFoundException e) {
		} catch(IOException e) {
		} catch(Exception e) {
			e.printStackTrace();
			try {
				if(in != null) in.close();
				if(out != null) out.close();
				if(db != null) db.close();
				if(dbHelper != null) dbHelper.close();
				throw new Exception();
			} catch(Exception e2) {}
			throw new Exception();
		}
	}

	static class DBHelper extends SQLiteOpenHelper {
		public DBHelper(Context context) {
			super(context, "DoshishaJikanwari.db", null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("drop table if exists DoshishaJikanwari");
			db.execSQL("create table if not exists DoshishaJikanwari" +
					"(id text primary key,season text,day text,time text,"+
					"place text,name text,data1 text,data2 text)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
			db.execSQL("drop table if exists DoshishaJikanwari");
			onCreate(db);
		}
	}

	private void viewdb(String season) {
		SQLiteDatabase db = null;
		try {
			DBHelper dbHelper = new DBHelper(this);
			db = dbHelper.getReadableDatabase();
			String sqlstr = "select distinct name from DoshishaJikanwari where season == " + season;
			Cursor c = db.rawQuery(sqlstr, null);
			String classdb = "";
			if(c.getCount() == 0) Toast.makeText(this, "DB読み込み失敗", Toast.LENGTH_LONG).show();
			c.moveToFirst();

			for(int i=0; i<c.getCount(); i++) {
				classdb += ItemBean.modifytext(c.getString(c.getColumnIndexOrThrow("name"))) + ",";
				c.moveToNext();
			}
			db.close();

			if(SettingsActivity.getMyClass(this) != null) {
				String classlist = "";
				classlist = "" + classdb;
				SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
				pre.edit().putString("class_conf", classlist).commit();
				new Builder(this)
				.setPositiveButton("OK", null)
				.setTitle("休講ナビ")
				.setMessage(classlist + "を追加しました。")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int which) {finish();}
				})
				.show();
			}
		} catch(Exception e) {
			Toast.makeText(this, "その他失敗", Toast.LENGTH_LONG).show();
			e.printStackTrace();
			try{
				if(db != null) db.close();
			} catch(Exception e2) {
				Toast.makeText(this, "その他失敗", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			return;
		}
	}
}
