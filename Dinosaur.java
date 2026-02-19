import java.awt.*;

public class Dinosaur {
    private boolean invincible = false;
    private long invincibleEndTime = 0;

    private int x, y;
    private int width = 40;
    private int height = 40;

    private int velocityY = 0;
    private boolean jumping = false;
    private final int gravity = 1;
    private int ground;

    public Dinosaur(int x, int groundY) {
        this.x = x;
        this.ground = groundY;
        this.y = groundY - height; // ให้ฐานติดพื้น
    }

    public void jump() {
        if (!jumping) {
            velocityY = -15;
            jumping = true;
        }
    }

    public void update() {
        y += velocityY;
        velocityY += gravity;

        if (y >= ground - height) {
            y = ground - height;
            jumping = false;
        }

        if (invincible && System.currentTimeMillis() > invincibleEndTime) {
            invincible = false;
        }
    }

    // public void draw(Graphics g) {
    // g.setColor(Color.GRAY);
    // g.fillRect(x, y, width, height);
    // }
    public void draw(Graphics g) {

        if (invincible) {
            // กระพริบทุก 200 ms
            if ((System.currentTimeMillis() / 200) % 2 == 0) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(Color.WHITE);
            }
        } else {
            g.setColor(Color.WHITE);
        }

        g.fillRect(x, y, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void activateInvincible() {
        invincible = true;
        invincibleEndTime = System.currentTimeMillis() + 3000; // 3 วิ
    }

    public boolean isInvincible() {
        return invincible;
    }

    public int getX() {
        return x;
    }

    public double getInvincibletTimeRemainingRatio() {
        if (!invincible)
            return 0;
        long remaining = invincibleEndTime - System.currentTimeMillis();
        if (remaining <= 0)
            return 0;
        return remaining / 3000.0;
    }

}
