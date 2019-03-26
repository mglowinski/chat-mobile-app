package android.com.mobilechat;

import android.com.mobilechat.model.message.MessageDto;
import android.com.mobilechat.model.notification.Notification;
import android.com.mobilechat.model.notification.body.MessageDeletedBody;
import android.com.mobilechat.model.notification.body.MessageEditedBody;
import android.com.mobilechat.utils.JsonConverter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonConverterUnitTests {

    @Test
    public void testValidParseMessageDtoNotification() {
        String notificationInJsonForm = "{\"type\":\"ADD_MESSAGE\",\"body\":{\"messageId\":127," +
                "\"author\":{\"email\":\"student@mail.mail\",\"name\":\"Student Henryk\",\"surname\":\"Arbuz\"},\"roomId\":1,\"content\":\"eeee\",\"timestamp\":\"2019-01-08T18:39:49.764Z\"}}\n";

        Notification notification =
                JsonConverter.parseReceivedJsonToNotificationObject(notificationInJsonForm);

        assert notification != null;
        assertEquals(MessageDto.class, notification.getBody().getClass());
    }

    @Test
    public void testValidParseMessageEditedNotification() {
        String notificationInJsonForm = "{\"type\":\"MESSAGE_EDITED\"," +
                "\"body\":{\"messageId\":108,\"roomId\":16,\"messageNewContent\":\"Jan\"}}";

        Notification notification =
                JsonConverter.parseReceivedJsonToNotificationObject(notificationInJsonForm);

        assert notification != null;
        assertEquals(MessageEditedBody.class, notification.getBody().getClass());
    }

    @Test
    public void testValidParseMessageDeletedNotification() {
        String notificationInJsonForm = "{\"type\":\"MESSAGE_DELETED\"," +
                "\"body\":{\"messageId\":108,\"roomId\":16}}";

        Notification notification =
                JsonConverter.parseReceivedJsonToNotificationObject(notificationInJsonForm);

        assert notification != null;
        assertEquals(MessageDeletedBody.class, notification.getBody().getClass());
    }

}