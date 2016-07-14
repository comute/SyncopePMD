/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.netbeans.plugin.view;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.apache.commons.io.IOUtils;
import org.apache.syncope.common.lib.to.MailTemplateTO;
import org.apache.syncope.common.lib.to.ReportTemplateTO;
import org.apache.syncope.common.lib.types.MailTemplateFormat;
import org.apache.syncope.common.lib.types.ReportTemplateFormat;
import org.apache.syncope.netbeans.plugin.connector.ResourceConnector;
import org.apache.syncope.netbeans.plugin.constants.PluginConstants;
import org.apache.syncope.netbeans.plugin.service.MailTemplateManagerService;
import org.apache.syncope.netbeans.plugin.service.ReportTemplateManagerService;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.apache.syncope.netbeans.plugin//ResourceExplorer//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ResourceExplorerTopComponent",
        iconBase = "images/syncope.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "org.apache.syncope.netbeans.plugin.ResourceExplorerTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "Apache Syncope",
        preferredID = "ResourceExplorerTopComponent"
)

public final class ResourceExplorerTopComponent extends TopComponent {

    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;
    private DefaultMutableTreeNode mailTemplates;
    private DefaultMutableTreeNode reportXslts;
    private MailTemplateManagerService mailTemplateManagerService;
    private ReportTemplateManagerService reportTemplateManagerService;
    private Charset encodingPattern;

    public ResourceExplorerTopComponent() {

        initComponents();
        setName(PluginConstants.DISPLAY_NAME);
        setToolTipText(PluginConstants.TOOL_TIP_TEXT);

        treeModel = (DefaultTreeModel) resourceExplorerTree.getModel();
        root = (DefaultMutableTreeNode) treeModel.getRoot();
        DefaultMutableTreeNode visibleRoot
                = new DefaultMutableTreeNode(PluginConstants.DISPLAY_NAME);
        mailTemplates = new DefaultMutableTreeNode(PluginConstants.MAIL_TEMPLTAE_CONSTANT);
        reportXslts = new DefaultMutableTreeNode(PluginConstants.REPORT_XSLTS_CONSTANT);
        root.add(visibleRoot);
        visibleRoot.add(mailTemplates);
        visibleRoot.add(reportXslts);
        treeModel.reload();

    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        resourceExplorerTree = new javax.swing.JTree();

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        resourceExplorerTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        resourceExplorerTree.setRootVisible(false);
        resourceExplorerTree.setScrollsOnExpand(true);
        resourceExplorerTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(final java.awt.event.MouseEvent evt) {
                resourceExplorerTreeMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(resourceExplorerTree);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
        );
    }
    // </editor-fold>//GEN-END:initComponents

