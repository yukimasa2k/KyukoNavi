package jp.thicklab.kyukonavi;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener{ // PreferenceActivityの継承

	ListPreference mycampus = (ListPreference)findPreference("campus_conf");
	EditTextPreference myclass = (EditTextPreference)findPreference("class_conf");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
		mycampus = (ListPreference)findPreference("campus_conf");
		myclass = (EditTextPreference)findPreference("class_conf");
		myclass.setOnPreferenceChangeListener(this);
		mycampus.setOnPreferenceChangeListener(this);
		if(getPreferences(MODE_PRIVATE).getString("class_conf", null) != null) {
			myclass.setSummary(getPreferences(MODE_PRIVATE).getString("class_conf", null));
		} else if(getPreferences(MODE_PRIVATE).getString("campus_conf", null) != null) {
			mycampus.setSummary(numtocampus(getPreferences(MODE_PRIVATE).getString("campus_conf", null)));
		}


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
		String campus = "";
		if(num.equals("2")) {
			campus = "京田辺";
		} else if(num.equals("1")) {
			campus = "今出川";
		} else if(num.equals("3")) {
			campus = "大学院";
		}
		return campus;
	}

}