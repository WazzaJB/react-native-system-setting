package com.ninty.system.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

/**
 * Created by ninty on 2017/5/29.
 */

public class SystemSetting extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private String TAG = SystemSetting.class.getSimpleName();

    private static final String VOL_VOICE_CALL = "call";
    private static final String VOL_SYSTEM = "system";
    private static final String VOL_RING = "ring";
    private static final String VOL_MUSIC = "music";
    private static final String VOL_ALARM = "alarm";
    private static final String VOL_NOTIFICATION = "notification";

    private ReactApplicationContext mContext;
    private AudioManager am;
    private BroadcastReceiver volumeBR;
    private IntentFilter filter;

    public SystemSetting(ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
        reactContext.addLifecycleEventListener(this);
        am = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        listenVolume(reactContext);
    }

    private void listenVolume(final ReactApplicationContext reactContext) {
        volumeBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                    WritableMap para = Arguments.createMap();
                    para.putDouble("value", getNormalizationVolume(VOL_MUSIC));
                    para.putDouble(VOL_VOICE_CALL, getNormalizationVolume(VOL_VOICE_CALL));
                    para.putDouble(VOL_SYSTEM, getNormalizationVolume(VOL_SYSTEM));
                    para.putDouble(VOL_RING, getNormalizationVolume(VOL_RING));
                    para.putDouble(VOL_MUSIC, getNormalizationVolume(VOL_MUSIC));
                    para.putDouble(VOL_ALARM, getNormalizationVolume(VOL_ALARM));
                    para.putDouble(VOL_NOTIFICATION, getNormalizationVolume(VOL_NOTIFICATION));
                    try {
                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("EventVolume", para);
                    } catch (RuntimeException e) {
                        // Possible to interact with volume before JS bundle execution is finished. 
                        // This is here to avoid app crashing.
                    }
                }
            }
        };
        filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");

        reactContext.registerReceiver(volumeBR, filter);
    }

    @Override
    public String getName() {
        return SystemSetting.class.getSimpleName();
    }

    @ReactMethod
    public void setVolume(float val, ReadableMap config) {
        mContext.unregisterReceiver(volumeBR);
        String type = config.getString("type");
        boolean playSound = config.getBoolean("playSound");
        boolean showUI = config.getBoolean("showUI");
        int volType = getVolType(type);
        int flags = 0;
        if (playSound) {
            flags |= AudioManager.FLAG_PLAY_SOUND;
        }
        if (showUI) {
            flags |= AudioManager.FLAG_SHOW_UI;
        }
        am.setStreamVolume(volType, (int) (val * am.getStreamMaxVolume(volType)), flags);
        mContext.registerReceiver(volumeBR, filter);
    }

    @ReactMethod
    public void getVolume(String type, Promise promise) {
        promise.resolve(getNormalizationVolume(type));
    }

    private float getNormalizationVolume(String type) {
        int volType = getVolType(type);
        return am.getStreamVolume(volType) * 1.0f / am.getStreamMaxVolume(volType);
    }

    private int getVolType(String type) {
        switch (type) {
            case VOL_VOICE_CALL:
                return AudioManager.STREAM_VOICE_CALL;
            case VOL_SYSTEM:
                return AudioManager.STREAM_SYSTEM;
            case VOL_RING:
                return AudioManager.STREAM_RING;
            case VOL_ALARM:
                return AudioManager.STREAM_ALARM;
            case VOL_NOTIFICATION:
                return AudioManager.STREAM_NOTIFICATION;
            default:
                return AudioManager.STREAM_MUSIC;
        }
    }

    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        mContext.unregisterReceiver(volumeBR);
        mContext.removeLifecycleEventListener(this);
    }
}
