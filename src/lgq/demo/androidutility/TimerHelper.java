package lgq.demo.androidutility;

import android.os.Handler;

/**
 * 定时器辅助类
 * 优点是：不需要进行线程转换，因为这里的执行是在主线程中
 * 缺点是：主线程消息队列的处理延迟会对定时器的间隔时间有影响，即排队处理时间会累加到这个定时器上
 * TODO：解决这个缺点
 * @author admin
 *
 */
public class TimerHelper {
	private Handler mHandler = null;
	
	public TimerHelper(Handler handler) {
		if (handler == null) {
			throw new IllegalArgumentException("初始化TimerHelper异常！");
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
	 * 添加定时器
	 * @param runnable 
	 * @param delayMillis 如果是周期性的定时器，不使用该参数，而是从runnable对象中获取
	 * @param isCycle
	 */
	public void addTimer(Runnable runnable, long delayMillis, boolean isCycle) {
		if (runnable == null) {
			throw new IllegalArgumentException("runnabel对象不能为null");
		}
		if (mHandler == null) {
			throw new IllegalArgumentException("未初始化TiemrHelper！");
		}
		if (isCycle) {
			// 如果不是此类型的对象，这里可能会抛出异常
			CycleRunnable cycleRunnable = (CycleRunnable)runnable;
			if (cycleRunnable != null) {
				mHandler.postDelayed(cycleRunnable, cycleRunnable.getDelayMills());
			}
		} else {
			mHandler.postDelayed(runnable, delayMillis);
		}
	}
	
	/**
	 * 移除定时器对象
	 * @param runnable
	 */
	public void removeTimer(Runnable runnable) {
		if (runnable == null) {
			throw new IllegalArgumentException("runnabel对象不能为null");
		}
		if (mHandler == null) {
			throw new IllegalArgumentException("未初始化TiemrHelper！");
		}
		
		mHandler.removeCallbacks(runnable);
	}
}
