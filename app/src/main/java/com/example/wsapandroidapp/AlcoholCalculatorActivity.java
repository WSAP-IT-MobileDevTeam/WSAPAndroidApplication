package com.example.wsapandroidapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.wsapandroidapp.Fragments.BeerFragment;
import com.example.wsapandroidapp.Fragments.LiquorFragment;
import com.example.wsapandroidapp.Fragments.WineFragment;
import com.example.wsapandroidapp.Adapters.AlcoholCalculatorAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.DecimalFormat;

public class AlcoholCalculatorActivity extends AppCompatActivity {
    AlcoholCalculatorAdapter alcoholCalculator_Adapter;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    BeerFragment beer_fragment = new BeerFragment();
    LiquorFragment liquor_fragment = new LiquorFragment();
    WineFragment wine_fragment = new WineFragment();

    private final String[] titles= new String[]{"BEER","WINE","LIQUOR"};

    EditText guest_num,dur_hours;
    TextView tv_bartender_tip,tv_estimated_cost,tv_bartender_tip_result,tv_estimated_cost_result;
    String guest,hours,answer,alcohol_1,alcohol_2,alcohol_3;
    double activity_cost_of_alcohol_1_value,activity_cost_of_alcohol_2_value,activity_cost_of_alcohol_3_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alcohol_calculator);

        guest_num = findViewById(R.id.et_guest_num);
        dur_hours = findViewById(R.id.et_hours);
        tv_estimated_cost = findViewById(R.id.tv_estimated_cost);
        tv_bartender_tip = findViewById(R.id.tv_bartender_tip);
        tv_estimated_cost_result = findViewById(R.id.tv_estimated_cost_result);
        tv_bartender_tip_result = findViewById(R.id.tv_bartender_tip_result);

        viewPager2 = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        alcoholCalculator_Adapter = new AlcoholCalculatorAdapter(this,beer_fragment,liquor_fragment,wine_fragment);

        viewPager2.setAdapter(alcoholCalculator_Adapter);
        new TabLayoutMediator(tabLayout,viewPager2,((tab, position) -> tab.setText(titles[position]))).attach();

        guest_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                guest = guest_num.getText().toString();
                transferValue();
                updateCalculation();
                calculateTotal();
                if (guest.isEmpty()){
                    tv_estimated_cost_result.setText(R.string.default_value);
                    tv_bartender_tip_result.setText(R.string.default_value);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        dur_hours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hours = dur_hours.getText().toString();
                transferValue();
                updateCalculation();
                calculateTotal();
                if (hours.isEmpty()){
                    tv_estimated_cost_result.setText(R.string.default_value);
                    tv_bartender_tip_result.setText(R.string.default_value);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        beer_fragment.setFragmentListener(new BeerFragment.FragmentListener() {
            @Override
            public void past_cost_of_alcohol_1_value(double beer_answer) {
                activity_cost_of_alcohol_1_value = beer_answer;
                calculateTotal();
            }
        });

        wine_fragment.setFragmentListener(new WineFragment.FragmentListener() {
            @Override
            public void past_cost_of_alcohol_2_value(double wine_answer) {
                activity_cost_of_alcohol_2_value = wine_answer;
                calculateTotal();
            }
        });

        liquor_fragment.setFragmentListener(new LiquorFragment.FragmentListener() {
            @Override
            public void past_cost_of_alcohol_3_value(double liquor_answer) {
                activity_cost_of_alcohol_3_value = liquor_answer;
                calculateTotal();
            }
        });


    }

    public void calculateTotal() {
        alcohol_1 = String.valueOf(activity_cost_of_alcohol_1_value);
        alcohol_2 = String.valueOf(activity_cost_of_alcohol_2_value);
        alcohol_3 = String.valueOf(activity_cost_of_alcohol_3_value);
        if (alcohol_1 !=null && alcohol_2 !=null && alcohol_3 !=null){
            if (!alcohol_1.isEmpty() && !alcohol_2.isEmpty() && !alcohol_3.isEmpty()) {
                int beer_cost = (int) activity_cost_of_alcohol_1_value;
                int wine_cost = (int) activity_cost_of_alcohol_2_value;
                int liquor_cost = (int) activity_cost_of_alcohol_3_value;
                int final_answer = beer_cost + wine_cost + liquor_cost;
                if (final_answer > 0) {
                    answer = String.valueOf(final_answer);
                    double result_double = Double.parseDouble(answer);
                    DecimalFormat df = new DecimalFormat("#,###");
                    String result_final = df.format(result_double);
                    String result = ("₱ " + result_final);
                    tv_estimated_cost_result.setText(result);
                    double bartender_tip = (final_answer * 0.2d);
                    String tip_final = df.format(bartender_tip);
                    String bar_tip = ("₱ " + tip_final);
                    tv_bartender_tip_result.setText(bar_tip);
                }
                else {
                    tv_estimated_cost_result.setText(R.string.default_value);
                    tv_bartender_tip_result.setText(R.string.default_value);
                }
            }
        }
    }

    private void transferValue() {
        BeerFragment.passValues(guest,hours);
        WineFragment.passValues(guest,hours);
        LiquorFragment.passValues(guest,hours);
    }

    private void updateCalculation() {
        beer_fragment.calculateBeer();
        beer_fragment.calculateBeerBottle();
        wine_fragment.calculateWine();
        wine_fragment.calculateWineBottle();
        liquor_fragment.calculateLiquor();
        liquor_fragment.calculateLiquorBottle();
    }
}
