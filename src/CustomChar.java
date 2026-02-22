import java.awt.*;

public class CustomChar extends Player {
    public CustomChar(int x, int y, Image customImage) {
        super(x, y);
        this.image = customImage;
    }

    @Override
    public void draw(Graphics g) {
        if (invincible && (System.currentTimeMillis() / 200) % 2 == 0) {
        }else{
            if(image != null){
                g.drawImage(image, x, y,width,height, null);
            }else{
                
            }
        }
    }
}