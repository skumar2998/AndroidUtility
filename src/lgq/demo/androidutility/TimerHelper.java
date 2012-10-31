package lgq.demo.androidutility;

import android.os.Handler;

/**
 * ��ʱ��������
 * �ŵ��ǣ�����Ҫ�����߳�ת������Ϊ�����ִ���������߳���
 * ȱ���ǣ����߳���Ϣ���еĴ����ӳٻ�Զ�ʱ���ļ��ʱ����Ӱ�죬���ŶӴ���ʱ����ۼӵ������ʱ����
 * TODO��������ȱ��
 * @author admin
 *
 */
public class TimerHelper {
	private Handler mHandler = null;
	
	public TimerHelper(Handler handler) {
		if (handler == null) {
			throw new IllegalArgumentException("��ʼ��TimerHelper�쳣��");
		}
		
		mHandler = handler;
	}
	
	public class CycleRunnable implements Runnable {
		private long mDelayMs = 0;
		
		public CycleRunnable(long delayMills) {
			mDelayMs = delayMills;
		}
				
		@Override
		public void run() {
			if (mHandler != null) {
				mHandler.postDelayed(this, mDelayMs);
			}
		}
		
		public final long getDelayMills() {
			return mDelayMs;
		}
		
		public void setDelayMills(long delayMills) {
			mDelayMs = delayMills;
		}
	}
	
	/**
	 * ��Ӷ�ʱ��
	 * @param runnable 
	 * @param delayMillis ����������ԵĶ�ʱ������ʹ�øò��������Ǵ�runnable�����л�ȡ
	 * @param isCycle
	 */
	public void addTimer(Runnable runnable, long delayMillis, boolean isCycle) {
		if (runnable == null) {
			throw new IllegalArgumentException("runnabel������Ϊnull");
		}
		if (mHandler == null) {
			throw new IllegalArgumentException("δ��ʼ��TiemrHelper��");
		}
		if (isCycle) {
			// ������Ǵ����͵Ķ���������ܻ��׳��쳣
			CycleRunnable cycleRunnable = (CycleRunnable)runnable;
			if (cycleRunnable != null) {
				mHandler.postDelayed(cycleRunnable, cycleRunnable.getDelayMills());
			}
		} else {
			mHandler.postDelayed(runnable, delayMillis);
		}
	}
	
	/**
	 * �Ƴ���ʱ������
	 * @param runnable
	 */
	public void removeTimer(Runnable runnable) {
		if (runnable == null) {
			throw new IllegalArgumentException("runnabel������Ϊnull");
		}
		if (mHandler == null) {
			throw new IllegalArgumentException("δ��ʼ��TiemrHelper��");
		}
		
		mHandler.removeCallbacks(runnable);
	}
}
