import java.awt.*;

public class TallCactus extends Obstacle {

    public TallCactus(int x, int y, int speed, String key) {
        super(x, 230, 30, 60, speed, key);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }
}
