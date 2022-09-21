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

public class LiquorFragment extends Fragment {

    private static String str_guest;
    private static String str_hours;
    EditText et_percent_liquor;
    EditText et_cost_of_alcohol_3;
    TextView tv_number_of_bottle_liquor;
    TextView tv_estimated_cost_liquor;
    String  percent_liquor_value;
    String cost_of_alcohol_3_value;
    int int_guest,int_hours;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_liquor,container,false);
        et_percent_liquor = view.findViewById(R.id.et_percent_liquor);
        et_percent_liquor.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        et_cost_of_alcohol_3 = view.findViewById(R.id.et_cost_of_alcohol_3);
        et_cost_of_alcohol_3.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "1000000")});
        tv_number_of_bottle_liquor = view.findViewById(R.id.tv_number_of_bottle_liquor);
        tv_estimated_cost_liquor = view.findViewById(R.id.tv_estimated_cost_liquor);

        et_percent_liquor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                percent_liquor_value = et_percent_liquor.getText().toString();
                if(TextUtils.isEmpty(percent_liquor_value)){
                    percent_liquor_value = "0";
                    tv_number_of_bottle_liquor.setText(R.string.bottle_default_value);
                    tv_estimated_cost_liquor.setText(R.string.default_value);
                    calculateLiquor();
                }
                else if(TextUtils.isEmpty(cost_of_alcohol_3_value)){
                    tv_estimated_cost_liquor.setText(R.string.default_value);
                    calculateLiquorBottle();
                }
                else {
                    calculateLiquor();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_cost_of_alcohol_3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cost_of_alcohol_3_value = et_cost_of_alcohol_3.getText().toString();
                if (TextUtils.isEmpty(percent_liquor_value) && TextUtils.isEmpty(cost_of_alcohol_3_value)){
                    cost_of_alcohol_3_value = "0";
                    calculateLiquor();
                }

                else if (TextUtils.isEmpty(cost_of_alcohol_3_value)){
                    cost_of_alcohol_3_value = "0";
                    calculateLiquorBottle();
                    calculateLiquor();
                }
                else {
                    calculateLiquor();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    public void calculateLiquor() {
        if(percent_liquor_value !=null && cost_of_alcohol_3_value != null && str_guest !=null && str_hours !=null){
            if(!percent_liquor_value.isEmpty() && !cost_of_alcohol_3_value.isEmpty() && !str_guest.isEmpty() && !str_hours.isEmpty()){
                int_guest = Integer.parseInt(str_guest);
                int_hours = Integer.parseInt(str_hours);
                int liquor_percent = Integer.parseInt(percent_liquor_value);
                int liquor_cost = Integer.parseInt(cost_of_alcohol_3_value);
                double liquorDrinkers;
                double liquor_answer;
                double liquor_to_buy;
                int int_liquor;

                liquorDrinkers = (liquor_percent / 100d) * int_guest;
                //2 is number of hours
                liquor_answer = ((((liquorDrinkers * int_hours) * 2)/18) * liquor_cost);
                liquor_to_buy = Math.ceil((liquorDrinkers * 2 * int_hours)/18);
                int_liquor = (int) liquor_to_buy;
                String estimated_liquor_cost = String.valueOf(liquor_answer);
                String liquor_bottles = String.valueOf(int_liquor);
                double result_double = Double.parseDouble(estimated_liquor_cost);
                DecimalFormat df = new DecimalFormat("#,###");
                String result_liquor = df.format(result_double);
                tv_number_of_bottle_liquor.setText(liquor_bottles);
                tv_estimated_cost_liquor.setText("₱ " + result_liquor);
                if (fragmentListener != null)fragmentListener.past_cost_of_alcohol_3_value(liquor_answer);
            }
            else {
                tv_estimated_cost_liquor.setText(R.string.default_value);
            }
        }
    }

    public void calculateLiquorBottle() {
        if(percent_liquor_value != null && str_guest !=null && str_hours!=null){
            if (!percent_liquor_value.isEmpty() && !str_guest.isEmpty() && !str_hours.isEmpty()){
                int_guest = Integer.parseInt(str_guest);
                int_hours = Integer.parseInt(str_hours);
                int liquor_percent = Integer.parseInt(percent_liquor_value);
                double liquorDrinkers;
                double liquor_to_buy;
                int int_liquor;

                liquorDrinkers = (liquor_percent / 100d) * int_guest;
                liquor_to_buy = Math.ceil((liquorDrinkers * 2 * int_hours)/18);
                int_liquor = (int) liquor_to_buy;
                String liquor_bottles = String.valueOf(int_liquor);
                tv_number_of_bottle_liquor.setText(liquor_bottles);
            }
            else {
                tv_estimated_cost_liquor.setText(R.string.default_value);
                tv_number_of_bottle_liquor.setText(R.string.default_value);
            }
        }
    }

    public static void passValues(String guest, String hours) {
        str_guest = guest;
        str_hours = hours;
    }

    private LiquorFragment.FragmentListener fragmentListener;

    public interface FragmentListener {
        void past_cost_of_alcohol_3_value(double liquor_answer);
    }

    public void setFragmentListener(LiquorFragment.FragmentListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }
}