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
        setLayout(null); // สำคัญ! ใช้จัดตำแหน่งเอง

        score = new Score();
        dino = new Dinosaur(100, 250);
        obstacles = new ArrayList<>();

        createButtons();

        timer = new Timer(16, this);
        // กำหนดให้กด SPACE แล้วเรียก jump
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("SPACE"), "jump");

        getActionMap().put("jump", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameRunning) {
                    dino.jump();
                }
            }
        });

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
        for (Obstacle obs : obstacles) {
            obs.update();
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
            obstacles.add(createRandomObstacle(getWidth()));
            return;
        }

        Obstacle last = obstacles.get(obstacles.size() - 1);

        // 🔹 ระยะขั้นต่ำที่กระโดดทัน (อย่าต่ำกว่านี้)
        int minGap = 110;

        // 🔹 ระยะสูงสุดจะลดลงเมื่อความเร็วเพิ่ม
        int maxGap = 250 - (gameSpeed * 5);

        // กัน maxGap ต่ำเกิน
        if (maxGap < minGap + 40) {
            maxGap = minGap + 40;
        }

        int gap;

        // 🔥 25% โอกาสเกิดแบบชิดมาก (ดักกระโดดซ้ำ)
        if (random.nextInt(4) == 0) {
            gap = minGap + random.nextInt(30); // ใกล้มาก
        } else {
            gap = random.nextInt(maxGap - minGap) + minGap;
        }

        int spawnX = last.getX() + last.getWidth() + gap;

        if (spawnX < WIDTH + 200) {
            obstacles.add(createRandomObstacle(spawnX));
        }
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
        obstacles.add(new SmallCactus(800, 250, gameSpeed));

        timer.start();
        requestFocusInWindow(); // ให้กด space ได้
    }

    private void restartGame() {

        score = new Score();
        dino = new Dinosaur(100, 250);
        obstacles.clear();
        powerUps.clear();
        obstacles.add(new SmallCactus(800, 250, gameSpeed));

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

    private Obstacle createRandomObstacle(int x) {
        if (random.nextBoolean()) {
            return new SmallCactus(x, 250, gameSpeed);
        } else {
            return new TallCactus(x, 230, gameSpeed);
        }
    }

}
