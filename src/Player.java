import java.awt.*;

public abstract class Player {
    protected boolean invincible = false;
    protected long invincibleEndTime = 0;

    protected int x, y;
    protected int width = 40;
    protected int height = 40;

    protected int velocityY = 0;
    protected boolean jumping = false;
    protected final int gravity = 1;
    protected int ground;

    protected Image image;

    public Player(int x, int groundY) {
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
            velocityY = 0;
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
    public boolean isJumping() {
        return jumping;
    }

    public int getX() {
        return x;
    }

    public Image getImage(){
        return image;
    }
    
    public int getY(){
        return y;
    }
    public void resetToGround() {
        y = ground - height;
        velocityY = 0;
        jumping = false;
    }

    public double getInvincibletTimeRemainingRatio() {
        if (!invincible)
            return 0;
        long remaining = invincibleEndTime - System.currentTimeMillis();
        if (remaining <= 0)
            return 0;
        return remaining / 3000.0;
    }
    public abstract void draw(Graphics g);
}
