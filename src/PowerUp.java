import java.awt.*;

public abstract class PowerUp {

    protected int x, y, size;
    protected int speed = 5;

    public PowerUp(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public void update() {
        x -= speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    public boolean isOffScreen() {
        return x < -size;
    }

    public abstract void draw(Graphics g);
}
