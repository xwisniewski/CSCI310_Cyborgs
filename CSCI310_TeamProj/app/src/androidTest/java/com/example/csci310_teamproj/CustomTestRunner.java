package com.example.csci310_teamproj;

import android.os.Bundle;
import androidx.test.runner.AndroidJUnitRunner;

public class CustomTestRunner extends AndroidJUnitRunner {
    @Override
    public void onCreate(Bundle arguments) {

        if (arguments == null) {
            arguments = new Bundle();
        }

        // Speed up Espresso tests
        arguments.putString("disableAnalytics", "true");
        arguments.putString("windowAnimationScale", "0");
        arguments.putString("transitionAnimationScale", "0");
        arguments.putString("animatorDurationScale", "0");

        super.onCreate(arguments);
    }
}
