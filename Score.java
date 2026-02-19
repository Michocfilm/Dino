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
        Font font = new Font("Arial", Font.BOLD, 24);
        g.setFont(font);

        String text = "Score: " + score;

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);

        int x = 1000 - textWidth - 20; // WIDTH - margin
        int y = 40;

        g.drawString(text, x, y);
    }

    public void addPoint() {
        score++;
    }

}
