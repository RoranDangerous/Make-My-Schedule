package com.roran.dangerous.makemyschedule;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by dangerous on 04/03/17.
 */

public class MySpinnerAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<String> list = new ArrayList<String>();
    private String courseN = "";
    private Context context;
    private static final int TYPE_READ = 0;
    private static final int TYPE_NON_READ = 1;
    private static final int TYPE_MAX_COUNT = TYPE_NON_READ + 1;

    public MySpinnerAdapter(ArrayList<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;//list.get(pos).getId();
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.spinnerlayout, null);
        }

        final TextView listItemText = (TextView)view.findViewById(R.id.tvspinner);
        listItemText.setText(list.get(position));

        final ImageView iv = (ImageView)view.findViewById(R.id.ivspinner);
        TypedArray a = context.getTheme().obtainStyledAttributes(R.style.AppTheme, new int[] {android.R.attr.textCheckMark});
        int attributeResourceId = a.getResourceId(0, 0);
        Drawable drawable = context.getResources().getDrawable(attributeResourceId);
        iv.setImageDrawable(drawable);

        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if(cn.getShortClassName().equals(".MainActivity"))
            System.out.println();

        return view;
    }
}
