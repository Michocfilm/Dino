import javax.swing.*;

public class GameFrame extends JFrame {

    public GameFrame() {
        setTitle("Dino Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new GamePanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setIconImage(new ImageIcon("icon.png").getImage());
    }

    public static void main(String[] args) {
        new GameFrame();
    }
}
