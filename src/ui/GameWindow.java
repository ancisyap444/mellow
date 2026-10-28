package ui;

import javax.swing.JFrame;
import util.GameConfig;

public class GameWindow extends JFrame {
    private final GamePanel gamePanel;

    public GameWindow() {
        super(GameConfig.GAME_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        this.gamePanel = new GamePanel();
        setContentPane(gamePanel);
        pack();
        setLocationRelativeTo(null);
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }
}
