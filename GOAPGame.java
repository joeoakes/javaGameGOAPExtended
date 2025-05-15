import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

// Base action class
abstract class GOAPAction {
    public abstract boolean checkPreconditions(EnemyAgent agent);
    public abstract void perform(EnemyAgent agent);
    public abstract int getCost();
}

class PatrolAction extends GOAPAction {
    public boolean checkPreconditions(EnemyAgent agent) {
        return true;
    }
    public void perform(EnemyAgent agent) {
        agent.currentAction = "Patrol";
        agent.x += Math.sin(System.currentTimeMillis() / 500.0) * 2;
        agent.y += Math.cos(System.currentTimeMillis() / 500.0) * 2;
    }
    public int getCost() {
        return 3;
    }
}

class ChaseAction extends GOAPAction {
    public boolean checkPreconditions(EnemyAgent agent) {
        return agent.distanceToPlayer() < 200;
    }
    public void perform(EnemyAgent agent) {
        agent.currentAction = "Chase";
        double dx = agent.playerX - agent.x;
        double dy = agent.playerY - agent.y;
        agent.x += dx / 20;
        agent.y += dy / 20;
    }
    public int getCost() {
        return 2;
    }
}

class AttackAction extends GOAPAction {
    public boolean checkPreconditions(EnemyAgent agent) {
        return agent.distanceToPlayer() < 50;
    }
    public void perform(EnemyAgent agent) {
        agent.currentAction = "Attack";
        agent.playerHealth -= 1;
    }
    public int getCost() {
        return 1;
    }
}

class FleeAction extends GOAPAction {
    public boolean checkPreconditions(EnemyAgent agent) {
        return agent.health < 30;
    }
    public void perform(EnemyAgent agent) {
        agent.currentAction = "Flee";
        double dx = agent.playerX - agent.x;
        double dy = agent.playerY - agent.y;
        agent.x -= dx / 10;
        agent.y -= dy / 10;
    }
    public int getCost() {
        return 1;
    }
}

class EnemyAgent {
    public double x, y;
    public double playerX, playerY;
    public int health = 100;
    public int playerHealth = 100;
    public String currentAction = "None";
    public List<GOAPAction> actions;

    public EnemyAgent(double x, double y) {
        this.x = x;
        this.y = y;
        actions = List.of(
                new FleeAction(),
                new AttackAction(),
                new ChaseAction(),
                new PatrolAction()
        );
    }

    public void update() {
        for (GOAPAction action : actions) {
            if (action.checkPreconditions(this)) {
                action.perform(this);
                break;
            }
        }
    }

    public double distanceToPlayer() {
        double dx = playerX - x;
        double dy = playerY - y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

public class GOAPGame extends JPanel implements KeyListener, Runnable {
    int playerX = 100, playerY = 100;
    boolean[] keys = new boolean[256];
    EnemyAgent enemy = new EnemyAgent(300, 300);

    public GOAPGame() {
        setPreferredSize(new Dimension(500, 500));
        setBackground(Color.BLACK);
        JFrame frame = new JFrame("GOAP AI Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setVisible(true);
        frame.addKeyListener(this);
        new Thread(this).start();
    }

    public void run() {
        while (true) {
            updateGame();
            repaint();
            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void updateGame() {
        if (keys[KeyEvent.VK_LEFT]) playerX -= 5;
        if (keys[KeyEvent.VK_RIGHT]) playerX += 5;
        if (keys[KeyEvent.VK_UP]) playerY -= 5;
        if (keys[KeyEvent.VK_DOWN]) playerY += 5;

        enemy.playerX = playerX;
        enemy.playerY = playerY;
        enemy.update();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.GREEN);
        g.fillRect(playerX, playerY, 20, 20);
        g.drawString("Player HP: " + enemy.playerHealth, 10, 20);

        g.setColor(Color.BLUE);
        g.fillRect((int) enemy.x, (int) enemy.y, 20, 20);
        g.drawString("Enemy HP: " + enemy.health, 10, 40);

        // Draw current action debug overlay
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("AI: " + enemy.currentAction, (int) enemy.x - 10, (int) enemy.y - 5);
    }

    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GOAPGame::new);
    }
}
