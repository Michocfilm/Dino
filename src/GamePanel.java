import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

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

    private String[] charFiles = { "dino.png", "robot.png", "ninja.png" };

    private JButton prevBtn;
    private JButton nextBtn;
    private JButton browseButton;
    private JButton homeButton;

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

    private String[] bossImageFiles = { "boss1.png", "boss2.png", "boss3.png" };
    private Image currentBossImage = null;

    private String targetText = "";
    private String playerInput = "";
    private String[] bossTexts = {
            "Polymorphism",
            "Object oriented programming",
            "Java language is fun",
            "Dino Ninja Robot boss fight",
            "Satana so handsome mak mak kub",
            "Computer Architecture",
            "Computer Ethics",
            "Discrete Mathematics",
            "public static void main(String[] args)",
            //ดวงซวย
            "FunctionalProgramming",
            "ObjectRelationalMapping",
            "Pneumonoultramicroscopics",
    };
    private long bossStartTime;
    private int timeLimit = 10;
    private int nextBossScore = 10;
    private int sentensetoType = 1;
    private int currentType = 0;

    private Image titleSelectImage;
    private Image groundImage;
    private Image backgroundImage;
    private Image bushImage;
    private Image homeImage;
    private int bushOffset = 0;
    private int bushLayerWidth =0;
    private int bushLayerHeight = 0;
    private Image[] cloudImage = new Image[2];
    private int groundOffset = 0;
    private final int NUM_CLOUD = 4;
    private ArrayList<Integer> cloudXs = new ArrayList<>();
    private ArrayList<Integer> cloudYs = new ArrayList<>();
    private ArrayList<Integer> cloudType = new ArrayList<>();

    public GamePanel() {

        titleSelectImage = new ImageIcon("title_select.png").getImage();
        backgroundImage = new ImageIcon("background.png").getImage();
        groundImage = new ImageIcon("ground.jpg").getImage();
        cloudImage[0] = new ImageIcon("cloud1.png").getImage();
        cloudImage[1] = new ImageIcon("cloud2.png").getImage();
        homeImage = new ImageIcon("home.png").getImage();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        GROUND_Y = HEIGHT - 80; // ปรับ 150 ได้ตามความหนาพื้น

        ImageIcon bushIcon = new ImageIcon("bush.png");
        bushImage = bushIcon.getImage();
        bushLayerHeight = 500;
        if(bushIcon.getIconHeight() > 0){
            bushLayerWidth = (bushIcon.getIconWidth() * bushLayerHeight) / bushIcon.getIconHeight();
        }else{
            bushLayerWidth = 800;
        }

        setBackground(Color.BLACK);
        setLayout(null);

        initCloud();

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
            updateCloud();

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
                currentType++;
                if (currentType >= sentensetoType) {
                    gameState = GameState.RUNNING;
                    nextBossScore += 10;
                    bossY = -200;
                    obstacles.clear();
                    playerInput = "";
                    // obstaclesPassedInWave = 0;
                } else {
                    targetText = bossTexts[random.nextInt(bossTexts.length)];
                    playerInput = "";
                }
            }
        }

        groundOffset -= gameSpeed;

        if (groundOffset <= -groundImage.getWidth(null)) {
            groundOffset = 0;
        }
        int bushSpeed = (int)(gameSpeed * 0.6);
        if(bushSpeed < 1) bushSpeed = 1;

        bushOffset -= bushSpeed;
        if(bushLayerWidth > 0 && bushOffset <= -bushLayerWidth){
            bushOffset = 0;
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
            if(homeImage != null){
                g.drawImage(homeImage, 0, 0, WIDTH , HEIGHT ,null);
            }else{
                g.setColor(Color.BLACK);
                g.fillRect(0,0,WIDTH,HEIGHT);
            }

            int boardWidth = 450;
            int boardHeigh = 150;
            int boardX = (WIDTH - boardWidth) / 2;
            int boardY = HEIGHT / 2 - 230;
            if(titleSelectImage != null){
                g.drawImage(titleSelectImage, boardX, boardY, boardWidth,boardHeigh - 30,null);
            }else{
                g.setColor(Color.WHITE);
                g.fillRoundRect(boardX, boardY, boardWidth, boardHeigh,20,20);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Comic Sans MS",Font.BOLD,38));
            String titleText = "SELECT CHARACTER";
            FontMetrics fm = g.getFontMetrics();
            int titleWidth = fm.stringWidth(titleText);

            int titleX = boardX + (boardWidth - titleWidth)/2;
            int titleY = boardY + 75;
            
            g.setColor(Color.BLACK);
            g.drawString(titleText,titleX + 2,titleY + 2);
            g.setColor(Color.WHITE);
            g.drawString(titleText,titleX,titleY);

            String[] charNames = { "Classic Dino", "Robot", "Ninja", "Custom Image" };
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 24));
            String name = charNames[selectedChar];
            int nameWidth = g.getFontMetrics().stringWidth(name);
            g.drawString(name, WIDTH / 2 - (nameWidth / 2), HEIGHT / 2);

            return;
        }
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }
        if (cloudImage != null) {
            for (int i = 0; i < cloudXs.size(); i++) {
                int type = cloudType.get(i);
                if (cloudImage[type] != null) {
                    g.drawImage(cloudImage[type], cloudXs.get(i), cloudYs.get(i), 500, 300, null);
                }
            }
        }
        if(bushImage != null && bushLayerWidth > 0){
            for(int x = bushOffset ; x < WIDTH ; x += bushLayerWidth){
                g.drawImage(bushImage,x,GROUND_Y - bushLayerHeight ,bushLayerWidth,bushLayerHeight,null);
            }
        }
        if (groundImage != null) {

            int imgWidth = groundImage.getWidth(null);

            for (int x = groundOffset; x < WIDTH; x += imgWidth) {
                g.drawImage(groundImage, x, GROUND_Y, null);
            }
        }
        g.setColor(Color.WHITE);
        g.drawLine(0, GROUND_Y, WIDTH, GROUND_Y);
        dino.draw(g);
        score.draw(g);
        // DEBUG
        g.setColor(Color.GREEN);
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        g.drawString("Debug Speed: " + gameSpeed, 20, 30);
        if (gameOver) {
            g.setColor(Color.WHITE);
            Font font = new Font("Comic Sans MS", Font.BOLD, 60);
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
                g.setFont(new Font("Comic Sans MS", Font.BOLD, 30));

                int textX = obs.getX() + 20;
                int textY = obs.getY() - 15;

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

            g.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            g.drawString("INVINCIBLE", x, y - 5);
        }
        if (gameState == GameState.BOSS) {

            if (currentBossImage != null) {
                g.drawImage(currentBossImage, bossX, bossY, 150, 150, null);
            } else {
                g.setColor(Color.RED);
                g.fillRect(bossX, bossY, 150, 150);
            }
            if (!bossSliding) {
                int startXs =220;
                g.setColor(Color.WHITE);
                g.setFont(new Font("Comic Sans MS",Font.BOLD,24));
                String titleText = "Type this: ";
                g.drawString(titleText,startXs,280);

                g.setFont(new Font("Consolas",Font.BOLD,28));
                FontMetrics fm = g.getFontMetrics();

                int textX = startXs;
                int textY = 330;
                
                for(int i = 0; i < targetText.length(); i++){
                    char targetChar = targetText.charAt(i);
                    String charToDraw = String.valueOf(targetChar);
                    Color textColor;

                    if(i < playerInput.length()){
                        char inputChar = playerInput.charAt(i);
                        if(inputChar == targetChar){
                            textColor = Color.GREEN;
                        }else{
                            textColor = Color.RED;
                            charToDraw = String.valueOf(inputChar);
                        }
                    }else{
                        textColor = Color.WHITE;
                    }
                    g.setColor(Color.BLACK);
                    g.drawString(charToDraw,textX + 2,textY + 2);

                    g.setColor(textColor);
                    g.drawString(charToDraw, textX, textY);

                    textX += fm.stringWidth(String.valueOf(targetChar));
                }
                g.setFont(new Font("Comic Sans MS",Font.BOLD,20));
                g.setColor(Color.WHITE);
                long elapsed = (System.currentTimeMillis() - bossStartTime) / 1000;
                g.drawString("Time left: " + (timeLimit - elapsed), 300, 390);
                g.setColor(Color.YELLOW);
                g.drawString("Wave: " + (currentType + 1) + " / " + sentensetoType, 300, 420);
            }
        }

        g.setColor(Color.RED);
        // วาดกรอบ Hitbox ของ Player
        Rectangle pBox = dino.getBounds();
        g.drawRect(pBox.x, pBox.y, pBox.width, pBox.height);

        // วาดกรอบ Hitbox ของสิ่งกีดขวาง
        for (Obstacle obs : obstacles) {
            Rectangle oBox = obs.getBounds();
            g.drawRect(oBox.x, oBox.y, oBox.width, oBox.height);
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

        ImageIcon startIcon = new ImageIcon(new ImageIcon("start_button.png").getImage().getScaledInstance(150, 60, Image.SCALE_SMOOTH));
        startButton = new JButton(startIcon);
        startButton.setBounds(WIDTH / 2 - 75,HEIGHT / 2 + 80,150,60);
        startButton.setContentAreaFilled(false);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        add(startButton);
        startButton.addActionListener(e -> startGame());

        ImageIcon restartIcon = new ImageIcon(new ImageIcon("restart_button.png").getImage().getScaledInstance(140, 40, Image.SCALE_SMOOTH));
        restartButton = new JButton(restartIcon);
        restartButton.setBounds(WIDTH / 2 - 70, HEIGHT / 2 + 10, 140, 40);
        startButton.setContentAreaFilled(false);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        restartButton.setVisible(false);
        add(restartButton);
        restartButton.addActionListener(e -> restartGame());

        ImageIcon homeIcon = new ImageIcon(new ImageIcon("home_button.png").getImage().getScaledInstance(140, 40, Image.SCALE_SMOOTH));
        homeButton = new JButton(homeIcon);
        homeButton.setBounds(WIDTH / 2 - 70, HEIGHT / 2 + 60, 140, 40);
        startButton.setContentAreaFilled(false);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        homeButton.setVisible(false);
        add(homeButton);

        ImageIcon exitIcon = new ImageIcon(new ImageIcon("exit_button.png").getImage().getScaledInstance(140, 40, Image.SCALE_SMOOTH));
        exitButton = new JButton(exitIcon);
        exitButton.setBounds(WIDTH / 2 - 70, HEIGHT / 2 + 110, 140, 40);
        startButton.setContentAreaFilled(false);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        exitButton.setVisible(false);
        add(exitButton);

        exitButton.addActionListener(e -> System.exit(0));

        ImageIcon leftIcon = new ImageIcon(new ImageIcon("left_button.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
        prevBtn = new JButton(leftIcon);
        prevBtn.setBounds(WIDTH / 2 - 130, HEIGHT / 2 - 30, 50, 50);
        prevBtn.setContentAreaFilled(false);
        prevBtn.setFocusPainted(false);
        prevBtn.setBorderPainted(false);
        prevBtn.addActionListener(e -> {
            selectedChar = (selectedChar - 1 + 4) % 4;
            updateBrowseButtonVisibility();
            repaint();
        });
        add(prevBtn);

        ImageIcon rightIcon = new ImageIcon(new ImageIcon("right_button.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
        nextBtn = new JButton(rightIcon);
        nextBtn.setBounds(WIDTH / 2 + 80, HEIGHT / 2 - 30, 50, 50);
        nextBtn.setContentAreaFilled(false);
        nextBtn.setFocusPainted(false);
        nextBtn.setBorderPainted(false);
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

        homeButton.addActionListener(e -> goToMenu());
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
        obstacles.add(new SmallTree(800, GROUND_Y - 80, gameSpeed, randomKey));

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
        String randomKey = possibleKeys[random.nextInt(possibleKeys.length)];
        obstacles.add(new SmallTree(800, GROUND_Y - 80, gameSpeed, randomKey));
        // Boss
        gameState = GameState.RUNNING;
        nextBossScore = 10;
        bossY = -200;
        playerInput = "";

        gameOver = false;
        gameRunning = true;
        restartButton.setVisible(false);
        homeButton.setVisible(false);
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
            return new SmallTree(x, GROUND_Y - 80, gameSpeed, key);
        } else {
            return new TallTree(x, GROUND_Y - 120, gameSpeed, key);
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

    private void initCloud() {
        cloudXs.clear();
        cloudYs.clear();
        cloudType.clear();
        for (int i = 0; i < NUM_CLOUD; i++) {
            cloudXs.add(random.nextInt(WIDTH));
            cloudYs.add(20 + random.nextInt(60));
            cloudType.add(random.nextInt(2));
        }
    }

    private void updateCloud() {
        for (int i = 0; i < cloudXs.size(); i++) {
            int newX = cloudXs.get(i) - (int) (gameSpeed * 0.2);
            if (newX + 200 < 0) {
                newX = WIDTH;
                cloudYs.set(i, 20 + random.nextInt(100));
                cloudType.set(i, random.nextInt(2));
            }
            cloudXs.set(i, newX);
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

        String randomBossFile = bossImageFiles[random.nextInt(bossImageFiles.length)];
        currentBossImage = new ImageIcon(randomBossFile).getImage();
    }

    private void startTypingChallenge() {
        bossStartTime = System.currentTimeMillis();
        playerInput = "";
        sentensetoType = nextBossScore / 10;
        currentType = 0;
        timeLimit = 10 + ((sentensetoType - 1) * 5);
        targetText = bossTexts[random.nextInt(bossTexts.length)];
    }

    @Override
    public void keyTyped(KeyEvent e) {

        if (gameState == GameState.BOSS) {
            char c = e.getKeyChar();

            if (c == '\b' && playerInput.length() > 0) {
                playerInput = playerInput.substring(0, playerInput.length() - 1);
            } else if (c != '\b' && c != KeyEvent.CHAR_UNDEFINED) {
                if (targetText != null && playerInput.length() < targetText.length()) {
                    playerInput += c;
                }
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
        homeButton.setVisible(true);
        restartButton.setVisible(true);
        exitButton.setVisible(true);

    }

    private void goToMenu() {

        gameState = GameState.MENU;
        gameRunning = false;
        gameOver = false;

        timer.stop();

        // ซ่อนปุ่มตอน Game Over
        restartButton.setVisible(false);
        exitButton.setVisible(false);
        homeButton.setVisible(false);

        // แสดงปุ่มเลือกตัวละคร
        startButton.setVisible(true);
        prevBtn.setVisible(true);
        nextBtn.setVisible(true);
        updateBrowseButtonVisibility();

        // รีเซ็ตข้อมูลเกม
        obstacles.clear();
        powerUps.clear();
        score = new Score();
        bossY = -200;
        playerInput = "";

        repaint();
    }
}
