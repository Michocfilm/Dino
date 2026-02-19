import java.awt.*;

public class SmallCactus extends Obstacle {

    public SmallCactus(int x) {
        super(x, 270, 20, 40);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);
    }
}
