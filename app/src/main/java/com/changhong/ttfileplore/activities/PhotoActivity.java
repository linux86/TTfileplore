package com.changhong.ttfileplore.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.changhong.ttfileplore.R;
import com.changhong.ttfileplore.application.MyApp;
import com.changhong.ttfileplore.base.BaseActivity;
import com.changhong.ttfileplore.fragment.MoreDialogFragment;
import com.changhong.ttfileplore.fragment.PhotoGridFragment;
import com.changhong.ttfileplore.fragment.PhotoTimeLineFragment;
import com.changhong.ttfileplore.view.CircleProgress;

/**
 * Created by tangli on 2015/11/2 !
 */
public class PhotoActivity extends BaseActivity implements MoreDialogFragment.UpDate {
    private PhotoTimeLineFragment mTimeLineFragment;
    private PhotoGridFragment mGridFragment;
    private String[] content;
    View layout;
    AlertDialog alertDialog;
    AlertDialog.Builder builder;
    CircleProgress mProgressView;
    private TextView tv_num;

    boolean isdefault = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        MyApp myapp = (MyApp) getApplication();
        myapp.setContext(this);
        content = getIntent().getStringArrayExtra("content");
        findView();
        initView();
        // setGridFragment();
        setDefaultFragment();
        showDialog();
        initToolBar();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void showDialog() {
        mProgressView.startAnim();
        alertDialog.show();
    }

    public void dismissDialog() {
        if (alertDialog.isShowing()) {
            mProgressView.stopAnim();
            alertDialog.dismiss();
        }
    }

    private void initView() {


        layout = LayoutInflater.from(this).inflate(R.layout.circle_progress, (ViewGroup) findViewById(R.id.rl_progress));
        builder = new AlertDialog.Builder(PhotoActivity.this).setView(layout);
        alertDialog = builder.create();
        mProgressView = (CircleProgress) layout.findViewById(R.id.progress);
    }

    private void setDefaultFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        mTimeLineFragment = new PhotoTimeLineFragment();
        Bundle b = new Bundle();
        b.putString("path", content[0].substring(0, content[0].lastIndexOf("/")));
        mTimeLineFragment.setArguments(b);
        transaction.replace(R.id.framelayout_photo, mTimeLineFragment);
        transaction.commit();
        isdefault = true;

    }

    private void setGridFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        mGridFragment = new PhotoGridFragment();
        Bundle b = new Bundle();
        b.putString("path", content[0].substring(0, content[0].lastIndexOf("/")));
        mGridFragment.setArguments(b);
        transaction.replace(R.id.framelayout_photo, mGridFragment);
        transaction.commit();
        isdefault = false;

    }

    @Override
    protected void findView() {
        tv_num = findView(R.id.tv_photo_num);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.change_view:
                if (isdefault) {
                    setGridFragment();
                } else
                    setDefaultFragment();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return true;
    }


    public void setPhotoNumText(String string) {
        tv_num.setText(string);
    }

    @Override
    public void update() {
        if (isdefault) {
            setDefaultFragment();
        } else
            setGridFragment();
    }

}
