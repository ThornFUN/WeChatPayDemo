package com.thorn.wechatpaydemo;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;


public class MainActivity extends FragmentActivity  {
	
	
	private FragmentManager manager;
	private Fragment fragment = new ZhifuFragment();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		manager = getSupportFragmentManager();
		manager.beginTransaction().replace(R.id.frame, fragment).commitAllowingStateLoss();
	}
	
	
	
	
	

	





}
