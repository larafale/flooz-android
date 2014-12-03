package flooz.android.com.flooz.UI.Tools;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.R;

/**
 * Created by Flooz on 10/14/14.
 */
public class CustomToast {

    public static void show(final Context ctx, final FLError content) {
        if (content.isVisible && content.type != null) {
            Handler mainHandler = new Handler(ctx.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    switch (content.type) {
                        case ErrorTypeSuccess:
                            CustomToast.showSuccess(ctx, content.title, content.text, content.time.intValue());
                            break;
                        case ErrorTypeWarning:
                            CustomToast.showWarning(ctx, content.title, content.text, content.time.intValue());
                            break;
                        case ErrorTypeError:
                            CustomToast.showError(ctx, content.title, content.text, content.time.intValue());
                            break;
                    }
                }
            });
        }
    }

    public static void showSuccess(Context ctx, String title, String content, int duration) {
        LayoutInflater myInflater = LayoutInflater.from(ctx);
        View view = myInflater.inflate(R.layout.alert_success, null);

        TextView titleView = (TextView) view.findViewById(R.id.alert_title);
        titleView.setText(title);

        TextView contentView = (TextView) view.findViewById(R.id.alert_content);
        contentView.setText(content);

        Toast toast = new Toast(ctx);

        toast.setView(view);
        toast.setDuration(duration);
        toast.show();
    }

    public static void showWarning(Context ctx, String title, String content, int duration) {
        LayoutInflater myInflater = LayoutInflater.from(ctx);
        View view = myInflater.inflate(R.layout.alert_warning, null);

        TextView titleView = (TextView) view.findViewById(R.id.alert_title);
        titleView.setText(title);

        TextView contentView = (TextView) view.findViewById(R.id.alert_content);
        contentView.setText(content);

        Toast toast = new Toast(ctx);

        toast.setView(view);
        toast.setDuration(duration);
        toast.show();
    }

    public static void showError(Context ctx, String title, String content, int duration) {
        LayoutInflater myInflater = LayoutInflater.from(ctx);
        View view = myInflater.inflate(R.layout.alert_error, null);

        TextView titleView = (TextView) view.findViewById(R.id.alert_title);
        titleView.setText(title);

        TextView contentView = (TextView) view.findViewById(R.id.alert_content);
        contentView.setText(content);

        Toast toast = new Toast(ctx);

        toast.setView(view);
        toast.setDuration(duration);
        toast.show();
    }
}
