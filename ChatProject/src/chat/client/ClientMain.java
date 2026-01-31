package chat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Optional;

public class ClientMain extends Application {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String username;

    //@author Berk
    @Override
    public void start(Stage primaryStage) {
        try {
            if (!connectToServer()) {
                Platform.exit();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("chat-view.fxml"));
            BorderPane root = loader.load();

            ChatController controller = loader.getController();
            controller.initializeConnection(socket, dis, dos, username);

            Scene scene = new Scene(root);
            primaryStage.setTitle("Chat İstemcisi - (" + username + ")");
            primaryStage.setScene(scene);

            primaryStage.setOnCloseRequest(e -> {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                    Platform.exit();
                    System.exit(0);
                } catch (Exception ex) {
                    System.err.println("HATA (ClientMain - onCloseRequest): Soket kapatılamadı.");
                    ex.printStackTrace();
                }
            });

            primaryStage.show();

        } catch (Exception e) {
            System.err.println("HATA (ClientMain - start): Ana pencere yüklenirken hata.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kritik Hata", "Uygulama başlatılamadı: " + e.getMessage());
        }
    }

    //@author Sertac & Berk
    private boolean connectToServer() {
        try {
            // IP Adress
            TextInputDialog ipDialog = new TextInputDialog();
            ipDialog.setTitle("Sunucu Bağlantısı");
            ipDialog.setHeaderText("Enter IP address of host");
            ipDialog.setContentText("Enter IP Address of host:");
            ipDialog.getEditor().setPromptText("localhost");

            Optional<String> ipResult = ipDialog.showAndWait();

            // if user pressed "cancel" just leave
            if (!ipResult.isPresent()) {
                return false; // show no error, just leave
            }

            //if user pressed "OK" but input is empty:
            if (!ipResult.isPresent() || ipResult.get().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Giriş Hatası", "IP Adresi boş bırakılamaz.");
                return false;
            }
            String host = ipResult.get().trim();

            // Port Address
            TextInputDialog portDialog = new TextInputDialog();
            portDialog.setTitle("Sunucu Bağlantısı");
            portDialog.setHeaderText("Enter port address of the server:");
            portDialog.setContentText("Enter port address of the server:");
            portDialog.getEditor().setPromptText("12000");

            Optional<String> portResult = portDialog.showAndWait();

            // if user presses cancel in port window, quit with no error notification
            if (!portResult.isPresent()) {
                return false;
            }

            if (!portResult.isPresent() || portResult.get().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Giriş Hatası", "Port numarası boş bırakılamaz.");
                return false;
            }
            int port = Integer.parseInt(portResult.get().trim());

            this.socket = new Socket(host, port);
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());

            // Username
            boolean loginSuccess = false;
            while (!loginSuccess) {
                TextInputDialog nameDialog = new TextInputDialog();
                nameDialog.setTitle("Giriş Yap");
                nameDialog.setHeaderText("Please enter your name:");
                nameDialog.setContentText("Please enter your name:");

                Optional<String> nameResult = nameDialog.showAndWait();

                // if user presses cancel in Username window, quit with no error notification
                if (!nameResult.isPresent()) {
                    socket.close();
                    return false;
                }

                if (!nameResult.isPresent() || nameResult.get().trim().isEmpty()) {
                    socket.close();
                    showAlert(Alert.AlertType.ERROR, "Giriş Hatası", "Kullanıcı adı boş bırakılamaz.");
                    return false;
                }

                String potentialUsername = nameResult.get().trim();

                dos.writeUTF("LOGIN:" + potentialUsername);
                dos.flush();

                String response = dis.readUTF();

                if (response.equals("LOGIN_SUCCESS")) {
                    this.username = potentialUsername;
                    loginSuccess = true;
                } else {
                    String reason = response.substring(11);
                    showAlert(Alert.AlertType.WARNING, "Giriş Hatası", "Giriş başarısız: " + reason);
                }
            }
            return true;

        } catch (NumberFormatException e) {
            System.err.println("HATA (connectToServer): Geçersiz port numarası.");
            showAlert(Alert.AlertType.ERROR, "Giriş Hatası", "Port numarası geçersiz. Lütfen sadece sayı girin.");
            return false;
        } catch (Exception e) {
            System.err.println("HATA (connectToServer): Sunucu bağlantı hatası.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Sunucu Hatası", "Sunucuya bağlanılamadı: " + e.getMessage());
            return false;
        }
    }

    //@author Berk
    private void showAlert(Alert.AlertType type, String title, String content) {
        try {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("HATA (showAlert): Uyarı gösterilemedi.");
            e.printStackTrace();
        }
    }

    //@author Berk
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            System.err.println("HATA (ClientMain - main): İstemci başlatılamadı.");
            e.printStackTrace();
        }
    }
}
