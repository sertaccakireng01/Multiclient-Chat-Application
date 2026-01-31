package chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ServerController serverController;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String username;

    //@author Sertac
    public ClientHandler(Socket socket, ServerController serverController) {
        try {
            this.socket = socket;
            this.serverController = serverController;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            System.err.println("HATA (ClientHandler Constructor): Stream oluşturulamadı.");
            e.printStackTrace();
        }
    }

    //@author Sertac
    public String getUsername() {
        return this.username;
    }

    //@author Sertac & Berk
    @Override
    public void run() {
        try {

            boolean loggedIn = false;

            while (!loggedIn) {
                String loginMessage = dis.readUTF();

                if (!loginMessage.startsWith("LOGIN:")) {
                    serverController.logToGui("Hatalı protokol girişi. Bağlantı kapatılıyor.");
                    socket.close();
                    return;
                }

                String potentialUsername = loginMessage.substring(6);

                if (serverController.addUser(potentialUsername, this)) {
                    this.username = potentialUsername;
                    sendMessage("LOGIN_SUCCESS");
                    serverController.broadcastUserList(); // send list
                    loggedIn = true;                   // loop exit
                } else {

                    // user typed already used username --> just send failed feedback, DONT close the socket! 
                    sendMessage("LOGIN_FAIL:Kullanıcı adı alınmış veya geçersiz.");

                } // now loop continues, client can type new username

            }

            // message listening
            while (true) {
                String clientMessage = dis.readUTF();

                if (clientMessage.startsWith("TO:")) {
                    String[] parts = clientMessage.split(":", 3);
                    if (parts.length == 3) {
                        String toUser = parts[1];
                        String content = parts[2];
                        serverController.routeMessage(this.username, toUser, content);
                    }
                }
            }

        } catch (java.io.EOFException e) {
            System.err.println("BİLGİ (ClientHandler - run): " + username + " bağlantısı kesildi (EOF).");
        } catch (Exception e) {
            System.err.println("HATA (ClientHandler - run): " + username + " ile iletişimde hata.");
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    //@author Sertac
    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
            dos.flush();
        } catch (Exception e) {
            System.err.println("HATA (sendMessage - " + username + "): Mesaj gönderilemedi: " + message);
            e.printStackTrace();
        }
    }

    //@author Sertac
    private void cleanup() {
        try {
            serverController.removeUser(this.username);

            if (dis != null) {
                dis.close();
            }
            if (dos != null) {
                dos.close();
            }
            if (socket != null) {
                socket.close();
            }

        } catch (Exception e) {
            System.err.println("HATA (cleanup - " + username + "): Kaynaklar temizlenemedi.");
            e.printStackTrace();
        }
    }
}
