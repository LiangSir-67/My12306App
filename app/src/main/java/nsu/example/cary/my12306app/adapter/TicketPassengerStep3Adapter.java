package nsu.example.cary.my12306app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nsu.example.cary.my12306app.R;
import nsu.example.cary.my12306app.activity.ticket.TicketPassengerStep3Activity;

public class TicketPassengerStep3Adapter extends BaseAdapter {
    private Context context;
    public static List<Map<String, Object>> data;
    private double price;

    public TicketPassengerStep3Adapter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
    }

    public double getPrice() {
        if (!data.isEmpty() && data.size() != 0)
            price = Double.parseDouble((String) data.get(0).get("seatPrice")) * getCount();
        else
            price = 0.0;
        return price;
    }

    public static List<Map<String, Object>> getData() {
        return data;
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
        TicketPassengerStep3Adapter.ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new TicketPassengerStep3Adapter.ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_ticket_passenger_step3, null);
            viewHolder.tvStep3Name = convertView.findViewById(R.id.tvStep3Name);
            viewHolder.tvStep3IdCard = convertView.findViewById(R.id.tvStep3IdCard);
            viewHolder.tvStep3Tel = convertView.findViewById(R.id.tvStep3Tel);
            viewHolder.ibDelete = convertView.findViewById(R.id.ibStep3Delete);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TicketPassengerStep3Adapter.ViewHolder) convertView.getTag();
        }
        viewHolder.tvStep3Name.setText((String) data.get(position).get("name"));
        viewHolder.tvStep3IdCard.setText((String) data.get(position).get("idCard"));
        viewHolder.tvStep3Tel.setText((String) data.get(position).get("tel"));

        viewHolder.ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.remove(position);
                notifyDataSetChanged();
                TicketPassengerStep3Activity.tvTicketPassengerStep3SumPrice.setText("订单总额：¥"+getPrice()+"元");
            }
        });

        return convertView;
    }

    public class ViewHolder {
        private TextView tvStep3Name;
        private TextView tvStep3IdCard;
        private TextView tvStep3Tel;
        private ImageButton ibDelete;
    }
}
