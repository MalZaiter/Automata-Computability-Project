import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;

public class DFA_Divisible extends JFrame {

    // ================= DFA LOGIC =================
    enum State {
        q0N, q0Z, q1N, q1Z, q2N, q2Z
    }

    static class DFAResult {
        boolean accepted;
        State finalState;
        List<String> steps;

        DFAResult(boolean accepted, State finalState, List<String> steps) {
            this.accepted = accepted;
            this.finalState = finalState;
            this.steps = steps;
        }
    }

    public static DFAResult runDFA(String input) {
        State state = State.q0N;
        List<String> steps = new ArrayList<>();
        steps.add("Start state: " + state);

        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);
            State oldState = state;
            switch (state) {
                case q0N:
                    if (symbol == '0')
                        state = State.q0Z;
                    else if (symbol == '1')
                        state = State.q1N;
                    else
                        return new DFAResult(false, state, steps);
                    break;
                case q0Z:
                    if (symbol == '0')
                        state = State.q0Z;
                    else if (symbol == '1')
                        state = State.q1N;
                    else
                        return new DFAResult(false, state, steps);
                    break;
                case q1N:
                    if (symbol == '0')
                        state = State.q1Z;
                    else if (symbol == '1')
                        state = State.q2N;
                    else
                        return new DFAResult(false, state, steps);
                    break;
                case q1Z:
                    if (symbol == '0')
                        state = State.q1Z;
                    else if (symbol == '1')
                        state = State.q2N;
                    else
                        return new DFAResult(false, state, steps);
                    break;
                case q2N:
                    if (symbol == '0')
                        state = State.q2Z;
                    else if (symbol == '1')
                        state = State.q0N;
                    else
                        return new DFAResult(false, state, steps);
                    break;
                case q2Z:
                    if (symbol == '0')
                        state = State.q2Z;
                    else if (symbol == '1')
                        state = State.q0N;
                    else
                        return new DFAResult(false, state, steps);
                    break;
            }
            steps.add("Read " + symbol + ": " + oldState + " \u2192 " + state);
        }
        boolean accepted = (state == State.q0Z);
        return new DFAResult(accepted, state, steps);
    }

    private static State nextState(State s, char c) {
        switch (s) {
            case q0N:
                return c == '0' ? State.q0Z : State.q1N;
            case q0Z:
                return c == '0' ? State.q0Z : State.q1N;
            case q1N:
                return c == '0' ? State.q1Z : State.q2N;
            case q1Z:
                return c == '0' ? State.q1Z : State.q2N;
            case q2N:
                return c == '0' ? State.q2Z : State.q0N;
            case q2Z:
                return c == '0' ? State.q2Z : State.q0N;
            default:
                return s;
        }
    }

    // ================= GUI =================
    private static final Color NAVY = new Color(15, 23, 42);
    private static final Color EDGE_0 = new Color(37, 99, 235); // blue for '0' transitions
    private static final Color EDGE_1 = new Color(168, 85, 247); // purple for '1' transitions
    private static final Color GRAY_TXT = new Color(30, 41, 59);
    private static final Color RING = new Color(180, 195, 215);

    private JTextField inputField;
    private JLabel resultLabel;
    private JPanel tapePanel, stateDiagramPanel;
    private JTable logTable;
    private DefaultTableModel logModel;
    private JButton runButton, stepButton, resetButton;
    private JSlider speedSlider;

    private javax.swing.Timer animationTimer;
    private final List<State> animPath = new ArrayList<>();
    private final List<Character> animLabels = new ArrayList<>();
    private int animIndex = 0;
    private String lastInput = null;

    public DFA_Divisible() {
        setTitle("DFA Simulator \u2014 #1s \u2261 0 (mod 3) AND ends with 0");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 820);
        setMinimumSize(new Dimension(1050, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 245, 248));
        setLayout(new BorderLayout(0, 0));

        // The header and bottom button bar stay fixed; the middle area scrolls
        // vertically so the transition log is reachable on shorter windows.
        add(buildHeader(), BorderLayout.NORTH);

        JScrollPane centerScroll = new JScrollPane(buildCenter(),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        centerScroll.setBorder(BorderFactory.createEmptyBorder());
        centerScroll.getViewport().setBackground(new Color(245, 245, 248));
        centerScroll.getVerticalScrollBar().setUnitIncrement(20);
        add(centerScroll, BorderLayout.CENTER);

        add(buildBottomPanel(), BorderLayout.SOUTH);

        // Single Timer; speed slider drives its delay anytime.
        animationTimer = new javax.swing.Timer(speedSlider.getValue(), e -> {
            if (animIndex < animPath.size()) {
                advanceOneStep();
            } else {
                animationTimer.stop();
            }
        });

        resetAnimation();
        setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        JLabel title = new JLabel("Deterministic Finite Automaton Simulator");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel(
                "Strings over {0,1} where the number of 1s is divisible by 3 AND the string ends with 0");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(219, 234, 254));
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(245, 245, 248));
        center.setBorder(new EmptyBorder(10, 14, 4, 14));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(5, 5, 5, 5);

        // Right-side column: Input String on top, Input Tape (vertical) below it.
        JPanel rightColumn = new JPanel(new GridBagLayout());
        rightColumn.setOpaque(false);
        GridBagConstraints rg = new GridBagConstraints();
        rg.fill = GridBagConstraints.BOTH;
        rg.gridx = 0;
        rg.weightx = 1;
        rg.gridy = 0;
        rg.weighty = 0;
        rightColumn.add(buildInputCard(), rg);
        rg.gridy = 1;
        rg.weighty = 1;
        rg.insets = new Insets(8, 0, 0, 0);
        rightColumn.add(buildTapeCard(), rg);

        // Top row: diagram (left, wide) and the right column (input + tape).
        g.gridy = 0;
        g.gridx = 0;
        g.gridwidth = 2;
        g.weightx = 0.78;
        g.weighty = 2.5;
        center.add(buildDiagramCard(), g);

        g.gridx = 2;
        g.gridwidth = 1;
        g.weightx = 0.22;
        center.add(rightColumn, g);

        // Bottom row: transition log spans full width.
        g.gridy = 1;
        g.gridx = 0;
        g.gridwidth = 3;
        g.weightx = 1;
        g.weighty = 1;
        center.add(buildLogCard(), g);

        return center;
    }

    private JPanel buildInputCard() {
        JPanel card = cardPanel("Input String");
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        JLabel label = new JLabel("Binary:");
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(100, 116, 139));

        inputField = new JTextField(14);
        inputField.setFont(new Font("Monospaced", Font.BOLD, 16));
        inputField.setToolTipText("Enter a binary string (0s and 1s only)");

        // Reject characters other than '0' and '1' as the user types.
        ((javax.swing.text.AbstractDocument) inputField.getDocument())
                .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                    @Override
                    public void insertString(FilterBypass fb, int off, String s,
                            javax.swing.text.AttributeSet a)
                            throws javax.swing.text.BadLocationException {
                        if (s != null && s.matches("[01]+"))
                            super.insertString(fb, off, s, a);
                    }

                    @Override
                    public void replace(FilterBypass fb, int off, int len, String s,
                            javax.swing.text.AttributeSet a)
                            throws javax.swing.text.BadLocationException {
                        if (s == null || s.isEmpty() || s.matches("[01]+"))
                            super.replace(fb, off, len, s, a);
                    }
                });

        // When input changes, drop any in-progress animation state.
        inputField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                onInputChanged();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                onInputChanged();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                onInputChanged();
            }
        });

        row.add(label);
        row.add(inputField);
        card.add(row);
        return card;
    }

    private void onInputChanged() {
        if (animationTimer != null && animationTimer.isRunning())
            animationTimer.stop();
        animPath.clear();
        animLabels.clear();
        animIndex = 0;
        lastInput = null;
        clearLog();
        if (resultLabel != null)
            resultLabel.setText(" ");
        updateTape(-1);
        if (stateDiagramPanel != null)
            stateDiagramPanel.repaint();
    }

    private JPanel buildDiagramCard() {
        JPanel card = cardPanel("State Diagram \u2014 Accept: q0Z");
        stateDiagramPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintDiagram((Graphics2D) g);
            }
        };
        stateDiagramPanel.setOpaque(false);
        stateDiagramPanel.setPreferredSize(new Dimension(820, 470));
        stateDiagramPanel.setMinimumSize(new Dimension(700, 470));
        card.add(stateDiagramPanel);
        return card;
    }

    private JPanel buildTapeCard() {
        JPanel card = cardPanel("Input Tape");
        tapePanel = new JPanel();
        tapePanel.setLayout(new BoxLayout(tapePanel, BoxLayout.Y_AXIS));
        tapePanel.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(tapePanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(220, 360));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scrollPane);
        return card;
    }

    private JPanel buildLogCard() {
        JPanel card = cardPanel("Transition Log");
        String[] cols = { "Step", "State", "Read", "Next State", "Action" };
        logModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        logTable = new JTable(logModel);
        logTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logTable.setRowHeight(22);
        logTable.setGridColor(new Color(226, 232, 240));
        logTable.setShowGrid(true);
        logTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        logTable.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        card.add(scrollPane);
        return card;
    }

    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(new Color(245, 245, 248));
        bottom.setBorder(new EmptyBorder(4, 14, 12, 14));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);

        resetButton = mainBtn("\u27F3 Reset", new Color(148, 163, 184));
        stepButton = mainBtn("\u25B6 Step", new Color(59, 130, 246));
        runButton = mainBtn("\u25B6\u25B6 Auto", new Color(99, 102, 241));

        resetButton.addActionListener(e -> resetAnimation());
        stepButton.addActionListener(e -> stepOnce());
        runButton.addActionListener(e -> runAnimation());

        buttons.add(resetButton);
        buttons.add(stepButton);
        buttons.add(runButton);

        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        speedPanel.setOpaque(false);
        JLabel speedLabel = new JLabel("Speed (ms):");
        speedLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        speedLabel.setForeground(new Color(100, 116, 139));
        speedSlider = new JSlider(100, 1500, 700);
        speedSlider.setOpaque(false);
        speedSlider.setPreferredSize(new Dimension(140, 28));
        speedSlider.addChangeListener(e -> {
            if (animationTimer != null)
                animationTimer.setDelay(speedSlider.getValue());
        });
        speedPanel.add(speedLabel);
        speedPanel.add(speedSlider);
        buttons.add(speedPanel);

        resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        resultLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        bottom.add(buttons, BorderLayout.WEST);
        bottom.add(resultLabel, BorderLayout.EAST);
        return bottom;
    }

    private JPanel cardPanel(String titleText) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 12, 10, 12)));
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setForeground(new Color(100, 116, 139));
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(title);
        return card;
    }

    private JButton mainBtn(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(110, 34));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            final Color original = bg;

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(original.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(original);
            }
        });
        return button;
    }

    private boolean validateInput(String input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != '0' && c != '1') {
                resultLabel
                        .setText("\u2717 Invalid char '" + c + "' at position " + i + " \u2014 only 0 and 1 allowed");
                resultLabel.setForeground(new Color(239, 68, 68));
                return false;
            }
        }
        return true;
    }

    private void runAnimation() {
        String input = inputField.getText();
        if (!validateInput(input))
            return;
        buildAnimationData(input);
        clearLog();
        updateTape(-1);
        animIndex = 0;
        if (animationTimer.isRunning())
            animationTimer.stop();
        animationTimer.setDelay(speedSlider.getValue());
        animationTimer.start();
    }

    private void stepOnce() {
        String input = inputField.getText();
        if (!validateInput(input))
            return;
        if (lastInput == null || !input.equals(lastInput) || animIndex >= animPath.size()) {
            buildAnimationData(input);
            animIndex = 0;
            clearLog();
            updateTape(-1);
        }
        if (animIndex < animPath.size())
            advanceOneStep();
    }

    private void buildAnimationData(String input) {
        animPath.clear();
        animLabels.clear();
        State current = State.q0N;
        animPath.add(current);
        for (char c : input.toCharArray()) {
            State next = nextState(current, c);
            animLabels.add(c);
            animPath.add(next);
            current = next;
        }
        lastInput = input;
    }

    private void advanceOneStep() {
        if (animIndex == 0) {
            clearLog();
            updateTape(-1);
            addLogRow("\u2014", animPath.get(0).name(), "\u2014", "\u2014",
                    "Initialized | input: \"" + inputField.getText() + "\"");
            animIndex = 1;
            stateDiagramPanel.repaint();
            if (animIndex >= animPath.size())
                concludeRun();
            return;
        }

        State from = animPath.get(animIndex - 1);
        State to = animPath.get(animIndex);
        char read = animLabels.get(animIndex - 1);
        addLogRow(String.valueOf(animIndex), from.name(), String.valueOf(read), to.name(), "");
        updateTape(animIndex - 1);
        resultLabel.setText(" ");

        animIndex++;
        stateDiagramPanel.repaint();

        if (animIndex >= animPath.size())
            concludeRun();
    }

    private void concludeRun() {
        State finalState = animPath.get(animPath.size() - 1);
        boolean accepted = (finalState == State.q0Z);
        addLogRow("Final", finalState.name(), "", "", accepted ? "ACCEPTED" : "REJECTED");
        resultLabel.setText(accepted
                ? "\u2713 ACCEPTED \u2014 input satisfies the DFA"
                : "\u2717 REJECTED \u2014 input does not satisfy the DFA");
        resultLabel.setForeground(accepted ? new Color(34, 197, 94) : new Color(239, 68, 68));
        if (animationTimer != null && animationTimer.isRunning())
            animationTimer.stop();
    }

    private void resetAnimation() {
        if (animationTimer != null && animationTimer.isRunning())
            animationTimer.stop();
        animPath.clear();
        animLabels.clear();
        animIndex = 0;
        lastInput = null;
        inputField.setText("");
        resultLabel.setText(" ");
        clearLog();
        updateTape(-1);
        if (stateDiagramPanel != null)
            stateDiagramPanel.repaint();
    }

    private void clearLog() {
        if (logModel != null)
            logModel.setRowCount(0);
    }

    private void addLogRow(String step, String state, String read, String nextState, String action) {
        if (logModel != null)
            logModel.addRow(new Object[] { step, state, read, nextState, action });
    }

    private void updateTape(int headIndex) {
        tapePanel.removeAll();
        String input = inputField.getText();

        if (input.isEmpty()) {
            JLabel empty = new JLabel("(empty)");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setForeground(new Color(100, 116, 139));
            tapePanel.add(empty);
        } else {
            for (int i = 0; i < input.length(); i++) {
                boolean active = (i == headIndex);

                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                row.setOpaque(false);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel pointer = new JLabel(active ? "\u25B6" : " ", SwingConstants.CENTER);
                pointer.setFont(new Font("SansSerif", Font.BOLD, 16));
                pointer.setForeground(new Color(59, 130, 246));
                pointer.setPreferredSize(new Dimension(18, 36));

                JLabel index = new JLabel(String.valueOf(i), SwingConstants.RIGHT);
                index.setFont(new Font("Monospaced", Font.PLAIN, 11));
                index.setForeground(new Color(148, 163, 184));
                index.setPreferredSize(new Dimension(22, 36));

                JLabel cell = new JLabel(String.valueOf(input.charAt(i)), SwingConstants.CENTER);
                cell.setFont(new Font("Monospaced", Font.BOLD, 16));
                cell.setBorder(BorderFactory.createLineBorder(
                        active ? new Color(59, 130, 246) : new Color(203, 213, 225), active ? 2 : 1));
                cell.setPreferredSize(new Dimension(40, 36));
                if (active) {
                    cell.setOpaque(true);
                    cell.setBackground(new Color(219, 234, 254));
                    cell.setForeground(new Color(37, 99, 235));
                } else {
                    cell.setForeground(new Color(30, 41, 59));
                }

                row.add(pointer);
                row.add(index);
                row.add(cell);
                tapePanel.add(row);
                tapePanel.add(Box.createVerticalStrut(2));
            }
        }

        tapePanel.revalidate();
        tapePanel.repaint();
    }

    // ================= DIAGRAM =================
    //
    // Layout (3 columns x 2 rows):
    // Top row: q0N q1N q2N
    // Bottom row: q0Z q1Z q2Z (accept: q0Z is the only accepting state)
    //
    // Row Y positions are FIXED (not derived from panel height) so the diagram
    // never collapses when the window is resized.
    //
    // Edge color convention:
    // Blue = '0' transitions
    // Purple = '1' transitions
    //
    private static final int NODE_R = 28;
    private static final int Y_TOP = 100;
    private static final int Y_BOTTOM = 260; // 160px between row centers
    private static final int ARC_SAG = 150; // depth of the q2Z->q0N under-arc

    private void paintDiagram(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = stateDiagramPanel.getWidth();
        int h = stateDiagramPanel.getHeight();

        int xLeft = Math.max(110, w / 7);
        int xMid = w / 2;
        int xRight = Math.min(w - 110, Math.max(xMid + 180, w - w / 7));
        int yTop = Y_TOP;
        int yBot = Y_BOTTOM;

        // Index mapping:
        // 0=q0N (top-left), 1=q1N (top-mid), 2=q2N (top-right),
        // 3=q0Z (bot-left), 4=q1Z (bot-mid), 5=q2Z (bot-right)
        int[] xs = { xLeft, xMid, xRight, xLeft, xMid, xRight };
        int[] ys = { yTop, yTop, yTop, yBot, yBot, yBot };

        // ----- '1' transitions (purple) -----
        // Top row going right: q0N -> q1N -> q2N (straight, label above)
        drawStraight(g2, xs[0] + NODE_R, ys[0], xs[1] - NODE_R, ys[1], "1", EDGE_1, Side.ABOVE);
        drawStraight(g2, xs[1] + NODE_R, ys[1], xs[2] - NODE_R, ys[2], "1", EDGE_1, Side.ABOVE);

        // Wrap: q2N -> q0N (big arc OVER the top)
        drawCurved(g2, xs[2], ys[2] - NODE_R, xs[0], ys[0] - NODE_R, "1", EDGE_1, 0, -95);

        // Bottom -> next-top diagonals on '1' (curve to the right to avoid the down
        // arrow)
        // q0Z -> q1N
        drawCurved(g2, xs[3] + NODE_R - 2, ys[3] - NODE_R + 8, xs[1] - NODE_R + 8, ys[1] + NODE_R - 2,
                "1", EDGE_1, +35, -5);
        // q1Z -> q2N
        drawCurved(g2, xs[4] + NODE_R - 2, ys[4] - NODE_R + 8, xs[2] - NODE_R + 8, ys[2] + NODE_R - 2,
                "1", EDGE_1, +35, -5);

        // q2Z -> q0N : a long arc UNDER the bottom row, with deep sag so it clears the
        // self-loops that hang below the bottom-row nodes.
        drawArcUnder(g2, xs[5] - NODE_R - 6, ys[5] + 4, xs[0] + NODE_R - 2, ys[0] + NODE_R - 2,
                "1", EDGE_1);

        // ----- '0' transitions (blue) -----
        // Top -> bottom (paired same column): straight vertical, slightly left of
        // center
        drawStraight(g2, xs[0] - 14, ys[0] + NODE_R, xs[3] - 14, ys[3] - NODE_R, "0", EDGE_0, Side.LEFT);
        drawStraight(g2, xs[1] - 14, ys[1] + NODE_R, xs[4] - 14, ys[4] - NODE_R, "0", EDGE_0, Side.LEFT);
        drawStraight(g2, xs[2] - 14, ys[2] + NODE_R, xs[5] - 14, ys[5] - NODE_R, "0", EDGE_0, Side.LEFT);

        // Self-loops on the bottom row (hang below each Z node)
        drawSelfLoop(g2, xs[3], ys[3], "0", EDGE_0);
        drawSelfLoop(g2, xs[4], ys[4], "0", EDGE_0);
        drawSelfLoop(g2, xs[5], ys[5], "0", EDGE_0);

        // ----- Start arrow into q0N (from the left) -----
        g2.setColor(GRAY_TXT);
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawLine(xs[0] - NODE_R - 50, ys[0], xs[0] - NODE_R - 6, ys[0]);
        g2.fillPolygon(new int[] { xs[0] - NODE_R - 6, xs[0] - NODE_R - 16, xs[0] - NODE_R - 16 },
                new int[] { ys[0], ys[0] - 5, ys[0] + 5 }, 3);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString("start", xs[0] - NODE_R - 56, ys[0] - 12);

        // ----- Legend (bottom-left of the panel) -----
        int lx = 14, ly = h - 60;
        g2.setColor(EDGE_0);
        g2.fillRect(lx, ly, 18, 3);
        g2.setColor(GRAY_TXT);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("'0' transition", lx + 26, ly + 5);
        g2.setColor(EDGE_1);
        g2.fillRect(lx, ly + 16, 18, 3);
        g2.setColor(GRAY_TXT);
        g2.drawString("'1' transition", lx + 26, ly + 21);
        g2.drawString("(( )) = accept state", lx, ly + 40);

        // ----- Nodes (drawn last to cover any arrow endpoints cleanly) -----
        String[] names = { "q0N", "q1N", "q2N", "q0Z", "q1Z", "q2Z" };
        boolean[] accepting = { false, false, false, true, false, false };
        State[] order = { State.q0N, State.q1N, State.q2N, State.q0Z, State.q1Z, State.q2Z };

        State currentActive = null;
        if (!animPath.isEmpty()) {
            if (animIndex == 0)
                currentActive = animPath.get(0);
            else if (animIndex <= animPath.size())
                currentActive = animPath.get(animIndex - 1);
        }

        for (int i = 0; i < 6; i++) {
            boolean active = order[i] == currentActive;
            g2.setColor(active ? new Color(59, 130, 246) : Color.WHITE);
            g2.fillOval(xs[i] - NODE_R, ys[i] - NODE_R, 2 * NODE_R, 2 * NODE_R);
            g2.setColor(RING);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(xs[i] - NODE_R, ys[i] - NODE_R, 2 * NODE_R, 2 * NODE_R);
            if (accepting[i]) {
                int inset = 5;
                g2.drawOval(xs[i] - NODE_R + inset, ys[i] - NODE_R + inset,
                        2 * (NODE_R - inset), 2 * (NODE_R - inset));
            }
            g2.setColor(active ? Color.WHITE : GRAY_TXT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(names[i], xs[i] - fm.stringWidth(names[i]) / 2,
                    ys[i] + fm.getAscent() / 2 - 1);
        }
    }

    private enum Side {
        ABOVE, LEFT, RIGHT
    }

    private void drawStraight(Graphics2D g2, int x1, int y1, int x2, int y2,
            String label, Color color, Side side) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawLine(x1, y1, x2, y2);

        double angle = Math.atan2(y2 - y1, x2 - x1);
        int[] ax = { x2,
                (int) (x2 - 11 * Math.cos(angle - Math.toRadians(20))),
                (int) (x2 - 11 * Math.cos(angle + Math.toRadians(20))) };
        int[] ay = { y2,
                (int) (y2 - 11 * Math.sin(angle - Math.toRadians(20))),
                (int) (y2 - 11 * Math.sin(angle + Math.toRadians(20))) };
        g2.fillPolygon(ax, ay, 3);

        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
        switch (side) {
            case ABOVE:
                g2.drawString(label, mx - fm.stringWidth(label) / 2, my - 7);
                break;
            case LEFT:
                g2.drawString(label, mx - fm.stringWidth(label) - 8, my + 5);
                break;
            case RIGHT:
                g2.drawString(label, mx + 8, my + 5);
                break;
        }
    }

    private void drawCurved(Graphics2D g2, int x1, int y1, int x2, int y2,
            String label, Color color, int ctrlDx, int ctrlDy) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.8f));
        Path2D path = new Path2D.Double();
        path.moveTo(x1, y1);
        int ctrlX = (x1 + x2) / 2 + ctrlDx;
        int ctrlY = (y1 + y2) / 2 + ctrlDy;
        path.quadTo(ctrlX, ctrlY, x2, y2);
        g2.draw(path);

        double dx = x2 - ctrlX, dy = y2 - ctrlY;
        double angle = Math.atan2(dy, dx);
        int[] ax = { x2,
                (int) (x2 - 11 * Math.cos(angle - Math.toRadians(20))),
                (int) (x2 - 11 * Math.cos(angle + Math.toRadians(20))) };
        int[] ay = { y2,
                (int) (y2 - 11 * Math.sin(angle - Math.toRadians(20))),
                (int) (y2 - 11 * Math.sin(angle + Math.toRadians(20))) };
        g2.fillPolygon(ax, ay, 3);

        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int yOff = (ctrlDy <= 0) ? -4 : 14;
        g2.drawString(label, ctrlX - fm.stringWidth(label) / 2, ctrlY + yOff);
    }

    private void drawArcUnder(Graphics2D g2, int x1, int y1, int x2, int y2,
            String label, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.8f));
        int controlY = Math.max(y1, y2) + ARC_SAG;
        Path2D path = new Path2D.Double();
        path.moveTo(x1, y1);
        path.curveTo(x1 + 30, controlY, x2 - 30, controlY, x2, y2);
        g2.draw(path);

        double dx = x2 - (x2 - 30);
        double dy = y2 - controlY;
        double angle = Math.atan2(dy, dx);
        int[] ax = { x2,
                (int) (x2 - 11 * Math.cos(angle - Math.toRadians(20))),
                (int) (x2 - 11 * Math.cos(angle + Math.toRadians(20))) };
        int[] ay = { y2,
                (int) (y2 - 11 * Math.sin(angle - Math.toRadians(20))),
                (int) (y2 - 11 * Math.sin(angle + Math.toRadians(20))) };
        g2.fillPolygon(ax, ay, 3);

        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int mx = (x1 + x2) / 2;
        g2.drawString(label, mx - fm.stringWidth(label) / 2, controlY - 6);
    }

    /**
     * Draws a self-loop hanging below a node as a smooth bezier curve that
     * leaves the node at the bottom-right edge, swings down and around, and
     * returns to the bottom-left edge with the arrowhead pointing into the node.
     */
    private void drawSelfLoop(Graphics2D g2, int cx, int cy, String label, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.7f));

        // Attachment points on the bottom of the node circle (~70 deg and ~110 deg
        // measured clockwise from the 3 o'clock direction).
        double rightAng = Math.toRadians(70);
        double leftAng = Math.toRadians(110);
        int x1 = cx + (int) Math.round(NODE_R * Math.cos(rightAng));
        int y1 = cy + (int) Math.round(NODE_R * Math.sin(rightAng));
        int x2 = cx + (int) Math.round(NODE_R * Math.cos(leftAng));
        int y2 = cy + (int) Math.round(NODE_R * Math.sin(leftAng));

        // Control points: pulled outward (left/right) and downward to form the loop.
        int outX = 26;
        int outY = 30;
        int cx1 = x1 + outX, cy1 = y1 + outY;
        int cx2 = x2 - outX, cy2 = y2 + outY;

        Path2D path = new Path2D.Double();
        path.moveTo(x1, y1);
        path.curveTo(cx1, cy1, cx2, cy2, x2, y2);
        g2.draw(path);

        // Arrowhead at the end (x2, y2). Tangent direction is (x2-cx2, y2-cy2),
        // which points up-and-right back into the node.
        double arrAngle = Math.atan2(y2 - cy2, x2 - cx2);
        int[] ax = { x2,
                (int) (x2 - 11 * Math.cos(arrAngle - Math.toRadians(22))),
                (int) (x2 - 11 * Math.cos(arrAngle + Math.toRadians(22))) };
        int[] ay = { y2,
                (int) (y2 - 11 * Math.sin(arrAngle - Math.toRadians(22))),
                (int) (y2 - 11 * Math.sin(arrAngle + Math.toRadians(22))) };
        g2.fillPolygon(ax, ay, 3);

        // Label sits just below the loop apex.
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int apexY = cy + NODE_R + outY + 12;
        g2.drawString(label, cx - fm.stringWidth(label) / 2, apexY);
    }

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(DFA_Divisible::new);
    }
}
