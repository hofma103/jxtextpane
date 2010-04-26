package javax.swing;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;

public class LineNumbersTextPane extends JXTextPane {

    public boolean displayLineNumbers = true;
    JSplitPane jSplitPane1;
    JScrollPane jScrollPane1;
    LineNumbersSidePane linenumbers;

    /**
     * Sets the currently installed kit for handling
     * content.  This is the bound property that
     * establishes the content type of the editor.
     *
     * @param kit the desired editor behavior
     * @exception IllegalArgumentException if kit is not a
     *		<code>WrapEditorKit</code>
     */
    @Override
    public final void setEditorKit(EditorKit kit) {
        if (kit instanceof LineWrapEditorKit) {
            super.setEditorKit(kit);
            ((LineWrapEditorKit) kit).setWrap(false);
        } else {
            throw new IllegalArgumentException("Must be WrapEditorKit");
        }
    }

    @Override
    protected EditorKit createDefaultEditorKit() {
        LineWrapEditorKit kit = new LineWrapEditorKit();
        kit.setWrap(false);
        return kit;
    }

    public LineNumbersTextPane() {
        jScrollPane1 = new JScrollPane(this);

        jScrollPane1.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            public void adjustmentValueChanged(AdjustmentEvent e) {
                updateLineNumberView();
            }
        });

        jSplitPane1 = new JSplitPane();

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerSize(0);
        jSplitPane1.setRightComponent(jScrollPane1);

        linenumbers = new LineNumbersSidePane(this);
        jSplitPane1.setLeftComponent(linenumbers);

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                updateLineNumberDivider();
            }
        });

        //System.err.println("  Init done.");
    }

    @Override
    public void setCaretPosition(int position) {
        super.setCaretPosition(position);
        updateLineNumberDivider();
    }

    @Override
    public void setText(String t) {
        super.setText(t);
        updateLineNumberDivider();
    }

    public void updateLineNumberDivider() {
        if (!displayLineNumbers) {
            return;
        }
        //System.err.println("updateLineNumberDivider");
        jSplitPane1.setDividerLocation((int) (linenumbers._editor.getFontMetrics(linenumbers._editor.getFont()).getWidths()[0] * (Math.log10(((LineWrapEditorKit) getEditorKit()).number_of_lines) + 2)));
    }

    public void updateLineNumberView() {
        if (!displayLineNumbers) {
            return;
        }
        linenumbers.repaint();
        //System.err.println("updateLineNumberView");
    }

    public Container getContainerWithLines() {
        return jSplitPane1;
    }

    class LineNumbersSidePane extends JPanel {

        JTextComponent _editor;

        public LineNumbersSidePane(JXTextPane editor) {
            _editor = editor;
        }

        @Override
        public void paint(Graphics g) {
            //System.err.println("JLineNumberPane.paint");
            super.paint(g);

            if (!displayLineNumbers) {
                return;
            }
            // We need to properly convert the points to match the viewport
// Read docs for viewport
            int start = _editor.viewToModel(jScrollPane1.getViewport().getViewPosition()); //        starting pos  in document
            int end = _editor.viewToModel(new Point(jScrollPane1.getViewport().getViewPosition().x + _editor.getWidth(),
                    jScrollPane1.getViewport().getViewPosition().y + _editor.getHeight()));
// end pos in doc

// translate offsets to lines
            Document doc = _editor.getDocument();
            int startline = doc.getDefaultRootElement().getElementIndex(start) + 1;
            int endline = doc.getDefaultRootElement().getElementIndex(end) + 1;

            g.setFont(_editor.getFont());

            int fontHeight = g.getFontMetrics(_editor.getFont()).getHeight();
            int fontDesc = g.getFontMetrics(_editor.getFont()).getDescent();
            int starting_y = -1;

            try {
                starting_y = _editor.modelToView(start).y - jScrollPane1.getViewport().getViewPosition().y + fontHeight - fontDesc;
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            } catch (NullPointerException e) {
            }

            for (int line = startline, y = starting_y; line <= endline; y += fontHeight, line++) {
                g.drawString(Integer.toString(line), 0, y);
            }

        }
    }
}
