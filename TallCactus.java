import java.awt.*;

public class TallCactus extends Obstacle {

    public TallCactus(int x) {
        super(x, 240, 30, 70);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }
}
