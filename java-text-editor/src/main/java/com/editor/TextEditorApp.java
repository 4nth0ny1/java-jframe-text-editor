
package com.editor;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class TextEditorApp {
    private File openedFile;
    private JFrame frame;
    private JTextArea textArea;
    private JFileChooser fileChooser;
    private FileHandler fileHandler;

    public TextEditorApp() {
        System.out.println("Launching editor...");
        frame = new JFrame("Java Text Editor");
        textArea = new JTextArea();
        fileChooser = new JFileChooser();
        fileHandler = new FileHandler();
        setupGUI();
    }

    private void updateTitle() {
        String title = "Java Text Editor";
        if (openedFile != null) {
            title += " - " + openedFile.getName();
        }
        frame.setTitle(title);
    }

    private void setupGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1600, 900);
        frame.setJMenuBar(createMenuBar());

        JScrollPane textScroll = new JScrollPane(textArea);

        FileSystemView fsv = fileChooser.getFileSystemView();
        File rootFile = fsv.getHomeDirectory();

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new FileNode(rootFile));
        addDummyNode(rootNode);

        JTree fileTree = new JTree(rootNode);
        fileTree.setRootVisible(true);
        fileTree.expandRow(0);

        fileTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();

                if (node.getChildCount() == 1 && node.getChildAt(0).toString().equals("Loading...")) {
                    node.removeAllChildren();
                    FileNode fileNode = (FileNode) node.getUserObject();
                    File[] files = fileNode.getFile().listFiles();

                    if (files != null) {
                        for (File file : files) {
                            FileNode childFileNode = new FileNode(file);
                            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childFileNode);
                            if (file.isDirectory()) {
                                addDummyNode(childNode);
                            }
                            node.add(childNode);
                        }
                    }
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {}
        });

        fileTree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = fileTree.getPathForLocation(e.getX(), e.getY());
                    if (path == null) return;

                    Object userObj = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                    if (!(userObj instanceof FileNode)) return;
                    FileNode fileNode = (FileNode) userObj;

                    File file = fileNode.getFile();
                    if (file.isFile()) {
                        String content = fileHandler.readFile(file);
                        textArea.setText(content);
                        openedFile = file;
                        updateTitle();
                    }
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(fileTree);
        treeScroll.setPreferredSize(new Dimension(300, 900));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, textScroll);
        splitPane.setDividerLocation(300);
        frame.add(splitPane, BorderLayout.CENTER);

        System.out.println("Reached before frame.setVisible(true)");
        frame.setVisible(true);
        System.out.println("Reached after frame.setVisible(true)");

        KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        textArea.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlS, "saveFile");
        textArea.getActionMap().put("saveFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (openedFile != null) {
                    fileHandler.writeFile(openedFile, textArea.getText());
                    updateTitle();
                } else {
                    if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                        fileHandler.writeFile(fileChooser.getSelectedFile(), textArea.getText());
                        openedFile = fileChooser.getSelectedFile();
                        updateTitle();
                    }
                }
            }
        });
    }

    private void addDummyNode(DefaultMutableTreeNode node) {
        node.add(new DefaultMutableTreeNode("Loading..."));
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open...");
        JMenuItem saveItem = new JMenuItem("Save...");
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        JMenuItem exitItem = new JMenuItem("Exit");

        newItem.addActionListener(e -> {
            textArea.setText("");
            openedFile = null;
        });

        openItem.addActionListener(e -> {
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                openedFile = fileChooser.getSelectedFile();
                String content = fileHandler.readFile(openedFile);
                textArea.setText(content);
                updateTitle();
            }
        });

        saveItem.addActionListener(e -> {
            if (openedFile != null) {
                fileHandler.writeFile(openedFile, textArea.getText());
                updateTitle();
            } else {
                JOptionPane.showMessageDialog(frame, "No file to save. Use 'Save As...'", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        saveAsItem.addActionListener(e -> {
            if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                openedFile = fileChooser.getSelectedFile();
                fileHandler.writeFile(openedFile, textArea.getText());
                updateTitle();
            }
        });

        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        return menuBar;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TextEditorApp::new);
    }

    private static class FileNode {
        private final File file;
        public FileNode(File file) {
            this.file = file;
        }
        public File getFile() {
            return file;
        }
        @Override
        public String toString() {
            String name = file.getName();
            return name.isEmpty() ? file.getAbsolutePath() : name;
        }
    }
}
