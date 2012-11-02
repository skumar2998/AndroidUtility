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
        
        // ��ʼ��UI�����̵߳ĸ���handler
        if (mMainHandler == null) {
        	mMainHandler = new MainActivityHandler(getMainLooper(), this);
        	if (mMainHandler == null) {
        		throw new IllegalArgumentException("�ڴ治�㣡");
        	}
        }
        
        // ���ý����¼�
        // ex1, ���û�ȡgps��Ϣ�İ�ť���¼�
        Button btnGetGpsInfo = (Button)findViewById(R.id.btnForGetGpsInfo);
        if (btnGetGpsInfo == null) {
        	throw new NoSuchElementException();
        }
        btnGetGpsInfo.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				getGpsinfoButtonClick(v);
			}
		});
        
        // ex2, ���ò��Զ�ʱ���İ�ť�¼�
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
        
        // ex3, ���û�վ��λ�İ�ť�¼�
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
     * ������ص��¼�����
     */
    // ex1, ��ȡgps��Ϣ�İ�ť���¼�
    private void getGpsinfoButtonClick(View v) {
    	if (mLocationHelper == null) {
    		mLocationHelper = LocationHelper.getInstance();
    	}
    	
    	int intRet = mLocationHelper.init(MainActivity.this, this);
    	if (intRet == Constants.ERROR_CANT_GET_GPS_SERVICE) {
    		Toast.makeText(this, "���GPS����", Toast.LENGTH_LONG).show();
    	} else if (intRet == Constants.ERROR_CANT_GET_GPS_INFO) {
    		Log.e(TAG, "�޷����GPS������Ϣ��");
    		return;
    	}
    }
    
    // ex2, ���Զ�ʱ��
    // ���һ����ʱ��
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
    					tView.setText("��ʱ����Ӧ�ˣ��߳�ID�ǣ�" + Thread.currentThread().getId() 
    							+ " �߳������ǣ�" + Thread.currentThread().getName() + " ��Ӧ�����ǣ�" + mTimerCount);
    					Log.d(TAG, "������ж�ʱ����ʱ����");
    				}
    			};
    			mTimerHelper.addTimer(mUpdateTimerRunnable, UPDATE_TIMER_INTERVAL, true);
    		} else {
    			// ��Ҫ�ظ�����
    		}
    	}
    }
    
    // �Ƴ�һ����ʱ��
    private void testTimerRmButtonClick(View v) {
    	if ((mTimerHelper != null) && (mUpdateTimerRunnable != null)) {
    		mTimerHelper.removeTimer(mUpdateTimerRunnable);
    		mUpdateTimerRunnable = null;
    	} else {
    		Log.w(TAG, "��ʱ��������Ϊnull�������Ƴ���");
    	}
    }
    
    // ex3, ��վ��λ�¼�
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
        // ��Ҫˢ��GPS��ȡ��Ϣ
        if (mLocationHelper != null) {
        	mLocationHelper.refresh();
        }
        super.onResume();
    }

	@Override
	public void onGetLocationResult(boolean isSuc, double lat, double lon) {
		Log.v(TAG, "����onGetLocationResult���߳�ID�ǣ�" + Thread.currentThread().getId() 
				+ " name: " + Thread.currentThread().getName());
		final TextView tvShowGpsInfo = (TextView)findViewById(R.id.tvForShowGpsinfo);
		if (isSuc) {
			String msg = "��ȡ��λ����Ϣ�������ǣ�" + lon + " γ���ǣ�" + lat;
			tvShowGpsInfo.setText(msg);
		} else {
			tvShowGpsInfo.setText("�޷�������ȡGPS��Ϣ");
		}
	}

	@Override
	public void onGetLocationTimeout() {
		Log.v(TAG, "����onGetLocationTimeout���߳�ID�ǣ�" + Thread.currentThread().getId() 
				+ " name: " + Thread.currentThread().getName());
		if (mMainHandler != null) {
			Message msg = Message.obtain();
			msg.what = MainActivityHandler.MSG_FOR_TIMER_UPDATE_UI;
			msg.arg1 = LocationListenerInterface.ERROR_TIMEOUT;
			mMainHandler.sendMessage(msg);
		} else {
			Log.e(TAG, "UI�޴�����");
		}
	}
	
	public void showInfo(String info) {
		Toast.makeText(this, info, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onGetLocationSuccess(Location loc) {
		// TODO Auto-generated method stub
		Log.v(TAG, "����onGetLocationSuccess���߳�ID�ǣ�" + Thread.currentThread().getId() 
				+ " name: " + Thread.currentThread().getName());
		Log.v(TAG, "λ���ǣ�" + loc.toString());
	}

	@Override
	public void onGetLocationFailue(int errCode) {
		// TODO Auto-generated method stub
		
	}
}
