import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Iterator;

public class GamePanel extends JPanel implements ActionListener {
    private Score score;
    private int gameSpeed = 5;
    // private int lastSpawnX = 800;
    private final int MIN_SAFE_DISTANCE = 180;
    private int clusterCount = 0;
    private String currentJumpKey = "SPACE";
    private String[] possibleKeys = { "W", "A", "S", "D", "SPACE" };

    private final int WIDTH = 800;
    private final int HEIGHT = 400;
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

    public GamePanel() {

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setLayout(null);

        score = new Score();
        dino = new Dinosaur(100, 250);
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
        dino.draw(g);
        score.draw(g);

        if (gameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", 280, 120);
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

    }

    private void increaseDifficulty() {
        gameSpeed = 5 + (int) (score.getScore() * 0.05);

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
        startButton.setBounds(350, 150, 100, 40);
        add(startButton);

        restartButton = new JButton("RESTART");
        restartButton.setBounds(350, 200, 100, 40);
        restartButton.setVisible(false);
        add(restartButton);

        exitButton = new JButton("EXIT");
        exitButton.setBounds(350, 250, 100, 40);
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
        obstacles.add(new SmallCactus(800, 250, gameSpeed, randomKey));

        timer.start();
        requestFocusInWindow(); // ให้กด space ได้
    }

    private void restartGame() {

        score = new Score();
        dino = new Dinosaur(100, 250);
        obstacles.clear();
        powerUps.clear();
        String randomKey = possibleKeys[random.nextInt(possibleKeys.length)];
        obstacles.add(new SmallCactus(800, 250, gameSpeed, randomKey));

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
                powerUps.add(new InvincibleItem(800));
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
            return new SmallCactus(x, 250, gameSpeed, key);
        } else {
            return new TallCactus(x, 230, gameSpeed, key);
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

}
