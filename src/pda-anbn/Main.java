package org.example;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class Main extends JFrame {

    // ── colour palette ──────────────────────────────────────────────────────
    private static final Color BG          = new Color(245, 245, 248);
    private static final Color PANEL_BG    = Color.WHITE;
    private static final Color ACCENT      = new Color(59, 130, 246);   // blue
    private static final Color SUCCESS     = new Color(34, 197, 94);    // green
    private static final Color DANGER      = new Color(239, 68, 68);    // red
    private static final Color NEUTRAL     = new Color(148, 163, 184);  // slate
    private static final Color TEXT_MAIN   = new Color(30, 41, 59);
    private static final Color TEXT_MUTED  = new Color(100, 116, 139);
    private static final Color TAPE_ACTIVE = new Color(219, 234, 254);
    private static final Color TAPE_DONE   = new Color(241, 245, 249);

    // ── PDA internals ───────────-───────────────────────────────────────────
    private String   inputString = "";
    private int      head        = 0;
    private Deque<Character> stack = new ArrayDeque<>();
    private String   currentState = "q0";
    private boolean  finished     = false;
    private boolean  accepted     = false;
    private List<String[]> transitionLog = new ArrayList<>();

    // ── Swing components ────────────────────────────────────────────────────
    private JTextField inputField;
    private JLabel     stateLabel, resultLabel, stackDepthLabel;
    private JPanel     tapePanel, stackPanel, stateDiagramPanel;
    private JTable     logTable;
    private DefaultTableModel logModel;
    private JButton    stepBtn, autoBtn, resetBtn, instantBtn;
    private Timer autoTimer;
    private int        stepCount = 0;

    // ── constructor ─────────────────────────────────────────────────────────
    public Main() {
        super("PDA Simulator  —  L = { aⁿbⁿ | n ≥ 0 }");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 720);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(10, 10));

        buildUI();
        resetPDA();
        setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UI BUILD
    // ════════════════════════════════════════════════════════════════════════
    private void buildUI() {
        add(buildHeader(),      BorderLayout.NORTH);
        add(buildCenter(),      BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    // ── header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(ACCENT);
        p.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Pushdown Automaton Simulator");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("L  =  { aⁿbⁿ  |  n ≥ 0 }");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub.setForeground(new Color(219, 234, 254));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(sub);
        p.add(titles, BorderLayout.WEST);

        return p;
    }

    // ── center ───────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(10, 14, 4, 14));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(5, 5, 5, 5);

        // row 0: input + tape
        g.gridy = 0; g.weightx = 1; g.weighty = 0;
        g.gridx = 0; g.gridwidth = 2; p.add(buildInputPanel(), g);
        g.gridx = 2; g.gridwidth = 1; p.add(buildTapePanel(), g);

        // row 1: state diagram (spans 2 cols, tall) + stack
        g.gridy = 1; g.weighty = 0.45;
        g.gridx = 0; g.gridwidth = 2; p.add(buildStateDiagram(), g);
        g.gridx = 2; g.gridwidth = 1; p.add(buildStackPanel(), g);

        // row 2: transition log
        g.gridy = 2; g.weighty = 1;
        g.gridx = 0; g.gridwidth = 3; p.add(buildLogPanel(), g);

        return p;
    }

    // ── input panel ──────────────────────────────────────────────────────────
    private JPanel buildInputPanel() {
        JPanel card = card(" ");

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        inputField = new JTextField(18);
        inputField.setFont(new Font("Monospaced", Font.BOLD, 16));
        inputField.setText("aabb");
        inputField.setToolTipText("Enter a string over {a, b}");
        inputField.addActionListener(e -> resetPDA());

        JLabel lbl = new JLabel("String:");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);

        row.add(lbl);
        row.add(inputField);

        // quick example buttons
        JPanel exRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        exRow.setOpaque(false);
        String[] examples = {"ε", "ab", "aabb", "aaabbb", "aab✗", "abb✗", "ba✗", "abab✗"};
        String[] values    = {"",  "ab", "aabb", "aaabbb", "aab",  "abb",  "ba",  "abab"};
        for (int i = 0; i < examples.length; i++) {
            final String val = values[i];
            JButton b = smallBtn(examples[i]);
            b.addActionListener(e -> { inputField.setText(val); resetPDA(); });
            exRow.add(b);
        }

        card.add(row);
        card.add(Box.createVerticalStrut(4));
        card.add(exRow);

        return card;
    }

    // ── state diagram ────────────────────────────────────────────────────────
    private JPanel buildStateDiagram() {
        JPanel card = card("State Diagram  —  Accept: q0 & q3");
        stateDiagramPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                drawStateDiagram((Graphics2D) g0);
            }
        };
        stateDiagramPanel.setOpaque(false);
        stateDiagramPanel.setPreferredSize(new Dimension(500, 200));
        card.add(stateDiagramPanel);
        return card;
    }

    /**
     * Draws 4 states in a horizontal line:
     */
    private void drawStateDiagram(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w  = stateDiagramPanel.getWidth();
        int h  = stateDiagramPanel.getHeight();
        int r  = 30;
        int cy = h / 2 + 18;

        // 4 evenly spaced centres
        int pad = 50;
        int gap = (w - 2 * pad) / 3;
        int[] cx = { pad, pad + gap, pad + 2 * gap, pad + 3 * gap };

        String[]  names   = { "q0", "q1", "q2", "q3" };
        boolean[] accepts = { false, false, false, true };

        // ── arrows between states ──────────────────────────────────────────
        String[] arrowLbls = { "a,Z/AZ", "b,A/ε", "ε,Z/Z" };
        g.setStroke(new BasicStroke(1.8f));
        for (int i = 0; i < 3; i++) {
            int x1 = cx[i] + r + 2, x2 = cx[i + 1] - r - 2;
            g.setColor(NEUTRAL);
            g.drawLine(x1, cy, x2, cy);
            // arrowhead
            g.fillPolygon(new int[]{ x2, x2-10, x2-10 }, new int[]{ cy, cy-5, cy+5 }, 3);
            // label above arrow
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.setColor(TEXT_MUTED);
            int mx = (x1 + x2) / 2;
            g.drawString(arrowLbls[i], mx - g.getFontMetrics().stringWidth(arrowLbls[i]) / 2, cy - 8);
        }

        // ── self-loops ─────────────────────────────────────────────────────
        drawSelfLoop(g, cx[1], cy, r, "a,A/AA");
        drawSelfLoop(g, cx[2], cy, r, "b,A/ε");

        // ── start arrow ────────────────────────────────────────────────────
        g.setColor(NEUTRAL);
        g.setStroke(new BasicStroke(1.8f));
        g.drawLine(cx[0] - r - 28, cy, cx[0] - r - 2, cy);
        g.fillPolygon(new int[]{ cx[0]-r-2, cx[0]-r-12, cx[0]-r-12 },
                new int[]{ cy, cy-5, cy+5 }, 3);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("      S", cx[0] - r - 36, cy - 8);

        // ── High Curved Arrow from q0 to q3 (for empty string transition) ──
        g.setColor(NEUTRAL);
        g.setStroke(new BasicStroke(1.8f)); // Solid line

        int arcX = cx[0];
        int arcY = cy - 87;               // Adjusted 'y' to make it higher
        int arcW = cx[3] - cx[0];          // Width from q0 to q3
        int arcH = 100;                    // Increased height of the arc curve

// Draw the arc (the top half of an oval)
// Parameters: x, y, width, height, startAngle, arcAngle
        g.drawArc(arcX, arcY, arcW, arcH, 0, 180);

// ── Draw the Arrowhead at the end of the arc (pointing down to q3) ──
        int tipX = cx[3];
        int tipY = cy - 30;                // Points exactly to the top of the q3 circle
        int[] arrowX = { tipX, tipX - 6, tipX + 6 };
        int[] arrowY = { tipY, tipY - 12, tipY - 12 };
        g.fillPolygon(arrowX, arrowY, 3);

// ── Label for the curve ──
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(TEXT_MUTED);
        String label = "ε, Z/Z";
        int labelX = (cx[0] + cx[3]) / 2 - (g.getFontMetrics().stringWidth(label) / 2);
        int labelY = cy - 75;             // Places text slightly above the peak of the arc
        g.drawString(label, labelX, labelY);
        // ── draw the 4 state circles ────────────────────────────────────────
        for (int i = 0; i < 4; i++) {
            boolean isActive   = currentState.equals(names[i]) && !finished;
            boolean isAccepted = currentState.equals(names[i]) && finished && accepted;
            boolean isRejected = currentState.equals(names[i]) && finished && !accepted;

            // fill
            if      (isAccepted) g.setColor(SUCCESS);
            else if (isRejected) g.setColor(DANGER);
            else if (isActive)   g.setColor(ACCENT);
            else                 g.setColor(PANEL_BG);
            g.fillOval(cx[i] - r, cy - r, 2 * r, 2 * r);

            // outer border
            boolean highlight = isActive || isAccepted || isRejected;
            g.setColor(highlight ? Color.WHITE : new Color(180, 195, 215));
            g.setStroke(new BasicStroke(highlight ? 2.5f : 1.5f));
            g.drawOval(cx[i] - r, cy - r, 2 * r, 2 * r);

            // double circle for accept states (q0 and q3)
            if (accepts[i]) {
                g.drawOval(cx[i] - r + 5, cy - r + 5, 2 * (r - 5), 2 * (r - 5));
            }

            // state label
            g.setColor(highlight ? Color.WHITE : TEXT_MAIN);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(names[i], cx[i] - fm.stringWidth(names[i]) / 2, cy + fm.getAscent() / 2 - 1);
        }

        // ── bottom legend ──────────────────────────────────────────────────
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("(( )) = accept state ", 6, h - 6);
    }

    private void drawSelfLoop(Graphics2D g, int cx, int cy, int r, String lbl) {
        int lx = cx - 18, ty = cy - r - 38;
        g.setColor(NEUTRAL);
        g.setStroke(new BasicStroke(1.5f));

        // 1. Draw the arc (310 degrees starting from angle 15)
        g.drawArc(lx, ty, 36, 38, 310, 320);

        // 2. Draw the Arrowhead at the end of the loop (right side)
        // These coordinates align the triangle with the end of the arc
        int tipX = cx + 10;
        int tipY = cy - 27;

// This polygon creates a sharp triangle angled to follow the curve of the loop
        int[] ax = { tipX, tipX - 4, tipX + 6 };
        int[] ay = { tipY, tipY - 10, tipY - 6 };

        g.fillPolygon(ax, ay, 3);

        // 3. Draw the label
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(lbl, cx - g.getFontMetrics().stringWidth(lbl) / 2, ty - 3);
    }

    // ── tape panel ───────────────────────────────────────────────────────────
    private JPanel buildTapePanel() {
        JPanel card = card("Input Tape");
        tapePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        tapePanel.setOpaque(false);
        JScrollPane sp = new JScrollPane(tapePanel);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setPreferredSize(new Dimension(300, 60));
        card.add(sp);
        return card;
    }

    // ── stack panel ──────────────────────────────────────────────────────────
    private JPanel buildStackPanel() {
        JPanel card = card("Stack  (top → bottom)");
        card.setLayout(new BorderLayout());

        stackPanel = new JPanel();
        stackPanel.setLayout(new BoxLayout(stackPanel, BoxLayout.Y_AXIS));
        stackPanel.setOpaque(false);

        JScrollPane sp = new JScrollPane(stackPanel);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        card.add(sp, BorderLayout.CENTER);

        stackDepthLabel = new JLabel("Depth: 0");
        stackDepthLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        stackDepthLabel.setForeground(TEXT_MUTED);
        stackDepthLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        card.add(stackDepthLabel, BorderLayout.SOUTH);

        return card;
    }

    // ── log panel ────────────────────────────────────────────────────────────
    private JPanel buildLogPanel() {
        JPanel card = card("Transition Log");
        String[] cols = {"Step", "State", "Read", "Stack Top", "Action", "Next State"};
        logModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        logTable = new JTable(logModel);
        logTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logTable.setRowHeight(22);
        logTable.setGridColor(new Color(226, 232, 240));
        logTable.setShowGrid(true);
        logTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        logTable.getTableHeader().setBackground(new Color(241, 245, 249));
        logTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String action = (String) t.getValueAt(row, 4);
                if (!sel) {
                    if (action != null && action.contains("ACCEPT"))      c.setBackground(new Color(220, 252, 231));
                    else if (action != null && action.contains("REJECT")) c.setBackground(new Color(254, 226, 226));
                    else if (row % 2 == 0) c.setBackground(Color.WHITE);
                    else c.setBackground(new Color(248, 250, 252));
                }
                return c;
            }
        });

        int[] widths = {45, 65, 55, 80, 220, 90};
        for (int i = 0; i < widths.length; i++)
            logTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(logTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        card.add(sp);
        return card;
    }

    // ── bottom controls ──────────────────────────────────────────────────────
    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(4, 14, 12, 14));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);

        resetBtn   = mainBtn("⟳  Reset",    NEUTRAL);
        stepBtn    = mainBtn("▶  Step",     ACCENT);
        autoBtn    = mainBtn("▶▶ Auto",     new Color(99, 102, 241));
        instantBtn = mainBtn("⚡ Instant",  new Color(245, 158, 11));

        resetBtn.addActionListener(e -> resetPDA());
        stepBtn.addActionListener(e -> stepPDA());
        autoBtn.addActionListener(e -> toggleAuto());
        instantBtn.addActionListener(e -> runInstant());

        btnRow.add(resetBtn);
        btnRow.add(stepBtn);
        btnRow.add(autoBtn);
        btnRow.add(instantBtn);

        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        speedPanel.setOpaque(false);
        JLabel speedLbl = new JLabel("Speed:");
        speedLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        speedLbl.setForeground(TEXT_MUTED);
        JSlider speedSlider = new JSlider(100, 1500, 700);
        speedSlider.setOpaque(false);
        speedSlider.setPreferredSize(new Dimension(120, 28));
        speedSlider.addChangeListener(e -> {
            if (autoTimer != null) autoTimer.setDelay(speedSlider.getValue());
        });
        speedPanel.add(speedLbl);
        speedPanel.add(speedSlider);
        btnRow.add(speedPanel);

        resultLabel = new JLabel("");
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        resultLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(btnRow,      BorderLayout.WEST);
        p.add(resultLabel, BorderLayout.EAST);
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PDA LOGIC  (4-state)
    // ════════════════════════════════════════════════════════════════════════

    private void resetPDA() {
        if (autoTimer != null) { autoTimer.stop(); autoTimer = null; autoBtn.setText("▶▶ Auto"); }
        inputString  = inputField.getText().trim();
        head         = 0;
        stack        = new ArrayDeque<>();
        stack.push('Z');
        currentState = "q0";
        finished     = false;
        accepted     = false;
        stepCount    = 0;
        transitionLog.clear();
        logModel.setRowCount(0);
        resultLabel.setText("");
        stepBtn.setEnabled(true);
        autoBtn.setEnabled(true);
        instantBtn.setEnabled(true);
        renderAll();
        addLogRow("—", "q0", "—", "Z", "Initialized | input: \"" + (inputString.isEmpty() ? "ε" : inputString) + "\"", "q0");
    }

    private void stepPDA() {
        if (finished) return;
        stepCount++;
        char sym = (head < inputString.length()) ? inputString.charAt(head) : 'ε';
        char top = stack.isEmpty() ? '∅' : stack.peek();
        String from = currentState;

        switch (currentState) {
            case "q0":
                if (sym == 'ε' && top == 'Z') {
                    // empty string ε ∈ L  →  accept in q3
                    currentState = "q3"; finished = true; accepted = true;
                    addLogRow(stepCount+"", from, "ε", "Z", "(q0,ε,Z)→(q3,Z)  ACCEPT ✓", "q3");
                } else if (sym == 'a') {
                    stack.push('A'); head++;
                    currentState = "q1";
                    addLogRow(stepCount+"", from, "a", ""+top, "(q0,a,Z)→(q1,AZ) push A", "q1");
                } else {
                    finished = true; accepted = false;
                    addLogRow(stepCount+"", from, ""+sym, ""+top, "No valid transition  REJECT ✗", from);
                }
                break;

            case "q1":
                if (sym == 'a' && top == 'A') {
                    stack.push('A'); head++;
                    addLogRow(stepCount+"", from, "a", "A", "(q1,a,A)→(q1,AA) push A", "q1");
                } else if (sym == 'b' && top == 'A') {
                    stack.pop(); head++;
                    currentState = "q2";
                    addLogRow(stepCount+"", from, "b", "A", "(q1,b,A)→(q2,ε) pop A", "q2");
                } else {
                    finished = true; accepted = false;
                    addLogRow(stepCount+"", from, ""+sym, ""+top, "No valid transition  REJECT ✗", from);
                }
                break;

            case "q2":
                if (sym == 'b' && top == 'A') {
                    stack.pop(); head++;
                    addLogRow(stepCount+"", from, "b", "A", "(q2,b,A)→(q2,ε) pop A", "q2");
                } else if (sym == 'ε' && top == 'Z') {
                    currentState = "q3"; finished = true; accepted = true;
                    addLogRow(stepCount+"", from, "ε", "Z", "(q2,ε,Z)→(q3,Z)  ACCEPT ✓", "q3");
                } else {
                    finished = true; accepted = false;
                    addLogRow(stepCount+"", from, ""+sym, ""+top, "No valid transition  REJECT ✗", from);
                }
                break;
        }

        renderAll();

        if (finished) {
            if (accepted) {
                resultLabel.setText("✓  ACCEPTED  —  \"" + (inputString.isEmpty() ? "ε" : inputString) + "\" ∈ L");
                resultLabel.setForeground(SUCCESS);
            } else {
                resultLabel.setText("✗  REJECTED  —  \"" + inputString + "\" ∉ L");
                resultLabel.setForeground(DANGER);
            }
            stepBtn.setEnabled(false);
            autoBtn.setEnabled(false);
            instantBtn.setEnabled(false);
            if (autoTimer != null) { autoTimer.stop(); autoTimer = null; autoBtn.setText("▶▶ Auto"); }
        }
    }

    private void toggleAuto() {
        if (autoTimer != null) {
            autoTimer.stop(); autoTimer = null;
            autoBtn.setText("▶▶ Auto");
        } else {
            autoBtn.setText("⏸ Pause");
            autoTimer = new Timer(700, e -> {
                if (finished) {
                    autoTimer.stop(); autoTimer = null;
                    autoBtn.setText("▶▶ Auto");
                } else {
                    stepPDA();
                }
            });
            autoTimer.start();
        }
    }

    private void runInstant() {
        resetPDA();
        int guard = 0;
        while (!finished && guard++ < 1000) stepPDA();
        renderAll();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RENDER
    // ════════════════════════════════════════════════════════════════════════

    private void renderAll() {
        renderTape();
        renderStack();
        stateDiagramPanel.repaint();
    }

    private void renderTape() {
        tapePanel.removeAll();
        for (int i = 0; i <= inputString.length(); i++) {
            JLabel cell = new JLabel();
            cell.setPreferredSize(new Dimension(34, 34));
            cell.setHorizontalAlignment(SwingConstants.CENTER);
            cell.setFont(new Font("Monospaced", Font.BOLD, 15));
            cell.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
            cell.setOpaque(true);

            if (i == inputString.length()) {
                cell.setText("⊣");
                cell.setFont(new Font("SansSerif", Font.PLAIN, 12));
                if (i == head) {
                    cell.setBackground(TAPE_ACTIVE);
                    cell.setForeground(ACCENT);
                } else {
                    cell.setBackground(TAPE_DONE);
                    cell.setForeground(TEXT_MUTED);
                }
            } else {
                cell.setText("" + inputString.charAt(i));
                if (i == head && !finished) {
                    cell.setBackground(TAPE_ACTIVE);
                    cell.setForeground(ACCENT);
                } else if (i < head) {
                    cell.setBackground(TAPE_DONE);
                    cell.setForeground(TEXT_MUTED);
                } else {
                    cell.setBackground(Color.WHITE);
                    cell.setForeground(TEXT_MAIN);
                }
            }
            tapePanel.add(cell);
        }

        JLabel hi = new JLabel("↑ head");
        hi.setFont(new Font("SansSerif", Font.PLAIN, 10));
        hi.setForeground(ACCENT);
        tapePanel.add(hi);

        tapePanel.revalidate();
        tapePanel.repaint();
    }

    private void renderStack() {
        stackPanel.removeAll();
        List<Character> items = new ArrayList<>(stack);  // top first
        if (items.isEmpty()) {
            JLabel e = new JLabel("(empty)");
            e.setFont(new Font("SansSerif", Font.ITALIC, 12));
            e.setForeground(TEXT_MUTED);
            e.setAlignmentX(Component.CENTER_ALIGNMENT);
            stackPanel.add(e);
        } else {
            for (int i = 0; i < items.size(); i++) {
                char c = items.get(i);
                JLabel cell = new JLabel("" + c, SwingConstants.CENTER);
                cell.setFont(new Font("Monospaced", Font.BOLD, 14));
                cell.setOpaque(true);
                cell.setPreferredSize(new Dimension(56, 28));
                cell.setMaximumSize(new Dimension(56, 28));
                cell.setAlignmentX(Component.CENTER_ALIGNMENT);
                cell.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

                if (i == 0 && !finished) {
                    cell.setBackground(TAPE_ACTIVE);
                    cell.setForeground(ACCENT);
                } else if (c == 'Z') {
                    cell.setBackground(new Color(248, 250, 252));
                    cell.setForeground(TEXT_MUTED);
                    cell.setFont(new Font("Monospaced", Font.PLAIN, 11));
                } else {
                    cell.setBackground(Color.WHITE);
                    cell.setForeground(TEXT_MAIN);
                }
                stackPanel.add(cell);
                stackPanel.add(Box.createVerticalStrut(2));
            }
        }
        stackDepthLabel.setText("Depth: " + (items.size() > 0 ? items.size() - 1 : 0) + " A's  +  Z");
        stackPanel.revalidate();
        stackPanel.repaint();
    }

    private void addLogRow(String step, String state, String sym, String top, String action, String next) {
        logModel.addRow(new Object[]{ step, state, sym, top, action, next });
        int last = logTable.getRowCount() - 1;
        logTable.scrollRectToVisible(logTable.getCellRect(last, 0, true));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private JPanel card(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        p.add(lbl);
        return p;
    }

    private JButton mainBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(110, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            Color orig = bg;
            public void mouseEntered(MouseEvent e) { b.setBackground(orig.darker()); }
            public void mouseExited(MouseEvent e)  { b.setBackground(orig); }
        });
        return b;
    }

    private JButton smallBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Monospaced", Font.PLAIN, 11));
        b.setBackground(new Color(241, 245, 249));
        b.setForeground(TEXT_MAIN);
        b.setFocusPainted(false);
        b.setBorderPainted(true);
        b.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MAIN
    // ════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(Main::new);
    }
}