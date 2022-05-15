package com.lulu.autonumbering;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {

    private final JPanel rootPanel = new JPanel(new BorderLayout());

    JTextArea outPutTextArea = new JTextArea();
    /**
     * 屏幕宽度
     */
    private final int width = 550;
    /**
     * 屏幕高度
     */
    private final int height = 300;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );

    private String curPath = "";
    /**
     * 当前备份文件
     */
    private File curBakFile;

    /**
     * 文件处理
     */
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
    private JCheckBox firstLevelTitle;
    private JCheckBox removeOldTitle;
    private final String slogan = "Welcome use markdown auto numbering tool! ";

    public MainFrame() {
        super("Markdown Title Auto Numbering");
        //禁止调整大小
        //setResizable(false);
        setSize(width, height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        configRootPanel();
        add(rootPanel);
        //最后让其展示
        setVisible(true);
    }


    public void configRootPanel() {
        rootPanel.setTransferHandler(fileHandler);
        addOutText();
        addBottomController();
        addRightSelector();
        log(slogan);
    }

    /**
     * 输出
     */
    private void addOutText() {
        //要先配置才能实现自动滚动
        outPutTextArea.setTransferHandler(fileHandler);
        outPutTextArea.setEnabled(false);
        //自动换行
        outPutTextArea.setLineWrap(true);
        outPutTextArea.setWrapStyleWord(true);
        outPutTextArea.setDisabledTextColor(Color.BLACK);
        JLabel label = new JLabel("Output: (drag md file here ^v^)");
        Panel comp = new Panel(new FlowLayout(FlowLayout.LEFT));
        comp.add(label);
        rootPanel.add(comp, BorderLayout.NORTH);

        JScrollPane jScrollPaneInfo = new JScrollPane(outPutTextArea);
        rootPanel.add(jScrollPaneInfo, BorderLayout.CENTER);
        //自动更新
        DefaultCaret caret = (DefaultCaret) outPutTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    /**
     * 底部控制器
     */
    private void addBottomController() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addConvertButton(bottomPanel);
        addRevertButton(bottomPanel);
        addClearButton(bottomPanel);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addConvertButton(JPanel bottomPanel) {
        JButton convertButton = new JButton("Convert");
        bottomPanel.add(convertButton);

        convertButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                curBakFile = AutoNumberingUtil.bakFile(MainFrame.this, curPath);
                AutoNumberingUtil.autoNumbering(new AutoNumberingUtil.Params(
                        MainFrame.this,
                        curPath,
                        firstLevelTitle.isSelected(),
                        removeOldTitle.isSelected()
                ));
            }
        });
    }

    private void addRevertButton(JPanel bottomPanel) {
        JButton revertButton = new JButton("Revert");
        bottomPanel.add(revertButton);

        revertButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AutoNumberingUtil.revertFile(MainFrame.this, curBakFile, curPath);
            }
        });
    }

    private void addClearButton(JPanel bottomPanel) {
        JButton clearButton = new JButton("Clear");
        bottomPanel.add(clearButton);
        clearButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                outPutTextArea.setText(slogan);
            }
        });
    }

    /**
     * 右边的选择
     */
    private void addRightSelector() {
        Box box = Box.createVerticalBox();
        addNeedFirstLevelTitle(box);
        addNeedRemoveTitle(box);
        addSelectFileButton(box);
        rootPanel.add(box, BorderLayout.EAST);
    }

    /**
     * 是否包含一级标题
     */
    private void addNeedFirstLevelTitle(Box box) {
        firstLevelTitle = new JCheckBox("First Level Title");
        box.add(firstLevelTitle);
    }

    /**
     * 是否包含一级标题
     */
    private void addNeedRemoveTitle(Box box) {
        removeOldTitle = new JCheckBox("Remove Old Title");
        removeOldTitle.setSelected(true);
        box.add(removeOldTitle);
    }

    private void addSelectFileButton(Box box) {
        JButton selectFile = new JButton("Select MD File");
        box.add(selectFile);
        selectFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               selectMdFile();
            }
        });
    }


    private void selectMdFile() {
        //动态获取保存文件路径
        JFileChooser fileChooser = new JFileChooser();
        FileSystemView fsv = FileSystemView.getFileSystemView();     //注意了，这里重要的一句
        //System.out.println(fsv.getHomeDirectory());                  //得到桌面路径
        //设置展示路径
        fileChooser.setCurrentDirectory(new File(
                KVStorage.get("mdPath", fsv.getHomeDirectory().getPath())
        ));
        fileChooser.setDialogTitle("Select markdown file");
        fileChooser.setApproveButtonText("Confirm");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(getDescription()) || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return ".md";
            }
        });
        int result = fileChooser.showOpenDialog(null);
        if (JFileChooser.APPROVE_OPTION == result) {
            curPath = fileChooser.getSelectedFile().getPath();
            log("Select md path: " + curPath);
            KVStorage.put("mdPath", new File(curPath).getParent());
        }
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
