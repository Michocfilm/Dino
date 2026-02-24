import java.awt.*;
import javax.swing.ImageIcon;

public class TallTree extends Obstacle {
    private Image image;

    public TallTree(int x, int y, int speed, String key) {
        super(x, y + 10, 80, 140, speed, key);
        this.image = new ImageIcon("tall_tree.png").getImage(); 
    }

    @Override
    public void draw(Graphics g) {
        if(image != null){
            g.drawImage(image, x, y,width,height, null);
        }else{
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
        }
    }
    public Rectangle getBounds(){
        return new Rectangle(x + 20, y + 15, width - 40, height - 20);
    }
}
