package me.flooz.app.Utils;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Model.FLNotification;

/**
 * Created by Flooz on 10/15/14.
 */
public class NotificationsManager {

    public List<FLNotification> notifications;
    public int nbUnreadNotifications;
    public String nextURL;

    public NotificationsManager() {
        this.notifications = new ArrayList<FLNotification>(0);
    }

    public void setNotifications(List<FLNotification> notifs) {
        this.notifications.clear();
        this.notifications.addAll(notifs);
        this.refreshUnreadNotifications();
    }

    public void addNotifications(List<FLNotification> notifs) {
        this.notifications.addAll(notifs);
        this.refreshUnreadNotifications();
    }

    public void refreshUnreadNotifications() {
        this.nbUnreadNotifications = 0;

        for (int i = 0; i < this.notifications.size(); i++) {
            if (!this.notifications.get(i).isRead)
                ++this.nbUnreadNotifications;
        }
    }

    public void setNextURL(String url) {
        this.nextURL = url;
    }
}
