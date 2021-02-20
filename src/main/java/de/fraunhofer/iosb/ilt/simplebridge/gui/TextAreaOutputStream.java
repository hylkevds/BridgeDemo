package de.fraunhofer.iosb.ilt.simplebridge.gui;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author hylke
 */
public class TextAreaOutputStream extends OutputStream {

    private final JTextArea textArea;

    public TextAreaOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        SwingUtilities.invokeLater(() -> {
            textArea.append(String.valueOf((char) b));
        });

    }

}
