package com.editor;

import javax.swing.*;
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
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setJMenuBar(createMenuBar());
        frame.setVisible(true);

        // Ctrl + S shortcut
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
                        updateTitle();
                    }
                    openedFile = fileChooser.getSelectedFile();
                    // JOptionPane.showMessageDialog(frame, "No file to save. Use 'Save As...'", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

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
                String content = fileHandler.readFile(fileChooser.getSelectedFile());
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
                fileHandler.writeFile(fileChooser.getSelectedFile(), textArea.getText());
                updateTitle();
            }
            openedFile = fileChooser.getSelectedFile();
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
}