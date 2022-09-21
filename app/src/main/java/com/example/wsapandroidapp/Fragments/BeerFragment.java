package com.example.wsapandroidapp.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.wsapandroidapp.DataModel.InputFilterMinMax;
import com.example.wsapandroidapp.R;

import java.text.DecimalFormat;

public class BeerFragment extends Fragment {

    private static String str_guest;
    private static String str_hours;
    EditText et_percent_beer;
    EditText et_cost_of_alcohol_1;
    TextView tv_number_of_bottle_beer;
    TextView tv_estimated_cost_beer;
    String  percent_beer_value;
    String cost_of_alcohol_1_value;
    int int_guest,int_hours;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beer,container,false);
        et_percent_beer = view.findViewById(R.id.et_percent_beer);
        et_percent_beer.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        et_cost_of_alcohol_1 = view.findViewById(R.id.et_cost_of_alcohol_1);
        et_cost_of_alcohol_1.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "1000000")});
        tv_number_of_bottle_beer = view.findViewById(R.id.tv_number_of_bottle_beer);
        tv_estimated_cost_beer = view.findViewById(R.id.tv_estimated_cost_beer);

        et_percent_beer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                percent_beer_value = et_percent_beer.getText().toString();
                if(TextUtils.isEmpty(percent_beer_value)){
                    percent_beer_value = "0";
                    tv_number_of_bottle_beer.setText(R.string.bottle_default_value);
                    tv_estimated_cost_beer.setText(R.string.default_value);
                    calculateBeer();
                }
                else if(TextUtils.isEmpty(cost_of_alcohol_1_value)){
                    tv_estimated_cost_beer.setText(R.string.default_value);
                    calculateBeerBottle();
                }
                else {
                    calculateBeer();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_cost_of_alcohol_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cost_of_alcohol_1_value = et_cost_of_alcohol_1.getText().toString();
                if (TextUtils.isEmpty(percent_beer_value) && TextUtils.isEmpty(cost_of_alcohol_1_value)){
                    cost_of_alcohol_1_value = "0";
                    calculateBeer();
                }

                else if (TextUtils.isEmpty(cost_of_alcohol_1_value)){
                    cost_of_alcohol_1_value = "0";
                    calculateBeerBottle();
                    calculateBeer();
                }
                else {
                    calculateBeer();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    public void calculateBeer() {
        if(percent_beer_value !=null && cost_of_alcohol_1_value !=null && str_guest !=null && str_hours !=null){
            if(!percent_beer_value.isEmpty() && !cost_of_alcohol_1_value.isEmpty() && !str_guest.isEmpty() && !str_hours.isEmpty()){
                int_guest = Integer.parseInt(str_guest);
                int_hours = Integer.parseInt(str_hours);
                int beer_percent = Integer.parseInt(percent_beer_value);
                int beer_cost = Integer.parseInt(cost_of_alcohol_1_value);
                double beerDrinkers;
                double beer_answer;
                double beer_to_buy;
                int int_beer;

                beerDrinkers = (beer_percent / 100d) * int_guest;
                beer_answer = (((beerDrinkers * int_hours) * 2) * beer_cost);
                beer_to_buy = Math.ceil(beerDrinkers * 2 * int_hours);
                int_beer = (int) beer_to_buy;
                String estimated_beer_cost = String.valueOf(beer_answer);
                String beer_bottles = String.valueOf(int_beer);
                double result_double = Double.parseDouble(estimated_beer_cost);
                DecimalFormat df = new DecimalFormat("#,###");
                String result_beer = df.format(result_double);
                tv_number_of_bottle_beer.setText(beer_bottles);
                tv_estimated_cost_beer.setText("₱ " + result_beer);
                if (fragmentListener != null)fragmentListener.past_cost_of_alcohol_1_value(beer_answer);
            }
            else {
                tv_estimated_cost_beer.setText(R.string.default_value);
            }
        }
    }

    public void calculateBeerBottle() {
        if(percent_beer_value !=null && str_guest !=null && str_hours!=null){
            if(!percent_beer_value.isEmpty() && !str_guest.isEmpty() && !str_hours.isEmpty()){
                int_guest = Integer.parseInt(str_guest);
                int_hours = Integer.parseInt(str_hours);
                int beer_percent = Integer.parseInt(percent_beer_value);
                double beerDrinkers;
                double beer_to_buy;
                int int_beer;

                beerDrinkers = (beer_percent / 100d) * int_guest;
                beer_to_buy = Math.ceil(beerDrinkers * 2 * int_hours);
                int_beer = (int) beer_to_buy;
                String beer_bottles = String.valueOf(int_beer);
                tv_number_of_bottle_beer.setText(beer_bottles);
            }
            else {
                tv_estimated_cost_beer.setText(R.string.default_value);
                tv_number_of_bottle_beer.setText(R.string.bottle_default_value);
            }
        }
    }

    public static void passValues(String guest, String hours) {
        str_guest = guest;
        str_hours = hours;
    }

    private FragmentListener fragmentListener;

    public interface FragmentListener {
        void past_cost_of_alcohol_1_value(double beer_answer);
    }

    public void setFragmentListener(FragmentListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }
}