package android.com.mobilechat.utils;

import android.com.mobilechat.model.message.MessageDto;
import android.com.mobilechat.model.notification.Notification;
import android.com.mobilechat.model.notification.body.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class JsonConverter {

    private static Gson gson = new Gson();

    public static <T> Notification<T> parseReceivedJsonToNotificationObject(String json) {
        Type collectionType = null;

        JsonParser parser = new JsonParser();
        JsonObject rootObj = parser.parse(json).getAsJsonObject();

        String notificationType = rootObj.get("type").getAsString();

        switch (notificationType) {
            case "ADD_MESSAGE":
                collectionType = new TypeToken<Notification<MessageDto>>() {
                }.getType();
                break;

            case "MESSAGE_DELETED":
                collectionType = new TypeToken<Notification<MessageDeletedBody>>() {
                }.getType();
                break;

            case "MESSAGE_EDITED":
                collectionType = new TypeToken<Notification<MessageEditedBody>>() {
                }.getType();
                break;

            case "BECAME_ROOM_MEMBER":
                collectionType = new TypeToken<Notification<BecameRoomMemberBody>>() {
                }.getType();
                break;

            case "ADD_FILE":
                collectionType = new TypeToken<Notification<AddFileBody>>() {
                }.getType();
                break;

            case "FILE_DELETED":
                collectionType = new TypeToken<Notification<FileDeletedBody>>() {
                }.getType();
                break;

            case "MEMBER_REMOVED":
                collectionType = new TypeToken<Notification<MemberRemovedBody>>() {
                }.getType();
                break;

            case "ROOM_ARCHIVED":
                collectionType = new TypeToken<Notification<RoomArchivedBody>>() {
                }.getType();
                break;
        }

        if (collectionType != null) {
            return gson.fromJson(json, collectionType);
        }

        return null;
    }

}
