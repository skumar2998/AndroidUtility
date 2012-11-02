package lgq.demo.androidutility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class ApnLocationUtility {
	public class CellIDInfo {
		public int cellId;
		public String mobileCountryCode;
		public String mobileNetworkCode;
		public int locationAreaCode;
		public String radioType;

		public CellIDInfo() {
		}
	}

	public class WifiInfo {
		public String mac;

		public WifiInfo() {
		}
	}

	private static final String TAG = "LocationHelperByAPN";
	private TelephonyManager manager = null;
	private PhoneStateListener listener = null;
	private GsmCellLocation gsm = null;
	private CdmaCellLocation cdma = null;
	int lac;
	String current_ci, mcc, mnc;
	private Context mContext = null;
	private ArrayList<CellIDInfo> mCellIDs = new ArrayList<CellIDInfo>();
	

	public ApnLocationUtility(Context context) {
		mContext = context;
	}

	/**
	 * 获取本地所处的基站信息
	 * @param context
	 * @return 基站ID信息
	 */
	private ArrayList<CellIDInfo> getCellIDInfo(Context context) {
		if (manager == null) {
			manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (manager == null) {
				Log.e(TAG, "获取服务：TELEPHONY_SERVICE 为空!");
				return null;
			}
		} else {
			return mCellIDs;
		}
		
		listener = new PhoneStateListener();
		manager.listen(listener, 0);
		
		int type = manager.getNetworkType();
		Log.d(TAG, "获得网络类型是：" + type);

		if (type == TelephonyManager.NETWORK_TYPE_GPRS
				|| type == TelephonyManager.NETWORK_TYPE_EDGE
				|| type == TelephonyManager.NETWORK_TYPE_HSDPA) {
			gsm = ((GsmCellLocation) manager.getCellLocation());
			Log.d(TAG, "gsm 位置：" + gsm.toString());
			if (gsm == null) {
				return null;
			}
			// 区号
			lac = gsm.getLac();
			// getNetworkOperator()
			// Returns the numeric name (MCC+MNC) of current registered operator
			final int MCC_START_POS = 0, MCC_END_POS = 3;
			final int MNC_START_POS = 3, MNC_END_POS = 5;
			mcc = manager.getNetworkOperator().substring(MCC_START_POS, MCC_END_POS);
			mnc = manager.getNetworkOperator().substring(MNC_START_POS, MNC_END_POS);

			CellIDInfo currentCell = new CellIDInfo();
			currentCell.cellId = gsm.getCid();
			currentCell.mobileCountryCode = mcc;
			currentCell.mobileNetworkCode = mnc;
			currentCell.locationAreaCode = lac;
			currentCell.radioType = "gsm";
			if (mCellIDs != null) {
				mCellIDs.add(currentCell);
			} else {
				return null;
			}

			List<NeighboringCellInfo> list = manager.getNeighboringCellInfo();
			int size = list.size();
			for (int i = 0; i < size; i++) {
				CellIDInfo info = new CellIDInfo();
				info.cellId = list.get(i).getCid();
				info.mobileCountryCode = mcc;
				info.mobileCountryCode = mnc;
				info.locationAreaCode = lac;
				if (mCellIDs != null) {
					mCellIDs.add(info);
				} else {
					return null;
				}
			}
			return mCellIDs;

		} else if (type == TelephonyManager.NETWORK_TYPE_CDMA
				|| type == TelephonyManager.NETWORK_TYPE_1xRTT) {
			cdma = ((CdmaCellLocation) manager.getCellLocation());
			if (cdma == null)
				return null;

			final String ERROR_CODE_OF_SIMOP = "460";
			if (ERROR_CODE_OF_SIMOP.equals(manager.getSimOperator().substring(0, 3)))
				return null;
		}
		
		return null;
	}

	/**
	 * 调用google的gear服务，获取基站对应的位置信息
	 * @param wifi：wifi列表
	 * @param cellID：基站列表
	 * @return
	 */
	private Location getLocationFromGear(ArrayList<WifiInfo> wifi,
			ArrayList<CellIDInfo> cellID) {

		// wifi 信号可以为null
		if (cellID == null) {
			throw new IllegalArgumentException("基站ID为null");
		}

		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(Constants.GOOGLE_LOCATION_REQ_URL);
		JSONObject holder = new JSONObject();
		try {
			holder.put(Constants.GOOGLE_LOCATION_VERSION_KEY, Constants.GOOGLE_LOCATION_VERSION_VALUE);
			holder.put(Constants.GOOGLE_LOCATION_HOST_KEY, Constants.GOOGLE_LOCATION_HOST_VALUE);
			holder.put(Constants.GOOGLE_LOCATION_HOME_CNTRY_CODE_KEY, cellID.get(0).mobileCountryCode);
			holder.put(Constants.GOOGLE_LOCATION_HOME_NET_CODE_KEY, cellID.get(0).mobileNetworkCode);
			holder.put(Constants.GOOGLE_LOCATION_RADIO_TYPE_KEY, cellID.get(0).radioType);
			holder.put(Constants.GOOGLE_LOCATION_REQ_ADDR_KEY, true);
			if (Constants.GOOGLE_LOCATION_ERROR_CODE.equals(cellID.get(0).mobileCountryCode)) {
				holder.put(Constants.GOOGLE_LOCATION_ADDR_LANG_KEY, 
						Constants.GOOGLE_LOCATION_ADDR_LANG_VALUE_CN);
			} else {
				holder.put(Constants.GOOGLE_LOCATION_ADDR_LANG_KEY, 
						Constants.GOOGLE_LOCATION_ADDR_LANG_VALUE_EN);
			}

			JSONObject data = null, current_data = null;
			JSONArray array = new JSONArray();
			current_data = new JSONObject();
			current_data.put(Constants.GOOGLE_LOCATION_CELL_ID_KEY, cellID.get(0).cellId);
			current_data.put(Constants.GOOGLE_LOCATION_CNTRY_CODE_KEY, cellID.get(0).mobileCountryCode);
			current_data.put(Constants.GOOGLE_LOCATION_NET_CODE_KEY, cellID.get(0).mobileNetworkCode);
			current_data.put(Constants.GOOGLE_LOCATION_AGE_KEY, 0);
			array.put(current_data);

			if (cellID.size() > 2) {
				for (int i = 1; i < cellID.size(); i++) {
					data = new JSONObject();
					data.put(Constants.GOOGLE_LOCATION_CELL_ID_KEY, cellID.get(i).cellId);
					data.put(Constants.GOOGLE_LOCATION_AREA_CODE_KEY, cellID.get(0).locationAreaCode);
					data.put(Constants.GOOGLE_LOCATION_CNTRY_CODE_KEY, cellID.get(0).mobileCountryCode);
					data.put(Constants.GOOGLE_LOCATION_NET_CODE_KEY, cellID.get(0).mobileNetworkCode);
					data.put(Constants.GOOGLE_LOCATION_AGE_KEY, 0);
					array.put(data);
				}
			}
			holder.put(Constants.GOOGLE_LOCATION_CELL_TOWER_KEY, array);

			if (wifi != null) {
				if (wifi.get(0).mac != null) {
					data = new JSONObject();
					data.put(Constants.GOOGLE_LOCATION_NET_CODE_KEY, wifi.get(0).mac);
					data.put(Constants.GOOGLE_LOCATION_SINGAL_STRENGTH_KEY, 8);
					data.put(Constants.GOOGLE_LOCATION_AGE_KEY, 0);
					array = new JSONArray();
					array.put(data);
					holder.put(Constants.GOOGLE_LOCATION_WIFI_TOWER_KEY, array);
				}
			}

			StringEntity se = new StringEntity(holder.toString());
			Log.e(TAG, "位置发送：" + holder.toString());
			post.setEntity(se);
			HttpResponse resp = client.execute(post);
			HttpEntity entity = resp.getEntity();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(
					entity.getContent()));
			StringBuffer sb = new StringBuffer();
			String result = br.readLine();
			while (result != null) {
				Log.e(TAG, "位置接收：" + result);
				sb.append(result);
				result = br.readLine();
			}

			data = new JSONObject(sb.toString());
			data = (JSONObject) data.get(Constants.GOOGLE_LOCATION_KEY);

			Location loc = new Location(LocationManager.NETWORK_PROVIDER);
			loc.setLatitude((Double) data.get(Constants.GOOGLE_LOCATION_LANTITUDE_KEY));
			loc.setLongitude((Double) data.get(Constants.GOOGLE_LOCATION_LANGITUDE_KEY));
			loc.setAccuracy(Float.parseFloat(data.get(Constants.GOOGLE_LOCATION_ACCURACY_KEY).toString()));
			loc.setTime(System.currentTimeMillis());
			Log.d(TAG, "位置是：" + loc.toString());
			return loc;
		} catch (JSONException e) {
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Location getGpsByAPN() {
		ArrayList<CellIDInfo> cellIds = getCellIDInfo(mContext);
		return getLocationFromGear(null, cellIds);
	}
}
