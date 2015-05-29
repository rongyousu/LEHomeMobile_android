/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package my.home.lehome.helper;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

import java.lang.ref.WeakReference;

import my.home.common.PrefUtil;

/**
 * Created by legendmohe on 15/4/16.
 */
public class PushSDKManager {
    public final static String TAG = "PushSDKManager";

    public final static int MSG_START_SDK = 0;
    public final static int MSG_STOP_SDK = 1;

    private static WeakReference<Context> CURRENT_CONTEXT;

    private static int MAX_RETRY_TIME = 10;
    private static int START_RETRY_TIME = 0;
    private static int STOP_RETRY_TIME = 0;

    private static boolean starting = false;
    private static boolean stopping = false;

    private static final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final Context context = CURRENT_CONTEXT.get();
            if (context == null) {
                Log.d(TAG, "null context.");
                return;
            }
            if (msg.what == PushSDKManager.MSG_START_SDK) {
                Log.d(TAG, "try start sdk.");
                XGPushManager.registerPush(context, new XGIOperateCallback() {
                    @Override
                    public void onSuccess(Object o, int i) {
                        Log.d(TAG, "start sdk succeed." + context.hashCode());
                        PrefUtil.setBooleanValue(context, "PushSDKManager.enable", true);
                        starting = false;
                    }

                    @Override
                    public void onFail(Object o, int i, String s) {
                        Log.d(TAG, "start sdk faild.");
                        START_RETRY_TIME++;
                        if (START_RETRY_TIME <= MAX_RETRY_TIME) {
                            Message msg = Message.obtain();
                            msg.what = MSG_START_SDK;
                            handler.sendMessageDelayed(msg, 300);
                        }
                    }
                });
            } else if (msg.what == PushSDKManager.MSG_STOP_SDK) {
                Log.d(TAG, "try stop sdk.");
                XGPushManager.unregisterPush(context, new XGIOperateCallback() {
                    @Override
                    public void onSuccess(Object o, int i) {
                        Log.d(TAG, "stop sdk succeed:" + context.hashCode());
                        PrefUtil.setBooleanValue(context, "PushSDKManager.enable", false);
                        stopping = false;
                    }

                    @Override
                    public void onFail(Object o, int i, String s) {
                        Log.d(TAG, "stop sdk faild.");
                        STOP_RETRY_TIME++;
                        if (STOP_RETRY_TIME <= MAX_RETRY_TIME) {
                            Message msg = Message.obtain();
                            msg.what = MSG_STOP_SDK;
                            handler.sendMessageDelayed(msg, 300);
                        }
                    }
                });

                // don't stop sdk
//                PrefUtil.setBooleanValue(context, "PushSDKManager.enable", false);
            }
        }
    };

    public static void startPushSDKService(final Context context) {
        startPushSDKService(context, false);
    }

    public static void startPushSDKService(final Context context, boolean force) {
        boolean enable = PrefUtil.getbooleanValue(context, "PushSDKManager.enable", false);
        if (!enable || force) {
            Log.d(TAG, "start context: " + context.hashCode());
            if (!starting) {
                starting = true;
                CURRENT_CONTEXT = new WeakReference<>(context);
                Message msg = Message.obtain();
                msg.what = MSG_START_SDK;
                handler.sendMessage(msg);
            }
        } else {
            Log.d(TAG, "skip startPushSDKService");
        }
    }

    public static void stopPushSDKService(Context context) {
        boolean enable = PrefUtil.getbooleanValue(context, "PushSDKManager.enable", false);
        if (enable) {
            Log.d(TAG, "stop context: " + context.hashCode());
            if (!stopping) {
                stopping = true;
                CURRENT_CONTEXT = new WeakReference<>(context);
                Message msg = Message.obtain();
                msg.what = MSG_STOP_SDK;
                handler.sendMessage(msg);
            }
        } else {
            Log.d(TAG, "skip stopPushSDKService");
        }
    }
}
