import java.awt.*;

public abstract class Obstacle {

    protected int x, y;
    protected int width, height;
    protected int speed = 5;
    protected boolean scored = false;

    public Obstacle(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public void update() {
        x -= speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public abstract void draw(Graphics g);

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isOffScreen() {
        return x < -width;
    }

    public int getX() {
        return x;
    }

    public boolean isScored() {
        return scored;
    }

    public void setScored(boolean scored) {
        this.scored = scored;
    }

    public int getWidth() {
        return width;
    }

}
