package jp.thicklab.kyukonavi;

import java.io.*;
import java.util.ArrayList;

import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.app.*;
import android.app.AlertDialog.Builder;
import android.content.*;

public class CsvImporterActivity extends Activity {
	// リストに設定するアイテム
	String[] item;
	static ContentValues values = new ContentValues();
	static ArrayList<String> list = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.csv_import);

		try {
			try {
				//ファイルディレクトリの取得
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_PICK);
				Uri startDir = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()));
				intent.setDataAndType(startDir, "vnd.android.cursor.dir/lysesoft.andexplorer.file");

				intent.putExtra("explorer_title", "jikanwari.csvを選択");
				intent.putExtra("browser_filter_extension_whitelist", "*.csv");
				intent.putExtra("browser_title_background_color", "440000AA");
				intent.putExtra("browser_title_foreground_color", "FFFFFFFF");
				intent.putExtra("browser_list_background_color", "66000000");
				intent.putExtra("browser_list_fontscale", "120%");
				intent.putExtra("browser_list_layout", "2");
				startActivityForResult(intent, 0);
			} catch(ActivityNotFoundException e) {
				Toast.makeText(this, "AndExplorerをインストールしてください", Toast.LENGTH_LONG).show();
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=lysesoft.andexplorer&hl=ja")));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		// 現在チェックされているアイテムを取得
		// チェックされてないアイテムは含まれない模様
		Button button1 = (Button) findViewById(R.id.add);
		button1.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				String selectItem = "";

				// マップの情報を取得する
				ListView listView = (ListView) findViewById(R.id.ListView1);
				SparseBooleanArray checked = listView.getCheckedItemPositions();

				for(int i=0; i<list.size(); i++) {
					if(checked.get(i) == true) {
						// マッピングされている(選択されている)項目だった場合は文字列に連結する
						selectItem += list.get(i) + ",";
					}
				}

				if(selectItem != "") {
					//Toast.makeText(CsvImporterActivity.this, selectItem, Toast.LENGTH_SHORT).show();
					String classlist = selectItem;
					SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(CsvImporterActivity.this);
					pre.edit().putString("class_conf", classlist).commit();
					new Builder(CsvImporterActivity.this)
						.setTitle("休講ナビ")
						.setMessage(classlist + "を追加しました。")
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {finish();}
						}).show();
				} else {
					new Builder(CsvImporterActivity.this)
					.setPositiveButton("OK", null)
					.setTitle("休講ナビ")
					.setMessage("何か選択してください。")
					.show();
				}
			}
		});
		Button button2 = (Button) findViewById(R.id.back);
		button2.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				CsvImporterActivity.this.finish();
			}
		});}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(resultCode != 0 && intent.getData() != null) {
			try {
				LoadJikanwari(CsvImporterActivity.this, intent.getDataString().replace("file://", ""));

				item = list.toArray(new String[0]);
				ListView listView = (ListView) findViewById(R.id.ListView1);
				listView.setItemsCanFocus(false);
				listView.setAdapter(new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_multiple_choice, item));
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			if(resultCode == 0) {
				CsvImporterActivity.this.finish();
			}
		}
	}

	public void LoadJikanwari(Activity activity,String filepath) throws Exception {
		FileInputStream in = null;
		ByteArrayOutputStream out = null;
		ByteArrayOutputStream outmeta = null;

		int x = 0;
		int get = 0;

		try {
			in = new FileInputStream(filepath);
			out = new ByteArrayOutputStream();
			outmeta = new ByteArrayOutputStream();

			while(true) {
				out.reset();
				outmeta.reset();
				int tmp;// ストリームから読み込んだ１バイト

				while(true) {
					tmp = in.read();

					if(tmp == -1) {
						out.close();
						outmeta.close();
						in.close();
						return;
					}
					if(tmp == 0x22 && get == 0) { // "
						get = 1;
						x = 0;
						break;
					}
					if(tmp == 0x22 && get == 1) {
						get = 0;
						x++;
						break;
					}

					if(tmp == 0x2c) { // ,
						break;
					}

					if(tmp == 0x0a) { // 改行
						if(get == 1) {
							x++;
						}
						break;
					}

					if(get == 1) {
						out.write(tmp);
					} else {
						outmeta.write(tmp);
					}
				}

				if(out.size() > 0) {
					String strout = out.toString("Shift_JIS");
					if(strout.equals("【先行登録】")) {
						x--;
						continue;
					}
					switch (x) {
					case 2:
						if(!list.contains(strout)) {
							list.add(strout);
						}
						break;
					default:
					}
				}
				//in.close();
			}

		} catch(Exception e) {
			e.printStackTrace();
			try {
				if(in != null) {
					in.close();
				}
				if(out != null) {
					out.close();
				}
			throw new Exception();
			} catch(Exception e2) {
			}
			throw new Exception();
		}
	}

}
