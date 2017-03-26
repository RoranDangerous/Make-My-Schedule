package com.roran.dangerous.makemyschedule;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class MyListViewAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<String> list = new ArrayList<String>();
    private String courseN = "";
    private Context context;
    private static final int TYPE_READ = 0;
    private static final int TYPE_NON_READ = 1;
    private static final int TYPE_MAX_COUNT = TYPE_NON_READ + 1;
    private static String lastCourseChecked;

    MyListViewAdapter(ArrayList<String> list, Context context) {
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
            //view = inflater.inflate(R.layout.lvlayout, null);
            view = inflater.inflate(android.R.layout.simple_list_item_checked, null);
        }

        //Handle TextView and display string from your list

        final CheckedTextView ctv = (CheckedTextView) view.findViewById(android.R.id.text1);
        if(ctv!=null) {
            //ctv.setChecked(true);
            ctv.setText(list.get(position));
        }

        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        final ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if(cn.getShortClassName().equals(".MainActivity"))
            if(MainActivity.isSelected(position))
                ctv.setChecked(true);
            else
                ctv.setChecked(false);

        try {
            if (SemesterProgramActivity.selectedCourses != null)
                for (JSONObject j : SemesterProgramActivity.selectedCourses)
                    if (j.get("name").toString().equals(ctv.getText().toString()))
                        ctv.setChecked(true);
        }catch(JSONException e){}

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(!ctv.isChecked()) {
                        if(cn.getShortClassName().equals(".MainActivity") || cn.getShortClassName().equals(".LoadingActivity")) {
                            courseN = ctv.getText().toString();
                            lastCourseChecked = courseN;
                            MainActivity.addToCourses(courseN);
                            if(MainActivity.hasLabs) {
                                Intent in = new Intent(context, chooseLabActivity.class);
                                in.putExtra("labs", MainActivity.setLabsArrayList(position));
                                in.putExtra("courseSelected", courseN);
                                context.startActivity(in);
                            }
                            MainActivity.addRemoveCourse(position);
                        }
                        else{
                            MainActivity.addLabToCourses(MainActivity.getFullLabName(ctv.getText().toString()), chooseLabActivity.cN);
                            chooseLabActivity.arrs[position]=!chooseLabActivity.arrs[position];
                        }
                        ctv.setChecked(true);
                    }
                    else {
                        courseN = ctv.getText().toString();
                        if(cn.getShortClassName().equals(".MainActivity") || cn.getShortClassName().equals(".LoadingActivity")) {
                            MainActivity.removeFromCourses(courseN);
                            MainActivity.addRemoveCourse(position);
                        }
                        else{
                            MainActivity.removeLabFromCourses(courseN,lastCourseChecked);
                            chooseLabActivity.arrs[position]=!chooseLabActivity.arrs[position];
                        }
                        ctv.setChecked(false);
                    }

                } catch (JSONException e) {
                }
            }
        });

        return view;
    }
}