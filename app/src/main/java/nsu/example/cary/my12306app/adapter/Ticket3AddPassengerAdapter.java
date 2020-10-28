package nsu.example.cary.my12306app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nsu.example.cary.my12306app.R;

public class Ticket3AddPassengerAdapter extends BaseAdapter {

    private Context context;
    private List<Map<String, Object>> data;
    private static List<Map<String, Object>> result = new ArrayList<>();

    public Ticket3AddPassengerAdapter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
    }

    public static List<Map<String, Object>>


    getResult() {
        return result;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //用于缓存当前布局
        ViewHolder viewHolder = null;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_ticket3_add_passenger,null);
            viewHolder.cbSubmit =convertView.findViewById(R.id.cbTicket3Add);
            viewHolder.tvPassengerName = convertView.findViewById(R.id.tvPassengerName);
            viewHolder.tvPassengerIdCard = convertView.findViewById(R.id.tvPassengerIdCard);
            viewHolder.tvPassengerTel = convertView.findViewById(R.id.tvPassengerTel);
            viewHolder.cbSubmit = convertView.findViewById(R.id.cbTicket3Add);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvPassengerName.setText((String) data.get(position).get("name"));
        viewHolder.tvPassengerIdCard.setText((String) data.get(position).get("idCard"));
        viewHolder.tvPassengerTel.setText((String) data.get(position).get("tel"));

        viewHolder.cbSubmit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    result.add(data.get(position));
                    Log.d("addPassenger",result.size()+"");
                }else {

                }
            }
        });
        return convertView;
    }

    public class ViewHolder {
        private CheckBox cbSubmit;
        private TextView tvPassengerName;
        private TextView tvPassengerIdCard;
        private TextView tvPassengerTel;
    }
}
