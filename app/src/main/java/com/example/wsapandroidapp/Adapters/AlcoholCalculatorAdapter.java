package com.example.wsapandroidapp.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.wsapandroidapp.Fragments.BeerFragment;
import com.example.wsapandroidapp.Fragments.LiquorFragment;
import com.example.wsapandroidapp.Fragments.WineFragment;

public class AlcoholCalculatorAdapter extends FragmentStateAdapter
{
    BeerFragment beer_fragment;
    LiquorFragment liquor_fragment;
    WineFragment wine_fragment;
    public AlcoholCalculatorAdapter(@NonNull FragmentActivity fragmentActivity, BeerFragment beer_fragment, LiquorFragment liquor_fragment, WineFragment wine_fragment) {
        super(fragmentActivity);
        this.beer_fragment = beer_fragment;
        this.liquor_fragment = liquor_fragment;
        this.wine_fragment = wine_fragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position)
        {
            case 0:
                return beer_fragment;
            case 1:
                return wine_fragment;
            case 2:
                return liquor_fragment;
        }

        return beer_fragment;

    }

    @Override
    public int getItemCount() {return 3;}
}