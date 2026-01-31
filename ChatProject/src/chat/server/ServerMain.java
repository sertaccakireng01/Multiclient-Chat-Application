package chat.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

//@author Sertac & Berk
public class ServerMain extends Application {
    
    //@author Sertac & Berk
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("server-view.fxml"));
            VBox root = loader.load();

            Scene scene = new Scene(root);
            
            primaryStage.setTitle("Chat Sunucusu");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("HATA (ServerMain - start): FXML yüklenirken bir hata oluştu.");
            e.printStackTrace();
        }
    }
    
    //@author Sertac & Berk
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            System.err.println("HATA (ServerMain - main): Sunucu uygulaması başlatılırken bir hata oluştu.");
            e.printStackTrace();
        }
    }
}
