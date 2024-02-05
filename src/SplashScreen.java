import javax.swing.*;
import java.awt.*;

public class SplashScreen {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Créez un JWindow pour l'écran de démarrage
            JWindow splashScreen = new JWindow();

            // Chargez une image à afficher sur l'écran de démarrage depuis le répertoire "images"
            ImageIcon splashImage = new ImageIcon("Images/2.jpg"); // Assurez-vous que le fichier "splash.png" est dans le répertoire "images"

            // Créez un JLabel pour afficher l'image
            JLabel splashLabel = new JLabel(splashImage);
            splashScreen.getContentPane().add(splashLabel);

            // Définissez la taille de l'écran de démarrage en fonction de la taille de l'image
            splashScreen.pack();

            // Centrez l'écran de démarrage sur l'écran
            Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
            splashScreen.setLocation((screenDim.width - splashScreen.getWidth()) / 2, (screenDim.height - splashScreen.getHeight()) / 2);

            // Affichez l'écran de démarrage pendant un certain temps (par exemple, 3 secondes)
            splashScreen.setVisible(true);
            try {
                Thread.sleep(3000); // Attendez 3 secondes (ajustez la durée selon vos besoins)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            splashScreen.setVisible(false);
            splashScreen.dispose();

            // Lancement de l'application principale
            MovieApp app = new MovieApp();
            app.run();
        });
    }
}
