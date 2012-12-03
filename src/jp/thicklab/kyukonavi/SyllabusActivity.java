package jp.thicklab.kyukonavi;

import android.app.Activity;
import android.content.Intent;
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
//		Intent i = getIntent();
//		String query = "javascript:void(document.forms[0].keyword.value='" + i.getStringExtra("keyword") + "')";
		webView = (WebView)findViewById(R.id.webview1);
		webView.setWebViewClient(new WebViewClient());
		webView.setVerticalScrollbarOverlay(true);
//		webView.getSettings().setJavaScriptEnabled(true); // JavaScriptを使えるようにする
		setContentView(webView);
		webView.loadUrl("http://syllabus.doshisha.ac.jp/");
//		webView.loadUrl(query);
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
