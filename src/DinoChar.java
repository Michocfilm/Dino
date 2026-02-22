import java.awt.Graphics;

public class DinoChar extends Player {
    public DinoChar(int x, int groundY) {
        super(x, groundY);
        this.image = new javax.swing.ImageIcon("dino.png").getImage();
    }

    @Override
    public void draw(Graphics g) {
        if (invincible && (System.currentTimeMillis() / 200) % 2 == 0) {
        } else {
            g.drawImage(image, x, y , width, height, null);
        }
    }
}