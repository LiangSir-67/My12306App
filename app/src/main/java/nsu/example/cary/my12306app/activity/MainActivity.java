package nsu.example.cary.my12306app.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import nsu.example.cary.my12306app.R;
import nsu.example.cary.my12306app.adapter.MainFragmentPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabLayout.Tab tabTicket,tabOrder,tabMy;
    private MainFragmentPagerAdapter mainFragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();//隐藏ActionBar
        setContentView(R.layout.activity_main);

        //viewpager和fragment绑定
        viewPager = findViewById(R.id.viewpager);
        mainFragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mainFragmentPagerAdapter);

        //tablayout和viewpager绑定
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        tabTicket = tabLayout.getTabAt(0);
        tabOrder = tabLayout.getTabAt(1);
        tabMy = tabLayout.getTabAt(2);



    }


}
