package com.thorn.wechatpaydemo.polling;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**    
 * 项目名称：PayDemo   
 * 类名称：PollingService   
 * 类描述：   
 * 创建人：lc   
 * 创建时间：2015-3-13 上午10:00:26   
 * 修改人：131   
 * 修改时间：2015-3-13 上午10:00:26   
 * 修改备注：   
 * @version    
 *    
 */
public class PollingService extends Service {
	public static final String ACTION = PollingService.class.getName();
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		new PollingThread().start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("mess", "Service : onDestory");
	}
	
	/**
	 * Polling Thread
	 * 模拟向Server轮询的异步线程
	 * */
	int count =0;
	class PollingThread extends Thread{

		@Override
		public void run() {
			count++;
			Log.i("mess", "---polling---------"+count+System.currentTimeMillis());
			//当计数能被5整除时，弹出通知
			if(count  == 5){
				PollingUtils.stopPollingService(getApplicationContext(), PollingService.class, PollingService.ACTION);
				Log.i("mess", "----new message----");
			}
		}
		
	}
	
}


