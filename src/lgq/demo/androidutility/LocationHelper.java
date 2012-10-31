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
	 * 1����ͨ���ٶȵ�ͼAPI��ȡ���ꣻDONE
	 * 2������޷���ȡ���꣬��ѡ��ʹ��Android sdk API��ȡ��DONE
	 * 3������������߶��޷���ȡ���������ã����Ը������Ƿ������Ĳ��ԣ�TODO
	 * 4��������粻���ã������Ƿ���ҪGPS���ݲ��ܽ��������� TODO
	 */
	
	private static final String TAG = "LocationHelper";
	private static final int AppMode = Constants.APP_MODE_RELEASE;
	
	// ���ඨΪ���ӱȽϺ���
	private static final LocationHelper mLocationHelperInstance = new LocationHelper();
	private LocationHelper() {
	}
	public static LocationHelper getInstance() {
		return mLocationHelperInstance;
	}
	
	// Android sdk ��λʹ�õı���
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
		 * ����λ����Ϣ���������ȡ����Ϣ��ʱ����£��������ĸ��̣߳�
		 * �����ǵ�һ�ε��û����Ժ�ĸ��¶��������߳���
		 * @param isSuc �Ƿ�ɹ���ȡ����Ϣ TODO ��Ϊ������
		 * @param lat γ�ȣ�ʧ��Ϊ0
		 * @param lon ���ȣ�ʧ��Ϊ0
		 */
		public void onGetLocationResult(boolean isSuc, double lat, double lon);
		/**
		 * ����֪ͨ���泬ʱ��������timer�̣߳���Ҫת��
		 */
		public void onGetLocationTimeout();
	}
	
	@SuppressWarnings("unused")
	public int init(final Context cxt, LocationListenerInterface listen) {
		if (cxt == null || listen == null) {
			throw new IllegalArgumentException("context ���� ������ ����ΪNULL");
		}
		
		mListenerInterface = listen;
		
		// 1, �õ�gps�ķ���
		mLocationManager = (LocationManager)cxt.getSystemService(Context.LOCATION_SERVICE);
    	if (mLocationManager == null) {
    		Log.e(TAG, "�޷���ȡLocation����");
    		return Constants.ERROR_CANT_GET_GPS_SERVICE;
    	}
    	
    	// 2, �õ�gps�Ķ�λ����
    	Criteria criteria = new Criteria();    	
    	// 3, ������ʾ����
    	criteria.setAccuracy(Criteria.ACCURACY_COARSE);    	
    	// 4, �Ƿ��ú�������
    	criteria.setAltitudeRequired(false);    	
    	// 5, �Ƿ��÷�������
    	criteria.setBearingRequired(false);    	
    	// 6, �Ƿ�������Ӫ�̼Ʒ�
    	criteria.setCostAllowed(true);
    	// 7, ���úĵ�̶�
    	criteria.setPowerRequirement(Criteria.POWER_LOW);    	
    	// 8, ��÷���provider
    	mProvider = mLocationManager.getBestProvider(criteria, true);
    	if (mProvider == null) {
    		Log.e(TAG, "�޷���ȡGPS��Ϣ");
    		return Constants.ERROR_CANT_GET_GPS_INFO;
    	}
    	
    	if (AppMode == Constants.APP_MODE_DEBUG) {
	    	Log.d(TAG, "��õ�provider�ǣ�" + mProvider + " �ܵ�provider�����ǣ�" 
	    			+ mLocationManager.getAllProviders().size());
	    	for (String tmpProvider : mLocationManager.getAllProviders()) {
	    		Log.d(TAG, "���е�provider�ǣ�" + tmpProvider);
	    	}
    	}
    	
    	// 9, �����һ����λ�� 
    	Location location = mLocationManager.getLastKnownLocation(mProvider);
    	if (location != null) {
    		Log.d(TAG, "�ϴε�λ����Ϣ�ǣ�" + location.getLongitude() + " " + location.getLatitude());
    		if (mListenerInterface != null) {
    			mListenerInterface.onGetLocationResult(true, location.getLatitude(), location.getLongitude());
    		}
    	}
    	        
        // 10, ���ö�λ������        
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
                Log.v(TAG, "λ�÷����ı䣡 �����ǣ�"  + longitude + " γ���ǣ�" + latitude);
                try {
                    //��þ���γ���ַ���
                	String msg = "";
                	msg = "���ȣ�" + longitude + "\n";
                	msg += "γ�ȣ�" + latitude + "\n";
                	//���ݾ�γ�Ȼ�øĵ��ַ��Ϣ
                	Geocoder gc = new Geocoder(cxt);
                	List<Address> addresses = gc.getFromLocation(latitude, longitude,1);
                	if (addresses.size() > 0) {
                		//��õ�ַ��Ϣ
                		msg+="��ַ: " + addresses.get(0).getAddressLine(0)+"\n";
                		//��ù�����
                		msg += "���ң�" + addresses.get(0).getCountryName()+"\n";
                		msg += "Locality��" + addresses.get(0).getLocality() + "\n";
                		msg += "FeatureName��" + addresses.get(0).getFeatureName();
                	}
                	if (mListenerInterface != null) {
                		mListenerInterface.onGetLocationResult(true, latitude, longitude);
                		if (mLocationUpdateTimer != null) {
                			mLocationUpdateTimer.cancel();
                		}
                	} else {
                		Log.w(TAG, "û�м�������");
                	}
                	Log.d(TAG, msg);
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
         };
         
         // 11, ע���������ÿ��ɨ��һ��
         mLocationManager.requestLocationUpdates(mProvider, Constants.MIN_TIME_INTEVAL_FOR_UPDATING_LOCATION
        		 , Constants.MIN_DISTANCE_FOR_UPDATING_LOCATION, mLocationListener);
         
         // ������ʱ��������ʱ��û����Ӧ�򱨴�
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
			Log.e(TAG, "δ��ʼ���ɹ��͵���refresh");
			throw new IllegalArgumentException("δ��ʼ���ɹ��͵���refresh");
		}
	}
	
	public void stop() {
		if ((mLocationManager != null) && (mLocationListener != null)) {
			mLocationManager.removeUpdates(mLocationListener);
			if (mLocationUpdateTimer != null) {
				mLocationUpdateTimer.cancel();
			}
		} else {
			Log.e(TAG, "δ��ʼ���ɹ��͵���stop");
			throw new IllegalArgumentException("δ��ʼ���ɹ��͵���stop");
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
