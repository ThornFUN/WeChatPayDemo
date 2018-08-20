package com.thorn.wechatpaydemo;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * 项目名称：PayDemo 类名称：ZhifuFragment 类描述： 创建人：lc 创建时间：2015-3-11 下午2:58:37 修改人：131
 * 修改时间：2015-3-11 下午2:58:37 修改备注：
 * 
 * @version
 * 
 */
public class ZhifuFragment extends Fragment implements OnClickListener {
	private Button online;
	private Button cash;
	private View rootView;

	// 微信是否支持支付
	private boolean isWeiXinSupportPayment = false;
	// 微信是否安装微信
	private boolean isWeiXinAppInstalled = false;
	private IWXAPI mApi;
	private static final String APP_ID = Constants.APP_ID;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.zhifu, container, false);
			online = (Button) rootView.findViewById(R.id.online);
			online.setOnClickListener(this);
			cash = (Button) rootView.findViewById(R.id.cash);
			cash.setOnClickListener(this);
		} else {
			ViewGroup v = (ViewGroup) rootView.getParent();
			if (v != null) {
				v.removeView(v);
			}
		}
		return rootView;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.online:
			goToOnlinePayment();
			break;
		}
	}

	/** 打开在线支付 */
	private void goToOnlinePayment() {
		mApi = WXAPIFactory.createWXAPI(getActivity(), Constants.APP_ID);
		registerToWeiXin();
		checkForWeixinInstall();
		checkForWeixinSupportPayment();
		if (isWeiXinAppInstalled && isWeiXinSupportPayment) {
			online.setText("立即支付");
			Toast.makeText(
					getActivity(),
					"打开微信支付" + isWeiXinAppInstalled + ":"
							+ isWeiXinSupportPayment, Toast.LENGTH_LONG).show();
			
			FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
			transaction.setCustomAnimations(R.anim.slide_from_bottom, 0, 0, 0);
			transaction.add(R.id.frame, new PayFragment()).addToBackStack(null);
			transaction.commitAllowingStateLoss();
		}

	}

	/** 将app注册到微信 */
	private void registerToWeiXin() {
		mApi = WXAPIFactory.createWXAPI(getActivity(), APP_ID, true);
		mApi.registerApp(APP_ID);
	}

	/** 检查微信是否安装 */
	private void checkForWeixinInstall() {
		if (mApi.isWXAppInstalled()) {
			isWeiXinAppInstalled = true;
		}
	}

	/** 检查微信版本是否支持支付 */
	private void checkForWeixinSupportPayment() {
		if (mApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT) {
			isWeiXinSupportPayment = true;
		}
	}

}
