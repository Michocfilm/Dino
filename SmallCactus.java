import java.awt.*;

public class SmallCactus extends Obstacle {

    public SmallCactus(int x, int y, int speed, String key) {
        super(x, y, 20, 40, speed , key);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);
    }
}
