package chat.client;

import java.io.DataInputStream;

//@author Sertac & Berk
public class ServerListener implements Runnable {

    private DataInputStream dis;
    private ChatController controller;

    //@author Berk
    public ServerListener(DataInputStream dis, ChatController controller) {
        try {
            this.dis = dis;
            this.controller = controller;
        } catch (Exception e) {
            System.err.println("HATA (ServerListener Constructor): Başlatılamadı.");
            e.printStackTrace();
        }
    }

    //@author Berk
    @Override
    public void run() {
        try {
            while (true) {
                String serverMessage = dis.readUTF();

                if (serverMessage.startsWith("USERLIST:")) {
                    // user list update:
                    String[] users = serverMessage.substring("USERLIST:".length()).split(",");
                    controller.updateUserList(users);

                } else if (serverMessage.startsWith("MSG_PRIVATE:")) {
                    // özel
                    String[] parts = serverMessage.split(":", 3);
                    if (parts.length == 3) {
                        String fromUser = parts[1];
                        String content = parts[2];
                        controller.logToGui("[ÖZEL] " + fromUser + ": " + content);
                    }

                } else if (serverMessage.startsWith("MSG:")) {
                    // genel
                    String[] parts = serverMessage.split(":", 3);
                    if (parts.length == 3) {
                        String fromUser = parts[1];
                        String content = parts[2];
                        controller.logToGui(fromUser + ": " + content);
                    }

                } else if (serverMessage.startsWith("SERVER_MSG:")) { // notification message from server
                    String content = serverMessage.substring("SERVER_MSG:".length());
                    controller.logToGui("SUNUCU: " + content);
                }
            }
        } catch (Exception e) {
            System.err.println("HATA (ServerListener - run): Sunucudan mesaj alınırken sorun oluştu.");
            e.printStackTrace();
        }

    }

}
