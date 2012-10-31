package lgq.demo.androidutility;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationHelper {

	/**
	 * 1、先通过百度地图API获取坐标；DONE
	 * 2、如果无法获取坐标，则选择使用Android sdk API获取；DONE
	 * 3、如果以上两者都无法获取，网络能用，则尝试根据我们服务器的策略：TODO
	 * 4、如果网络不可用，讨论是否需要GPS数据才能进行组网； TODO
	 */
	
	private static final String TAG = "LocationHelper";
	private static final int AppMode = Constants.APP_MODE_RELEASE;
	
	// 本类定为单子比较合适
	private static final LocationHelper mLocationHelperInstance = new LocationHelper();
	private LocationHelper() {
	}
	public static LocationHelper getInstance() {
		return mLocationHelperInstance;
	}
	
	// Android sdk 定位使用的变量
	private LocationListenerInterface mListenerInterface = null;
	private LocationManager mLocationManager = null;
	private LocationListener mLocationListener = null; 
	private String mProvider = null;
	private Timer mLocationUpdateTimer = null;
	
	public interface LocationListenerInterface {
		public static final int ERROR_TIMEOUT = -1;
		public static final int ERROR_NO_SERVICE = -2;
		public static final int ERROR_NO_GPS_DATA = -3;
		
		/**
		 * 返回位置信息，当本类获取到信息的时候更新，运行于哪个线程？
		 * 无论是第一次调用还是以后的更新都是在主线程中
		 * @param isSuc 是否成功获取到信息 TODO 更为错误码
		 * @param lat 纬度，失败为0
		 * @param lon 经度，失败为0
		 */
		public void onGetLocationResult(boolean isSuc, double lat, double lon);
		/**
		 * 用于通知界面超时，运行于timer线程，需要转换
		 */
		public void onGetLocationTimeout();
	}
	
	@SuppressWarnings("unused")
	public int init(final Context cxt, LocationListenerInterface listen) {
		if (cxt == null || listen == null) {
			throw new IllegalArgumentException("context 或者 监听器 参数为NULL");
		}
		
		mListenerInterface = listen;
		
		// 1, 得到gps的服务
		mLocationManager = (LocationManager)cxt.getSystemService(Context.LOCATION_SERVICE);
    	if (mLocationManager == null) {
    		Log.e(TAG, "无法获取Location服务！");
    		return Constants.ERROR_CANT_GET_GPS_SERVICE;
    	}
    	
    	// 2, 得到gps的定位配置
    	Criteria criteria = new Criteria();    	
    	// 3, 设置显示精度
    	criteria.setAccuracy(Criteria.ACCURACY_COARSE);    	
    	// 4, 是否获得海拔数据
    	criteria.setAltitudeRequired(false);    	
    	// 5, 是否获得方向数据
    	criteria.setBearingRequired(false);    	
    	// 6, 是否允许运营商计费
    	criteria.setCostAllowed(true);
    	// 7, 设置耗电程度
    	criteria.setPowerRequirement(Criteria.POWER_LOW);    	
    	// 8, 获得服务provider
    	mProvider = mLocationManager.getBestProvider(criteria, true);
    	if (mProvider == null) {
    		Log.e(TAG, "无法获取GPS信息");
    		return Constants.ERROR_CANT_GET_GPS_INFO;
    	}
    	
    	if (AppMode == Constants.APP_MODE_DEBUG) {
	    	Log.d(TAG, "获得的provider是：" + mProvider + " 总的provider个数是：" 
	    			+ mLocationManager.getAllProviders().size());
	    	for (String tmpProvider : mLocationManager.getAllProviders()) {
	    		Log.d(TAG, "所有的provider是：" + tmpProvider);
	    	}
    	}
    	
    	// 9, 获得上一个定位点 
    	Location location = mLocationManager.getLastKnownLocation(mProvider);
    	if (location != null) {
    		Log.d(TAG, "上次的位置信息是：" + location.getLongitude() + " " + location.getLatitude());
    		if (mListenerInterface != null) {
    			mListenerInterface.onGetLocationResult(true, location.getLatitude(), location.getLongitude());
    		}
    	}
    	        
        // 10, 设置定位监听器        
        mLocationListener = new LocationListener(){
            
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            	Log.v(TAG, "onStatusChanged");
            }
           
            @Override
            public void onProviderEnabled(String provider) {
            	Log.v(TAG, "onProviderEnabled");
            }
           
            @Override
            public void onProviderDisabled(String provider) {
            	Log.v(TAG, "onProviderDisabled");
            }
           
            @Override
            public void onLocationChanged(Location location) {
                Double latitude=location.getLatitude();
                Double longitude=location.getLongitude();
                Log.v(TAG, "位置发生改变！ 经度是："  + longitude + " 纬度是：" + latitude);
                try {
                    //获得精度纬度字符串
                	String msg = "";
                	msg = "经度：" + longitude + "\n";
                	msg += "纬度：" + latitude + "\n";
                	//根据经纬度获得改点地址信息
                	Geocoder gc = new Geocoder(cxt);
                	List<Address> addresses = gc.getFromLocation(latitude, longitude,1);
                	if (addresses.size() > 0) {
                		//获得地址信息
                		msg+="地址: " + addresses.get(0).getAddressLine(0)+"\n";
                		//获得国家名
                		msg += "国家：" + addresses.get(0).getCountryName()+"\n";
                		msg += "Locality：" + addresses.get(0).getLocality() + "\n";
                		msg += "FeatureName：" + addresses.get(0).getFeatureName();
                	}
                	if (mListenerInterface != null) {
                		mListenerInterface.onGetLocationResult(true, latitude, longitude);
                		if (mLocationUpdateTimer != null) {
                			mLocationUpdateTimer.cancel();
                		}
                	} else {
                		Log.w(TAG, "没有监听对象！");
                	}
                	Log.d(TAG, msg);
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
         };
         
         // 11, 注册监听器，每秒扫描一次
         mLocationManager.requestLocationUpdates(mProvider, Constants.MIN_TIME_INTEVAL_FOR_UPDATING_LOCATION
        		 , Constants.MIN_DISTANCE_FOR_UPDATING_LOCATION, mLocationListener);
         
         // 开启定时器，若长时间没有响应则报错
         mLocationUpdateTimer = new Timer();
         if (mLocationUpdateTimer != null) {
             mLocationUpdateTimer.schedule(new TimerTask() {
     			
     			@Override
     			public void run() {
     				onLocationUpdateTimeout();
     			}
     		}, Constants.LOCATION_UPDATE_TIMER, Constants.LOCATION_UPDATE_TIMER);
         }
		
		return Constants.SUCCESS;
	}
	
	public void uninit() {
		if ((mLocationManager != null) && (mLocationListener != null)) {
			mLocationManager.removeUpdates(mLocationListener);
			if (mLocationUpdateTimer != null) {
				mLocationUpdateTimer.cancel();
			}
			mLocationListener = null;
			mLocationManager = null;
		}
	}
	
	public void refresh() {
		if ((mLocationManager != null) && (mLocationListener != null)) {
			mLocationManager.requestLocationUpdates(mProvider, Constants.MIN_TIME_INTEVAL_FOR_UPDATING_LOCATION
	       		 , Constants.MIN_DISTANCE_FOR_UPDATING_LOCATION, mLocationListener);
		} else {
			Log.e(TAG, "未初始化成功就调用refresh");
			throw new IllegalArgumentException("未初始化成功就调用refresh");
		}
	}
	
	public void stop() {
		if ((mLocationManager != null) && (mLocationListener != null)) {
			mLocationManager.removeUpdates(mLocationListener);
			if (mLocationUpdateTimer != null) {
				mLocationUpdateTimer.cancel();
			}
		} else {
			Log.e(TAG, "未初始化成功就调用stop");
			throw new IllegalArgumentException("未初始化成功就调用stop");
		}
	}
	
	public void start() {
		refresh();
	}
	
	private void onLocationUpdateTimeout() {
		if (mListenerInterface != null) {
			mListenerInterface.onGetLocationTimeout();
		}
	}
	
	public void changeToLocateByCellTower() {
		
	}
}
