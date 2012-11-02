package lgq.demo.androidutility;

public class Constants {
	public static final int APP_MODE_RELEASE = 0;
	public static final int APP_MODE_DEBUG = 1;
	public static final int SUCCESS = 0;
	public static final int ERROR_CANT_GET_GPS_SERVICE = -1000;
	public static final int ERROR_CANT_GET_GPS_INFO = -1000;
	
	/**
	 * 用于gps定位的常量
	 */
	public static final int MIN_TIME_INTEVAL_FOR_UPDATING_LOCATION = 0; // 1s
	public static final int MIN_DISTANCE_FOR_UPDATING_LOCATION = 0; // 1m
	public static final int LOCATION_UPDATE_TIMER = 60000;
	
	/**
	 * 用于基站定位的常量
	 */
	public static final int CHECK_APN_LOCATION_TIMER = 2 * 60 * 1000; // 2min
	public static final String GOOGLE_LOCATION_REQ_URL = "http://www.google.com/loc/json";
	public static final String GOOGLE_LOCATION_VERSION_KEY = "version";
	public static final String GOOGLE_LOCATION_VERSION_VALUE = "1.1.0";
	public static final String GOOGLE_LOCATION_HOST_KEY = "host";
	public static final String GOOGLE_LOCATION_HOST_VALUE = "maps.google.com";
	public static final String GOOGLE_LOCATION_HOME_CNTRY_CODE_KEY = "home_mobile_country_code";
	public static final String GOOGLE_LOCATION_HOME_NET_CODE_KEY = "home_mobile_network_code";
	public static final String GOOGLE_LOCATION_RADIO_TYPE_KEY = "radio_type";
	public static final String GOOGLE_LOCATION_REQ_ADDR_KEY = "request_address";
	public static final String GOOGLE_LOCATION_ERROR_CODE = "460";
	public static final String GOOGLE_LOCATION_ADDR_LANG_KEY = "address_language";
	public static final String GOOGLE_LOCATION_ADDR_LANG_VALUE_CN = "zh_CN";
	public static final String GOOGLE_LOCATION_ADDR_LANG_VALUE_EN = "en_US";
	public static final String GOOGLE_LOCATION_CELL_ID_KEY = "cell_id";
	public static final String GOOGLE_LOCATION_CNTRY_CODE_KEY = "mobile_country_code";
	public static final String GOOGLE_LOCATION_NET_CODE_KEY = "mobile_network_code";
	public static final String GOOGLE_LOCATION_AGE_KEY = "age";
	public static final String GOOGLE_LOCATION_CELL_TOWER_KEY = "cell_towers";
	public static final String GOOGLE_LOCATION_AREA_CODE_KEY = "location_area_code";
	public static final String GOOGLE_LOCATION_MAC_ADDR_KEY = "mac_address";
	public static final String GOOGLE_LOCATION_SINGAL_STRENGTH_KEY = "signal_strength";
	public static final String GOOGLE_LOCATION_WIFI_TOWER_KEY = "wifi_towers";
	public static final String GOOGLE_LOCATION_KEY = "location";
	public static final String GOOGLE_LOCATION_LANTITUDE_KEY = "latitude";
	public static final String GOOGLE_LOCATION_LANGITUDE_KEY = "longitude";
	public static final String GOOGLE_LOCATION_ACCURACY_KEY = "accuracy";
}
