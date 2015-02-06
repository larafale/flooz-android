package flooz.android.com.flooz.UI.Tools;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;

/**
 * Created by Flooz on 10/14/14.
 */
public class CustomToast {

    public static void show(final Context ctx, final FLError content) {
        if (content.isVisible && content.type != null && FloozApplication.appInForeground) {
            Handler mainHandler = new Handler(ctx.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    int layoutId;
                    switch (content.type) {
                        case ErrorTypeSuccess:
                            layoutId = R.layout.alert_success;
                            break;
                        case ErrorTypeWarning:
                            layoutId = R.layout.alert_warning;
                            break;
                        case ErrorTypeError:
                            layoutId = R.layout.alert_error;
                            break;
                        default:
                            return;
                    }

                    LayoutInflater myInflater = LayoutInflater.from(ctx);
                    View view = myInflater.inflate(layoutId, null);

                    TextView titleView = (TextView) view.findViewById(R.id.alert_title);
                    titleView.setText(content.title);

                    TextView contentView = (TextView) view.findViewById(R.id.alert_content);
                    contentView.setText(content.text);

                    view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

                    final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
                    popupWindow.setContentView(view);

                    view.findViewById(R.id.layout).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (int i = 0; i < content.triggers.size(); i++) {
                                FloozRestClient.getInstance().handleTrigger(content.triggers.get(i));
                            }
                            popupWindow.dismiss();
                        }
                    });

                    popupWindow.setOutsideTouchable(true);
                    popupWindow.setTouchable(true);

                    popupWindow.showAtLocation(FloozApplication.getInstance().getCurrentActivity().findViewById(android.R.id.content), Gravity.TOP, 0, view.getMeasuredHeight() / 2 + 10);
                    popupWindow.setAnimationStyle(R.style.alert_animation);

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (popupWindow.isShowing())
                                popupWindow.dismiss();
                        }
                    }, content.time.intValue() * 1000);
                }
            });
        }
    }
}
