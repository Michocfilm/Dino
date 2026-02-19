import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Iterator;

public class GamePanel extends JPanel implements ActionListener {
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
    private Dinosaur dino;
    private ArrayList<Obstacle> obstacles;
    private Random random = new Random();

    // --- ส่วนของฉากหลัง (Sky Elements) ---
    private final int MOON_X = 650; // ตำแหน่ง X พระจันทร์ (ขวาบน)
    private final int MOON_Y = 50; // ตำแหน่ง Y พระจันทร์
    private final int MOON_SIZE = 80; // ขนาดพระจันทร์

    // ใช้ ArrayList เก็บพิกัด X และ Y ของดาวแต่ละดวง
    private ArrayList<Integer> starXs = new ArrayList<>();
    private ArrayList<Integer> starYs = new ArrayList<>();
    private final int NUM_STARS = 100; // จำนวนดาวที่ต้องการ

    public GamePanel() {

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        GROUND_Y = HEIGHT - 150; // ปรับ 150 ได้ตามความหนาพื้น

        setBackground(Color.BLACK);
        setLayout(null);

        initStars();

        score = new Score();
        dino = new Dinosaur(100, GROUND_Y);

        obstacles = new ArrayList<>();

        createButtons();

        timer = new Timer(16, this);

        // 🔥 bind ทุกปุ่มที่ใช้ได้
        for (String key : possibleKeys) {
            bindKey(key);
        }

        // สุ่มปุ่มเริ่มต้น
        randomizeJumpKey();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (!gameRunning)
            return;

        dino.update();
        // score.update();
        updateScoreFromObstacles();

        increaseDifficulty();
        updateObstacles();
        spawnObstacle();
        checkCollision();
        spawnPowerUp();
        updatePowerUps();
        checkPowerUpCollision();
        updateStars();

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

                gameOver = true;
                gameRunning = false;

                timer.stop();

                restartButton.setVisible(true);
                exitButton.setVisible(true);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
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
        startButton.setBounds(WIDTH / 2 - 50, HEIGHT / 2 - 40, 100, 40);
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
    }

    private void startGame() {
        gameRunning = true;
        gameOver = false;

        startButton.setVisible(false);

        obstacles.clear();
        String randomKey = possibleKeys[random.nextInt(possibleKeys.length)];
        obstacles.add(new SmallCactus(800, GROUND_Y - 40, gameSpeed, randomKey));

        timer.start();
        requestFocusInWindow(); // ให้กด space ได้
    }

    private void restartGame() {

        score = new Score();
        dino = new Dinosaur(100, GROUND_Y);
        obstacles.clear();
        powerUps.clear();
        initStars();
        String randomKey = possibleKeys[random.nextInt(possibleKeys.length)];
        obstacles.add(new SmallCactus(800, GROUND_Y - 40, gameSpeed, randomKey));

        gameOver = false;
        gameRunning = true;
        restartButton.setVisible(false);
        exitButton.setVisible(false);

        timer.start();
        requestFocusInWindow();
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

}
