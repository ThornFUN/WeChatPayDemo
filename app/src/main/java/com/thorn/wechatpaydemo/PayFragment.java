package com.thorn.wechatpaydemo;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * 项目名称：PayDemo 类名称：PayFragment 类描述： 创建人：lc 创建时间：2015-3-11 下午2:53:25 修改人：131
 * 修改时间：2015-3-11 下午2:53:25 修改备注：
 * 
 * @version
 * 
 */
public class PayFragment extends Fragment implements OnClickListener {
	private final static String TAG = "mess";
	private IWXAPI api;

	private EditText moneyEdt;
	private View rootView;
	private ImageView back;
	private Button confirmBtn;
	private Button payBtn;
	private TextView tip;

	private boolean isGetAccessToken = false;
	private boolean isGetPrepayId = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 键盘顶起页面
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		api = WXAPIFactory.createWXAPI(getActivity(), Constants.APP_ID);
	}
	
	

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}



	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.pay, container, false);
			moneyEdt = (EditText) rootView.findViewById(R.id.money);
			back = (ImageView) rootView.findViewById(R.id.back);
			back.setOnClickListener(this);
			confirmBtn = (Button) rootView.findViewById(R.id.confirm);
			confirmBtn.setOnClickListener(this);
			payBtn = (Button) rootView.findViewById(R.id.paynow);
			payBtn.setOnClickListener(this);
			tip = (TextView) rootView.findViewById(R.id.tip);

		} else {
			ViewGroup v = (ViewGroup) rootView.getParent();
			if (v != null) {
				v.removeView(rootView);
			}
		}
		ControlKeyboard();
		return rootView;
	}

	/**
	 * 自动弹出键盘 :可能会由于页面未完全加载，无法弹出，所以最好延迟在弹出
	 * */
	private void popKeyboard() {

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(moneyEdt, 0);
			}

		}, 1000);
	}

	/**
	 * 控制键盘 显示时自动隐藏，隐藏时自动显示
	 * 
	 * */
	private void ControlKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			back();
			break;
		case R.id.confirm:
			confirm();
			break;
		case R.id.paynow:
			payNow();
			break;
		}

	}

	private void back() {
		getActivity().getSupportFragmentManager().popBackStack();
		ControlKeyboard();
	}

	private void confirm() {
		String money = moneyEdt.getText().toString();
		if (money != null && !money.equals("")) {

			moneyEdt.setText((Integer.parseInt(money) - 5) + ".00");
			moneyEdt.setInputType(InputType.TYPE_NULL);
			ControlKeyboard();
			confirmBtn.setVisibility(ViewGroup.GONE);
			payBtn.setVisibility(View.VISIBLE);
			tip.setText("趣网吧为您支付5.00元上网费用");
		}
	}

	private void payNow() {
		//
		payBtn.setClickable(false);
		Toast.makeText(getActivity(), "去微信支付", Toast.LENGTH_LONG).show();
		new GetAccessTokenTask().execute();
	}

	/**
	 * 微信公众平台商户模块和商户约定的密钥
	 * 
	 * 注意：不能hardcode在客户端，建议genPackage这个过程由服务器端完成
	 */
	private static final String PARTNER_KEY = "8934e7d15453e97507ef794cf7b0519d";

	/**
	 * 服务端完成 package生成方法
	 * */
	private String genPackage(List<NameValuePair> params) {
		Log.i("mess", "----------genPackage---------");
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(PARTNER_KEY); // 注意：不能hardcode在客户端，建议genPackage这个过程都由服务器端完成

		// 进行md5摘要前，params内容为原始内容，未经过url encode处理
		String packageSign = MD5.getMessageDigest(sb.toString().getBytes())
				.toUpperCase();
		Log.d("d", "package签名串：" + sb.toString() + "----:" + packageSign);
		return URLEncodedUtils.format(params, "utf-8") + "&sign=" + packageSign;
	}

	/**
	 * 微信开放平台和商户约定的密钥
	 * 
	 * 注意：不能hardcode在客户端，建议genSign这个过程由服务器端完成
	 */
	private static final String APP_SECRET = "db426a9829e4b49a0dcac7b4162da6b6"; // wxd930ea5d5a258f4f
																					// 对应的密钥

	/**
	 * 微信开放平台和商户约定的支付密钥
	 * 
	 * 注意：不能hardcode在客户端，建议genSign这个过程由服务器端完成
	 */
	private static final String APP_KEY = "L8LrMqqeGRxST5reouB0K66CaYAWpqhAVsq7ggKkxHCOastWksvuX1uvmvQclxaHoYd3ElNBrNO2DHnnzgfVG9Qs473M3DTOZug5er46FhuGofumV8H2FVR9qkjSlC5K"; // wxd930ea5d5a258f4f
																																												// 对应的支付密钥

	private class GetAccessTokenTask extends
			AsyncTask<Void, Void, GetAccessTokenResult> {

		@Override
		protected GetAccessTokenResult doInBackground(Void... params) {
			GetAccessTokenResult result = new GetAccessTokenResult();

			/**
			 * 获取access_token一天内有频次限制， 商户侧必须统一管理access_token，
			 * 每次获取的access_token有效期为2个小时， 不能每次获取prepayid都去请求一次，否则超过微信频次限制将无法下单。
			 * 
			 * */
			/*
			 * 获取access_token,http请求GET grant_type:获取access_token
			 * ,此处填写client_credential appid:第三方用户唯一凭证
			 * secret:第三方用户唯一凭证密钥，即：appsecret
			 * 
			 * 返回结果： 正确的Json:{"access_token":"ACCESS_TOKEN","expires_in":7200}
			 * access_token:获取到的凭证（最大长度为512字节）
			 * expires_in:凭证有效时间，单位：秒。正常情况下有效期为7200秒，重复获取将导致上次获取的access_token失效
			 * 错误的Json：{"errcode":40013,"errmsg":invalid appid}
			 */
			String url = String
					.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
							Constants.APP_ID, APP_SECRET);
			Log.d(TAG, "get access token, url = " + url);

			byte[] buf = Util.httpGet(url);
			Log.i(TAG, "" + (buf == null));
			if (buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}

			String content = new String(buf);
			result.parseFrom(content);
			return result;
		}

		@Override
		protected void onPostExecute(GetAccessTokenResult result) {

			if (result.localRetCode == LocalRetCode.ERR_OK) {
				isGetAccessToken = true;
				Toast.makeText(getActivity(), R.string.get_access_token_succ,
						Toast.LENGTH_LONG).show();
				Log.d(TAG, "onPostExecute, accessToken = " + result.accessToken);

				GetPrepayIdTask getPrepayId = new GetPrepayIdTask(
						result.accessToken);
				getPrepayId.execute();
			} else {
				Toast.makeText(
						getActivity(),
						getString(R.string.get_access_token_fail,
								result.localRetCode.name()), Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	private class GetPrepayIdTask extends
			AsyncTask<Void, Void, GetPrepayIdResult> {

		private String accessToken;

		public GetPrepayIdTask(String accessToken) {
			this.accessToken = accessToken;
		}

		@Override
		protected void onPreExecute() {
			Log.i("mess", "-------GetPrepayIdTask--------onPreExecute-------");
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		/**
		 * 获取到access_token后，通 过微信开放平台接口生成预支付订单。 http请求方式：POST {
		 * "appid":"wxd930ea5d5a258f4f", "traceid":"test_1399514976",
		 * ", "noncestr":"e7d161ac8d8a76529d39d9f5b4249ccb
		 * "timestamp":1399514976,
		 * "package":"bank_type=WX&body=%E6%94%AF%E4%BB%98%E6%B5%8B%E8%AF%
		 * 95&fee_type
		 * =1&input_charset=UTF-8&notify_url=http%3A%2F%2Fweixin.qq.com
		 * &out_trade_ no=7240b65810859cbf2a8d9f76a638c0a3&partner=1900000109&
		 * spbill_create_ip=196.168.1.1&
		 * total_fee=1&sign=7F77B507B755B3262884291517E380F8",
		 * "sign_method":"sha1",
		 * "app_signature":"7f77b507b755b3262884291517e380f8" 其中，各字段含义如下： }
		 * 
		 * 
		 * appid:开放平台账户的唯一标示 traceid:由开发者定义，可用于订单的查询与跟踪，建议根据支付用户信息生成此id
		 * noncestr:32位内的随机串，防重发 package：订单详情 timestamp:时间戳 app_signature:签名
		 * sign_method:密方式，默认为sha1;
		 * 
		 * 返回结果说明： 正确的Json: {"prepayid"：,"errcode":0,"errmsg":"Success"}
		 * 错误的JSon: {"errcode":48001,"errmsg":"api unauthorized"}
		 * 
		 * */
		@Override
		protected GetPrepayIdResult doInBackground(Void... params) {

			String url = String.format(
					"https://api.weixin.qq.com/pay/genprepay?access_token=%s",
					accessToken);
			String entity = genProductArgs();

			Log.d(TAG, "doInBackground, url = " + url);
			Log.d(TAG, "doInBackground, entity = " + entity);

			GetPrepayIdResult result = new GetPrepayIdResult();

			byte[] buf = Util.httpPost(url, entity);
			if (buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}

			String content = new String(buf);
			Log.d(TAG, "doInBackground, content = " + content);
			result.parseFrom(content);
			return result;
		}

		@Override
		protected void onPostExecute(GetPrepayIdResult result) {

			if (result.localRetCode == LocalRetCode.ERR_OK) {
				isGetPrepayId = true;
				Toast.makeText(getActivity(), R.string.get_prepayid_succ,
						Toast.LENGTH_LONG).show();
				sendPayReq(result);
			} else {
				Toast.makeText(
						getActivity(),
						getString(R.string.get_prepayid_fail,
								result.localRetCode.name()), Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	private static enum LocalRetCode {
		ERR_OK, ERR_HTTP, ERR_JSON, ERR_OTHER
	}

	private static class GetAccessTokenResult {

		private static final String TAG = "GetAccessTokenResult";

		public LocalRetCode localRetCode = LocalRetCode.ERR_OTHER;
		public String accessToken;
		public int expiresIn;
		public int errCode;
		public String errMsg;

		public void parseFrom(String content) {
			Log.i("mess",
					"--------GetAccessTokenResult---------parseFrom-------");

			if (content == null || content.length() <= 0) {
				Log.e(TAG, "parseFrom fail, content is null");
				localRetCode = LocalRetCode.ERR_JSON;
				return;
			}

			try {

				JSONObject json = new JSONObject(content);
				if (json.has("access_token")) { // success case
					accessToken = json.getString("access_token");
					expiresIn = json.getInt("expires_in");
					localRetCode = LocalRetCode.ERR_OK;
				} else {
					errCode = json.getInt("errcode");
					errMsg = json.getString("errmsg");
					localRetCode = LocalRetCode.ERR_JSON;
				}

			} catch (Exception e) {
				localRetCode = LocalRetCode.ERR_JSON;
			}
		}
	}

	private static class GetPrepayIdResult {

		private static final String TAG = "GetPrepayIdResult";

		public LocalRetCode localRetCode = LocalRetCode.ERR_OTHER;
		public String prepayId;
		public int errCode;
		public String errMsg;

		public void parseFrom(String content) {
			Log.i("mess", "------GetPrepayIdResult-------parseFrom--------");

			if (content == null || content.length() <= 0) {
				Log.e(TAG, "parseFrom fail, content is null");
				localRetCode = LocalRetCode.ERR_JSON;
				return;
			}

			try {
				JSONObject json = new JSONObject(content);
				if (json.has("prepayid")) { // success case
					prepayId = json.getString("prepayid");
					localRetCode = LocalRetCode.ERR_OK;
				} else {
					localRetCode = LocalRetCode.ERR_JSON;
				}

				errCode = json.getInt("errcode");
				errMsg = json.getString("errmsg");

			} catch (Exception e) {
				localRetCode = LocalRetCode.ERR_JSON;
			}
		}
	}

	/** MD5随机的转换为16进制 */
	private String genNonceStr() {
		Log.i("mess", "----------genNonceStr---------");
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
				.getBytes());
	}

	/** 获得时间戳 */
	private long genTimeStamp() {
		Log.i("mess", "-------genTimeStamp---------");
		return System.currentTimeMillis() / 1000;
	}

	/**
	 * 建议 traceid 字段包含用户信息及订单信息，方便后续对订单状态的查询和跟踪
	 */
	private String getTraceId() {
		Log.i("mess", "-----------getTraceId-----------");
		return "crestxu_" + genTimeStamp();
	}

	/**
	 * 注意：商户系统内部的订单号,32个字符内、可包含字母,确保在商户系统唯一
	 */
	private String genOutTradNo() {
		Log.i("mess", "----------genOutTradNo-----------");
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
				.getBytes());
	}

	private long timeStamp;
	private String nonceStr, packageValue;

	/**
	 * 服务端完成 支付签名生成方法
	 **/
	private String genSign(List<NameValuePair> params) {
		Log.i("mess", "------------genSign-------------");
		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (; i < params.size() - 1; i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append(params.get(i).getName());
		sb.append('=');
		sb.append(params.get(i).getValue());

		String sha1 = Util.sha1(sb.toString());
		Log.d("d", "sha1签名串：" + sb.toString());
		Log.d(TAG, "genSign, sha1 = " + sha1);
		return sha1;
	}

	private String genProductArgs() {

		Log.i("mess", "--------genProductArgs--------");
		JSONObject json = new JSONObject();

		try {
			json.put("appid", Constants.APP_ID);
			String traceId = getTraceId(); // traceId
											// 由开发者自定义，可用于订单的查询与跟踪，建议根据支付用户信息生成此id
			json.put("traceid", traceId);
			nonceStr = genNonceStr();
			json.put("noncestr", nonceStr);
			/** 订单详情扩展字符串定义 */
			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			// 固定为“WX”
			packageParams.add(new BasicNameValuePair("bank_type", "WX"));
			// 商品描述：128字节以下
			packageParams.add(new BasicNameValuePair("body", "趣网吧订单"));
			// 支持币种，1人民币
			packageParams.add(new BasicNameValuePair("fee_type", "1"));
			// 传入参数字符编码：默认GBK
			packageParams.add(new BasicNameValuePair("input_charset", "UTF-8"));
			// 通知URL,支付完成后，接收微信通知结果的URL,需要绝对路径,
			packageParams.add(new BasicNameValuePair("notify_url",
					"http://121.14.73.81:8080/agent/wxpay/payNotifyUrl.jsp"));
			// 商户系统内部订单号，32个字符以内，可包含字母
			packageParams.add(new BasicNameValuePair("out_trade_no",
					genOutTradNo()));
			// 商户号，注册时分配的财付通商户号
			packageParams.add(new BasicNameValuePair("partner", "1900000109"));
			// 用户浏览器端ip,不是商户服务器ip，格式为IPV4
			packageParams.add(new BasicNameValuePair("spbill_create_ip",
					"196.168.1.1"));
			// 订单总金额，单位是分
			packageParams.add(new BasicNameValuePair("total_fee", "1"));
			/** 最终的package字符串 */
			packageValue = genPackage(packageParams);
			Log.i("mess", "----packageValue----:" + packageValue);

			json.put("package", packageValue);
			timeStamp = genTimeStamp();
			json.put("timestamp", timeStamp);

			/** 支付签名的生成方法 */
			List<NameValuePair> signParams = new LinkedList<NameValuePair>();
			signParams.add(new BasicNameValuePair("appid", Constants.APP_ID));
			signParams.add(new BasicNameValuePair("appkey", APP_KEY));
			signParams.add(new BasicNameValuePair("noncestr", nonceStr));
			signParams.add(new BasicNameValuePair("package", packageValue));
			signParams.add(new BasicNameValuePair("timestamp", String
					.valueOf(timeStamp)));
			signParams.add(new BasicNameValuePair("traceid", traceId));
			json.put("app_signature", genSign(signParams));

			json.put("sign_method", "sha1");
		} catch (Exception e) {
			Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
			return null;
		}

		return json.toString();
	}

	/**
	 * 用服务器返回的参数调起支付 获取到服务器的订单参数后，
	 * 
	 * */

	private void sendPayReq(GetPrepayIdResult result) {

		if (!isGetAccessToken || !isGetPrepayId) {
			payBtn.setClickable(true);
		}

		Log.i("mess", "-------sendPayReq--------");
		PayReq req = new PayReq();
		req.appId = Constants.APP_ID;
		req.partnerId = Constants.PARTNER_ID;
		req.prepayId = result.prepayId;
		req.nonceStr = nonceStr;
		req.timeStamp = String.valueOf(timeStamp);
		req.packageValue = "Sign=WXpay";// "Sign=" + packageValue;

		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("appkey", APP_KEY));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
		/**
		 * 
		 * 添加prepayid重新签名 签名后，返回给APP
		 * */
		req.sign = genSign(signParams);
		Log.i("mess", "appId:" + Constants.APP_ID);
		Log.i("mess", ",partnerId:" + Constants.PARTNER_ID);
		Log.i("mess", ",prepayId:" + result.prepayId);
		Log.i("mess", ",noceStr:" + nonceStr);
		Log.i("mess", ",timeStamp:" + String.valueOf(timeStamp));
		Log.i("mess", ",sign:" + genSign(signParams));
		Log.d("d", "调起支付的package串：" + req.packageValue);
		// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
		api.sendReq(req);
		/*
		 * Timer timer = new Timer(); timer.schedule(new TimerTask() {
		 * 
		 * @Override public void run() {
		 * getActivity().getSupportFragmentManager(
		 * ).beginTransaction().remove(PayFragment
		 * .this).commitAllowingStateLoss();
		 * 
		 * } }, 2500);
		 */
	}

}
