package com.changhong.ttfileplore.base;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.changhong.ttfileplore.R;
import com.changhong.ttfileplore.application.MyApp;

public abstract class BaseActivity   extends Activity {
	SharedPreferences sharedPreferences;
	/**
	 * 加载布局中的控件
	 */	
	protected abstract void findView();


	@SuppressWarnings("unchecked")
	public <T extends View> T findView(int id) {
	        return (T) findViewById(id);
	   
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 sharedPreferences = getSharedPreferences("set", Context.MODE_PRIVATE); //私有数据
		switch(sharedPreferences.getInt("Theme",R.style.DayTheme)){
			case R.style.DayTheme:
				setTheme(R.style.DayTheme);
				break;
			case R.style.NightTheme:
				setTheme(R.style.NightTheme);
				break;
		}
		ActionBar actionBar = getActionBar();
		if(actionBar!=null){
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}
	@Override
	protected void onStart() {
		MyApp.context = this;
		super.onStart();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			MyApp myapp = (MyApp) getApplication();
			myapp.setContext(null);

		}
		return super.onKeyDown(keyCode, event);

	}
}
