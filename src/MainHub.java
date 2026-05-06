import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainHub extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final Map<String, ProgramScreen> screens = new LinkedHashMap<>();

    private String currentScreen = "HOME";

    public MainHub() {
        super("Automata Programs Hub");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 820);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        registerScreens();

        add(buildSidebar(), BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        cardLayout.show(cardPanel, "HOME");
    }

    private void registerScreens() {
        cardPanel.add(buildHomePage(), "HOME");

        addProgram("PROGRAM_ONE", new ProgramOne());
        addProgram("DFA_DIVISIBLE", new DFA_Divisible());
        addProgram("PROGRAM_THREE", new ProgramThree());
    }

    private void addProgram(String key, ProgramScreen screen) {
        screens.put(key, screen);
        cardPanel.add((Component) screen, key);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(15, 23, 42));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));
        sidebar.setPreferredSize(new Dimension(230, 0));

        JLabel title = new JLabel("Automata Hub");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(24));

        sidebar.add(navButton("Home", "HOME"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("CFG → PDA Converter", "PROGRAM_ONE"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("DFA Divisible", "DFA_DIVISIBLE"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("PDA aⁿbⁿ", "PROGRAM_THREE"));

        return sidebar;
    }

    private JButton navButton(String text, String target) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(new Color(30, 41, 59));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> showScreen(target));

        return button;
    }

    private JPanel buildHomePage() {
        JPanel home = new JPanel(new GridBagLayout());
        home.setBackground(new Color(245, 245, 248));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(30, 40, 30, 40)
        ));

        JLabel title = new JLabel("Choose a Program");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Select one of your automata tools.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitle.setForeground(new Color(100, 116, 139));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));

        card.add(homeButton("CFG → PDA Converter", "PROGRAM_ONE"));
        card.add(Box.createVerticalStrut(10));
        card.add(homeButton("DFA Divisible Simulator", "DFA_DIVISIBLE"));
        card.add(Box.createVerticalStrut(10));
        card.add(homeButton("PDA aⁿbⁿ Simulator", "PROGRAM_THREE"));

        home.add(card);
        return home;
    }

    private JButton homeButton(String text, String target) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 42));
        button.setPreferredSize(new Dimension(300, 42));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> showScreen(target));

        return button;
    }

    private void showScreen(String target) {
        if (currentScreen != null && screens.containsKey(currentScreen)) {
            screens.get(currentScreen).onHide();
        }

        cardLayout.show(cardPanel, target);
        currentScreen = target;

        if (screens.containsKey(target)) {
            screens.get(target).onShow();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            new MainHub().setVisible(true);
        });
    }
}