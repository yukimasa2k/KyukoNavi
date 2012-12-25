package jp.thicklab.kyukonavi;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.webkit.*;

public class SyllabusActivity extends Activity{
	WebView webView;

	@Override
	@android.annotation.SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.webview);
		webView = (WebView)findViewById(R.id.webview1);
		webView.setWebViewClient(new WebViewClient());
		webView.setVerticalScrollbarOverlay(true);
		setContentView(webView);
		webView.loadUrl("http://syllabus.doshisha.ac.jp/");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
