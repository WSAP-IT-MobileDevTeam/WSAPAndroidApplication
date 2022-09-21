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

public class WineFragment extends Fragment {

    private static String str_guest;
    private static String str_hours;
    EditText et_percent_wine;
    EditText et_cost_of_alcohol_2;
    TextView tv_number_of_bottle_wine;
    TextView tv_estimated_cost_wine;
    String  percent_wine_value;
    String cost_of_alcohol_2_value;
    int int_guest,int_hours;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wine,container,false);
        et_percent_wine = view.findViewById(R.id.et_percent_wine);
        et_percent_wine.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        et_cost_of_alcohol_2 = view.findViewById(R.id.et_cost_of_alcohol_2);
        et_cost_of_alcohol_2.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "1000000")});
        tv_number_of_bottle_wine = view.findViewById(R.id.tv_number_of_bottle_wine);
        tv_estimated_cost_wine = view.findViewById(R.id.tv_estimated_cost_wine);

        et_percent_wine.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                percent_wine_value = et_percent_wine.getText().toString();
                if(TextUtils.isEmpty(percent_wine_value)){
                    percent_wine_value = "0";
                    tv_number_of_bottle_wine.setText(R.string.bottle_default_value);
                    tv_estimated_cost_wine.setText(R.string.default_value);
                    calculateWine();
                }
                else if(TextUtils.isEmpty(cost_of_alcohol_2_value)){
                    tv_estimated_cost_wine.setText(R.string.default_value);
                    calculateWineBottle();
                }
                else {
                    calculateWine();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_cost_of_alcohol_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cost_of_alcohol_2_value = et_cost_of_alcohol_2.getText().toString();
                if (TextUtils.isEmpty(percent_wine_value) && TextUtils.isEmpty(cost_of_alcohol_2_value)){
                    cost_of_alcohol_2_value = "0";
                    calculateWine();
                }

                else if (TextUtils.isEmpty(cost_of_alcohol_2_value)){
                    cost_of_alcohol_2_value = "0";
                    calculateWineBottle();
                    calculateWine();
                }
                else {
                    calculateWine();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }
    public void calculateWine() {
        if(percent_wine_value !=null && cost_of_alcohol_2_value !=null && str_guest !=null && str_hours !=null){
            if(!percent_wine_value.isEmpty() && !cost_of_alcohol_2_value.isEmpty() && !str_guest.isEmpty() && !str_hours.isEmpty()){
                int_guest = Integer.parseInt(str_guest);
                int_hours = Integer.parseInt(str_hours);
                int wine_percent = Integer.parseInt(percent_wine_value);
                int wine_cost = Integer.parseInt(cost_of_alcohol_2_value);
                double wineDrinkers;
                double wine_answer;
                double wine_to_buy;
                int int_wine;

                wineDrinkers = (wine_percent / 100d) * int_guest;
                wine_answer = ((((wineDrinkers * int_hours) * 2)/5) * wine_cost);
                wine_to_buy = Math.ceil((wineDrinkers * 2 * int_hours)/5);
                int_wine = (int) wine_to_buy;
                String estimated_wine_cost = String.valueOf(wine_answer);
                String wine_bottles = String.valueOf(int_wine);
                double result_double = Double.parseDouble(estimated_wine_cost);
                DecimalFormat df = new DecimalFormat("#,###");
                String result_wine = df.format(result_double);
                tv_number_of_bottle_wine.setText(wine_bottles);
                tv_estimated_cost_wine.setText("₱ " + result_wine);
                if (fragmentListener != null)fragmentListener.past_cost_of_alcohol_2_value(wine_answer);
            }
            else {
                tv_estimated_cost_wine.setText(R.string.default_value);
            }
        }
    }

    public void calculateWineBottle() {
        if(percent_wine_value !=null && str_guest !=null && str_hours!=null){
            if(!percent_wine_value.isEmpty() && !str_guest.isEmpty() && !str_hours.isEmpty()){
                int_guest = Integer.parseInt(str_guest);
                int_hours = Integer.parseInt(str_hours);
                int wine_percent = Integer.parseInt(percent_wine_value);
                double wineDrinkers;
                double wine_to_buy;
                int int_wine;

                wineDrinkers = (wine_percent / 100d) * int_guest;
                wine_to_buy = Math.ceil((wineDrinkers * 2 * int_hours)/5);
                int_wine = (int) wine_to_buy;
                String wine_bottles = String.valueOf(int_wine);
                tv_number_of_bottle_wine.setText(wine_bottles);
            }
            else {
                tv_estimated_cost_wine.setText(R.string.default_value);
                tv_number_of_bottle_wine.setText(R.string.bottle_default_value);
            }
        }
    }

    public static void passValues(String guest, String hours) {
        str_guest = guest;
        str_hours = hours;
    }

    private WineFragment.FragmentListener fragmentListener;

    public interface FragmentListener {
        void past_cost_of_alcohol_2_value(double wine_answer);
    }

    public void setFragmentListener(WineFragment.FragmentListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }
}