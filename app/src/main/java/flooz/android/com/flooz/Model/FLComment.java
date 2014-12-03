package flooz.android.com.flooz.Model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Flooz on 9/9/14.
 */
public class FLComment {

    public FLUser user;
    public String content;
    public Date date;
    public String when;
    public String dateText;

    public FLComment(JSONObject json) {
        super();
        this.setJson(json);
    }

    private void setJson(JSONObject json) {
        try {
            this.content = json.getString("comment");
            this.user = new FLUser(json);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'");
            dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            this.date = dateFormatter.parse(json.getString("cAt"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

//    {
//        static NSDateFormatter *dateFormatter;
//        if(!dateFormatter){
//            dateFormatter = [NSDateFormatter new];
//            [dateFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
//            [dateFormatter setDateFormat:@"dd' 'MMMM', 'HH':'mm"];
//            [dateFormatter setDateStyle:NSDateFormatterShortStyle];
//            [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
//            [dateFormatter setDoesRelativeDateFormatting:YES];
//
//        }
//        NSTimeZone *currentTimeZone = [NSTimeZone localTimeZone];
//        NSTimeZone *utcTimeZone = [NSTimeZone timeZoneWithAbbreviation:@"UTC"];
//        NSInteger currentGMTOffset = [currentTimeZone secondsFromGMTForDate:_date];
//        NSInteger gmtOffset = [utcTimeZone secondsFromGMTForDate:_date];
//        NSTimeInterval gmtInterval = currentGMTOffset - gmtOffset;
//        NSDate *destinationDate = [[NSDate alloc] initWithTimeInterval:gmtInterval sinceDate:_date];
//
//        _dateText = [dateFormatter stringFromDate: destinationDate];
//    }
//
//    _when = [FLHelper formatedDateFromNow:_date];

}
