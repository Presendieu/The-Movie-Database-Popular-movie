import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MovieApp {

    public static final String API_KEY = "665daceca0075aae3dd413678fa8a590";
    public static final String NOW_PLAYING_URL = "https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY;

    private List<Film> films = new ArrayList<>();
    private int currentIndex = 0;
    private int numFilmsToDisplay = 3;

    private JFrame mainFrame;
    private JPanel imagePanel;
    private List<JLabel> imageLabels;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MovieApp app = new MovieApp();
            app.run();
        });
    }

    public MovieApp() {
        fetchMovies(NOW_PLAYING_URL);
        mainFrame = new JFrame("Movie App");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.addComponentListener(new ComponentAdapter()

        {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeImages();
            }
        });

        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Création d'un dégradé de couleur bleue à cyan en arrière-plan
                GradientPaint gradient = new GradientPaint(0, 0, Color.BLUE, 0, getHeight(), Color.CYAN);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainFrame.add(imagePanel);


        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Popular Movies");
        Font titleFont = new Font("Times new roman", Font.ITALIC, 36); // Changer la police, le style (gras), et la taille (24)
        titleLabel.setFont(titleFont);
        titlePanel.add(titleLabel);

        imagePanel = new JPanel();
        imageLabels = new ArrayList<>();
        imagePanel.setLayout(new GridLayout(1, numFilmsToDisplay, 10, 10)); // Utilisation de GridLayout
        mainFrame.add(titlePanel, BorderLayout.NORTH);
        mainFrame.add(imagePanel, BorderLayout.CENTER);

        initImageLabels();
        updateImages();

        JButton prevButton = new JButton("Previous");
        prevButton.addActionListener(e -> showPreviousMovies());
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> showNextMovies());

        customizeButton(prevButton, Color.RED, Color.ORANGE);
        customizeButton(nextButton, Color.GREEN, Color.YELLOW);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);
    }

    public void run() {
        mainFrame.setVisible(true);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }


    public void fetchMovies(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();

                JSONParser parser = new JSONParser();
                JSONObject jsonData = (JSONObject) parser.parse(responseContent.toString());
                JSONArray movies = (JSONArray) jsonData.get("results");
                films = Film.fromJsonArray(movies);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void showNextMovies() {
        currentIndex += numFilmsToDisplay;
        updateImages();
    }

    public void showPreviousMovies() {
        currentIndex -= numFilmsToDisplay;
        updateImages();
    }

    public void initImageLabels() {
        for (int i = 0; i < numFilmsToDisplay; i++) {
            JLabel label = new JLabel();
            imageLabels.add(label);
        }
    }

    public void updateImages() {
        imagePanel.removeAll();
        for (int i = currentIndex; i < currentIndex + numFilmsToDisplay && i < films.size(); i++) {
            Film currentFilm = films.get(i);
            JLabel label = createImageLabel(currentFilm.getPosterPath(), currentFilm);
            imageLabels.set(i - currentIndex, label);
            imagePanel.add(label);
        }
        mainFrame.revalidate();
        mainFrame.repaint();
        resizeImages();
    }

    public void resizeImages() {
        if (films.isEmpty()) {
            return;
        }
        int width = mainFrame.getWidth() / numFilmsToDisplay;
        int height = (int) (width * 1.5); // Maintenir le ratio de 1.5
        for (JLabel label : imageLabels) {
            if (label.getIcon() != null) {
                ImageIcon icon = (ImageIcon) label.getIcon();
                Image image = icon.getImage();
                Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaledImage));
            }
        }
    }

    public JLabel createImageLabel(String url, Film currentFilm) {
        JLabel label = new JLabel();
        try {
            BufferedImage image = ImageIO.read(new URL(url));
            Image scaledImage = image.getScaledInstance(mainFrame.getWidth() / numFilmsToDisplay, -1, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(scaledImage);
            label.setIcon(icon);
            label.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    showFilmInfo(currentFilm);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return label;
    }

    public void showFilmInfo(Film film) {
        mainFrame.dispose();

        JFrame infoFrame = new JFrame("Film Information");
        infoFrame.setSize(mainFrame.getSize());
        infoFrame.setLocationRelativeTo(null);
        infoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel infoPanel = new JPanel(new BorderLayout());

        // Ajout de l'image du film
        JLabel imageLabel = new JLabel();
        try {
            BufferedImage image = ImageIO.read(new URL(film.getPosterPath()));
            Image scaledImage = image.getScaledInstance(200, -1, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(scaledImage);
            imageLabel.setIcon(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JTextArea infoTextArea = new JTextArea();
        infoTextArea.setFont(new Font("Times new roman", Font.PLAIN, 16)); // Changer la police
        infoTextArea.setWrapStyleWord(true);
        infoTextArea.setLineWrap(true);
        infoTextArea.setText("Title: " + film.getTitle() + "\n" +
                "Original Title: " + film.getOriginalTitle() + "\n" +
                "Original Language: " + film.getOriginalLanguage() + "\n" +
                "Release Date: " + film.getReleaseDate() + "\n" +
                "Adult: " + (film.isAdult() ? "Yes" : "No") + "\n" +
                "Popularity: " + film.getPopularity() + "\n" +
                "Vote Average: " + film.getVoteAverage() + "\n" +
                "Vote Count: " + film.getVoteCount() + "\n\n" +
                "Overview:\n" + film.getOverview());

        // Utiliser un JScrollPane pour le défilement
        JScrollPane scrollPane = new JScrollPane(infoTextArea);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            infoFrame.dispose();
            mainFrame.setVisible(true);
        });
        backButton.setPreferredSize(new Dimension(100, 30)); // Ajustez les dimensions selon vos besoins
// ...

        infoPanel.add(imageLabel, BorderLayout.WEST);
        infoPanel.add(scrollPane, BorderLayout.CENTER);
        infoPanel.add(backButton, BorderLayout.SOUTH);
        infoFrame.add(infoPanel);
        infoFrame.setVisible(true);
    }





    public void customizeButton(JButton button, Color startColor, Color endColor) {
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(startColor);
        button.setForeground(endColor);
        button.setMargin(new Insets(10, 20, 10, 20));
    }

    public static class Film {
        private String posterPath;
        private boolean adult;
        private String originalTitle;
        private String originalLanguage;
        private String releaseDate;
        private String title;
        private double popularity;
        private double voteAverage;
        private long voteCount;
        private String overview;

        public Film(String posterPath, boolean adult, String originalTitle, String originalLanguage,
                    String releaseDate, String title, double popularity, double voteAverage, long voteCount,
                    String overview) {
            this.posterPath = posterPath;
            this.adult = adult;
            this.originalTitle = originalTitle;
            this.originalLanguage = originalLanguage;
            this.releaseDate = releaseDate;
            this.title = title;
            this.popularity = popularity;
            this.voteAverage = voteAverage;
            this.voteCount = voteCount;
            this.overview = overview;
        }

        public String getPosterPath() {
            return "https://image.tmdb.org/t/p/w342" + posterPath;
        }

        public boolean isAdult() {
            return adult;
        }

        public String getOriginalTitle() {
            return originalTitle;
        }

        public String getOriginalLanguage() {
            return originalLanguage;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public String getTitle() {
            return title;
        }

        public double getPopularity() {
            return popularity;
        }

        public double getVoteAverage() {
            return voteAverage;
        }

        public long getVoteCount() {
            return voteCount;
        }

        public String getOverview() {
            return overview;
        }

        public static List<Film> fromJsonArray(JSONArray array) {
            List<Film> films = new ArrayList<>();
            for (Object item : array) {
                if (item instanceof JSONObject) {
                    JSONObject filmData = (JSONObject) item;
                    String posterPath = (String) filmData.get("poster_path");
                    boolean adult = (boolean) filmData.get("adult");
                    String originalTitle = (String) filmData.get("original_title");
                    String originalLanguage = (String) filmData.get("original_language");
                    String releaseDate = (String) filmData.get("release_date");
                    String title = (String) filmData.get("title");
                    double popularity = (double) filmData.get("popularity");
                    double voteAverage = (double) filmData.get("vote_average");
                    long voteCount = (long) filmData.get("vote_count");
                    String overview = (String) filmData.get("overview");
                    Film film = new Film(posterPath, adult, originalTitle, originalLanguage, releaseDate, title,
                            popularity, voteAverage, voteCount, overview);
                    films.add(film);
                }
            }
            return films;
        }
    }
}
