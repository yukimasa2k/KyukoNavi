package jp.thicklab.kyukonavi;

import android.app.*;
import android.content.Intent;
import android.os.IBinder;

public class KyukoNaviService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
//		Toast.makeText(this, "create service", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStart(Intent intent, int StartId) {
		final NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		final Notification n = new Notification(android.R.drawable.stat_notify_sync, "休講情報が更新されました", System.currentTimeMillis());
		Intent execIntent = new Intent(KyukoNaviService.this, KyukoNaviActivity.class);
		PendingIntent service = PendingIntent.getActivity(this, 0, execIntent, 0);
		n.setLatestEventInfo(getApplicationContext(), "休講ナビ", "表示するにはここをタップ",service);
		n.flags = Notification.FLAG_AUTO_CANCEL;
		nm.notify(R.string.app_name, n);
		stopSelf();
	}

	@Override
	public void onDestroy() {
//		Toast.makeText(this, "destroy service", Toast.LENGTH_SHORT).show();
	}

}