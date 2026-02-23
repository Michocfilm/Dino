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
            int drawY = y;
            if(!jumping){
            int bobbing = (int)(Math.sin(System.currentTimeMillis() / 100.0 ) * 4);
            drawY += bobbing;
            }
            g.drawImage(image, x, drawY, width , height ,null);
        }
    }
}