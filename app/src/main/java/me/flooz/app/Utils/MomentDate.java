package me.flooz.app.Utils;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import me.flooz.app.R;

/**
 * Created by Flooz on 12/9/14.
 */
public class MomentDate {

    public Context context;
    public Date _date;

    public MomentDate(Context ctx, String dateString) throws ParseException {
        super();

        this.context = ctx;

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'", Locale.FRANCE);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        this._date = dateFormatter.parse(dateString);
    }

    public Date getDate() {
        return this._date;
    }

    public String fromNow() {
        return this.fromNow(true);
    }

    public String fromNow(Boolean suffixed) {
        return this.fromDate(new Date(), suffixed);
    }

    public String fromDate(Date date) {
        return this.fromDate(new Date(), true);
    }

    public String fromDate(Date date, Boolean suffixed) {

        long referenceTime = this._date.getTime() - date.getTime();

        double seconds = TimeUnit.MILLISECONDS.toSeconds(Math.abs(referenceTime));
        double minutes = Math.round(seconds / 60.0f);
        double hours = Math.round(minutes / 60.0f);
        double days = Math.round(hours / 24.0f);
        double years = Math.round(days / 365.0f);

        String formattedString;
        int unit = 0;

        if (seconds < 45) {
            formattedString = this.getLocalizedString(R.string.s);
            unit = (int)seconds;
        } else if (minutes == 1)
        {
            formattedString = this.getLocalizedString(R.string.m);
        } else if (minutes < 45)
        {
            formattedString = this.getLocalizedString(R.string.mm);
            unit = (int)minutes;
        } else if (hours == 1)
        {
            formattedString = this.getLocalizedString(R.string.h);
        } else if (hours < 22)
        {
            formattedString = this.getLocalizedString(R.string.hh);
            unit = (int)hours;
        } else if (days == 1)
        {
            formattedString = this.getLocalizedString(R.string.d);
        } else if (days <= 25)
        {
            formattedString = this.getLocalizedString(R.string.dd);
            unit = (int)days;
        } else if (days <= 45)
        {
            formattedString = this.getLocalizedString(R.string.M);
        } else if (days < 345)
        {
            formattedString = this.getLocalizedString(R.string.MM);
            unit = (int)Math.round(days / 30);
        } else if (years == 1)
        {
            formattedString = this.getLocalizedString(R.string.y);
        } else
        {
            formattedString = this.getLocalizedString(R.string.yy);
            unit = (int)years;
        }

        formattedString = String.format(formattedString, unit);

        if (suffixed) {
            Boolean isFuture = (referenceTime > 0);

            String suffixedString;
            if (isFuture)
                suffixedString = this.getLocalizedString(R.string.future);
            else
                suffixedString = this.getLocalizedString(R.string.past);

            formattedString = String.format(suffixedString, formattedString);

        }

        return formattedString;
    }

    private String getLocalizedString(int resId) {
        return this.context.getResources().getString(resId);
    }
}
