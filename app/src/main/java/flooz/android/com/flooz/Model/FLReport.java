package flooz.android.com.flooz.Model;

/**
 * Created by Flooz on 12/11/14.
 */
public class FLReport {

    public enum ReportType {
        User,
        Transaction
    }

    public ReportType type;
    public String resourceId;
    public String comment;

    public FLReport(ReportType reportType, String id) {
        this.type = reportType;
        this.resourceId = id;
        this.comment = "";
    }

    public String convertReportTypeToParam() {
        switch (this.type) {
            case User:
                return "user";
            case Transaction:
                return "line";
        }
        return "";
    }
}
