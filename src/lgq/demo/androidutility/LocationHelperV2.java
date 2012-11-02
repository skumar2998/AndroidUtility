package lgq.demo.androidutility;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;


public class LocationHelperV2 extends AsyncTask<Void, Void, Location> {
	private static final String TAG = "LocationHelperV2";
	
	public interface LocationHelperV2CallBack {
		public static final int ERROR_TIMEOUT = -1;
		public static final int ERROR_OTHER = -2;
		
		public void onGetLocationSuccess(Location loc);
		public void onGetLocationFailue(int errCode);
	}

	private Context mContext = null;
	private LocationHelperV2CallBack mLocationCallBack = null;
	private Timer mCheckTimer = null;
	
	public LocationHelperV2(Context context, LocationHelperV2CallBack callBack) {
		if (context == null || callBack == null) {
			throw new IllegalArgumentException("����ΪNUL��");
		}
		mContext = context;
		mLocationCallBack = callBack;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		if (mCheckTimer == null) {
			mCheckTimer = new Timer();
			mCheckTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					cancelTask();
				}
			}, Constants.CHECK_APN_LOCATION_TIMER);
		}
		
	}
	
	
	@Override
	protected void onPostExecute(Location result) {
		super.onPostExecute(result);
		if (result != null) {
			cancelTimer();
			if (mLocationCallBack != null) {
				mLocationCallBack.onGetLocationSuccess(result);
			}
		}
	}

	@Override
	protected Location doInBackground(Void... params) {
		Log.v(TAG, "��̨���������ˣ�");
		Looper.prepare(); // fix bug��PhoneStateListener ���������ҪLooper�����Ա���������߳��д���looper
		
		if (mContext != null) {
			ApnLocationUtility apnUtility = new ApnLocationUtility(mContext);
			return apnUtility.getGpsByAPN();
		} else {
			throw new RuntimeException("ContextΪNULL");
		}
	}

	public void cancelTask() {
		super.cancel(true);
	}
	
	public void cancelTimer() {
		mCheckTimer.cancel();
	}
}
