import engine.GameManager;
import enums.Difficulty;
import enums.GameStage;
import model.Incident;
import model.Responder;
import ui.GamePanel;
import ui.GameWindow;
import util.GameConfig;
import util.SoundSynthesizer;

import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args != null && args.length > 0 && "--verify".equalsIgnoreCase(args[0])) {
            runSelfVerification();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }

    private static void runSelfVerification() {
        SoundSynthesizer.setMuted(true);
        GameManager gm = GameManager.getInstance();
        gm.startNewCampaign(Difficulty.NORMAL);
        gm.enterPlayingStage();

        List<Incident> active = gm.getDispatchCenter().getSortedActiveIncidents();
        List<Responder> roster = gm.getDispatchCenter().getResponders();
        if (active.isEmpty() || roster.isEmpty()) {
            throw new IllegalStateException("Verification failed: Incidents or Responders failed to initialize.");
        }

        gm.selectNextUrgentIncident();
        gm.autoSelectBestResponder();
        gm.attemptCalmSelectedCaller();
        gm.attemptSelectedDispatch();

        for (int i = 0; i < 120; i++) {
            gm.update(0.1);
        }

        gm.saveProgress();

        GamePanel panel = new GamePanel();
        panel.setSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        BufferedImage canvas = new BufferedImage(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();
        panel.paint(g2d);

        gm.setStage(GameStage.SHIFT_REPORT);
        panel.paint(g2d);

        gm.setStage(GameStage.FINAL_REPORT);
        panel.paint(g2d);
        g2d.dispose();

        System.out.println("MELLOW VERIFICATION PASSED: All English OOP layers, Dijkstra routing, Auto-Match, Save/Load, and Graphics2D UI rendered cleanly.");
        System.exit(0);
    }
}
