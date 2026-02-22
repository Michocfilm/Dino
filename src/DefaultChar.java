import java.awt.*;
import javax.swing.ImageIcon;

public class DefaultChar extends Player{
    public DefaultChar(int x,int y,String imagePath){
        super(x,y);
        this.image = new ImageIcon(imagePath).getImage();
    }
    @Override
    public void draw(Graphics g){
        if(invincible && (System.currentTimeMillis() / 200) % 2 == 0){
        }else{
            g.drawImage(image, x, y, width,height,null);
        }
    }
}