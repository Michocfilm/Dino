import java.awt.*;

public class InvincibleItem extends PowerUp {

    public InvincibleItem(int x) {
        super(x, 250, 20);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval(x, y, size, size);
    }
}
