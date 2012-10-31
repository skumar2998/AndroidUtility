package lgq.demo.androidutility;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * 负责UI相关事件的更新
 * @author admin
 *
 */
public class MainActivityHandler extends Handler {
	private static final String TAG = "MainActivityHandler";
	public static final int MSG_FOR_TIMER_UPDATE_UI = 1; // 用于定时器线程更新UI界面
	
	private MainActivity mUIActivity = null;
	
	public MainActivityHandler(Looper looper, MainActivity uiActivity) {
		super(looper);
		if ((looper == null) || (uiActivity == null)) {
			throw new IllegalArgumentException("looper is null or uiActivity is null");
		}
		mUIActivity = uiActivity;
	}
	
	@Override
	public void handleMessage(Message msg) {
		Log.v(TAG, "handleMessage, what is: " + msg.what);
		switch (msg.what) {
		case MSG_FOR_TIMER_UPDATE_UI:
			if (mUIActivity != null) {
				mUIActivity.showInfo("获取GPS信息超时！");
			}
			break;

		default:
			break;
		}
	}
}
