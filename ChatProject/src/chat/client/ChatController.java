package chat.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ChatController {

    @FXML
    private TextArea messageArea;
    @FXML
    private ListView<String> userListView;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;

    private boolean suppressSelectionLog = false; //to supress selection changes after list updates. if not, repeated notifications will appear

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String username;

    private ServerListener listener;

    //@author Berk
    public void initializeConnection(Socket socket, DataInputStream dis, DataOutputStream dos, String username) {
        try {
            this.socket = socket;
            this.dis = dis;
            this.dos = dos;
            this.username = username;

            this.listener = new ServerListener(dis, this);
            Thread listenerThread = new Thread(listener);
            listenerThread.setDaemon(true);
            listenerThread.start();

            logToGui("Sunucuya bağlandınız. ('Genel' kanalı seçili)"); //"genel" channel selected by default

        } catch (Exception e) {
            System.err.println("HATA (ChatController - initializeConnection): Bağlantı başlatılamadı.");
            e.printStackTrace();
            logToGui("HATA: Sunucu dinleyicisi başlatılamadı.");
        }
    }

    //author Berk
    @FXML
    private void initialize() { //for selection between "genel" and "özel"
        userListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {

                    /* dont log, if triggered during list update or if there is no selection
                    so that "Genel seçildi" notification wont appear more than needed: */
                    if (suppressSelectionLog || newValue == null) {
                        return;
                    }

                    if ("Genel".equalsIgnoreCase(newValue)) {
                        logToGui("SİSTEM: 'Genel' kanalı seçildi.");
                    } else {
                        logToGui("SİSTEM: '" + newValue + "' özel sohbeti seçildi.");
                    }
                });
    }

    //@author Berk
    @FXML
    private void handleSendAction(ActionEvent event) {
        try {
            String messageContent = messageField.getText();
            if (messageContent.isEmpty()) {
                return;
            }

            String recipient = userListView.getSelectionModel().getSelectedItem();

            if (recipient == null || recipient.isEmpty()) {
                logToGui("SİSTEM: Lütfen mesaj göndermek için listeden bir kullanıcı ('Genel' dahil) seçin.");
                return;
            }

            String messageToSend = "TO:" + recipient + ":" + messageContent;
            dos.writeUTF(messageToSend);
            dos.flush();

            // show if it's "genel" or "özel"
            if ("Genel".equalsIgnoreCase(recipient)) {
                // genel
                logToGui(this.username + ": " + messageContent);
            } else {
                // özel
                logToGui("[ÖZEL \u2192 " + recipient + "] " + this.username + ": " + messageContent);
            }

            messageField.clear();

        } catch (Exception e) {
            System.err.println("HATA (handleSendAction): Mesaj gönderilemedi.");
            e.printStackTrace();
            logToGui("HATA: Mesaj gönderilirken bir hata oluştu. Bağlantı kopmuş olabilir.");
        }
    }

    //@author Berk
    public void logToGui(String message) {
        try {
            Platform.runLater(() -> messageArea.appendText(message + "\n"));
        } catch (Exception e) {
            System.err.println("HATA (logToGui): İstemci GUI loglama hatası.");
            e.printStackTrace();
        }
    }

    //@author Berk
    public void updateUserList(String[] users) {
        Platform.runLater(() -> {

            // suppressing logging temporarily
            suppressSelectionLog = true;

            String selected = userListView.getSelectionModel().getSelectedItem();

            userListView.setItems(FXCollections.observableArrayList(users));

            if (selected != null && userListView.getItems().contains(selected)) {
                userListView.getSelectionModel().select(selected);
            } else {
                userListView.getSelectionModel().select("Genel");
            }

            // update over, can log now
            suppressSelectionLog = false;
        });

    }
}
