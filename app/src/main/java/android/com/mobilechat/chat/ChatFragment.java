package android.com.mobilechat.chat;

import android.Manifest;
import android.com.mobilechat.R;
import android.com.mobilechat.adapter.MessagesAdapter;
import android.com.mobilechat.model.message.MessageDto;
import android.com.mobilechat.model.message.MessageFormDto;
import android.com.mobilechat.model.notification.Notification;
import android.com.mobilechat.model.room.RoomDto;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.com.mobilechat.notification_action.NotificationMethodContext;
import android.com.mobilechat.notification_action.strategy.*;
import android.com.mobilechat.room.EditRoomFragment;
import android.com.mobilechat.service.ServiceGenerator;
import android.com.mobilechat.service.requests.FileService;
import android.com.mobilechat.service.requests.MessageService;
import android.com.mobilechat.utils.*;
import android.com.mobilechat.web_socket.WebSocketClient;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import lombok.NoArgsConstructor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.StompHeader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;

@NoArgsConstructor
public class ChatFragment extends Fragment implements WebSocketClient.Callback,
        MessagesAdapter.ClickListener {

    public static final String TAG = ChatFragment.class.getName();
    public static final String SPLIT_FILE_REGEX = ";";

    private static final long MESSAGES_LIMIT = 100;

    private static final int SHOW_GALLERY_REQUEST_CODE = 1;
    private static final int SHOW_FILES_REQUEST_CODE = 3;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 4;

    private WebSocketClient webSocketClient;
    private RoomDto roomDto;
    private List<MessageDto> messageDtos = new ArrayList<>();
    private RecyclerView recyclerViewRoomMessages;
    private AlertDialog dialog;
    private boolean isGallerySourceChosen;
    private BottomSheetDialog bottomSheetDialog;
    private NotificationMethodContext notificationMethodContext = new NotificationMethodContext();

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            roomDto = (RoomDto) bundle.getSerializable("roomDto");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configureSendingMessage(view);
        configureMessagesRecyclerView(view);
        configureToolbar(view);
        configureChooseFile(view);
    }

    @Override
    public void onResume() {
        super.onResume();

        createWebSocketClient();
        initConnection();
        subscribeOnTopic();
        getRoomMessagesRequest();
    }

    @Override
    public void onStop() {
        super.onStop();
        closeConnection();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_chat_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                loadEditRoomFragment();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createWebSocketClient() {
        webSocketClient = new WebSocketClient(this);
    }

    private void initConnection() {
        webSocketClient.initConnection(createInitConnectionRequestHeader());
    }

    private List<StompHeader> createInitConnectionRequestHeader() {
        List<StompHeader> headers = new ArrayList<>();

        StompHeader token = new StompHeader("Authorization", "Bearer " + TokenStore.getToken(getContext()));
        StompHeader contentType = new StompHeader("Content-Type", ConnectionProperties.CONTENT_TYPE);
        StompHeader acceptVersion = new StompHeader("accept-version", ConnectionProperties.ACCEPT_VERSION);
        StompHeader heartBeat = new StompHeader("heart-beat", ConnectionProperties.HEART_BEAT);

        headers.add(token);
        headers.add(contentType);
        headers.add(acceptVersion);
        headers.add(heartBeat);

        return headers;
    }

    private void closeConnection() {
        webSocketClient.closeConnection();
    }

    private void subscribeOnTopic() {
        webSocketClient.subscribeOnMessage(ConnectionProperties.SUBSCRIBE_TOPIC_PATH);
    }

    private void loadEditRoomFragment() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            EditRoomFragment editRoomFragment = EditRoomFragment.newInstance();
            editRoomFragment.setArguments(loadRoomToBundle());

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, editRoomFragment, EditRoomFragment.TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private Bundle loadRoomToBundle() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("roomDto", roomDto);
        return bundle;
    }

    @Override
    public void onMessageReceived(String jsonMessageForm) {
        Log.e("MSG", jsonMessageForm);
        Notification notification = parseReceivedJsonMessage(jsonMessageForm);
        NotificationMethod notificationMethod = null;

        if (notification != null) {
            switch (notification.getType()) {
                case ADD_MESSAGE:
                    notificationMethod = new AddMessageChatNotificationStrategy(recyclerViewRoomMessages);
                    break;

                case MESSAGE_DELETED:
                    notificationMethod = new MessageDeletedNotificationStrategy(recyclerViewRoomMessages);
                    break;

                case MESSAGE_EDITED:
                    notificationMethod = new MessageEditedNotificationStrategy(recyclerViewRoomMessages);
                    break;

                case ADD_FILE:
                    notificationMethod = new AddFileChatNotificationStrategy(recyclerViewRoomMessages);
                    break;

                case FILE_DELETED:
                    notificationMethod = new FileDeletedNotificationStrategy(recyclerViewRoomMessages);
                    break;

                case BECAME_ROOM_MEMBER:
                    notificationMethod = new BecameRoomMemberChatNotificationStrategy(getContext());
                    break;

                case ROOM_ARCHIVED:
                    notificationMethod = new RoomArchivedChatNotificationStrategy(getContext());
                    break;

                case MEMBER_REMOVED:
                    notificationMethod = new MemberRemovedChatNotificationStrategy(getContext(),
                            getFragmentManager(),
                            TokenStore.decodeToken(TokenStore.getToken(getContext())).getSub(),
                            roomDto.getId());
                    break;
            }

            if (notificationMethod != null) {
                notificationMethodContext.setNotificationMethodStrategy(notificationMethod);
                notificationMethodContext.createReactOnNotification(notification.getBody());
            }
        }
    }

    private void configureChooseFile(View view) {
        ImageView chooseFileButton = view.findViewById(R.id.btn_choose_file);
        chooseFileButton.setOnClickListener(v -> initModalBottomSheet());
    }

    public void initModalBottomSheet() {
        View modalBottomSheetView = getLayoutInflater().inflate(R.layout.modal_bottomsheet, null);

        bottomSheetDialog = configureBottomSheetDialog(modalBottomSheetView);

        bottomSheetDialog.show();

        Button btnGallery = modalBottomSheetView.findViewById(R.id.btn_gallery);
        btnGallery.setOnClickListener(v -> {
            isGallerySourceChosen = true;

            if (isReadStoragePermissionGranted()) {
                gallerySourceShow();
            }
        });

        Button btnFiles = modalBottomSheetView.findViewById(R.id.btn_files);
        btnFiles.setOnClickListener(v -> {
            isGallerySourceChosen = false;

            if (isReadStoragePermissionGranted()) {
                filesSourceShow();
            }
        });

        Button btnCancel = modalBottomSheetView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> closeBottomSheetDialog());
    }

    private boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && isGallerySourceChosen) {
                    gallerySourceShow();
                } else if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && !isGallerySourceChosen) {
                    filesSourceShow();
                }
                break;
        }
    }

    private BottomSheetDialog configureBottomSheetDialog(View modalBottomSheetView) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(modalBottomSheetView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);

        bottomSheetDialog.setOnKeyListener((arg0, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                bottomSheetDialog.dismiss();
            }
            return true;
        });

        return bottomSheetDialog;
    }

    private void gallerySourceShow() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, SHOW_GALLERY_REQUEST_CODE);
    }

    private void filesSourceShow() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, SHOW_FILES_REQUEST_CODE);
    }

    private void closeBottomSheetDialog() {
        bottomSheetDialog.hide();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();

            String filePath = getPathOfSelectedFile(selectedImage);
            uploadFileRequest(filePath, selectedImage);
        } else if (requestCode == SHOW_FILES_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();

            String filePath = getPathOfSelectedFile(selectedImage);
            uploadFileRequest(filePath, selectedImage);
        }
    }

    public String getPathOfSelectedFile(Uri selectedImage) {
        String imgPath = "";
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        if (getActivity() != null) {
            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();
            }
        }

        return imgPath;
    }

    private void uploadFileRequest(String filePath, Uri uri) {
        File file = new File(filePath);

        RequestBody requestBody =
                RequestBody.create(
                        MediaType.parse(getContext().getContentResolver().getType(uri)),
                        file);

        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

        FileService fileService = ServiceGenerator.createService(
                FileService.class, TokenStore.getToken(getContext()));

        fileService.uploadFile(roomDto.getId(), fileToUpload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.e("UP_FILE", "SUCCESS");

                    onResume();
                    closeBottomSheetDialog();
                } else {
                    Log.e("UP_FILE", "FAIL");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private void configureSendingMessage(View view) {
        EditText editTextMessage = view.findViewById(R.id.inputMsg);
        Button sendMessageButton = view.findViewById(R.id.btn_send_message);

        sendMessageButton.setOnClickListener(v -> {
            String messageContent = getValueFromEditText(editTextMessage);
            if (!messageContent.isEmpty()) {
                createMessageRequest(messageContent);
                editTextMessage.getText().clear();
            }
        });
    }

    private void configureMessagesRecyclerView(View view) {
        recyclerViewRoomMessages = view.findViewById(R.id.recycler_messages);
    }

    private Notification parseReceivedJsonMessage(String jsonMessageForm) {
        return JsonConverter.parseReceivedJsonToNotificationObject(jsonMessageForm);
    }

    private String getValueFromEditText(EditText editText) {
        return editText.getText().toString();
    }

    private void configureToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_fragment_chat);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        }
    }

    private void getRoomMessagesRequest() {
        MessageService roomService = ServiceGenerator.createService(
                MessageService.class, TokenStore.getToken(getContext()));

        roomService.getRoomMessages(roomDto.getId(), MESSAGES_LIMIT)
                .enqueue(new Callback<List<MessageDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<MessageDto>> call,
                                           @NonNull Response<List<MessageDto>> response) {
                        if (response.isSuccessful()) {
                            messageDtos = response.body();
                        } else {
                            Log.e("GET_MESSAGES", "ERROR GET MESSAGES HISTORY");
                        }
                        setAdapterWithRoomMessages(messageDtos);
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<MessageDto>> call,
                                          @NonNull Throwable t) {
                        Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
                    }
                });
    }

    private void setAdapterWithRoomMessages(List<MessageDto> messageDtos) {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        MessagesAdapter messagesAdapter =
                new MessagesAdapter(messageDtos, TokenStore.decodeToken(TokenStore.getToken
                        (getContext())), this);
        recyclerViewRoomMessages.setLayoutManager(layoutManager);
        recyclerViewRoomMessages.setAdapter(messagesAdapter);
        scrollToDown();
    }

    private void createMessageRequest(String message) {
        MessageFormDto messageFormDto = new MessageFormDto(message);

        MessageService roomService = ServiceGenerator.createService(
                MessageService.class, TokenStore.getToken(getContext()));

        roomService.createMessage(roomDto.getId(), messageFormDto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.e("CREATE_MESSAGE", "MESSAGE_SENT_SUCCESSFULLY");
                } else {
                    NotificationManager.showToast(getContext(), "You cannot sent message -"
                            + "\n"
                            + "room " + "is ARCHIVED");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    @Override
    public void onFileItemClick(int position, View v) {
        String uniqueFileName = getUniqueFileName(messageDtos.get(position).getContent());
        String fileExtension = getFileExtension(messageDtos.get(position).getContent());

        if (isImageExtension(fileExtension)) {
            showFileRequest(uniqueFileName);
        } else {
            NotificationManager.showToast(getContext(), "Extension not allowed to open");
        }
    }

    private boolean isImageExtension(String fileExtension) {
        return fileExtension.equals("jpg")
                || fileExtension.equals("png")
                || fileExtension.equals("jpeg");
    }

    private void showFileRequest(String uniqueFileName) {
        FileService fileService = ServiceGenerator.createService(
                FileService.class, TokenStore.getToken(getContext()));

        fileService.getFile(uniqueFileName).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    showDialogWithImage(response.body().byteStream());
                } else {
                    NotificationManager.showToast(getContext(), "Cannot show file");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private void showDialogWithImage(InputStream inputStream) {
        ImageView image = new ImageView(getContext());
        Bitmap bmp = BitmapFactory.decodeStream(inputStream);
        image.setImageBitmap(bmp);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(getContext()).setView(image);
        builder.create().show();
    }

    @Override
    public void onTextItemLongClick(int position, View v) {
        showTextMessageDialog(position);
    }

    @Override
    public void onFileSentItemLongClick(int position, View v) {
        String uniqueFileName = getUniqueFileName(messageDtos.get(position).getContent());
        String originalFileName = getOriginalFileName(messageDtos.get(position).getContent());
        showFileMessageDialog(uniqueFileName, originalFileName);
    }

    @Override
    public void onFileReceivedItemLongClick(int position, View v) {
        String uniqueFileName = getUniqueFileName(messageDtos.get(position).getContent());
        String originalFileName = getOriginalFileName(messageDtos.get(position).getContent());

        getFileRequest(uniqueFileName, originalFileName);
    }

    private String getUniqueFileName(String content) {
        String[] parts = content.split(SPLIT_FILE_REGEX);
        return parts[0];
    }

    private String getOriginalFileName(String content) {
        String[] parts = content.split(SPLIT_FILE_REGEX);
        return parts[1];
    }

    private String getFileExtension(String content) {
        String[] parts = content.split("\\.");
        return parts[parts.length - 1];
    }

    private void showFileMessageDialog(String fileName, String originalFileName) {
        if (getActivity() != null) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_file_editor, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(dialogView);

            dialog = builder.create();
            dialog.show();

            configureFileMessageDialog(dialogView, fileName, originalFileName);
        }
    }

    private void configureFileMessageDialog(View view, String fileName, String originalFileName) {
        Button deleteFile = view.findViewById(R.id.btn_delete_file);
        Button downloadFile = view.findViewById(R.id.btn_download_file);

        deleteFile.setOnClickListener(v -> deleteFileRequest(fileName));
        downloadFile.setOnClickListener(v -> getFileRequest(fileName, originalFileName));
    }

    private void showTextMessageDialog(int position) {
        if (getActivity() != null) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_modify_message, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(dialogView);

            dialog = builder.create();
            dialog.show();

            configureTextMessageDialog(dialogView, position);
        }
    }

    private void configureTextMessageDialog(View view, int position) {
        long messageId = messageDtos.get(position).getMessageId();

        Button deleteMessage = view.findViewById(R.id.btn_delete_message);
        Button modifyMessage = view.findViewById(R.id.btn_modify_message);

        deleteMessage.setOnClickListener(v -> deleteMessageRequest(messageId));
        modifyMessage.setOnClickListener(v -> modifyMessageConfigure(messageId, position, view));
    }

    private void deleteMessageRequest(long messageId) {
        MessageService messageService = ServiceGenerator.createService(
                MessageService.class, TokenStore.getToken(getContext()));

        messageService.deleteMessage(messageId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    closeDialog();
                    NotificationManager.showToast(getContext(), MessageConstants.MESSAGE_DELETED);
                } else {
                    NotificationManager.showToast(getContext(), MessageConstants.CANNOT_DELETE_MESSAGE_ERROR);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private void deleteFileRequest(String fileName) {
        FileService fileService = ServiceGenerator.createService(
                FileService.class, TokenStore.getToken(getContext()));

        fileService.deleteFile(fileName).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    closeDialog();
                    NotificationManager.showToast(getContext(), MessageConstants.FILE_DELETED);
                } else {
                    NotificationManager.showToast(getContext(),
                            MessageConstants.FILE_CANNOT_BE_DELETED);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private void getFileRequest(String uniqueFileName, String originalFileName) {
        FileService fileService = ServiceGenerator.createService(
                FileService.class, TokenStore.getToken(getContext()));

        fileService.getFile(uniqueFileName).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (isFileSaveOnDisk(response.body(), originalFileName)) {
                        NotificationManager.showToast(getContext(), MessageConstants.FILE_DOWNLOAD_MESSAGE);
                    } else {
                        NotificationManager.showToast(getContext(), MessageConstants.CANNOT_SAVED_FILE_ERROR);
                    }
                } else {
                    NotificationManager.showToast(getContext(), MessageConstants.CANNOT_DOWNLOAD_FILE_ERROR);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private boolean isFileSaveOnDisk(ResponseBody responseBody, String fileName) {
        File outputFile = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        + File.separator
                        + fileName);

        byte[] fileReader = new byte[4096];

        try (InputStream inputStream = responseBody.byteStream();
             OutputStream outputStream = new FileOutputStream(outputFile)) {

            while (true) {
                int read = inputStream.read(fileReader);

                if (read == -1) {
                    break;
                }

                outputStream.write(fileReader, 0, read);
            }

            outputStream.flush();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void closeDialog() {
        dialog.dismiss();
    }

    private void modifyMessageConfigure(long messageId, int position, View view) {
        EditText newMessageContent = view.findViewById(R.id.edit_text_modify_message);
        Button saveMessageModification = view.findViewById(R.id.btn_modify_message_confirm);

        newMessageContent.setVisibility(View.VISIBLE);
        saveMessageModification.setVisibility(View.VISIBLE);

        newMessageContent.setText(messageDtos.get(position).getContent());

        saveMessageModification.setOnClickListener(v -> {
            String messageContent = newMessageContent.getText().toString();
            modifyMessageRequest(messageId, messageContent);
        });
    }

    private void modifyMessageRequest(long messageId, String messageContent) {
        MessageFormDto messageFormDto = new MessageFormDto(messageContent);

        MessageService messageService = ServiceGenerator.createService(
                MessageService.class, TokenStore.getToken(getContext()));

        messageService.modifyMessage(messageId, messageFormDto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    NotificationManager.showToast(getContext(), MessageConstants.MESSAGE_MODIFY);
                } else {
                    NotificationManager.showToast(getContext(), MessageConstants.CANNOT_MODIFY_MESSAGE_ERROR);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private void scrollToDown() {
        recyclerViewRoomMessages.scrollToPosition(messageDtos.size() - 1);
    }

}