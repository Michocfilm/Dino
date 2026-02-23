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
            int drawY = y;
            if(!jumping){
            int bobbing = (int)(Math.sin(System.currentTimeMillis() / 100.0 ) * 4);
            drawY += bobbing;
            }
            g.drawImage(image, x, drawY , width, height, null);
        }
    }
}