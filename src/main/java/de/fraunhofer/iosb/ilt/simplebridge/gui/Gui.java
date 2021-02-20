package de.fraunhofer.iosb.ilt.simplebridge.gui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.fraunhofer.iosb.ilt.configurable.ConfigurationException;
import de.fraunhofer.iosb.ilt.configurable.annotations.AnnotationHelper;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorMap;
import de.fraunhofer.iosb.ilt.simplebridge.ContextListener;
import de.fraunhofer.iosb.ilt.simplebridge.LinkApi;
import de.fraunhofer.iosb.ilt.simplebridge.RestApi;
import de.fraunhofer.iosb.ilt.simplebridge.ServerConfig;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class Gui extends javax.swing.JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(Gui.class.getName());

    private EditorMap<?> configEditor;
    private final JFileChooser fileChooser = new JFileChooser(new File(".").getAbsoluteFile());

    private Server myServer;

    /**
     * Creates new form NewJFrame
     */
    public Gui() {
        initComponents();
        OutputStream os = new TextAreaOutputStream(jTextAreaOutput);
        StaticOutputStreamAppender.setStaticOutputStream(os);

        jButtonStop.setEnabled(false);
        Optional<EditorMap<?>> optionalEditor = AnnotationHelper.generateEditorFromAnnotations(ServerConfig.class, null, null);
        configEditor = optionalEditor.get();
        replaceEditor();
    }

    private void loadConfig() {
        JsonElement json = loadFromFile("Load Config");
        if (json == null) {
            return;
        }
        Optional<EditorMap<?>> optionalEditor = AnnotationHelper.generateEditorFromAnnotations(ServerConfig.class, null, null);
        configEditor = optionalEditor.get();
        configEditor.setConfig(json);
        replaceEditor();
    }

    private void saveConfig() {
        JsonElement json = configEditor.getConfig();
        saveToFile(json, "Save Importer");
    }

    private void replaceEditor() {
        java.awt.EventQueue.invokeLater(() -> {
            jPanelConfig.removeAll();
            jPanelConfig.add(configEditor.getGuiFactorySwing().getComponent());
            jPanelConfig.invalidate();
            jPanelConfig.revalidate();
        });
    }

    private JsonElement loadFromFile(String title) {
        try {
            fileChooser.setDialogTitle(title);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(null);
            if (result != JFileChooser.APPROVE_OPTION) {
                return null;
            }
            File file = fileChooser.getSelectedFile();
            String config = FileUtils.readFileToString(file, "UTF-8");
            JsonElement json = JsonParser.parseString(config);
            return json;
        } catch (IOException ex) {
            LOGGER.error("Failed to read file", ex);
            JOptionPane.showMessageDialog(this,
                    ex.getLocalizedMessage(),
                    "failed to read file",
                    JOptionPane.WARNING_MESSAGE);
        }
        return null;
    }

    private void saveToFile(JsonElement json, String title) {
        String config = elementToString(json);
        fileChooser.setDialogTitle(title);
        int result = fileChooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fileChooser.getSelectedFile();
        try {
            FileUtils.writeStringToFile(file, config, "UTF-8");
        } catch (IOException ex) {
            LOGGER.error("Failed to write file.", ex);
            JOptionPane.showMessageDialog(this,
                    ex.getLocalizedMessage(),
                    "failed to write file",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void startServer() {
        JsonElement config = configEditor.getConfig();
        ServerConfig serverConfig = new ServerConfig();
        try {
            serverConfig.configure(config, null, null, null);
            startServer(serverConfig);
        } catch (ConfigurationException ex) {
            LOGGER.error("Failed to load config", ex);
        }
    }

    private void startServer(ServerConfig config) {
        jButtonStart.setEnabled(false);
        jButtonStop.setEnabled(true);

        myServer = new Server(config.getPort());
        HandlerCollection contextHandlerCollection = new HandlerCollection(true);
        myServer.setHandler(contextHandlerCollection);
        LOGGER.info("Server starting...");
        try {
            myServer.start();
        } catch (Exception ex) {
            LOGGER.error("Exception starting server!");
            throw new IllegalStateException(ex);
        }

        ServletContextHandler handler = new ServletContextHandler();
        handler.getServletContext().setExtendedListenerTypes(true);
        String configString = elementToString(configEditor.getConfig());

        handler.setInitParameter(ServerConfig.TAG_SERVER_CONFIG, configString);
        handler.addEventListener(new ContextListener());
        handler.addServlet(LinkApi.class, "/link/*");
        handler.addServlet(RestApi.class, "/resource/*");
        contextHandlerCollection.addHandler(handler);
        try {
            handler.start();
        } catch (Exception ex) {
            LOGGER.error("Exception starting server!");
            throw new IllegalStateException(ex);
        }

        LOGGER.info("Server started.");
    }

    private String elementToString(JsonElement element) {
        return new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .create()
                .toJson(element);
    }

    private void stopServer() {
        jButtonStart.setEnabled(true);
        jButtonStop.setEnabled(false);
        try {
            myServer.stop();
        } catch (Exception ex) {
            LOGGER.error("Exception stopping server", ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaOutput = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jButtonLoad = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonStart = new javax.swing.JButton();
        jButtonStop = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanelConfig = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jSplitPane1.setDividerLocation(400);
        jSplitPane1.setResizeWeight(1.0);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jTextAreaOutput.setColumns(20);
        jTextAreaOutput.setRows(5);
        jScrollPane2.setViewportView(jTextAreaOutput);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jButtonLoad.setText("Load");
        jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jButtonLoad, gridBagConstraints);

        jButtonSave.setText("Save");
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel2.add(jButtonSave, gridBagConstraints);

        jButtonStart.setText("Start");
        jButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel2.add(jButtonStart, gridBagConstraints);

        jButtonStop.setText("Stop");
        jButtonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStopActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel2.add(jButtonStop, gridBagConstraints);

        jPanelConfig.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelConfig.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(jPanelConfig);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jScrollPane1, gridBagConstraints);

        jSplitPane1.setRightComponent(jPanel2);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadActionPerformed
        loadConfig();
    }//GEN-LAST:event_jButtonLoadActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        saveConfig();
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartActionPerformed
        startServer();
    }//GEN-LAST:event_jButtonStartActionPerformed

    private void jButtonStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStopActionPerformed
        stopServer();
    }//GEN-LAST:event_jButtonStopActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            Gui gui = new Gui();
            gui.setTitle("SimpleBridge");
            gui.setSize(800, 500);
            gui.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonStart;
    private javax.swing.JButton jButtonStop;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelConfig;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextAreaOutput;
    // End of variables declaration//GEN-END:variables
}
