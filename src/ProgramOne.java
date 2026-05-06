import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class ProgramOne extends JPanel implements ProgramScreen {

    private static final Color COLOR_BG         = new Color(245, 247, 251);
    private static final Color COLOR_HEADER_TOP = new Color(28,  57, 110);
    private static final Color COLOR_HEADER_BOT = new Color(42,  82, 152);
    private static final Color COLOR_ACCENT     = new Color(52, 120, 220);
    private static final Color COLOR_ACCEPT     = new Color(39, 174,  96);
    private static final Color COLOR_REJECT     = new Color(192,  57,  43);
    private static final Color COLOR_PANEL_BG   = Color.WHITE;
    private static final Color COLOR_BORDER     = new Color(210, 218, 230);
    private static final Color COLOR_TEXT_MONO  = new Color(30,  40,  60);
    private static final Color COLOR_EPS        = new Color(30,  80, 180);
    private static final Color COLOR_TERM       = new Color(30, 130,  60);

    private final JTextField          startSymbolField = new JTextField("S", 8);
    private final JTextArea           grammarArea      = new JTextArea(9, 40);
    private final JTextArea           outputArea       = new JTextArea(18, 60);
    private final StateDiagramPanel   diagramPanel     = new StateDiagramPanel();
    private final StackVisualPanel    stackVis         = new StackVisualPanel();
    private final TransitionTablePanel transitionPanel = new TransitionTablePanel();
    private final SimulationPanel     simulationPanel  = new SimulationPanel();

    // kept so JSplitPane can be given an initial location after pack()
    private JSplitPane mainSplit;

    private List<Production> currentProductions = new ArrayList<>();
    private String           currentStartSymbol = "";

   public ProgramOne() {
    try {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    } catch (Exception ignored) {}

    setLayout(new BorderLayout());
    setBackground(COLOR_BG);
    setPreferredSize(new Dimension(980, 740));

    grammarArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    grammarArea.setForeground(COLOR_TEXT_MONO);
    grammarArea.setLineWrap(false);
    grammarArea.setText("S -> a S b | ε");

    outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    outputArea.setForeground(COLOR_TEXT_MONO);
    outputArea.setEditable(false);
    outputArea.setBackground(new Color(250, 251, 255));

    add(buildHeaderPanel(), BorderLayout.NORTH);
    add(buildMainPanel(), BorderLayout.CENTER);

    SwingUtilities.invokeLater(() -> mainSplit.setDividerLocation(0.42));
}

@Override
public String getProgramName() {
    return "CFG → PDA Converter";
}

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, COLOR_HEADER_TOP, 0, getHeight(), COLOR_HEADER_BOT));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 62));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel title    = new JLabel("CFG → PDA Converter");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Context-Free Grammar to Pushdown Automaton");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitle.setForeground(new Color(180, 200, 240));

        JPanel col = new JPanel(new GridBagLayout()); col.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.anchor = GridBagConstraints.WEST; col.add(title, g);
        g.gridy = 1; col.add(subtitle, g);
        header.add(col, BorderLayout.WEST);
        return header;
    }

    // ── Main layout ───────────────────────────────────────────────────────────

    private JPanel buildMainPanel() {
        JPanel grammar = buildGrammarPanel();
        grammar.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 5, 10),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                        new EmptyBorder(8, 8, 8, 8))));
        grammar.setBackground(COLOR_PANEL_BG);

        // ── Left side: transition table ───────────────────────────────────────
        transitionPanel.setPreferredSize(new Dimension(230, 0));
        transitionPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDER));

        // ── Right side: stack ─────────────────────────────────────────────────
        stackVis.setPreferredSize(new Dimension(155, 0));

        JLabel stackTitle = new JLabel("Stack  (top → bottom)", JLabel.CENTER);
        stackTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        stackTitle.setForeground(COLOR_HEADER_TOP);
        stackTitle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(5, 0, 4, 0)));

        JPanel stackWrapper = new JPanel(new BorderLayout());
        stackWrapper.setBackground(COLOR_PANEL_BG);
        stackWrapper.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_BORDER));
        stackWrapper.add(stackTitle, BorderLayout.NORTH);
        stackWrapper.add(stackVis,   BorderLayout.CENTER);

        // ── Diagram row: transitions (left) | state machine (center) | stack (right)
        JPanel diagramRow = new JPanel(new BorderLayout());
        diagramRow.setBackground(COLOR_PANEL_BG);
        diagramRow.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));
        diagramRow.add(transitionPanel, BorderLayout.WEST);
        diagramRow.add(diagramPanel,    BorderLayout.CENTER);
        diagramRow.add(stackWrapper,    BorderLayout.EAST);

        // ── Tabs ─────────────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

        JPanel outputTab = new JPanel(new BorderLayout(0, 6));
        outputTab.setBackground(COLOR_PANEL_BG);
        outputTab.setBorder(new EmptyBorder(8, 8, 8, 8));
        outputTab.add(styledScroll(outputArea), BorderLayout.CENTER);

        tabs.addTab("  PDA Output  ", outputTab);
        tabs.addTab("  Simulation  ", simulationPanel);

        // ── Draggable split between diagram and tabs ──────────────────────────
        mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, diagramRow, tabs);
        mainSplit.setDividerSize(8);
        mainSplit.setContinuousLayout(true);
        mainSplit.setOneTouchExpandable(true);
        mainSplit.setBorder(null);
        mainSplit.setBackground(COLOR_BG);

        JPanel center = new JPanel(new BorderLayout(0, 4));
        center.setBackground(COLOR_BG);
        center.setBorder(new EmptyBorder(0, 10, 6, 10));
        center.add(mainSplit, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_BG);
        root.add(grammar, BorderLayout.NORTH);
        root.add(center,  BorderLayout.CENTER);
        return root;
    }

    // ── Grammar panel ─────────────────────────────────────────────────────────

    private JPanel buildGrammarPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 8));
        JPanel inputRow = new JPanel(new GridBagLayout()); inputRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0,0,0,8); gbc.anchor = GridBagConstraints.WEST; gbc.gridy = 0;

        JLabel sl = new JLabel("Start Symbol:"); sl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        gbc.gridx = 0; inputRow.add(sl, gbc);
        startSymbolField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        startSymbolField.setPreferredSize(new Dimension(70, 30));
        gbc.gridx = 1; inputRow.add(startSymbolField, gbc);
        JLabel gl = new JLabel("Productions (one rule per line, | for alternatives):");
        gl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        gbc.gridx = 2; inputRow.add(gl, gbc);

        JButton convertBtn = accentButton("Convert to PDA",  COLOR_ACCENT);
        JButton sampleBtn  = accentButton("Load Sample",     new Color(60, 130, 80));
        JButton clearBtn   = accentButton("Clear",           new Color(140, 60, 60));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); btnPanel.setOpaque(false);
        btnPanel.add(convertBtn); btnPanel.add(sampleBtn); btnPanel.add(clearBtn);

        JScrollPane gramScroll = styledScroll(grammarArea);
        gramScroll.setPreferredSize(new Dimension(0, 110));
        JPanel top = new JPanel(new BorderLayout(0,4)); top.setOpaque(false);
        top.add(inputRow, BorderLayout.WEST); top.add(btnPanel, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH); panel.add(gramScroll, BorderLayout.CENTER);

        convertBtn.addActionListener(e -> convertGrammar());
        sampleBtn.addActionListener(e  -> loadSample());
        clearBtn.addActionListener(e   -> clearFields());
        return panel;
    }

    // ── Shared UI helpers ─────────────────────────────────────────────────────

    private JButton accentButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color top = getModel().isPressed() ? bg.darker() : getModel().isRollover() ? bg.brighter() : bg;
                g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bg.darker()));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btn.setForeground(Color.WHITE); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(150, 32));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JScrollPane styledScroll(java.awt.Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        return s;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void convertGrammar() {
        String ss  = startSymbolField.getText().trim();
        String txt = grammarArea.getText().trim();
        if (ss.isEmpty())  { JOptionPane.showMessageDialog(this, "Please enter a start symbol.", "Missing Input", JOptionPane.WARNING_MESSAGE); return; }
        if (txt.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter at least one production.", "Missing Input", JOptionPane.WARNING_MESSAGE); return; }
        try {
            List<Production> prods = parseProductions(txt);
            outputArea.setText(buildPda(ss, prods));
            diagramPanel.setPda(ss, prods);
            Set<String> nts = ntsOf(prods);
            transitionPanel.update(ss, prods, nts, tsOf(prods, nts));
            currentProductions = prods; currentStartSymbol = ss;
            stackVis.setStack(new ArrayList<>(), "idle");
            simulationPanel.reset();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Parse Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSample() {
        startSymbolField.setText("S"); grammarArea.setText("S -> a S b | ε");
        outputArea.setText(""); diagramPanel.clear(); transitionPanel.clear();
        stackVis.setStack(new ArrayList<>(), "idle"); simulationPanel.reset();
        currentProductions = new ArrayList<>(); currentStartSymbol = "";
    }

    private void clearFields() {
        startSymbolField.setText(""); grammarArea.setText("");
        outputArea.setText(""); diagramPanel.clear(); transitionPanel.clear();
        stackVis.setStack(new ArrayList<>(), "idle"); simulationPanel.reset();
        currentProductions = new ArrayList<>(); currentStartSymbol = "";
    }

    // ── Grammar / PDA logic ───────────────────────────────────────────────────

    private List<Production> parseProductions(String text) {
        List<Production> prods = new ArrayList<>();
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim(); if (line.isEmpty()) continue;
            String norm = line.replace("→","->"); String[] sides = norm.split("->",2);
            if (sides.length != 2) throw new IllegalArgumentException("Invalid production at line "+(i+1)+". Use form: A -> alpha");
            String lhs = sides[0].trim(), rhsGroup = sides[1].trim();
            if (lhs.isEmpty()||rhsGroup.isEmpty()) throw new IllegalArgumentException("Invalid production at line "+(i+1)+".");
            for (String alt : rhsGroup.split("\\|")) prods.add(new Production(lhs, normalizeEpsilon(alt.trim())));
        }
        if (prods.isEmpty()) throw new IllegalArgumentException("No valid productions found.");
        return prods;
    }

    private String buildPda(String start, List<Production> prods) {
        Set<String> nts = ntsOf(prods); Set<String> ts = tsOf(prods, nts);
        StringBuilder b = new StringBuilder();
        b.append("CFG → PDA (Correct Construction)\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        b.append("States:           {q_start, q_loop, q_accept}\n");
        b.append("Input alphabet:   ").append(ts).append("\n");
        b.append("Stack alphabet:   terminals ∪ non-terminals ∪ {$}\n");
        b.append("Start state:      q_start\nInitial stack:    $\n");
        b.append("Accepting state:  q_accept\nType:             Non-deterministic PDA (NPDA)\n\n");
        b.append("Transitions\n━━━━━━━━━━━\n");
        b.append("(q_start) -- ε, $ → ").append(start).append(" $ --> (q_loop)\n\n");
        for (Production p : prods)
            b.append("(q_loop)  -- ε, ").append(p.lhs).append(" → ").append(reverseSymbols(p.rhs,nts)).append(" --> (q_loop)\n");
        b.append("\n");
        for (String t : ts) b.append("(q_loop)  -- ").append(t).append(", ").append(t).append(" → ε --> (q_loop)\n");
        b.append("\n(q_loop)  -- ε, $ → $ --> (q_accept)\n\n");
        String fmt = "%-12s %-10s %-14s %-26s %-12s%n";
        b.append(String.format(fmt,"State","Input","Stack Top","Push (rev.)","Next State"));
        b.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        b.append(String.format(fmt,"q_start","ε","$",start+" $","q_loop"));
        for (Production p : prods) b.append(String.format(fmt,"q_loop","ε",p.lhs,reverseSymbols(p.rhs,nts),"q_loop"));
        for (String t : ts) b.append(String.format(fmt,"q_loop",t,t,"ε","q_loop"));
        b.append(String.format(fmt,"q_loop","ε","$","$","q_accept"));
        b.append("\nNotes\n━━━━━\n• NPDA: multiple productions cause non-determinism.\n");
        b.append("• RHS pushed in REVERSE (LIFO).\n• Acceptance: input consumed AND stack = [$].\n");
        b.append("• ε-productions (A→ε) pop A with no push.\n");
        return b.toString();
    }

    String reverseSymbols(String rhs, Set<String> nts) {
        List<String> t = tokenizeRhs(rhs, nts); if (t.isEmpty()) return "ε";
        StringBuilder r = new StringBuilder();
        for (int i=t.size()-1;i>=0;i--) { r.append(t.get(i)); if(i!=0) r.append(" "); }
        return r.toString();
    }

    List<String> tokenizeRhs(String rhs, Set<String> nts) {
        List<String> s = new ArrayList<>(); if (isEpsilon(rhs)) return s;
        if (rhs.contains(" ")) { for (String p : rhs.trim().split("\\s+")) if(!p.isBlank()) s.add(p.trim()); return s; }
        List<String> sorted = new ArrayList<>(nts);
        sorted.sort((a, b) -> Integer.compare(b.length(), a.length()));
        int idx = 0;
        while (idx < rhs.length()) {
            String m = null; for (String nt : sorted) if (rhs.startsWith(nt,idx)) { m=nt; break; }
            if (m!=null) { s.add(m); idx+=m.length(); } else { s.add(String.valueOf(rhs.charAt(idx++))); }
        }
        return s;
    }

    Set<String> ntsOf(List<Production> prods)  { Set<String> s=new TreeSet<>(); for(Production p:prods) s.add(p.lhs); return s; }
    Set<String> tsOf(List<Production> prods, Set<String> nts) {
        Set<String> s=new TreeSet<>();
        for(Production p:prods) for(String sym:tokenizeRhs(p.rhs,nts)) if(!nts.contains(sym)&&!isEpsilon(sym)) s.add(sym);
        return s;
    }
    private String normalizeEpsilon(String v) {
        if(v.equalsIgnoreCase("epsilon")||v.equalsIgnoreCase("eps")||v.equalsIgnoreCase("lambda")||v.equals("e")||v.equals("λ")||v.equals("#")) return "ε";
        return v;
    }
    private boolean isEpsilon(String v) { return "ε".equals(v); }

    // ── Simulation engine ─────────────────────────────────────────────────────

    private static class SimResult {
        final List<SimStep> path; final boolean accepted;
        SimResult(List<SimStep> p, boolean a) { path=p; accepted=a; }
    }

    private SimResult simulate(String start, List<Production> prods, List<String> input) {
        Set<String> nts = ntsOf(prods);
        List<String> initStack = new ArrayList<>(); initStack.add("$");
        List<SimStep> p0 = new ArrayList<>();
        p0.add(new SimStep("q_start", new ArrayList<>(input), initStack, "Initial configuration"));
        Queue<SimConfig> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(new SimConfig("q_start", 0, initStack, p0));
        SimConfig bestPartial = queue.peek(); int bestInputPos = -1, bestPathLen = 0;

        while (!queue.isEmpty()) {
            SimConfig cur = queue.poll();
            String key = cur.state+"|"+cur.inputPos+"|"+cur.stack;
            if (!visited.add(key)) continue;
            if (cur.path.size() > 600) continue;
            if ("q_accept".equals(cur.state)) return new SimResult(cur.path, true);
            if (cur.inputPos > bestInputPos || (cur.inputPos==bestInputPos && cur.path.size()>bestPathLen)) {
                bestInputPos=cur.inputPos; bestPathLen=cur.path.size(); bestPartial=cur;
            }
            if ("q_start".equals(cur.state)) {
                if (!cur.stack.isEmpty() && "$".equals(cur.stack.get(0))) {
                    List<String> ns=new ArrayList<>(cur.stack); ns.remove(0); ns.add(0,"$"); ns.add(0,start);
                    List<SimStep> np=new ArrayList<>(cur.path);
                    np.add(new SimStep("q_loop",sub(input,cur.inputPos),new ArrayList<>(ns),"ε, $ → "+start+" $   [push start symbol]"));
                    queue.add(new SimConfig("q_loop",cur.inputPos,ns,np));
                }
            } else if ("q_loop".equals(cur.state)) {
                if (cur.stack.isEmpty()) continue;
                String top = cur.stack.get(0);
                if ("$".equals(top)) {
                    if (cur.inputPos==input.size()) {
                        List<SimStep> np=new ArrayList<>(cur.path);
                        np.add(new SimStep("q_accept",new ArrayList<>(),new ArrayList<>(cur.stack),"ε, $ → $   [input consumed → q_accept]"));
                        queue.add(new SimConfig("q_accept",cur.inputPos,new ArrayList<>(cur.stack),np));
                    }
                } else if (nts.contains(top)) {
                    for (Production p : prods) {
                        if (!p.lhs.equals(top)) continue;
                        List<String> rhs=tokenizeRhs(p.rhs,nts);
                        List<String> ns=new ArrayList<>(cur.stack); ns.remove(0);
                        for (int i=rhs.size()-1;i>=0;i--) ns.add(0,rhs.get(i));
                        List<String> rev=new ArrayList<>(rhs); Collections.reverse(rev);
                        String pushStr=rev.isEmpty()?"ε":String.join(" ",rev);
                        String rhsStr=rhs.isEmpty()?"ε":String.join(" ",rhs);
                        List<SimStep> np=new ArrayList<>(cur.path);
                        np.add(new SimStep("q_loop",sub(input,cur.inputPos),new ArrayList<>(ns),"ε, "+top+" → "+pushStr+"   [apply "+top+" → "+rhsStr+"]"));
                        queue.add(new SimConfig("q_loop",cur.inputPos,ns,np));
                    }
                } else {
                    if (cur.inputPos<input.size() && input.get(cur.inputPos).equals(top)) {
                        List<String> ns=new ArrayList<>(cur.stack); ns.remove(0); int nip=cur.inputPos+1;
                        List<SimStep> np=new ArrayList<>(cur.path);
                        np.add(new SimStep("q_loop",sub(input,nip),new ArrayList<>(ns),top+", "+top+" → ε   [consume terminal '"+top+"']"));
                        queue.add(new SimConfig("q_loop",nip,ns,np));
                    }
                }
            }
        }
        List<SimStep> path = bestPartial!=null ? new ArrayList<>(bestPartial.path) : new ArrayList<>(p0);
        SimStep last = path.get(path.size()-1);
        String reason;
        if (!last.inputRemaining.isEmpty() && !last.stack.isEmpty())
            reason = "Stuck: input '"+String.join(" ",last.inputRemaining)+"'  but stack top '"+last.stack.get(0)+"' has no matching move → REJECTED";
        else if (last.inputRemaining.isEmpty() && !last.stack.isEmpty() && !"$".equals(last.stack.get(0)))
            reason = "Input exhausted but stack contains '"+String.join(" ",last.stack)+"' → REJECTED";
        else if (!last.inputRemaining.isEmpty() && last.stack.isEmpty())
            reason = "Stack empty but input '"+String.join(" ",last.inputRemaining)+"' remains → REJECTED";
        else reason = "No accepting path found → REJECTED";
        path.add(new SimStep("q_reject",new ArrayList<>(last.inputRemaining),new ArrayList<>(last.stack),reason));
        return new SimResult(path, false);
    }

    private List<String> sub(List<String> list, int from) {
        return from>=list.size() ? new ArrayList<>() : new ArrayList<>(list.subList(from,list.size()));
    }

    // ── Data classes ──────────────────────────────────────────────────────────

    private static class SimStep {
        final String state; final List<String> inputRemaining, stack; final String transition;
        SimStep(String st,List<String> ir,List<String> sk,String tr){state=st;inputRemaining=ir;stack=sk;transition=tr;}
    }
    private static class SimConfig {
        final String state; final int inputPos; final List<String> stack; final List<SimStep> path;
        SimConfig(String s,int ip,List<String> sk,List<SimStep> p){state=s;inputPos=ip;stack=sk;path=p;}
    }
    private static class Production {
        final String lhs, rhs;
        Production(String lhs,String rhs){this.lhs=lhs;this.rhs=rhs;}
    }

    // ── Transition Table Panel ────────────────────────────────────────────────

    private class TransitionTablePanel extends JPanel {
        private final JPanel content = new JPanel();

        TransitionTablePanel() {
            setLayout(new BorderLayout());
            setBackground(COLOR_PANEL_BG);

            JLabel title = new JLabel("q_loop  Transitions", JLabel.CENTER);
            title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            title.setForeground(COLOR_HEADER_TOP);
            title.setOpaque(true);
            title.setBackground(new Color(235, 240, 252));
            title.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                    new EmptyBorder(6, 0, 6, 0)));

            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(COLOR_PANEL_BG);

            JScrollPane scroll = new JScrollPane(content);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(12);

            add(title,  BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
        }

        void update(String startSymbol, List<Production> prods, Set<String> nts, Set<String> ts) {
            content.removeAll();

            // ε-productions
            if (!prods.isEmpty()) {
                addSection("ε-productions:", COLOR_EPS, new Color(232, 238, 255));
                for (int i = 0; i < prods.size(); i++) {
                    Production p = prods.get(i);
                    String rev = reverseSymbols(p.rhs, nts);
                    String rhsDisplay = "ε".equals(rev) ? "ε" : rev;
                    addRow("ε, " + p.lhs + "  →  " + rhsDisplay, i % 2 == 0, COLOR_EPS, new Color(237, 241, 255));
                }
            }

            // Terminals
            if (!ts.isEmpty()) {
                addSection("Terminals:", COLOR_TERM, new Color(232, 248, 236));
                int i = 0;
                for (String t : ts) {
                    addRow(t + ", " + t + "  →  ε", i % 2 == 0, COLOR_TERM, new Color(236, 249, 240));
                    i++;
                }
            }

            content.revalidate();
            content.repaint();
        }

        void clear() {
            content.removeAll();
            content.revalidate();
            content.repaint();
        }

        private void addSection(String text, Color fg, Color bg) {
            JLabel l = new JLabel("  " + text);
            l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            l.setForeground(fg);
            l.setOpaque(true);
            l.setBackground(bg);
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(3, 0, 0, 0, COLOR_BORDER),
                    new EmptyBorder(4, 4, 3, 4)));
            l.setMaximumSize(new Dimension(Integer.MAX_VALUE, l.getPreferredSize().height + 8));
            content.add(l);
        }

        private void addRow(String text, boolean alt, Color fg, Color altBg) {
            JLabel l = new JLabel("  " + text);
            l.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            l.setForeground(fg);
            l.setOpaque(true);
            l.setBackground(alt ? altBg : COLOR_PANEL_BG);
            l.setBorder(new EmptyBorder(3, 8, 3, 8));
            l.setMaximumSize(new Dimension(Integer.MAX_VALUE, l.getPreferredSize().height + 4));
            content.add(l);
        }
    }

    // ── Simulation Panel ──────────────────────────────────────────────────────

    private class SimulationPanel extends JPanel {
        private final JTextField inputField   = new JTextField();
        private final JButton    simButton    = accentButton("▶  Run Simulation", COLOR_ACCENT);
        private final JButton    firstBtn     = navButton("|◀  First");
        private final JButton    prevBtn      = navButton("◀  Prev");
        private final JButton    nextBtn      = navButton("Next  ▶");
        private final JButton    lastBtn      = navButton("Last  ▶|");
        private final JLabel     stepLabel    = new JLabel("Enter a test string and click Run Simulation");
        private final JTextArea  infoArea     = new JTextArea(7, 0);
        private final JLabel     verdictLabel = new JLabel(" ");

        private List<SimStep> steps = new ArrayList<>();
        private int currentStep = -1;

        SimulationPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(COLOR_BG);
            setBorder(new EmptyBorder(12, 14, 12, 14));

            infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            infoArea.setEditable(false);
            infoArea.setBackground(new Color(248, 250, 255));
            infoArea.setForeground(COLOR_TEXT_MONO);
            infoArea.setLineWrap(false);

            inputField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
            inputField.setPreferredSize(new Dimension(320, 36));
            inputField.setToolTipText("Space-separated tokens (e.g. a a b b) or compact (e.g. aabb)");
            inputField.addActionListener(e -> runSimulation());

            verdictLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            verdictLabel.setHorizontalAlignment(JLabel.CENTER);
            stepLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            stepLabel.setForeground(new Color(100, 110, 130));
            stepLabel.setHorizontalAlignment(JLabel.CENTER);

            JPanel inputRow = new JPanel(new GridBagLayout());
            inputRow.setBackground(COLOR_PANEL_BG);
            inputRow.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER, 1, true), new EmptyBorder(12, 14, 12, 14)));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(0,0,0,10); gc.gridy = 0;
            JLabel hint = new JLabel("Test String:"); hint.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            gc.gridx = 0; inputRow.add(hint, gc);
            gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1; inputRow.add(inputField, gc);
            gc.gridx = 2; gc.fill = GridBagConstraints.NONE; gc.weightx = 0; gc.insets = new Insets(0,0,0,0);
            inputRow.add(simButton, gc);

            JLabel tip = new JLabel("  Tip: use spaces for multi-char symbols (e.g. a a b b), or compact (e.g. aabb)");
            tip.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11)); tip.setForeground(Color.GRAY);

            JPanel topArea = new JPanel(new BorderLayout(0,4)); topArea.setOpaque(false);
            topArea.add(inputRow, BorderLayout.CENTER); topArea.add(tip, BorderLayout.SOUTH);

            JPanel infoWrapper = new JPanel(new BorderLayout(0,6)); infoWrapper.setOpaque(false);
            infoWrapper.add(verdictLabel, BorderLayout.NORTH);
            infoWrapper.add(styledScroll(infoArea), BorderLayout.CENTER);

            JPanel navBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4)); navBar.setOpaque(false);
            navBar.add(firstBtn); navBar.add(prevBtn); navBar.add(stepLabel); navBar.add(nextBtn); navBar.add(lastBtn);

            add(topArea,     BorderLayout.NORTH);
            add(infoWrapper, BorderLayout.CENTER);
            add(navBar,      BorderLayout.SOUTH);

            setNavEnabled(false);
            simButton.addActionListener(e -> runSimulation());
            firstBtn.addActionListener(e  -> goToStep(0));
            prevBtn.addActionListener(e   -> goToStep(currentStep - 1));
            nextBtn.addActionListener(e   -> goToStep(currentStep + 1));
            lastBtn.addActionListener(e   -> goToStep(steps.size() - 1));
        }

        private JScrollPane styledScroll(java.awt.Component c) {
            JScrollPane s = new JScrollPane(c); s.setBorder(BorderFactory.createLineBorder(COLOR_BORDER)); return s;
        }

        void reset() {
            steps=new ArrayList<>(); currentStep=-1;
            infoArea.setText(""); stackVis.setStack(new ArrayList<>(), "idle");
            verdictLabel.setText(" ");
            stepLabel.setText("Enter a test string and click Run Simulation");
            setNavEnabled(false);
        }

        private void runSimulation() {
            if (currentStartSymbol.isEmpty() || currentProductions.isEmpty()) {
                JOptionPane.showMessageDialog(ProgramOne.this, "Please convert a grammar first.", "No Grammar", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String raw = inputField.getText().trim();
            List<String> tokens = new ArrayList<>();
            if (!raw.isEmpty()) {
                if (raw.contains(" ")) { for (String t:raw.split("\\s+")) if(!t.isBlank()) tokens.add(t.trim()); }
                else { for (char c:raw.toCharArray()) tokens.add(String.valueOf(c)); }
            }
            SimResult result = simulate(currentStartSymbol, currentProductions, tokens);
            steps = result.path; verdictLabel.setText(" ");
            goToStep(0);
        }

        private void goToStep(int idx) {
            if (steps.isEmpty()||idx<0||idx>=steps.size()) return;
            currentStep = idx;
            SimStep step = steps.get(currentStep);
            boolean accepted = "q_accept".equals(step.state);
            boolean rejected = "q_reject".equals(step.state);

            if (accepted) { verdictLabel.setText("✓  ACCEPTED"); verdictLabel.setForeground(COLOR_ACCEPT); }
            else if (rejected) { verdictLabel.setText("✗  REJECTED"); verdictLabel.setForeground(COLOR_REJECT); }
            else { verdictLabel.setText("Step  "+(currentStep+1)+"  /  "+steps.size()); verdictLabel.setForeground(COLOR_ACCENT); }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("State:       %s%n", step.state));
            sb.append(String.format("Input left:  %s%n", step.inputRemaining.isEmpty() ? "(empty — fully consumed)" : String.join(" ", step.inputRemaining)));
            sb.append(String.format("Transition:  %s%n", step.transition));
            sb.append("\nStack (top → bottom):   ").append(step.stack.isEmpty() ? "(empty)" : String.join("  │  ", step.stack));
            if (accepted) sb.append("\n\n✓  Input accepted — all input consumed and stack reduced to [$].");
            if (rejected) sb.append("\n\n✗  No accepting path was found. The string is NOT in the language of this grammar.");

            infoArea.setText(sb.toString()); infoArea.setCaretPosition(0);
            stackVis.setStack(step.stack, accepted ? "accept" : (rejected ? "reject" : "normal"));
            stepLabel.setText("Step  "+(currentStep+1)+"  /  "+steps.size());
            updateNav();
        }

        private void updateNav() {
            firstBtn.setEnabled(currentStep > 0); prevBtn.setEnabled(currentStep > 0);
            nextBtn.setEnabled(currentStep < steps.size()-1); lastBtn.setEnabled(currentStep < steps.size()-1);
        }
        private void setNavEnabled(boolean e) {
            firstBtn.setEnabled(e); prevBtn.setEnabled(e); nextBtn.setEnabled(e); lastBtn.setEnabled(e);
        }
        private JButton navButton(String text) {
            JButton b = new JButton(text); b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            b.setFocusPainted(false); b.setPreferredSize(new Dimension(110, 30)); return b;
        }
    }

    // ── Stack Visual Panel ────────────────────────────────────────────────────

    private static class StackVisualPanel extends JPanel {
        private List<String> stack = new ArrayList<>();
        private String mode = "idle";

        StackVisualPanel() {
            setBackground(COLOR_PANEL_BG);
            setBorder(new EmptyBorder(4, 6, 6, 6));
        }

        void setStack(List<String> s, String m) { stack=new ArrayList<>(s); mode=m; repaint(); }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int pw=getWidth(), ph=getHeight();
            if (stack.isEmpty()) {
                g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
                g.setColor("reject".equals(mode) ? new Color(200,100,80) : new Color(190,195,210));
                String msg = "reject".equals(mode) ? "rejected" : "idle";
                FontMetrics fm = g.getFontMetrics();
                g.drawString(msg, (pw-fm.stringWidth(msg))/2, ph/2);
                return;
            }
            int boxW=pw-12, boxH=28, gap=3, sx=6, topY=4;
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            int maxVis = Math.max(1, (ph-topY-10)/(boxH+gap));
            int count  = Math.min(stack.size(), maxVis);
            for (int i=0; i<count; i++) {
                int y=topY+i*(boxH+gap); String sym=stack.get(i);
                Color fill, border;
                if (i==0) {
                    fill="accept".equals(mode)?new Color(210,245,220):"reject".equals(mode)?new Color(255,220,215):new Color(210,228,255);
                    border="accept".equals(mode)?COLOR_ACCEPT:"reject".equals(mode)?COLOR_REJECT:COLOR_ACCENT;
                } else if ("$".equals(sym)) { fill=new Color(255,243,208); border=new Color(190,150,50); }
                else { fill=new Color(242,244,250); border=COLOR_BORDER; }
                g.setColor(fill); g.fillRoundRect(sx,y,boxW,boxH,7,7);
                g.setColor(border); g.setStroke(new BasicStroke(i==0?2f:1f)); g.drawRoundRect(sx,y,boxW,boxH,7,7);
                g.setStroke(new BasicStroke(1f));
                g.setColor(new Color(25,35,65));
                g.drawString(sym, sx+(boxW-fm.stringWidth(sym))/2, y+(boxH+fm.getAscent()-fm.getDescent())/2);
                if (i==0) {
                    g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 9)); g.setColor(new Color(120,140,180));
                    g.drawString("top", sx+3, y+9);
                    g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12)); fm=g.getFontMetrics();
                }
            }
            if (stack.size()>maxVis) {
                g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11)); g.setColor(Color.GRAY);
                String more = "·· +"+(stack.size()-maxVis)+" more";
                FontMetrics fm2 = g.getFontMetrics();
                g.drawString(more, (pw-fm2.stringWidth(more))/2, topY+count*(boxH+gap)+13);
            }
        }
    }

    // ── State Diagram Panel ───────────────────────────────────────────────────

    private static class StateDiagramPanel extends JPanel {
        private String startSymbol = "S";

        StateDiagramPanel() {
            setPreferredSize(new Dimension(680, 260));
            setBackground(COLOR_PANEL_BG);
        }

        void setPda(String ss, List<Production> prods) { startSymbol = ss; repaint(); }
        void clear() { repaint(); }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int pw = getWidth(), ph = getHeight();
            int radius = 42;
            int stateY   = (int)(ph * 0.70);
            int qStartX  = pw / 5;
            int qLoopX   = pw / 2;
            int qAcceptX = pw * 4 / 5;

            // ── Self-loop arc ─────────────────────────────────────────────────
            int arcW = 84, arcH = 58;
            int arcX = qLoopX - arcW / 2;
            int arcY = stateY - radius - arcH + 6;

            g.setColor(COLOR_ACCENT);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawArc(arcX, arcY, arcW, arcH, 22, 308);
            drawArrowHead(g, qLoopX+arcW/2-2, stateY-radius+3, qLoopX+24, stateY-radius-10, COLOR_ACCENT);

            // Label above arc: short descriptive text pointing to side table
            String arcLabel = "ε-moves & terminal matches";
            String arcSub   = "(see table →)";
            g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
            FontMetrics fmA = g.getFontMetrics();
            int lx = qLoopX - fmA.stringWidth(arcLabel) / 2;
            int ly = arcY - 18;
            // pill background
            int pw2 = Math.max(fmA.stringWidth(arcLabel), fmA.stringWidth(arcSub)) + 10;
            g.setColor(new Color(245, 247, 255, 220));
            g.fillRoundRect(qLoopX - pw2/2, ly - fmA.getAscent() - 2, pw2, fmA.getHeight()*2 + 6, 6, 6);
            g.setColor(new Color(100, 120, 190));
            g.drawString(arcLabel, qLoopX - fmA.stringWidth(arcLabel)/2, ly);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            fmA = g.getFontMetrics();
            g.setColor(new Color(60, 90, 180));
            g.drawString(arcSub, qLoopX - fmA.stringWidth(arcSub)/2, ly + fmA.getHeight() + 2);

            // ── Arrows ────────────────────────────────────────────────────────
            // Entry
            g.setColor(COLOR_HEADER_BOT); g.setStroke(new BasicStroke(2f));
            g.drawLine(qStartX-radius-34, stateY, qStartX-radius-1, stateY);
            drawArrowHead(g, qStartX-radius, stateY, qStartX-radius-34, stateY, COLOR_HEADER_BOT);
            g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10)); g.setColor(Color.GRAY);
            g.drawString("start", qStartX-radius-34, stateY-5);

            // q_start → q_loop
            drawArrow(g, qStartX+radius, stateY, qLoopX-radius, stateY, COLOR_HEADER_BOT);
            drawPillLabel(g, "ε, $ → "+startSymbol+" $", (qStartX+qLoopX)/2, stateY-12,
                    new Font(Font.MONOSPACED, Font.BOLD, 11), COLOR_HEADER_TOP);

            // q_loop → q_accept
            drawArrow(g, qLoopX+radius, stateY, qAcceptX-radius, stateY, COLOR_HEADER_BOT);
            drawPillLabel(g, "ε, $ → $", (qLoopX+qAcceptX)/2, stateY-12,
                    new Font(Font.MONOSPACED, Font.BOLD, 11), COLOR_HEADER_TOP);

            // ── States ────────────────────────────────────────────────────────
            drawState(g, qStartX,  stateY, radius, "q_start",  false);
            drawState(g, qLoopX,   stateY, radius, "q_loop",   false);
            drawState(g, qAcceptX, stateY, radius, "q_accept", true);
        }

        private void drawState(Graphics2D g, int cx, int cy, int r, String label, boolean accept) {
            g.setColor(new Color(180,190,215,80)); g.fillOval(cx-r+3,cy-r+3,r*2,r*2);
            g.setPaint(new GradientPaint(cx-r,cy-r,new Color(235,242,255),cx+r,cy+r,new Color(215,228,252)));
            g.fillOval(cx-r,cy-r,r*2,r*2);
            g.setColor(COLOR_HEADER_BOT); g.setStroke(new BasicStroke(2f)); g.drawOval(cx-r,cy-r,r*2,r*2);
            if (accept) { g.setColor(COLOR_ACCEPT); g.drawOval(cx-r+6,cy-r+6,r*2-12,r*2-12); }
            g.setColor(COLOR_TEXT_MONO); g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(label, cx-fm.stringWidth(label)/2, cy+fm.getAscent()/2-1);
        }

        private void drawPillLabel(Graphics2D g, String text, int cx, int baseY, Font font, Color color) {
            g.setFont(font); FontMetrics fm = g.getFontMetrics(); int tw = fm.stringWidth(text);
            g.setColor(new Color(245,247,255,210));
            g.fillRoundRect(cx-tw/2-4, baseY-fm.getAscent()-1, tw+8, fm.getHeight()+2, 5, 5);
            g.setColor(color); g.drawString(text, cx-tw/2, baseY);
        }

        private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2, Color c) {
            g.setColor(c); g.setStroke(new BasicStroke(2f)); g.drawLine(x1,y1,x2,y2);
            drawArrowHead(g, x2, y2, x1, y1, c);
        }

        private static void drawArrowHead(Graphics2D g, int tx, int ty, int fx, int fy, Color c) {
            double angle = Math.atan2(ty-fy,tx-fx); int len=12;
            int x1=tx-(int)(len*Math.cos(angle-Math.PI/7)), y1=ty-(int)(len*Math.sin(angle-Math.PI/7));
            int x2=tx-(int)(len*Math.cos(angle+Math.PI/7)), y2=ty-(int)(len*Math.sin(angle+Math.PI/7));
            g.setColor(c); g.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g.drawLine(tx,ty,x1,y1); g.drawLine(tx,ty,x2,y2);
        }
    }

   public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("CFG → PDA Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new ProgramOne());
        frame.setSize(980, 740);
        frame.setMinimumSize(new Dimension(980, 740));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    });
    }
}
