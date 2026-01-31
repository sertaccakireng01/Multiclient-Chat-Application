package chat.server;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ServerController {

    @FXML private TextField portField;
    @FXML private Button startButton;
    @FXML private TextArea logArea;

    private static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    
    //@author Sertac
    @FXML
    private void handleStartServer(ActionEvent event) {
        try {
            int port = Integer.parseInt(portField.getText());
            startButton.setDisable(true);
            portField.setDisable(true);
            logToGui("Sunucu " + port + " portunda başlatılıyor...");

            Thread serverThread = new Thread(() -> startServer(port));
            serverThread.setDaemon(true); 
            serverThread.start();

        } catch (NumberFormatException e) {
            System.err.println("HATA (handleStartServer): Geçersiz port numarası girildi.");
            logToGui("HATA: Lütfen geçerli bir port numarası girin.");
            startButton.setDisable(false); 
            portField.setDisable(false);
        } catch (Exception e) {
            System.err.println("HATA (handleStartServer): Sunucu başlatma butonunda beklenmedik hata.");
            logToGui("HATA: Sunucu başlatılamadı: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    //@author Sertac
    private void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Platform.runLater(() -> logToGui("Sunucu " + port + " portunda dinlemede... İstemciler bekleniyor."));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Platform.runLater(() -> logToGui("Yeni bağlantı kabul edildi: " + clientSocket.getInetAddress()));

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
            }

        } catch (Exception e) {
            System.err.println("HATA (startServer): Sunucu soket hatası. Port kullanımda olabilir.");
            Platform.runLater(() -> logToGui("HATA: Sunucu soket hatası: " + e.getMessage()));
            e.printStackTrace();
        }
    }
    
    //@author Sertac
    public void logToGui(String message) {
        try {
            if (Platform.isFxApplicationThread()) {
                logArea.appendText(message + "\n");
            } else {
                Platform.runLater(() -> logArea.appendText(message + "\n"));
            }
        } catch (Exception e) {
            System.err.println("HATA (logToGui): Sunucu GUI loglama hatası.");
            e.printStackTrace();
        }
    }
    
    //@author Sertac
    public boolean addUser(String username, ClientHandler handler) {
        try {
            if (clients.containsKey(username) || username.equalsIgnoreCase("Genel")) {
                return false; 
            }
            clients.put(username, handler);
            logToGui("Kullanıcı eklendi: " + username);
            
            return true;
        } catch (Exception e) {
            System.err.println("HATA (addUser): Kullanıcı eklenirken hata.");
            e.printStackTrace();
            return false;
        }
    }
    
    //@author Sertac
    public void removeUser(String username) {
        try {
            if (username != null) {
                clients.remove(username);
                logToGui("Kullanıcı ayrıldı: " + username);
                broadcastUserList(); 
            }
        } catch (Exception e) {
            System.err.println("HATA (removeUser): Kullanıcı çıkarılırken hata.");
            e.printStackTrace();
        }
    }
    
    //@author Sertac
    public void broadcastUserList() {
        try {
            String userList = "Genel," + String.join(",", clients.keySet());
            String message = "USERLIST:" + userList;
            
            for (ClientHandler handler : clients.values()) {
                handler.sendMessage(message);
            }
        } catch (Exception e) {
            System.err.println("HATA (broadcastUserList): Kullanıcı listesi gönderilemedi.");
            e.printStackTrace();
        }
    }
    
    
    //@author Sertac & Berk
    public void routeMessage(String fromUser, String toUser, String messageContent) {
    try {

        if (toUser.equalsIgnoreCase("Genel")) {
            String message = "MSG:" + fromUser + ":" + messageContent;

            for (ClientHandler handler : clients.values()) {
                if (!handler.getUsername().equals(fromUser)) {
                    handler.sendMessage(message);
                }
            }
        } else {
            String privateMessage = "MSG_PRIVATE:" + fromUser + ":" + messageContent;

            ClientHandler recipientHandler = clients.get(toUser);
            if (recipientHandler != null) {
                recipientHandler.sendMessage(privateMessage);
            } else {
                logToGui("HATA (routeMessage): Alıcı bulunamadı: " + toUser);
                ClientHandler senderHandler = clients.get(fromUser);
                if (senderHandler != null) {
                    senderHandler.sendMessage("SERVER_MSG:Hata: '" 
                        + toUser + "' kullanıcısı bulunamadı veya çevrimdışı.");
                }
            }
        }

    } catch (Exception e) {
        System.err.println("HATA (routeMessage): Mesaj yönlendirme hatası.");
        e.printStackTrace();
    }
    
    }
}