    private void resourceExplorerTreeMouseClicked(final java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) resourceExplorerTree.
                    getLastSelectedPathComponent();
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
            if (selectedNode.isLeaf()) {
                String name = (String) selectedNode.getUserObject();
                if (parentNode.getUserObject().equals(PluginConstants.MAIL_TEMPLTAE_CONSTANT)) {
                    try {
                        openMailEditor(name);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    try {
                        openReportEditor(name);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        } else if (evt.getButton() == MouseEvent.BUTTON3 && evt.getClickCount() == 1) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) resourceExplorerTree.
                    getLastSelectedPathComponent();
            String selectedNodeName = (String) selectedNode.getUserObject();
            if (selectedNode.isLeaf()) {
                leafRightClickAction(evt, selectedNode);
            } else if (selectedNodeName.equals(PluginConstants.MAIL_TEMPLTAE_CONSTANT)) {
                folderRightClickAction(evt, mailTemplates);
            } else if (selectedNodeName.equals(PluginConstants.REPORT_XSLTS_CONSTANT)) {
                folderRightClickAction(evt, reportXslts);
            } else if (selectedNodeName.equals(PluginConstants.DISPLAY_NAME)) {
                rootRightClickAction(evt);
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree resourceExplorerTree;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        File file = new File("UserData.txt");
        if (!file.exists()) {
            new ServerDetailsView(null, true).setVisible(true);
        }
        try {
            mailTemplateManagerService = ResourceConnector.getMailTemplateManagerService();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error Occured.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            new ServerDetailsView(null, true).setVisible(true);
        }
        try {
            reportTemplateManagerService
                    = ResourceConnector.getReportTemplateManagerService();
        } catch (IOException ex) {
            new ServerDetailsView(null, true).setVisible(true);
        }

        Runnable tsk = new Runnable() {
            @Override
            public void run() {
                final ProgressHandle progr = ProgressHandleFactory.createHandle("Loading Templates", new Cancellable() {
                    @Override
                    public boolean cancel() {
                        return true;
                    }
                }, new Action() {
                    @Override
                    public Object getValue(String key) {
                        return null;
                    }

                    @Override
                    public void putValue(String key, Object value) {
                    }

                    @Override
                    public void setEnabled(boolean b) {
                    }

                    @Override
                    public boolean isEnabled() {
                        return false;
                    }

                    @Override
                    public void addPropertyChangeListener(PropertyChangeListener listener) {
                    }

                    @Override
                    public void removePropertyChangeListener(PropertyChangeListener listener) {
                    }

                    @Override
                    public void actionPerformed(ActionEvent e) {
                    }
                });

                progr.start();
                progr.progress("Loading Templates.");
                addMailTemplates();
                addReportXslts();
                progr.finish();
            }

        };
        RequestProcessor.getDefault().post(tsk);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(final java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(final java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private void addMailTemplates() {
        List<MailTemplateTO> mailTemplateList = mailTemplateManagerService.list();
        for (MailTemplateTO mailTemplate : mailTemplateList) {
            this.mailTemplates.add(new DefaultMutableTreeNode(
                    mailTemplate.getKey()));
        }
        treeModel.reload();
    }

    private void addReportXslts() {
        List<ReportTemplateTO> reportTemplates = reportTemplateManagerService.list();
        for (ReportTemplateTO reportTemplate : reportTemplates) {
            reportXslts.add(new DefaultMutableTreeNode(
                    reportTemplate.getKey()));
        }
        treeModel.reload();
    }

    private void rootRightClickAction(final MouseEvent evt) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem resetConnectionItem = new JMenuItem("Reset Connection");
        menu.add(saveItem);
        menu.add(resetConnectionItem);

        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                saveContent();
            }
        });

        resetConnectionItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                File file = new File("UserData.txt");
                try {
                    BufferedReader bf = new BufferedReader(new FileReader(file));
                    String host = bf.readLine();
                    String userName = bf.readLine();
                    String password = bf.readLine();
                    ServerDetailsView serverDetails = new ServerDetailsView(null, true);
                    serverDetails.setDetails(host, userName, password);
                    serverDetails.setVisible(true);
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        menu.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    private void folderRightClickAction(final MouseEvent evt,
            final DefaultMutableTreeNode node) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("New");
        menu.add(addItem);

        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                String name = JOptionPane.showInputDialog("Enter Name");
                boolean added = false;
                if (node.getUserObject().equals(
                        PluginConstants.MAIL_TEMPLTAE_CONSTANT)) {
                    MailTemplateTO mailTemplate = new MailTemplateTO();
                    mailTemplate.setKey(name);
                    added = mailTemplateManagerService.create(mailTemplate);
                    mailTemplateManagerService.setFormat(name,
                            MailTemplateFormat.HTML,
                            IOUtils.toInputStream("//Enter Content here", encodingPattern));
                    mailTemplateManagerService.setFormat(name,
                            MailTemplateFormat.TEXT,
                            IOUtils.toInputStream("//Enter Content here", encodingPattern));
                    try {
                        openMailEditor(name);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    ReportTemplateTO reportTemplate = new ReportTemplateTO();
                    reportTemplate.setKey(name);
                    added = reportTemplateManagerService.create(reportTemplate);
                    reportTemplateManagerService.setFormat(name,
                            ReportTemplateFormat.FO,
                            IOUtils.toInputStream("//Enter content here", encodingPattern));
                    reportTemplateManagerService.setFormat(name,
                            ReportTemplateFormat.CSV,
                            IOUtils.toInputStream("//Enter content here", encodingPattern));
                    reportTemplateManagerService.setFormat(name,
                            ReportTemplateFormat.HTML,
                            IOUtils.toInputStream("//Enter content here", encodingPattern));
                    try {
                        openReportEditor(name);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                if (added) {
                    node.add(new DefaultMutableTreeNode(name));
                    treeModel.reload(node);
                } else {
                    JOptionPane.showMessageDialog(null, "Error while creating "
                            + "new element", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        menu.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    private void leafRightClickAction(final MouseEvent evt,
            final DefaultMutableTreeNode node) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        menu.add(deleteItem);

        deleteItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(null,
                        "Do you want to delete ?");
                if (result == JOptionPane.OK_OPTION) {
                    DefaultMutableTreeNode parent
                            = (DefaultMutableTreeNode) node.getParent();
                    String name = (String) node.getUserObject();
                    boolean deleted;
                    if (parent.getUserObject().equals(
                            PluginConstants.MAIL_TEMPLTAE_CONSTANT)) {
                        deleted = mailTemplateManagerService.delete(
                                (String) node.getUserObject());
                    } else {
                        deleted = reportTemplateManagerService.delete(
                                (String) node.getUserObject());
                    }
                    if (deleted) {
                        node.removeFromParent();
                        treeModel.reload(parent);
                    } else {

                        JOptionPane.showMessageDialog(null,
                                "Error while deleting new element", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        menu.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    private void openMailEditor(final String name) throws IOException {
        String type;
        String content;
        InputStream is;
//        Object format = JOptionPane.showInputDialog(null, "Select File Format",
//                "File format", JOptionPane.QUESTION_MESSAGE, null,
//                PluginConstants.MAIL_TEMPLATE_FORMATS, "TEXT");

        //if (format.equals("HTML")) {
        type = "html";
        is = (InputStream) mailTemplateManagerService.getFormat(name,
                MailTemplateFormat.HTML);
        content = IOUtils.toString(is, encodingPattern);
        //} else {
//            type = "txt";
//            is = (InputStream) mailTemplateManagerService.getFormat(name,
//                    MailTemplateFormat.TEXT);
//            content = IOUtils.toString(is, encodingPattern);
        //}
//
//        File directory = new File("Template/Mail");
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//        File file = new File("Template/Mail/" + name + "." + type);
//        FileWriter fw = new FileWriter(file);
//        fw.write(content);
//        fw.flush();
//        FileObject fob = FileUtil.toFileObject(file.getAbsoluteFile());
//        fob.setAttribute("description", "TEXTTTT");
//        DataObject data = DataObject.find(fob);
//        OpenCookie cookie = (OpenCookie) data.getCookie(OpenCookie.class);
//        cookie.open();

        MultiViewDescription[] descriptionArray = new MultiViewDescription[3];
        descriptionArray[0] = new MultiViewDescription() {
            @Override
            public int getPersistenceType() {
                return 0;
            }

            @Override
            public String getDisplayName() {
                return "Test 1";
            }

            @Override
            public Image getIcon() {
                return null;
            }

            @Override
            public HelpCtx getHelpCtx() {
                return null;
            }

            @Override
            public String preferredID() {
                return "A";
            }

            @Override
            public MultiViewElement createElement() {
                return new MultiViewEditorElement(Lookup.EMPTY);

            }
        };

        descriptionArray[1] = new MultiViewDescription() {
            @Override
            public int getPersistenceType() {
                return 0;
            }

            @Override
            public String getDisplayName() {
                return "Test 2";
            }

            @Override
            public Image getIcon() {
                return null;
            }

            @Override
            public HelpCtx getHelpCtx() {
                return null;
            }

            @Override
            public String preferredID() {
                return "A";
            }

            @Override
            public MultiViewElement createElement() {
                return new MultiViewEditorElement(Lookup.EMPTY);

            }
        };

        descriptionArray[2] = new MultiViewDescription() {
            @Override
            public int getPersistenceType() {
                return 0;
            }

            @Override
            public String getDisplayName() {
                return "Test 3";
            }

            @Override
            public Image getIcon() {
                return null;
            }

            @Override
            public HelpCtx getHelpCtx() {
                return null;
            }

            @Override
            public String preferredID() {
                return "A";
            }

            @Override
            public MultiViewElement createElement() {
                return new MultiViewEditorElement(Lookup.EMPTY);

            }
        };

        TopComponent ctc = MultiViewFactory.createMultiView(descriptionArray, descriptionArray[0]);
        ctc.open();
        ctc.requestActive();
    }

    private void openReportEditor(final String name) throws IOException {
        String type;
        String content;
        InputStream is;
        Object format = JOptionPane.showInputDialog(null, "Select File Format",
                "File format", JOptionPane.QUESTION_MESSAGE, null,
                PluginConstants.REPORT_TEMPLATE_FORMATS, "TEXT");

        if (format.equals("HTML")) {
            type = "html";
            is = (InputStream) reportTemplateManagerService.getFormat(name,
                    ReportTemplateFormat.HTML);
            content = IOUtils.toString(is, encodingPattern);
        } else if (format.equals("CSV")) {
            type = "csv";
            is = (InputStream) reportTemplateManagerService.getFormat(name,
                    ReportTemplateFormat.CSV);
            content = IOUtils.toString(is, encodingPattern);
        } else {
            type = "fo";
            is = (InputStream) reportTemplateManagerService.getFormat(name,
                    ReportTemplateFormat.FO);
            content = IOUtils.toString(is, encodingPattern);
        }

        File directory = new File("Template/Report");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File("Template/Report/" + name + "." + type);
        FileWriter fw = new FileWriter(file);
        fw.write(content);
        fw.flush();
        FileObject fob = FileUtil.toFileObject(file.getAbsoluteFile());
        DataObject data = DataObject.find(fob);
        OpenCookie cookie = (OpenCookie) data.getCookie(OpenCookie.class);
        cookie.open();
    }

    private void saveContent() {

        try {
            JTextComponent ed = EditorRegistry.lastFocusedComponent();
            Document document = ed.getDocument();
            String content = document.getText(0, document.getLength());
            String path = (String) document.getProperty(Document.TitleProperty);
            String[] temp = path.split(File.separator);
            String name = temp[temp.length - 1];
            String templateType = temp[temp.length - 2];
            temp = name.split("\\.");
            String format = temp[1];
            String key = temp[0];

            if (templateType.equals("Mail")) {
                if (format.equals("txt")) {
                    mailTemplateManagerService.setFormat(key,
                            MailTemplateFormat.TEXT,
                            IOUtils.toInputStream(content, encodingPattern));
                } else {
                    mailTemplateManagerService.setFormat(key,
                            MailTemplateFormat.HTML,
                            IOUtils.toInputStream(content, encodingPattern));
                }
            } else if (format.equals("html")) {
                reportTemplateManagerService.setFormat(key,
                        ReportTemplateFormat.HTML,
                        IOUtils.toInputStream(content, encodingPattern));
            } else if (format.equals("fo")) {
                reportTemplateManagerService.setFormat(key,
                        ReportTemplateFormat.FO,
                        IOUtils.toInputStream(content, encodingPattern));
            } else {
                reportTemplateManagerService.setFormat(key,
                        ReportTemplateFormat.CSV,
                        IOUtils.toInputStream(content, encodingPattern));
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
