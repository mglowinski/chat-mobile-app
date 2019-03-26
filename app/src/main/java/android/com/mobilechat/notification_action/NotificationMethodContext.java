package android.com.mobilechat.notification_action;

public class NotificationMethodContext {

    private NotificationMethod strategy;

    public void setNotificationMethodStrategy(NotificationMethod strategy) {
        this.strategy = strategy;
    }

    public void createReactOnNotification(Object object) {
        strategy.reactOnNotification(object);
    }
}
