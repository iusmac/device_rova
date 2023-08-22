/*
 * Copyright (C) 2018 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.dirac;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.UserHandle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;

import dagger.Lazy;
import dagger.hilt.android.qualifiers.ApplicationContext;

import java.lang.IllegalArgumentException;
import java.util.List;

import javax.inject.Inject;

public final class DiracUtils {
    private final Context mContext;
    private final Lazy<MediaSessionManager> mMediaSessionManagerLazy;
    private final Lazy<AlarmManager> mAlarmManagerLazy;
    private final DiracSound mDiracSound;

    private final Handler mHandler;

    @Inject
    public DiracUtils(final @ApplicationContext Context context,
            final Lazy<MediaSessionManager> mediaSessionManagerLazy,
            final Lazy<AlarmManager> alarmManagerLazy,
            final DiracSound diracSound) {

        mContext = context;
        mMediaSessionManagerLazy = mediaSessionManagerLazy;
        mAlarmManagerLazy = alarmManagerLazy;
        mDiracSound = diracSound;

        final HandlerThread handlerThread = new HandlerThread("DiracUtilsThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    public void onBootCompleted() {
        postAlarmTask(/*minutes=*/ 1);
    }

    private void postAlarmTask(final int minutes) {
        final Intent i = new Intent(mContext, DiracInitializer.class);
        final PendingIntent pi = PendingIntent.getBroadcast(mContext, /*requestCode=*/ 0, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        final long millis = (System.currentTimeMillis() / 1000L + minutes * 60) * 1000L;
        mAlarmManagerLazy.get().setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi);
    }


    protected void refreshPlaybackIfNecessary(){
        final List<MediaController> sessions
                = mMediaSessionManagerLazy.get().getActiveSessionsForUser(null, UserHandle.ALL);
        for (MediaController aController : sessions) {
            if (PlaybackState.STATE_PLAYING ==
                    getMediaControllerPlaybackState(aController)) {
                triggerPlayPause(aController);
                break;
            }
        }
    }

    private void triggerPlayPause(MediaController controller) {
        long when = SystemClock.uptimeMillis();
        final KeyEvent evDownPause = new KeyEvent(when, when, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
        final KeyEvent evUpPause = KeyEvent.changeAction(evDownPause, KeyEvent.ACTION_UP);
        final KeyEvent evDownPlay = new KeyEvent(when, when, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
        final KeyEvent evUpPlay = KeyEvent.changeAction(evDownPlay, KeyEvent.ACTION_UP);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                controller.dispatchMediaButtonEvent(evDownPause);
            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                controller.dispatchMediaButtonEvent(evUpPause);
            }
        }, 20);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                controller.dispatchMediaButtonEvent(evDownPlay);
            }
        }, 1000);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                controller.dispatchMediaButtonEvent(evUpPlay);
            }
        }, 1020);
    }

    private int getMediaControllerPlaybackState(MediaController controller) {
        if (controller != null) {
            final PlaybackState playbackState = controller.getPlaybackState();
            if (playbackState != null) {
                return playbackState.getState();
            }
        }
        return PlaybackState.STATE_NONE;
    }
    protected void setEnabled(boolean enable) {
        mDiracSound.setEnabled(enable);
        mDiracSound.setMusic(enable ? 1 : 0);
        if (enable) {
            refreshPlaybackIfNecessary();
        }
    }

    protected boolean isDiracEnabled() {
        return mDiracSound.getMusic() == 1;
    }

    protected void setLevel(String preset) {
        String[] level = preset.split("\\s*,\\s*");

        for (int band = 0; band <= level.length - 1; band++) {
            mDiracSound.setLevel(band, Float.valueOf(level[band]));
        }
    }

    protected String getLevel() {
        String selected = "";
        for (int band = 0; band <= 6; band++) {
            int temp = (int) mDiracSound.getLevel(band);
            selected += String.valueOf(temp);
            if (band != 6) selected += ",";
        }
        return selected;
    }

    protected void setHeadsetType(int paramInt) {
         mDiracSound.setHeadsetType(paramInt);
    }

    protected int getHeadsetType() {
        return mDiracSound.getHeadsetType();
    }
}
