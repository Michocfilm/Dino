import java.awt.*;
import javax.swing.ImageIcon;

public class SmallTree extends Obstacle {

    private Image image;
    public SmallTree(int x, int y, int speed, String key) {
        super(x, y, 40, 40, speed , key);
        this.image = new ImageIcon("small_tree.png").getImage();
    }

    @Override
    public void draw(Graphics g) {
        if(image != null){
            g.drawImage(image, x, y,width,height ,null);
        }else{
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);
        }
    }
}
