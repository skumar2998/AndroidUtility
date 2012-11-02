package lgq.demo.androidutility;

import java.util.NoSuchElementException;

import lgq.demo.androidutility.LocationHelper.LocationListenerInterface;
import lgq.demo.androidutility.LocationHelperV2.LocationHelperV2CallBack;
import lgq.demo.androidutility.TimerHelper.CycleRunnable;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListenerInterface
	, LocationHelperV2CallBack {
	private static final String TAG = "MainActivity";
	
	private MainActivityHandler mMainHandler = null;
	// FOR getting gps info.
	private LocationHelper mLocationHelper = null;
	// FOR setting timer.
	private TimerHelper mTimerHelper = null;
	private CycleRunnable mUpdateTimerRunnable = null;
	private static final long UPDATE_TIMER_INTERVAL = 1000; // 1000ms
	private int mTimerCount = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化UI界面线程的更新handler
        if (mMainHandler == null) {
        	mMainHandler = new MainActivityHandler(getMainLooper(), this);
        	if (mMainHandler == null) {
        		throw new IllegalArgumentException("内存不足！");
        	}
        }
        
        // 设置界面事件
        // ex1, 设置获取gps信息的按钮的事件
        Button btnGetGpsInfo = (Button)findViewById(R.id.btnForGetGpsInfo);
        if (btnGetGpsInfo == null) {
        	throw new NoSuchElementException();
        }
        btnGetGpsInfo.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				getGpsinfoButtonClick(v);
			}
		});
        
        // ex2, 设置测试定时器的按钮事件
        Button btnForTestTimer = (Button)findViewById(R.id.btnForTestTimer);
        if (btnForTestTimer == null) {
        	throw new NoSuchElementException();
        }
        btnForTestTimer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				testTimerAddButtonClick(v);
			}
		});
        
        Button btnForRmTimer = (Button)findViewById(R.id.btnRemoveTimer);
        if (btnForRmTimer == null) {
        	throw new NoSuchElementException();
        }
        btnForRmTimer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				testTimerRmButtonClick(v);
			}
		});
        
        // ex3, 设置基站定位的按钮事件
        Button btnForApn = (Button)findViewById(R.id.btnForAPN);
        if (btnForApn == null) {
        	throw new NoSuchElementException();
        }
        btnForApn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				apnButtonClick(v);
			}
		});
    }
    
    /**
     * 界面相关的事件函数
     */
    // ex1, 获取gps信息的按钮的事件
    private void getGpsinfoButtonClick(View v) {
    	if (mLocationHelper == null) {
    		mLocationHelper = LocationHelper.getInstance();
    	}
    	
    	int intRet = mLocationHelper.init(MainActivity.this, this);
    	if (intRet == Constants.ERROR_CANT_GET_GPS_SERVICE) {
    		Toast.makeText(this, "请打开GPS服务！", Toast.LENGTH_LONG).show();
    	} else if (intRet == Constants.ERROR_CANT_GET_GPS_INFO) {
    		Log.e(TAG, "无法获得GPS数据信息！");
    		return;
    	}
    }
    
    // ex2, 测试定时器
    // 添加一个定时器
    private void testTimerAddButtonClick(View v) {
    	mTimerHelper = new TimerHelper(mMainHandler);
    	if (mTimerHelper != null) {
    		if (mUpdateTimerRunnable == null) {
    			mUpdateTimerRunnable = mTimerHelper.new CycleRunnable(UPDATE_TIMER_INTERVAL) {
    				@Override
    				public void run() {
    					super.run();
    					mTimerCount++;
    					TextView tView = (TextView)findViewById(R.id.tvForShowTimer);
    					tView.setText("定时器响应了，线程ID是：" + Thread.currentThread().getId() 
    							+ " 线程名字是：" + Thread.currentThread().getName() + " 响应次数是：" + mTimerCount);
    					Log.d(TAG, "这里进行定时器超时处理");
    				}
    			};
    			mTimerHelper.addTimer(mUpdateTimerRunnable, UPDATE_TIMER_INTERVAL, true);
    		} else {
    			// 不要重复加入
    		}
    	}
    }
    
    // 移除一个定时器
    private void testTimerRmButtonClick(View v) {
    	if ((mTimerHelper != null) && (mUpdateTimerRunnable != null)) {
    		mTimerHelper.removeTimer(mUpdateTimerRunnable);
    		mUpdateTimerRunnable = null;
    	} else {
    		Log.w(TAG, "定时器辅助类为null，不许移除！");
    	}
    }
    
    // ex3, 基站定位事件
    private void apnButtonClick(View v) {
    	LocationHelperV2 locationHelperV2 = new LocationHelperV2(this, this);
    	locationHelperV2.execute((Void)null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public void onDestroy() {
    	if (mLocationHelper != null) {
    		mLocationHelper.uninit();
    	}
    	
    	if ((mTimerHelper != null) && mUpdateTimerRunnable != null) {
    		mTimerHelper.removeTimer(mUpdateTimerRunnable);
    		mTimerHelper = null;
    	}
    	super.onDestroy();    	
    }
    
    @Override
    protected void onResume() {
        // 需要刷新GPS获取信息
        if (mLocationHelper != null) {
        	mLocationHelper.refresh();
        }
        super.onResume();
    }

	@Override
	public void onGetLocationResult(boolean isSuc, double lat, double lon) {
		Log.v(TAG, "调用onGetLocationResult，线程ID是：" + Thread.currentThread().getId() 
				+ " name: " + Thread.currentThread().getName());
		final TextView tvShowGpsInfo = (TextView)findViewById(R.id.tvForShowGpsinfo);
		if (isSuc) {
			String msg = "获取到位置信息，经度是：" + lon + " 纬度是：" + lat;
			tvShowGpsInfo.setText(msg);
		} else {
			tvShowGpsInfo.setText("无法正常获取GPS信息");
		}
	}

	@Override
	public void onGetLocationTimeout() {
		Log.v(TAG, "调用onGetLocationTimeout，线程ID是：" + Thread.currentThread().getId() 
				+ " name: " + Thread.currentThread().getName());
		if (mMainHandler != null) {
			Message msg = Message.obtain();
			msg.what = MainActivityHandler.MSG_FOR_TIMER_UPDATE_UI;
			msg.arg1 = LocationListenerInterface.ERROR_TIMEOUT;
			mMainHandler.sendMessage(msg);
		} else {
			Log.e(TAG, "UI无处理句柄");
		}
	}
	
	public void showInfo(String info) {
		Toast.makeText(this, info, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onGetLocationSuccess(Location loc) {
		// TODO Auto-generated method stub
		Log.v(TAG, "调用onGetLocationSuccess，线程ID是：" + Thread.currentThread().getId() 
				+ " name: " + Thread.currentThread().getName());
		Log.v(TAG, "位置是：" + loc.toString());
	}

	@Override
	public void onGetLocationFailue(int errCode) {
		// TODO Auto-generated method stub
		
	}
}
