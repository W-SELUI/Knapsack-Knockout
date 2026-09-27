package knapsack;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JPanel;

// Draws the live best-profit traces used by the Q4 dashboard.
public class LiveGraphPanel extends JPanel {
    private static final Color BLUE = new Color(47, 107, 255);
    private static final Color RED = new Color(224, 90, 71);
    private static final Color GRID = new Color(226, 231, 240);
    private static final Color TEXT = new Color(43, 52, 69);
    private static final Color MUTED = new Color(105, 114, 130);

    private final String problemName;
    private final int knownOptimum;
    private final boolean showDp;
    private final boolean showGa;
    private final List<GraphPoint> dpPoints = new ArrayList<>();
    private final List<GraphPoint> gaPoints = new ArrayList<>();
    private String status = "Waiting to start";
    private boolean dpFinished;
    private boolean gaFinished;

    public LiveGraphPanel(String problemName, int knownOptimum) {
        this(problemName, knownOptimum, true, true);
    }

    public LiveGraphPanel(String problemName, int knownOptimum,
            boolean showDp, boolean showGa) {
        this.problemName = problemName;
        this.knownOptimum = knownOptimum;
        this.showDp = showDp;
        this.showGa = showGa;
        setBackground(new Color(242, 245, 250));
        setPreferredSize(new Dimension(1000, 535));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public void addPoint(String algorithm, long nfc, int profit) {
        if (algorithm.equals("DP") && !showDp) return;
        if (algorithm.equals("GA") && !showGa) return;
        List<GraphPoint> points = algorithm.equals("DP") ? dpPoints : gaPoints;
        points.add(new GraphPoint(nfc, profit));
        repaint();
    }

    public void markFinished(String algorithm) {
        if (algorithm.equals("DP")) dpFinished = true;
        if (algorithm.equals("GA")) gaFinished = true;

        boolean dpDone = !showDp || dpFinished;
        boolean gaDone = !showGa || gaFinished;
        if (dpDone && gaDone) {
            status = "Finished  •  both selected algorithms have completed";
        } else if (dpFinished) {
            status = "Dynamic Programming finished  •  GA is still running";
        } else if (gaFinished) {
            status = "Genetic Algorithm finished  •  DP is still running";
        }
        repaint();
    }

    public void showError(String message) {
        status = "Error  •  " + message;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int cardX = 3;
        int cardY = 3;
        int cardWidth = Math.max(1, width - 6);
        int cardHeight = Math.max(1, height - 6);
        int left = 88;
        int right = 34;
        int top = 92;
        int bottom = 66;
        int plotWidth = Math.max(1, width - left - right);
        int plotHeight = Math.max(1, height - top - bottom);
        int xAxis = top + plotHeight;

        g.setPaint(new GradientPaint(0, cardY, Color.WHITE, 0, cardY + cardHeight,
                new Color(250, 251, 253)));
        g.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 14, 14);
        g.setColor(new Color(218, 224, 234));
        g.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 14, 14);

        g.setColor(TEXT);
        g.setFont(getFont().deriveFont(Font.BOLD, 20f));
        g.drawString("Live algorithm comparison", 26, 31);
        g.setFont(getFont().deriveFont(Font.PLAIN, 13f));
        g.setColor(MUTED);
        g.drawString(problemName + "  •  best valid profit against NFC", 27, 53);

        drawStatus(g, width - 250, 22);

        int maximumNfc = (int) Math.min(Integer.MAX_VALUE,
                Math.max(1L, Math.max(lastNfc(dpPoints), lastNfc(gaPoints))));
        int maximumProfit = Math.max(1, Math.max(knownOptimum,
                Math.max(highestProfit(dpPoints), highestProfit(gaPoints))));
        maximumProfit = paddedMaximum(maximumProfit);

        g.setColor(Color.WHITE);
        g.fillRect(left, top, plotWidth, plotHeight);
        drawGrid(g, left, top, plotWidth, plotHeight, xAxis,
                maximumNfc, maximumProfit);

