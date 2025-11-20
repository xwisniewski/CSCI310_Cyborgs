package com.example.csci310_teamproj.util;

import android.os.ParcelFileDescriptor;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Utility class to disable animations during instrumented tests.
 * Espresso requires animations to be disabled for reliable test execution.
 */
public class AnimationDisabler {
    
    private static final float DISABLED = 0.0f;
    private static final float DEFAULT = 1.0f;
    
    private static float originalWindowAnimationScale;
    private static float originalTransitionAnimationScale;
    private static float originalAnimatorDurationScale;
    
    /**
     * Disables all animations on the device.
     * Should be called in @Before methods.
     */
    public static void disableAnimations() {
        originalWindowAnimationScale = getWindowAnimationScale();
        originalTransitionAnimationScale = getTransitionAnimationScale();
        originalAnimatorDurationScale = getAnimatorDurationScale();
        
        setWindowAnimationScale(DISABLED);
        setTransitionAnimationScale(DISABLED);
        setAnimatorDurationScale(DISABLED);
    }
    
    /**
     * Re-enables animations on the device.
     * Should be called in @After methods.
     */
    public static void enableAnimations() {
        setWindowAnimationScale(originalWindowAnimationScale);
        setTransitionAnimationScale(originalTransitionAnimationScale);
        setAnimatorDurationScale(originalAnimatorDurationScale);
    }
    
    private static float getWindowAnimationScale() {
        return getGlobalSetting("window_animation_scale");
    }
    
    private static float getTransitionAnimationScale() {
        return getGlobalSetting("transition_animation_scale");
    }
    
    private static float getAnimatorDurationScale() {
        return getGlobalSetting("animator_duration_scale");
    }
    
    private static void setWindowAnimationScale(float scale) {
        setGlobalSetting("window_animation_scale", scale);
    }
    
    private static void setTransitionAnimationScale(float scale) {
        setGlobalSetting("transition_animation_scale", scale);
    }
    
    private static void setAnimatorDurationScale(float scale) {
        setGlobalSetting("animator_duration_scale", scale);
    }
    
    private static float getGlobalSetting(String setting) {
        try {
            ParcelFileDescriptor pfd = InstrumentationRegistry.getInstrumentation()
                    .getUiAutomation()
                    .executeShellCommand("settings get global " + setting);
            
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new android.os.ParcelFileDescriptor.AutoCloseInputStream(pfd)));
            String value = reader.readLine();
            reader.close();
            
            if (value != null && !value.trim().isEmpty()) {
                return Float.parseFloat(value.trim());
            }
            return DEFAULT;
        } catch (Exception e) {
            return DEFAULT;
        }
    }
    
    private static void setGlobalSetting(String setting, float value) {
        try {
            String command = "settings put global " + setting + " " + value;
            ParcelFileDescriptor pfd = InstrumentationRegistry.getInstrumentation()
                    .getUiAutomation()
                    .executeShellCommand(command);
            if (pfd != null) {
                pfd.close();
            }
        } catch (Exception e) {
            // Ignore - some devices may not support this
        }
    }
}

