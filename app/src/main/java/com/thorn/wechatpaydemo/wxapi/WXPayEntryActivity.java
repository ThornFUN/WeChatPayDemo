package com.thorn.wechatpaydemo.wxapi;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.thorn.wechatpaydemo.Constants;
import com.thorn.wechatpaydemo.R;

/**    
 * 项目名称：PayDemo   
 * 类名称：WXPayEntryActivity   
 * 类描述：   
 * 创建人：lc   
 * 创建时间：2015-3-9 下午3:59:57   
 * 修改人：131   
 * 修改时间：2015-3-9 下午3:59:57   
 * 修改备注：   
 * @version    
 *    
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	private IWXAPI api;
	private static final String TAG = "mess";
	private TextView message;
	private boolean isPaySuccess = false;
	
	private Handler handler = new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(0, 0);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wxpay);
		message = (TextView) findViewById(R.id.message);
		api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
		api.handleIntent(getIntent(), this);
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}


	@Override
	public void onReq(BaseReq req) {
		
	}
	/**
	 * 在这里处理支付结果的通知和下一步界面操作，
	 * 注意：客户端返回的支付结果不能作为最终支付的可信结果 ，应该以服务端的支付结果通知为准
	 * 
	 * */
	@Override
	public void onResp(BaseResp resp) {
		if(resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX){
			/*while(!isPaySuccess){
				//跟服务器请求数据，如果成功，isPaySuccess=true,跳出循环；否则一直请求
			}*/
			Log.i("mess", "----onResp---startService----");
//			PollingUtils.startPollingService(this, 5, PollingService.class, PollingService.ACTION);
			switch(resp.errCode){
			case BaseResp.ErrCode.ERR_COMM:
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
			case BaseResp.ErrCode.ERR_SENT_FAILED:
			case BaseResp.ErrCode.ERR_UNSUPPORT:
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
			{
				Log.i(TAG, "----------用户取消支付------");
				WXPayEntryActivity.this.finish();
			}
				break;
			case BaseResp.ErrCode.ERR_OK:{
				
				Log.i(TAG, "------支付成功------");
				message.setText("支付成功");
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						WXPayEntryActivity.this.finish();
						
					}
				}, 2000);
				
			}
				break;
			}
			
		}
	}
	
}