        int optimumY = yFor(knownOptimum, maximumProfit, top, plotHeight);
        g.setColor(new Color(99, 108, 124));
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, new float[]{7, 5}, 0));
        g.drawLine(left, optimumY, left + plotWidth, optimumY);
        g.setFont(getFont().deriveFont(Font.BOLD, 11f));
        g.drawString("Known optimum: " + format(knownOptimum), left + 9, optimumY - 7);

        if (showDp) {
            drawSeries(g, dpPoints, BLUE, maximumNfc, maximumProfit,
                    left, xAxis, plotWidth, plotHeight, top);
        }
        if (showGa) {
            drawSeries(g, gaPoints, RED, maximumNfc, maximumProfit,
                    left, xAxis, plotWidth, plotHeight, top);
        }

        if ((showDp && dpPoints.isEmpty()) && (showGa && gaPoints.isEmpty())) {
            g.setColor(MUTED);
            g.setFont(getFont().deriveFont(Font.PLAIN, 14f));
            String empty = "Press Start comparison to watch the algorithms progress";
            int emptyX = left + (plotWidth - g.getFontMetrics().stringWidth(empty)) / 2;
            g.drawString(empty, emptyX, top + plotHeight / 2);
        }

        drawLegend(g, left + 12, top - 16);
        g.setStroke(new BasicStroke(1f));
        g.setColor(MUTED);
        g.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        g.drawString("NFC (number of core calculations)",
                left + plotWidth / 2 - 90, height - 18);
        Graphics2D rotated = (Graphics2D) g.create();
        rotated.rotate(-Math.PI / 2);
        rotated.drawString("Best valid profit", -(top + plotHeight / 2 + 38), 22);
        rotated.dispose();
        g.dispose();
    }

    private void drawGrid(Graphics2D g, int left, int top, int width,
            int height, int xAxis, int maximumNfc, int maximumProfit) {
        g.setColor(GRID);
        g.setStroke(new BasicStroke(1f));
        for (int step = 0; step <= 5; step++) {
            int y = xAxis - step * height / 5;
            g.drawLine(left, y, left + width, y);
            if (step < 5) {
                int x = left + (step + 1) * width / 5;
                g.drawLine(x, top, x, xAxis);
            }
        }

        g.setColor(new Color(72, 82, 101));
        g.drawLine(left, xAxis, left + width, xAxis);
        g.drawLine(left, top, left, xAxis);
        g.setFont(getFont().deriveFont(Font.PLAIN, 10f));
        for (int step = 0; step <= 5; step++) {
            int y = xAxis - step * height / 5;
            String label = format((long) maximumProfit * step / 5);
            g.drawString(label, left - 12 - g.getFontMetrics().stringWidth(label), y + 4);
            String xLabel = format((long) maximumNfc * step / 5);
            int x = left + step * width / 5;
            g.drawString(xLabel, x - g.getFontMetrics().stringWidth(xLabel) / 2, xAxis + 17);
        }
    }

    private void drawSeries(Graphics2D g, List<GraphPoint> points, Color color,
            int maximumNfc, int maximumProfit, int yAxis, int xAxis,
            int graphWidth, int graphHeight, int top) {
        if (points.isEmpty()) return;
        g.setColor(color);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 1; i < points.size(); i++) {
            GraphPoint previous = points.get(i - 1);
            GraphPoint current = points.get(i);
            g.drawLine(xFor(previous.nfc, maximumNfc, yAxis, graphWidth),
                    yFor(previous.profit, maximumProfit, top, graphHeight),
                    xFor(current.nfc, maximumNfc, yAxis, graphWidth),
                    yFor(current.profit, maximumProfit, top, graphHeight));
        }
        for (GraphPoint point : points) {
            int x = xFor(point.nfc, maximumNfc, yAxis, graphWidth);
            int y = yFor(point.profit, maximumProfit, top, graphHeight);
            g.fillOval(x - 3, y - 3, 6, 6);
        }
    }

    private void drawLegend(Graphics2D g, int x, int y) {
        int cursor = x;
        if (showDp) cursor = legendItem(g, cursor, y, BLUE, "DP");
        if (showGa) cursor = legendItem(g, cursor, y, RED, "GA");
        legendItem(g, cursor, y, new Color(99, 108, 124), "Known optimum");
    }

    private int legendItem(Graphics2D g, int x, int y, Color color, String label) {
        g.setColor(color);
        g.setStroke(new BasicStroke(3f));
        g.drawLine(x, y, x + 17, y);
        g.setColor(TEXT);
        g.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        g.drawString(label, x + 23, y + 4);
        return x + 37 + g.getFontMetrics().stringWidth(label);
    }

    private void drawStatus(Graphics2D g, int x, int y) {
        String text = status;
        int available = Math.max(100, getWidth() - x - 22);
        if (g.getFontMetrics().stringWidth(text) > available) text = "Working...";
        g.setColor(new Color(238, 243, 250));
        g.fillRoundRect(x, y - 14, Math.min(230, available), 25, 12, 12);
        g.setColor(MUTED);
        g.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        g.drawString(text, x + 11, y + 3);
    }

    private static int xFor(long value, long maximum, int origin, int length) {
        return origin + (int) Math.round(value * (double) length / maximum);
    }

    private static int yFor(int value, int maximum, int top, int height) {
        return top + height - (int) Math.round(value * (double) height / maximum);
    }

    private static long lastNfc(List<GraphPoint> points) {
        return points.isEmpty() ? 0 : points.get(points.size() - 1).nfc;
    }

    private static int highestProfit(List<GraphPoint> points) {
        int highest = 0;
        for (GraphPoint point : points) highest = Math.max(highest, point.profit);
        return highest;
    }

    private static int paddedMaximum(int maximum) {
        long padded = Math.max((long) maximum + 1, Math.round(maximum * 1.10));
        return padded > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) padded;
    }

    private static String format(long value) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(value);
    }

    private static final class GraphPoint {
        private final long nfc;
        private final int profit;

        private GraphPoint(long nfc, int profit) {
            this.nfc = nfc;
            this.profit = profit;
        }
    }
}

