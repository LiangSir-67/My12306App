package nsu.example.cary.my12306app.activity.ticket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import nsu.example.cary.my12306app.R;
import nsu.example.cary.my12306app.activity.MainActivity;
import nsu.example.cary.my12306app.bean.Order;
import nsu.example.cary.my12306app.utils.ZxingUtils;

public class TicketPaySuccessStep5Activity extends AppCompatActivity {

    private Button btnTicketPaySuccessStep5;
    private ImageView ivQRCode;
    private TextView ticketPay5Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_pay_success_step5);

        btnTicketPaySuccessStep5 = findViewById(R.id.btnTicketPaySuccessStep5);
        ivQRCode= findViewById(R.id.ivQRCode);
        ticketPay5Id = findViewById(R.id.ticketPay5Id);

        Order order = (Order) getIntent().getSerializableExtra("order");
        ticketPay5Id.setText(order.getId());
        //创建二维码
        String data = order.getId()+","+order.getTrain().getTrainNo()+","+order.getPassengerList();
        ZxingUtils.createQRImage(data,ivQRCode,800,800);
        btnTicketPaySuccessStep5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(TicketPaySuccessStep5Activity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}
