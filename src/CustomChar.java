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
                int drawY = y;
                if(!jumping){
                    int bobbing = (int)(Math.sin(System.currentTimeMillis() / 100 ) * 4);
                    drawY += bobbing;
                }
                g.drawImage(image, x, drawY , width , height , null);
            }else{
                
            }
        }
    }
}