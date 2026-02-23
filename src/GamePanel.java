import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Iterator;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Score score;
    private int gameSpeed = 5;
    private int clusterCount = 0;
    private String currentJumpKey = "SPACE";
    private String[] possibleKeys = { "W", "A", "S", "D", "SPACE" };

    private final int WIDTH = 1000;
    private final int HEIGHT = 700;
    private int GROUND_Y;

    private boolean gameRunning = false;
    private boolean gameOver = false;
    private JButton startButton;
    private JButton restartButton;
    private JButton exitButton;
    private ArrayList<PowerUp> powerUps = new ArrayList<>();

    private Timer timer;
    private Player dino;
    private ArrayList<Obstacle> obstacles;
    private Random random = new Random();

    private int selectedChar = 0;
    private Image customUserImage = null;

    private String[] charFiles = { "dino.png", "robot.png", "ninja.java" };

    private JButton prevBtn;
    private JButton nextBtn;
    private JButton browseButton;

    // --- ส่วนของฉากหลัง (Sky Elements) ---
    private final int MOON_X = 650; // ตำแหน่ง X พระจันทร์ (ขวาบน)
    private final int MOON_Y = 50; // ตำแหน่ง Y พระจันทร์
    private final int MOON_SIZE = 80; // ขนาดพระจันทร์

    // ใช้ ArrayList เก็บพิกัด X และ Y ของดาวแต่ละดวง
    private ArrayList<Integer> starXs = new ArrayList<>();
    private ArrayList<Integer> starYs = new ArrayList<>();
    private final int NUM_STARS = 100; // จำนวนดาวที่ต้องการ

    private int bossX = 400;
    private int bossY = -200;
    private boolean bossSliding = false;
    private boolean bossTriggered = false;

    private String targetText = "";
    private String playerInput = "";
    private String[] bossTexts = {
            "java is fun",
            "object oriented programming",
            "type faster to win",
            "dino boss fight"
    };
    private long bossStartTime;
    private int timeLimit = 10;
    private int nextBossScore = 15;

    public GamePanel() {

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        GROUND_Y = HEIGHT - 150; // ปรับ 150 ได้ตามความหนาพื้น

        setBackground(Color.BLACK);
        setLayout(null);

        initStars();

        score = new Score();
        dino = new DinoChar(100, GROUND_Y);

        obstacles = new ArrayList<>();

        createButtons();

        timer = new Timer(16, this);

        // 🔥 bind ทุกปุ่มที่ใช้ได้
        for (String key : possibleKeys) {
            bindKey(key);
        }

        // สุ่มปุ่มเริ่มต้น
        randomizeJumpKey();
        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (!gameRunning)
            return;

        // ===== RUNNING =====
        if (gameState == GameState.RUNNING) {

            dino.update();
            updateObstacles();
            spawnObstacle();
            checkCollision();
            updateScoreFromObstacles();
            increaseDifficulty();
            spawnPowerUp();
            updatePowerUps();
            checkPowerUpCollision();
            updateStars();

            if (score.getScore() >= nextBossScore
                    && gameState == GameState.RUNNING) {

                gameState = GameState.PRE_BOSS;
            }
        }

        // ===== PRE_BOSS (ให้ตกลงพื้นก่อน) =====
        if (gameState == GameState.PRE_BOSS) {
            dino.update();

            if (!dino.isJumping()) {
                startBossFight();
            }
        }

        // ===== BOSS SLIDE =====
        if (gameState == GameState.BOSS && bossSliding) {
            bossY += 4;

            if (bossY >= 120) {
                bossSliding = false;
                startTypingChallenge();
            }
        }

        // ===== BOSS GAMEPLAY =====
        if (gameState == GameState.BOSS && !bossSliding) {

            long elapsed = (System.currentTimeMillis() - bossStartTime) / 1000;

            if (elapsed >= timeLimit) {
                triggerGameOver();
            }

            if (playerInput.equals(targetText)) {
                gameState = GameState.RUNNING;
                nextBossScore += 15;
                bossY = -200;
                obstacles.clear();
                playerInput = "";
                // obstaclesPassedInWave = 0;
            }
        }

        repaint();
    }

    private void updateObstacles() {

        Iterator<Obstacle> it = obstacles.iterator();

        while (it.hasNext()) {
            Obstacle obs = it.next();
            obs.update();

            if (obs.getX() + obs.getWidth() < 0) {
                it.remove(); // 🔥 ลบต้นที่ออกจอแล้ว
            }
        }
    }

    private void checkCollision() {
        for (Obstacle obs : obstacles) {
            if (!dino.isInvincible() && dino.getBounds().intersects(obs.getBounds())) {
                triggerGameOver();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameState == GameState.MENU) {
            g.setColor(new Color(10, 10, 40));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("SELECT CHARACTER", WIDTH / 2 - 200, HEIGHT / 2 - 120);

            String[] charNames = { "Classic Dino", "Robot", "Ninja", "Custom Image" };
            g.setFont(new Font("Arial", Font.BOLD, 24));
            String name = charNames[selectedChar];
            int nameWidth = g.getFontMetrics().stringWidth(name);
            g.drawString(name, WIDTH / 2 - (nameWidth / 2), HEIGHT / 2);

            return;
        }

        g.setColor(new Color(10, 10, 40)); // สีเทาเกือบดำ
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.WHITE);
        for (int i = 0; i < starXs.size(); i++) {
            g.fillRect(starXs.get(i), starYs.get(i), 2, 2);
        }
        g.setColor(new Color(255, 255, 255, 50));
        g.fillOval(MOON_X - 10, MOON_Y - 10, MOON_SIZE + 20, MOON_SIZE + 20);
        g.setColor(new Color(240, 240, 220));
        g.fillOval(MOON_X, MOON_Y, MOON_SIZE, MOON_SIZE);

        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, GROUND_Y, WIDTH, HEIGHT - GROUND_Y);
        g.setColor(Color.WHITE);
        g.drawLine(0, GROUND_Y, WIDTH, GROUND_Y);
        dino.draw(g);
        score.draw(g);
        // DEBUG
        g.setColor(Color.GREEN); // ใช้สีเขียวจะได้เห็นชัดๆ
                                 // ตัดกับฉากหลังC:\Users\66967\Documents\GitHub\Dino#\GamePanel.java
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Debug Speed: " + gameSpeed, 20, 30);
        if (gameOver) {
            g.setColor(Color.WHITE);
            Font font = new Font("Arial", Font.BOLD, 60);
            g.setFont(font);

            String text = "GAME OVER";

            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int x = (WIDTH - textWidth) / 2;
            int y = HEIGHT / 4;

            g.drawString(text, x, y);
        }

        for (Obstacle obs : obstacles) {
            obs.draw(g);
        }
        for (Obstacle obs : obstacles) {
            if (!obs.getRequiredKey().equals("SPACE")) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 20));

                int textX = obs.getX() + 10;
                int textY = obs.getY() - 10;

                g.drawString(obs.getRequiredKey(), textX, textY);
            }
        }

        for (PowerUp p : powerUps) {
            p.draw(g);
        }
        if (dino.isInvincible()) {
            int barWidth = 200;
            int barHeight = 15;
            int x = 20;
            int y = 50;
            g.setColor(Color.WHITE);
            g.drawRect(x, y, barWidth, barHeight);

            double ratio = dino.getInvincibletTimeRemainingRatio();
            int currentBarWidth = (int) (barWidth * ratio);

            g.setColor(Color.YELLOW);
            g.fillRect(x + 1, y + 1, currentBarWidth - 1, barHeight - 1);

            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("INVINCIBLE", x, y - 5);
        }
        if (gameState == GameState.BOSS) {

            g.setColor(Color.RED);
            g.fillRect(bossX, bossY, 150, 150);
            if (!bossSliding) {
                g.setColor(Color.WHITE);
                g.drawString("Type this:", 300, 300);
                if (targetText != null) {
                    g.drawString(targetText, 300, 330);
                }
                if (playerInput != null) {
                    g.drawString(playerInput, 300, 360);
                }
                long elapsed = (System.currentTimeMillis() - bossStartTime) / 1000;
                g.drawString("Time left: " + (timeLimit - elapsed), 300, 390);
            }
        }

    }

    private void increaseDifficulty() {
        gameSpeed = 5 + (score.getScore() / 15);

        if (gameSpeed > 18) {
            gameSpeed = 18;
        }

        for (Obstacle obs : obstacles) {
            obs.setSpeed(gameSpeed);
        }
    }

    private void spawnObstacle() {
        // 🔴 หยุด spawn ถ้าไม่ใช่ RUNNING
        if (gameState != GameState.RUNNING)
            return;

        if (obstacles.isEmpty()) {
            randomizeJumpKey();
            String randomKey = possibleKeys[random.nextInt(possibleKeys.length)];
            obstacles.add(createRandomObstacle(WIDTH, randomKey));

            return;
        }

        Obstacle last = obstacles.get(obstacles.size() - 1);

        // รอให้ตัวสุดท้ายเข้ามาในจอก่อน
        if (last.getX() > WIDTH - 250)
            return;

        int gap;

        // 🔴 ถ้าอยู่ในช่วง cluster (ถี่)
        if (clusterCount > 0) {
            gap = 100 + random.nextInt(40); // ใกล้ ๆ
            clusterCount--;
        } else {

            int mode = random.nextInt(5);

            if (mode == 0) {
                // 🔵 เว้นยาว
                gap = 350 + random.nextInt(150);
            } else if (mode <= 2) {
                // 🔴 เริ่ม cluster 2-3 อัน
                clusterCount = 2 + random.nextInt(2);
                gap = 110 + random.nextInt(40);
            } else {
                // 🟢 ปกติ
                gap = 180 + random.nextInt(120);
            }
        }

        String randomKey = possibleKeys[random.nextInt(possibleKeys.length)];
        Obstacle obs = createRandomObstacle(WIDTH + gap, randomKey);
        obstacles.add(obs);

    }

    private void createButtons() {

        startButton = new JButton("START");
        startButton.setBounds(WIDTH / 2 - 50, HEIGHT / 2 + 80, 100, 40);
        add(startButton);

        restartButton = new JButton("RESTART");
        restartButton.setBounds(WIDTH / 2 - 50, HEIGHT / 2 + 10, 100, 40);
        restartButton.setVisible(false);
        add(restartButton);

        exitButton = new JButton("EXIT");
        exitButton.setBounds(WIDTH / 2 - 50, HEIGHT / 2 + 60, 100, 40);
        exitButton.setVisible(false);
        add(exitButton);

        startButton.addActionListener(e -> startGame());
        restartButton.addActionListener(e -> restartGame());
        exitButton.addActionListener(e -> System.exit(0));

        prevBtn = new JButton("<");
        prevBtn.setBounds(WIDTH / 2 - 150, HEIGHT / 2 - 30, 50, 40);
        prevBtn.addActionListener(e -> {
            selectedChar = (selectedChar - 1 + 4) % 4;
            updateBrowseButtonVisibility();
            repaint();
        });
        add(prevBtn);
        nextBtn = new JButton(">");
        nextBtn.setBounds(WIDTH / 2 + 100, HEIGHT / 2 - 30, 50, 40);
        nextBtn.addActionListener(e -> {
            selectedChar = (selectedChar + 1) % 4;
            updateBrowseButtonVisibility();
            repaint();
        });
        add(nextBtn);
        browseButton = new JButton("Choose Image");
        browseButton.setBounds(WIDTH / 2 - 60, HEIGHT / 2 + 140, 120, 30);
        browseButton.setVisible(false);
        browseButton.addActionListener(e -> {

            JFileChooser chooser = new JFileChooser();

            // ✅ กรองเฉพาะไฟล์ภาพ
            javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
                    "Image Files (*.png, *.jpg, *.jpeg)",
                    "png", "jpg", "jpeg");

            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false); // ❌ ไม่ให้เลือก All Files

            int result = chooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {

                File selectedFile = chooser.getSelectedFile();
                String name = selectedFile.getName().toLowerCase();

                // 🔒 เช็คซ้ำอีกรอบกันนามสกุลปลอม
                if (!(name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"))) {
                    JOptionPane.showMessageDialog(this,
                            "Please select a valid image file (.png, .jpg, .jpeg)",
                            "Invalid File",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                customUserImage = new ImageIcon(selectedFile.getAbsolutePath()).getImage();

                repaint();
            }
        });
        add(browseButton);
    }

    private void updateBrowseButtonVisibility() {
        if (selectedChar == 3) {
            browseButton.setVisible(true);
        } else {
            browseButton.setVisible(false);
        }
    }

    private void startGame() {
        if (selectedChar == 3 && customUserImage == null) {
            JOptionPane.showMessageDialog(this, "Please  select an image first for Custom Character!", "Image Missing",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        startButton.setVisible(false);
        prevBtn.setVisible(false);
        nextBtn.setVisible(false);
        browseButton.setVisible(false);

        if (selectedChar < 3) {
            dino = new DefaultChar(100, GROUND_Y, charFiles[selectedChar]);
        } else {
            dino = new CustomChar(100, GROUND_Y, customUserImage);
        }
        gameRunning = true;
        gameState = GameState.RUNNING;
        startButton.setVisible(false);

        obstacles.clear();
        String randomKey = possibleKeys[random.nextInt(possibleKeys.length)];
        obstacles.add(new SmallCactus(800, GROUND_Y - 40, gameSpeed, randomKey));

        timer.start();
        requestFocusInWindow(); // ให้กด space ได้
    }

    private void restartGame() {

        score = new Score();
        // dino = new DinoChar(100, GROUND_Y);
        if (selectedChar < 3) {
            dino = new DefaultChar(100, GROUND_Y, charFiles[selectedChar]);
        } else {
            dino = new CustomChar(100, GROUND_Y, customUserImage);
        }
        obstacles.clear();
        powerUps.clear();
        initStars();
        String randomKey = possibleKeys[random.nextInt(possibleKeys.length)];
        obstacles.add(new SmallCactus(800, GROUND_Y - 40, gameSpeed, randomKey));
        // Boss
        gameState = GameState.RUNNING;
        nextBossScore = 15;
        bossY = -200;
        playerInput = "";

        gameOver = false;
        gameRunning = true;
        restartButton.setVisible(false);
        exitButton.setVisible(false);

        timer.start();
        requestFocusInWindow();
        gameState = GameState.RUNNING;
        bossY = -200;
        bossSliding = false;
        bossTriggered = false;
        playerInput = "";
    }

    private void spawnPowerUp() {

        // โอกาสสุ่มน้อย ๆ
        if (random.nextInt(500) == 0) {

            // ตรวจสอบไม่ให้ติดกับ obstacle
            boolean safe = true;
            for (Obstacle obs : obstacles) {
                if (Math.abs(obs.getBounds().x - 800) < 100) {
                    safe = false;
                    break;
                }
            }

            if (safe) {
                powerUps.add(new InvincibleItem(800, GROUND_Y));
            }
        }

        powerUps.removeIf(PowerUp::isOffScreen);
    }

    private void updatePowerUps() {
        for (PowerUp p : powerUps) {
            p.update();
            p.setSpeed(gameSpeed);
        }
    }

    private void checkPowerUpCollision() {
        Iterator<PowerUp> it = powerUps.iterator();
        while (it.hasNext()) {
            PowerUp p = it.next();
            if (dino.getBounds().intersects(p.getBounds())) {
                dino.activateInvincible();
                it.remove();
            }
        }

    }

    // private void addNewObstacle() {

    // if (random.nextBoolean()) {
    // obstacles.add(new SmallCactus(800, 250, gameSpeed));
    // } else {
    // obstacles.add(new TallCactus(800));
    // }
    // }

    private void updateScoreFromObstacles() {

        for (Obstacle obs : obstacles) {

            if (!obs.isScored() &&
                    obs.getX() + obs.getWidth() < dino.getX()) {

                score.addPoint();
                obs.setScored(true);
            }
        }
    }

    private Obstacle createRandomObstacle(int x, String key) {
        if (random.nextBoolean()) {
            return new SmallCactus(x, GROUND_Y - 40, gameSpeed, key);
        } else {
            return new TallCactus(x, GROUND_Y - 60, gameSpeed, key);
        }
    }

    private void randomizeJumpKey() {
        currentJumpKey = possibleKeys[random.nextInt(possibleKeys.length)];
        System.out.println("Current Jump Key: " + currentJumpKey);
    }

    private void bindKey(String keyName) {

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(keyName), keyName);

        getActionMap().put(keyName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (!gameRunning)
                    return;

                if (!obstacles.isEmpty()) {

                    Obstacle first = obstacles.get(0);

                    if (keyName.equals(first.getRequiredKey())) {
                        dino.jump();
                    }
                }

            }
        });
    }

    private void initStars() {
        starXs.clear();
        starYs.clear();
        for (int i = 0; i < NUM_STARS; i++) {
            // สุ่มตำแหน่ง X เต็มความกว้างจอ
            starXs.add(random.nextInt(WIDTH));
            // สุ่มตำแหน่ง Y เฉพาะครึ่งบนของจอ (เหนือพื้นดินที่ Y=290)
            starYs.add(random.nextInt(GROUND_Y - 20));
        }
    }

    private void updateStars() {

        for (int i = 0; i < starXs.size(); i++) {

            int newX = starXs.get(i) - (int) (gameSpeed * 0.3);

            if (newX < 0) {
                newX = WIDTH;
                starYs.set(i, random.nextInt(GROUND_Y - 20));

            }

            starXs.set(i, newX);
        }
    }

    enum GameState {
        MENU,
        RUNNING,
        BOSS,
        GAME_OVER,
        PRE_BOSS
    }

    private GameState gameState = GameState.MENU;

    private void startBossFight() {
        gameState = GameState.BOSS;
        bossSliding = true;
        dino.resetToGround();
    }

    private void startTypingChallenge() {
        bossStartTime = System.currentTimeMillis();
        playerInput = "";
        targetText = bossTexts[random.nextInt(bossTexts.length)];
    }

    @Override
    public void keyTyped(KeyEvent e) {

        if (gameState == GameState.BOSS) {

            char c = e.getKeyChar();

            if (c == '\b' && playerInput.length() > 0) {
                playerInput = playerInput.substring(0, playerInput.length() - 1);
            } else {
                playerInput += c;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private void triggerGameOver() {
        gameState = GameState.GAME_OVER;
        gameOver = true;
        gameRunning = false;
        timer.stop();
        restartButton.setVisible(true);
        exitButton.setVisible(true);
    }
}
