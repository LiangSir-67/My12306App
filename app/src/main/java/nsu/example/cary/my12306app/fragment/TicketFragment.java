package nsu.example.cary.my12306app.fragment;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nsu.example.cary.my12306app.R;
import nsu.example.cary.my12306app.activity.ticket.TicketResultStep1Activity;
import nsu.example.cary.my12306app.stationlist.StationListActivity;

public class TicketFragment extends Fragment {

    private TextView tvTicketStationFrom;
    private TextView tvTicketStationTo;
    private TextView tvTicketDateStart;
    private ImageView imgStationExchange;
    private Button btnTicketQuery;
    private String stationFrom,stationTo;
    private ListView lvMainCheck;
    private SimpleAdapter adapter;
    private List<Map<String,Object>> data;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ticket,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Calendar oldCalendar = Calendar.getInstance();
        final int oldYear = oldCalendar.get(Calendar.YEAR);
        final int oldMonth = oldCalendar.get(Calendar.MONTH);
        final int oldDay = oldCalendar.get(Calendar.DATE);
        String oldWeekDay = DateUtils.formatDateTime(getActivity(),oldCalendar.getTimeInMillis(),DateUtils.FORMAT_SHOW_WEEKDAY);


        tvTicketStationFrom = getActivity().findViewById(R.id.tvTicketStationFrom);
        tvTicketStationTo = getActivity().findViewById(R.id.tvTicketStationto);
        tvTicketDateStart = getActivity().findViewById(R.id.tvTicketDateFrom);
        imgStationExchange = getActivity().findViewById(R.id.imgTicketStationExchange);
        btnTicketQuery = getActivity().findViewById(R.id.btnTicketQuery);
        lvMainCheck = getActivity().findViewById(R.id.lvMainCheck);

        data = new ArrayList<>();
        btnTicketQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> map = new HashMap<>();
                map.put("list",tvTicketStationFrom.getText().toString()+"-->"+tvTicketStationTo.getText().toString());
                data.add(map);
                adapter = new SimpleAdapter(getContext(),
                        data,
                        R.layout.item_main_check,
                        new String[] {"list"},
                        new int[]{R.id.itemMainCheck});
                lvMainCheck.setAdapter(adapter);

                Intent intent = new Intent();
                intent.putExtra("stationFrom",tvTicketStationFrom.getText().toString());
                intent.putExtra("stationTo",tvTicketStationTo.getText().toString());
                intent.putExtra("startTrainDate",tvTicketDateStart.getText().toString());
                Log.d("tvTicketDateFrom",tvTicketDateStart.getText().toString());
                intent.setClass(getActivity(), TicketResultStep1Activity.class);
                startActivity(intent);
            }
        });

        //出发站
        tvTicketStationFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent();
                intent.setClass(getActivity(), StationListActivity.class);
                startActivityForResult(intent,100);
            }
        });

        //终点站
        tvTicketStationTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent();
                intent.setClass(getActivity(),StationListActivity.class);
                startActivityForResult(intent,150);
            }
        });


        imgStationExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stationFrom = tvTicketStationFrom.getText().toString();
                stationTo = tvTicketStationTo.getText().toString();

                TranslateAnimation animationFrom = new TranslateAnimation(0,650,0,0);
                animationFrom.setDuration(300);
                animationFrom.setInterpolator(new AccelerateInterpolator());
                animationFrom.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        tvTicketStationTo.setText(stationFrom);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                TranslateAnimation animationTo = new TranslateAnimation(0,-650,0,0);
                animationTo.setDuration(300);
                animationTo.setInterpolator(new AccelerateInterpolator());
                animationTo.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        tvTicketStationFrom.setText(stationTo);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                tvTicketStationFrom.startAnimation(animationFrom);
                tvTicketStationTo.startAnimation(animationTo);
            }
        });

        tvTicketDateStart.setText(oldYear+"-"+(oldMonth+1)+"-"+oldDay+" "+oldWeekDay);
        tvTicketDateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar newCalendar = Calendar.getInstance();
                        newCalendar.set(year,month,dayOfMonth);
                        String weekDay = DateUtils.formatDateTime(getActivity(),newCalendar.getTimeInMillis(),DateUtils.FORMAT_SHOW_WEEKDAY);
                        tvTicketDateStart.setText(year+"-"+(month+1)+"-"+dayOfMonth+" "+weekDay);
                    }
                },oldYear,oldMonth,oldDay).show();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String stationName = data.getStringExtra("name");
        if (!TextUtils.isEmpty(stationName)){
            switch (requestCode){
                case 100:
                    tvTicketStationFrom.setText(stationName);
                    break;
                case 150:
                    tvTicketStationTo.setText(stationName);
                    break;
            }
        }
    }
}
