package jp.thicklab.kyukonavi;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.*;
import org.xml.sax.*;

import android.app.*;
import android.app.AlertDialog.Builder;
import android.content.*;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import android.widget.AdapterView.*;

public class KyukoNaviActivity extends ListActivity {
	List<ItemBean> list = new ArrayList<ItemBean>();
	ListAdapter lista;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.list);
		Calendar cal = Calendar.getInstance();

		Calendar caltoday = Calendar.getInstance();
		int week = cal.get(Calendar.DAY_OF_WEEK);// 2-7 1:sun
		ArrayAdapter<String> _adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);// 0-5
		String[] week_chr = { "月", "火", "水", "木", "金", "土" };
		cal.add(Calendar.DATE, 2 - week);

		for(int i = 0; i <= 5; i++) {
			if(cal.before(caltoday)) {
				cal.add(Calendar.DATE, 7);
				_adapter.add(cal.get(Calendar.MONTH) + 1 + "/"
				+ cal.get(Calendar.DATE) + "(" + week_chr[i] + ")");
				cal.add(Calendar.DATE, -7);
			} else {
				_adapter.add(cal.get(Calendar.MONTH) + 1 + "/"
				+ cal.get(Calendar.DATE) + "(" + week_chr[i] + ")");
			}
			cal.add(Calendar.DATE, 1);
		}
		_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		final Spinner _spinner = (Spinner) findViewById(R.id.spinner1);
		_spinner.setPrompt("曜日を選択して下さい。");
		_spinner.setAdapter(_adapter);

		final Handler mHandler = new Handler();
		_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				setProgressBarVisibility(true);
				setProgressBarIndeterminate(true);
				new Thread(new Runnable() {
					public void run() {
						mHandler.post(new Runnable() {
							public void run() {
								updatedata(_spinner.getSelectedItemPosition() + 1);// 1-6
							}
						});
					}
				}).start();
			}

			public void onNothingSelected(AdapterView<?> arg0) {}
		});

		if(week != 1) {
			_spinner.setSelection(week - 2);
		} else {
			_spinner.setSelection(0);
		}

		if(SettingsActivity.getMyCampus(this) == null) {
			new Builder(this).setTitle(R.string.app_name)
				.setMessage("初回起動のため\n設定画面に移動します").setIcon(R.drawable.icon)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(KyukoNaviActivity.this, SettingsActivity.class));
					}
				})
				.create()
				.show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Spinner _spinner = (Spinner)findViewById(R.id.spinner1);
		updatedata(_spinner.getSelectedItemPosition() + 1);// 1-6
	}

	// アイテムが選択されたときの処理
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, R.id.config, 0, R.string.config_label).setIcon(android.R.drawable.ic_menu_manage);
		menu.add(0, R.id.readcsv, 0, R.string.readcsv_label).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, R.id.perm_start, 0, R.string.perm_start_label).setIcon(android.R.drawable.ic_media_play);
		menu.add(0, R.id.perm_end, 0, R.string.perm_end_label).setIcon(android.R.drawable.ic_media_pause);
		menu.add(0, R.id.about, 0, R.string.about_label).setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, R.id.end, 0, R.string.end_label).setIcon(android.R.drawable.ic_menu_close_clear_cancel);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		PendingIntent service = PendingIntent.getService(KyukoNaviActivity.this, 0, (new Intent(KyukoNaviActivity.this, KyukoNaviService.class)), 0);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		long first = System.currentTimeMillis() + 6 * 60 * 60 * 1000;
		long interval = 6 * 60 * 60 * 1000;

		switch(item.getItemId()) {
		case R.id.perm_start:
			Toast.makeText(this, "常駐を開始します。", Toast.LENGTH_SHORT).show();
			am.setRepeating(AlarmManager.RTC, first, interval, service);
			break;
		case R.id.perm_end:
			Toast.makeText(this, "常駐を停止します。", Toast.LENGTH_SHORT).show();
			am.cancel(service);
			nm.cancelAll();
			break;
		case R.id.about:
			// about画面表示
	        PackageInfo packageInfo = null;
			try {
				packageInfo = getPackageManager().getPackageInfo("jp.thicklab.kyukonavi", PackageManager.GET_META_DATA);
				new Builder(this)
				.setTitle("同志社休講ナビについて")
				.setMessage("duetのデータを利用して、\nykmsがつくっています。\n" +
						"versionName : "+packageInfo.versionName)
				.setPositiveButton("OK", null)
				.show();
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			break;
		case R.id.end:
			this.finish();
			break;
		case R.id.config:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.readcsv:
			new Builder(this)
				.setTitle("休講ナビ")
				.setMessage("時間割ファイルはダウンロード済みですか？")
				.setPositiveButton("はい",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(KyukoNaviActivity.this, CsvImporterActivity.class));
						}
					})
				.setNeutralButton("いいえ",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							new Builder(KyukoNaviActivity.this)
								.setTitle("休講ナビ")
								.setMessage("duetのトップに移動します。\nログインして時間割ファイルを\nダウンロードして読み込んでください。")
								.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://duet.doshisha.ac.jp/")));
										}
									})
								.show();
						}
					})
				.setNegativeButton("キャンセル", null)
				.create()
				.show();
			break;
		default:
		}
		return false;
	}

	class ListAdapter extends ArrayAdapter<ItemBean> {
		private LayoutInflater mInflater;
		TextView K_Time;
		TextView K_name;
		TextView Reason;
		TextView T_name;

		ListAdapter(Context context, List<ItemBean>objects) {
			super(context, 0, objects);
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(final int vposition, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.oneline, null);
			}
			final ItemBean item = this.getItem(vposition);

			if(item != null) {
				K_Time = (TextView)convertView.findViewById(R.id.K_Time);
				K_Time.setText(item.getTime());
				K_name = (TextView)convertView.findViewById(R.id.K_name);
				K_name.setText(item.getK_name());
				Reason = (TextView)convertView.findViewById(R.id.Reason);
				Reason.setText(item.getReason());
				T_name = (TextView)convertView.findViewById(R.id.T_name);
				T_name.setText(item.getT_name());
			}
			return convertView;
		}

		public String getK_name(int position) {
			return this.getItem(position).getK_name();
		}

		public String getT_name(int position) {
			return this.getItem(position).getT_name();
		}
	}

	// ここにリスト長押しメニュー置く
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
		super.onCreateContextMenu(menu, view, info);
		lista = new ListAdapter(getApplicationContext(), list);
		AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo)info;

		menu.setHeaderTitle(lista.getK_name(adapterInfo.position));
		menu.add(0, R.id.share, 0, R.string.share_label);
		menu.add(0, R.id.add_fav, 0, R.string.add_fav_label);
		menu.add(0, R.id.search_syllabus, 0, R.string.search_syllabus_label);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		lista = new ListAdapter(getApplicationContext(), list);
		AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo)item.getMenuInfo();
		Spinner _spinner = (Spinner) findViewById(R.id.spinner1);

		switch(item.getItemId()) {
			case R.id.share:
				String intent_text =
					_spinner.getSelectedItem().toString() + "の"
					+ lista.getK_name(adapterInfo.position)
					+ "(" + lista.getT_name(adapterInfo.position)
					+ ")が休講です。 #doshisha #kyukonavi";
				try {
					startActivity(new Intent().setAction(Intent.ACTION_SEND)
						.setType("text/plain")
						.putExtra(Intent.EXTRA_TEXT, intent_text));
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
				break;

			case R.id.add_fav:
				String fav_value = lista.getK_name(adapterInfo.position);
				fav_value += "," + PreferenceManager.getDefaultSharedPreferences(this).getString("class_conf", null);
				PreferenceManager.getDefaultSharedPreferences(this).edit().putString("class_conf", fav_value).commit();

				Toast.makeText(this, "通知するクラス："+PreferenceManager.getDefaultSharedPreferences(this).getString("class_conf", null)
					, Toast.LENGTH_SHORT).show();
				break;

			case R.id.search_syllabus:
				startActivity(new Intent(KyukoNaviActivity.this, SyllabusActivity.class)
					.putExtra("keyword", lista.getK_name(adapterInfo.position))
				);
				break;

			default:
		}
		return false;
	}

	public void updatedata(int n) {
		/*
		 * n=week(1:Monday,2:Tuesday,,,6:Saturday)
		 */
		InputStream istr = null;
		InputSource isrc = null;
		String kouchi = "2";
		try {
			if(SettingsActivity.getMyCampus(this) != null) {
				kouchi = SettingsActivity.getMyCampus(this);
			}

			istr = new URL("http://duet.doshisha.ac.jp/info/KK1000.jsp?katei=1&youbi=" + n + "&kouchi=" + kouchi + "&mobile=1").openStream();
			if(kouchi.equals("3")) {
				istr = new URL("http://duet.doshisha.ac.jp/info/KK1000.jsp?katei=3&youbi=" + n + "&kouchi=3&mobile=1").openStream();
			}
			isrc = new InputSource(new InputStreamReader(istr, "Shift_JIS"));
			try {
				XMLReader reader = new Parser();
				reader.setFeature(Parser.namespacesFeature, false);
				reader.setFeature(Parser.namespacePrefixesFeature, false);

				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				DOMResult result = new DOMResult();
				transformer.transform(new SAXSource(reader, isrc), result);
				Document doc = (Document)result.getNode();
				NodeList childs = doc.getElementsByTagName("td");

				final ItemBean[] data = new ItemBean[childs.getLength() / 4];
				for(int i=0; i<(childs.getLength() / 4); i++) {
					data[i] = new ItemBean();
				}

				for(int i=0; i<childs.getLength(); i++) {
					Element elem = (Element)childs.item(i);
					switch (i % 4) {
					case 0:
						data[i / 4].setTime(elem.getTextContent());
						break;
					case 1:
						data[i / 4].setK_name(elem.getTextContent());
						break;
					case 2:
						data[i / 4].setT_name(elem.getTextContent());
						break;
					case 3:
						data[i / 4].setReason(elem.getTextContent());
						break;
					default:
					}
				}

				list.clear();
				if(data.length > 0){
					for(int j=0; j<data.length; j++) {
						list.add(data[j]);
					}
				}

				// コンテキストメニュー登録
				registerForContextMenu(getListView());
				setListAdapter(new ListAdapter(getApplicationContext(), list));
				setProgressBarVisibility(false);

				if(SettingsActivity.getMyClass(this) != null) {
					String[] compstr = SettingsActivity.getMyClass(this).split(",");
					String str = "";
					for(int i = 0; i < compstr.length; i++) {
						for(int j = 0; j < data.length; j++) {
							if(data[j].getK_name().indexOf(compstr[i]) != -1) {
								str += data[j].getTime() + "講時:" + data[j].getK_name() + "\n";
							}
						}
					}
					if(str != "") {
						new Builder(this)
						.setTitle("休講ナビ")
						.setMessage(str + "は休講です。")
						.setPositiveButton("OK",null)
						.show();
					}
				}

			} catch(SAXException e) {
			} catch(TransformerException e) {
				throw new RuntimeException(e);
			}
		} catch(IOException e) {
			setProgressBarVisibility(false);
			Toast.makeText(this, "取得に失敗しました。\n電波状態を確認してください。", Toast.LENGTH_LONG).show();
			list.clear();
		} finally {
			if(istr != null) {
				try {
					istr.close();
				} catch(IOException e) {
				} catch(NullPointerException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
