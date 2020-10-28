package nsu.example.cary.my12306app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import nsu.example.cary.my12306app.R;

public class OrderAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String,Object>> datalist;
    private LayoutInflater inflater;

    public OrderAdapter(Context context,List<Map<String,Object>> datalist){
        this.context = context;
        this.datalist = datalist;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return datalist.size();
    }

    @Override
    public Object getItem(int i) {
        return datalist.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (holder == null){
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.item_order_adapter_list,null);
            holder.tvOrderId = view.findViewById(R.id.tvOrderId);
            holder.tvOrderStatus = view.findViewById(R.id.tvOrderStatus);
            holder.tvOrderTrainNo = view.findViewById(R.id.tvOrderTrainNo);
            holder.tvOrderDateFrom = view.findViewById(R.id.tvOrderDateFrom);
            holder.tvOrderStationFrom = view.findViewById(R.id.tvOrderStationFrom);
            holder.tvOrderPrice = view.findViewById(R.id.tvOrderPrice);
            holder.imgOrderFlag = view.findViewById(R.id.imgOrderFlag);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tvOrderId.setText(datalist.get(i).get("orderId").toString());
        holder.tvOrderStatus.setText(datalist.get(i).get("orderStatus").toString());
        if ("未支付".equals(datalist.get(i).get("orderStatus").toString())){
            holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.orange));
        }else if ("已支付".equals(datalist.get(i).get("orderStatus").toString())){
            holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.colorAccent));
        }
        holder.tvOrderTrainNo.setText(datalist.get(i).get("orderTrainNo").toString());
        holder.tvOrderDateFrom.setText(datalist.get(i).get("orderDateFrom").toString());
        holder.tvOrderStationFrom.setText(datalist.get(i).get("orderStationFrom").toString());
        holder.tvOrderPrice.setText(datalist.get(i).get("orderPrice").toString());

        Integer resId = (Integer) datalist.get(i).get("orderFlag");
        if (resId == null){
            holder.imgOrderFlag.setImageDrawable(null);
        }else {
            holder.imgOrderFlag.setImageDrawable(context.getResources().getDrawable(resId));
        }
        return view;
    }
    class ViewHolder{
        TextView tvOrderId;
        TextView tvOrderStatus;
        TextView tvOrderTrainNo;
        TextView tvOrderDateFrom;
        TextView tvOrderStationFrom;
        TextView tvOrderPrice;
        ImageView imgOrderFlag;
    }
}
