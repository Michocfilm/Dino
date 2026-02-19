import java.awt.*;

public class TallCactus extends Obstacle {

    public TallCactus(int x, int y, int speed) {
        super(x, y, 30, 60, speed);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }
}
