import java.awt.*;

public class Score {

    private int score = 0;

    public void update() {
        score++; // เพิ่มทุก frame
    }

    public int getScore() {
        return score;
    }

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 650, 30);
    }

    public void addPoint() {
        score++;
    }

}
