import java.awt.*;

public class InvincibleItem extends PowerUp {
    private static final int ITEM_SIZE = 20;
    public InvincibleItem(int x, int groundY) {
        super(x, groundY - ITEM_SIZE - 20, ITEM_SIZE);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval(x, y, size, size);
    }
}
