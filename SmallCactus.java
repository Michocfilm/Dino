import java.awt.*;

public class SmallCactus extends Obstacle {

    public SmallCactus(int x, int y, int speed) {
        super(x, y, 20, 40, speed);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);
    }
}
