package jp.thicklab.kyukonavi;

import android.content.Context;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener{
	EditTextPreference myclass = (EditTextPreference)findPreference("class_conf");
	ListPreference mycampus = (ListPreference)findPreference("campus_conf");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
		myclass = (EditTextPreference)findPreference("class_conf");
		mycampus = (ListPreference)findPreference("campus_conf");
		myclass.setOnPreferenceChangeListener(this);
		mycampus.setOnPreferenceChangeListener(this);

		if(myclass.getText() != "") {
			myclass.setSummary(myclass.getText());
		}
		if(mycampus.getValue() != null) {
			mycampus.setSummary(numtocampus(mycampus.getValue()));
		}
		Toast.makeText(this, mycampus.getValue(), Toast.LENGTH_SHORT).show();
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.equals(myclass)) {
			myclass.setSummary(newValue.toString());
		} else if(preference.equals(mycampus)) {
			mycampus.setSummary(numtocampus(newValue.toString()));
		}
		return true;
	}

	public static String getMyCampus(Context ctx){
		return PreferenceManager.getDefaultSharedPreferences(ctx).getString("campus_conf", null);
	}
	public static String getMyClass(Context ctx){
		return PreferenceManager.getDefaultSharedPreferences(ctx).getString("class_conf", null);
	}
	public String numtocampus (String num) {
		if(num.equals("2")) {
			return "京田辺";
		} else if(num.equals("1")) {
			return "今出川";
		} else if(num.equals("3")) {
			return "大学院";
		} else {
			return "error!";
		}
	}

}