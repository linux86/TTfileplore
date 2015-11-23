package com.changhong.ttfileplore.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.changhong.ttfileplore.R;
import com.changhong.ttfileplore.activities.PhotoActivity;
import com.changhong.ttfileplore.adapter.PhotoGirdAdapter2;
import com.changhong.ttfileplore.utils.Content;
import com.changhong.ttfileplore.utils.Utils;
import com.changhong.ttfileplore.view.PopupMoreDialog;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by tangli on 2015/11/3.
 */
public class PhotoGridFragment extends Fragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    private View view ;
    private String path;
    private GridView gv_photogrid;
    private File file ;
    private PhotoGirdAdapter2 photoGirdAdapter2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =inflater.inflate(R.layout.fragment_photo_grid, container, false);
        Bundle b =getArguments();
        path =b.getString("path");
        Log.e("onCreateView", "onCreateView");
        findView();
        initView();
        return view;
    }

    private void initView() {
         file = new File(path);
        ArrayList<File> files = new ArrayList<>();
        File[] tmp = file.listFiles();
        for(int i =0;i<tmp.length;i++){
            if(Utils.getMIMEType(tmp[i]).equals("image/*"))
                files.add(tmp[i]);
        }
        photoGirdAdapter2 = new PhotoGirdAdapter2(files,getActivity());
        gv_photogrid.setAdapter(photoGirdAdapter2);
        ((PhotoActivity)getActivity()).dismissDialog();
        ((PhotoActivity)getActivity()).setPhotoNumText(files.size() + "张");
        gv_photogrid.setOnItemClickListener(this);
        gv_photogrid.setOnItemLongClickListener(this);

    }

    private void findView() {
        gv_photogrid = (GridView)view.findViewById(R.id.gv_photogrid);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        WindowManager.LayoutParams lp=getActivity().getWindow().getAttributes();
        lp.alpha=0.5f;
        getActivity().getWindow().setAttributes(lp);
        PopupMoreDialog p = new PopupMoreDialog(getActivity(),300, ViewGroup.LayoutParams.WRAP_CONTENT, true,file.getPath());
        int[] viewLocation = new int[2];
        view.getLocationInWindow(viewLocation);
        int viewX = viewLocation[0]; // x 坐标
        int viewY = viewLocation[1]; // y 坐标
        Point point = new Point();
        getActivity().getWindow().getWindowManager().getDefaultDisplay().getSize(point);
        Log.e("yy", p.getHeight() + " " + p.getWidth());
        if (point.x - viewX > 300) {
            if (point.y - viewY > 400) {
                p.setAnimationStyle(R.style.PopupAnimationTop);
                p.showAsDropDown(view, 150, -250);
            } else {
                p.setAnimationStyle(R.style.PopupAnimationBottom);
                p.showAtLocation(view, Gravity.NO_GRAVITY, viewX + 150, viewY - 350);
            }
        } else {
            if (point.y - viewY > 350) {
                p.setAnimationStyle(R.style.PopupAnimationTopRight);
                p.showAsDropDown(view, -150, -250);
            } else {
                p.setAnimationStyle(R.style.PopupAnimationBottomRight);
                p.showAtLocation(view, Gravity.NO_GRAVITY, viewX - 150, viewY - 350);
            }
        }

        p.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp=getActivity().getWindow().getAttributes();
                lp.alpha=1f;
                getActivity().getWindow().setAttributes(lp);
            }
        });

            return true;


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = (File) parent.getItemAtPosition(position);
        Intent intent = Utils.openFile(file);
        startActivity(intent);
    }
}
