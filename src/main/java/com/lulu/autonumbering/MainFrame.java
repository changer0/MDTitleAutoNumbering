package com.lulu.autonumbering;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {

    private final JPanel panel = new JPanel();

    JTextArea outPutTextArea = new JTextArea();

    /**
     * 屏幕宽度
     */
    private int width = 500;
    /**
     * 屏幕高度
     */
    private int height = 250;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );

    private String curPath = "";
    /**
     * 当前备份文件
     */
    private File curBakFile;

    public MainFrame() {
        super("Markdown Title Auto Numbering");
        //禁止调整大小
        setResizable(false);
        setSize(width, height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        add(panel);
        showPanel();
    }

    public void showPanel() {
        addConvertButton();
        addRevertButton();
        addOutText();
        TransferHandler fileHandler = new TransferHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean importData(JComponent comp, Transferable t) {
                try {
                    Object o = t.getTransferData(DataFlavor.javaFileListFlavor);

                    String filepath = o.toString();
                    if (filepath.startsWith("[")) {
                        filepath = filepath.substring(1);
                    }
                    if (filepath.endsWith("]")) {
                        filepath = filepath.substring(0, filepath.length() - 1);
                    }
                    curPath = filepath;
                    log("drag file" + filepath);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean canImport(JComponent comp, DataFlavor[] flavors) {
                for (DataFlavor flavor : flavors) {
                    if (DataFlavor.javaFileListFlavor.equals(flavor)) {
                        return true;
                    }
                }
                return false;
            }
        };

        panel.setTransferHandler(fileHandler);
        outPutTextArea.setTransferHandler(fileHandler);
    }

    private void addOutText() {
        outPutTextArea.setEnabled(false);
        //自动换行
        outPutTextArea.setLineWrap(true);
        outPutTextArea.setWrapStyleWord(true);
        JLabel comp = new JLabel("Output: (drag md file here ^v^)");
        comp.setBounds(10, 0, width, 40);
        panel.add(comp);

        JScrollPane jScrollPaneInfo = new JScrollPane(outPutTextArea);
        jScrollPaneInfo.setBounds(0, 40, width, 100);
        panel.add(jScrollPaneInfo);
        //自动更新
        DefaultCaret caret = (DefaultCaret) outPutTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    private void addConvertButton() {
        JButton loginButton = new JButton("Convert");
        loginButton.setBounds(10, 160, 80, 25);
        panel.add(loginButton);

        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                curBakFile = AutoNumberingUtil.bakFile(MainFrame.this, curPath);
                AutoNumberingUtil.autoNumbering(MainFrame.this, curPath);
            }
        });
    }

    private void addRevertButton() {
        JButton revertButton = new JButton("Revert");
        revertButton.setBounds(100, 160, 80, 25);
        panel.add(revertButton);

        revertButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AutoNumberingUtil.revertFile(MainFrame.this, curBakFile, curPath);
            }
        });
    }


    public void log(String msg) {
        msg = msg.trim();
        if (msg.isEmpty()) {
            return;
        }
        Date date = new Date(System.currentTimeMillis());
        outPutTextArea.append(sdf.format(date) + " " + msg + "\n");
        System.out.println(msg);
    }


    public static void main(String[] args) {
        new MainFrame();
    }


}
