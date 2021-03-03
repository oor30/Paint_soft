import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;

// 新規プロジェクトを作成するクラス
public class NewProjectFrame extends JFrame implements ActionListener {
  DrawingApli02 da02;
  int mode=0;
  ImageIcon folder = new ImageIcon("./icon/folder.png");
  JButton openFile = new JButton(folder);
  JButton create = new JButton("作成");
  JButton cancel = new JButton("取消");
  JLabel name = new JLabel("プロジェクト名：");
  JLabel txt = new JLabel(".txt");
  JLabel path = new JLabel("保存場所：");
  JLabel background = new JLabel("背景");
  JLabel aspectRatio = new JLabel("比率：");
  JLabel size = new JLabel("サイズ：");
  JLabel width = new JLabel("横");
  JLabel height = new JLabel("x 縦");
  JTextField nameT = new JTextField("");
  JTextField pathT = new JTextField("");
  JTextField widthT = new JTextField("700");
  JTextField heightT = new JTextField("500");
  String[] ratios = {"指定なし", "4:3", "16:9", "1:1"};
  JComboBox combo = new JComboBox(ratios);
  File dir, file;
  boolean makeNpf;
  Dimension d;

  NewProjectFrame (DrawingApli02 da02) {
    super("新規プロジェクト");
    this.da02 = da02;
    setBounds(100, 100, 500, 300);
    setVisible(true);
    setLayout(null);
    openFile.addActionListener(this);
    create.addActionListener(this);
    cancel.addActionListener(this);
    nameT.addActionListener(this);
    combo.addActionListener(this);
    widthT.addActionListener(this);
    heightT.addActionListener(this);
    openFile.setBounds(380, 50, 20, 20);
    create.setBounds(320, 200, 60, 30);
    cancel.setBounds(390, 200, 60, 30);
    name.setBounds(50, 20, 100, 15);
    txt.setBounds(375, 20, 25, 15);
    path.setBounds(50, 50, 100, 15);
    background.setBounds(50, 100, 100, 15);
    aspectRatio.setBounds(100, 100, 50, 15);
    size.setBounds(100, 137, 50, 15);
    width.setBounds(150, 137, 20, 15);
    height.setBounds(220, 137, 30, 15);
    nameT.setBounds(150, 20, 225, 20);
    pathT.setBounds(150, 50, 230, 20);
    widthT.setBounds(170, 135, 40, 20);
    heightT.setBounds(250, 135, 40, 20);
    combo.setBounds(150, 100, 100, 20);
    name.setHorizontalAlignment(JTextField.RIGHT);
    txt.setHorizontalAlignment(JTextField.RIGHT);
    path.setHorizontalAlignment(JTextField.RIGHT);
    aspectRatio.setHorizontalAlignment(JTextField.RIGHT);
    size.setHorizontalAlignment(JTextField.RIGHT);
    pathT.setEditable(false);
    add(openFile);
    add(create);
    add(cancel);
    add(name);
    add(txt);
    add(path);
    add(background);
    add(aspectRatio);
    add(size);
    add(width);
    add(height);
    add(nameT);
    add(pathT);
    add(widthT);
    add(heightT);
    add(combo);
  }

  public Dimension getD() {
    return d;
  }

  public void actionPerformed (ActionEvent e) {
    if (e.getSource()==openFile) {
      JFileChooser fc =new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.setDialogTitle("フォルダを選択");
      int selected = fc.showDialog(this, "選択");
      if (selected == JFileChooser.APPROVE_OPTION) {
        dir = fc.getSelectedFile();
        String filePath = dir.getAbsolutePath() + "\\" + nameT.getText() + ".txt";
        pathT.setText(filePath);
      }
    }
    else if (e.getSource()==nameT) {
      String fileName = nameT.getText();
      nameT.setText(fileName);
      if (dir!=null) {
        String filePath = dir.getAbsolutePath() + "\\" + fileName + ".txt";
        pathT.setText(filePath);
      }
    }
    else if (e.getSource()==combo) {
      mode = combo.getSelectedIndex();
      if (mode==1) {
        widthT.setText("800");
        heightT.setText("600");
      }
      else if (mode==2) {
        widthT.setText("800");
        heightT.setText("450");
      }
      else if (mode==3) {
        widthT.setText("500");
        heightT.setText("500");
      }
    }
    else if (e.getSource()==widthT) {
      if (mode==1) {
        int w = Integer.parseInt(widthT.getText());
        w = w/4*3;
        heightT.setText(Integer.toString(w));
      }
      else if (mode==2) {
        int w =Integer.parseInt(widthT.getText());
        w = w/16*9;
        heightT.setText(Integer.toString(w));
      }
      else if (mode==3) {
        heightT.setText(widthT.getText());
      }
    }
    else if (e.getSource()==heightT) {
      if (mode==1) {
        int h = Integer.parseInt(heightT.getText());
        h = h/3*4;
        widthT.setText(Integer.toString(h));
      }
      else if (mode==2) {
        int h = Integer.parseInt(heightT.getText());
        h = h/9*16;
        widthT.setText(Integer.toString(h));
      }
      else {
        widthT.setText(heightT.getText());
      }
    }
    else if (e.getSource()==create) {
      if (nameT.getText().isEmpty()) {
        JOptionPane.showMessageDialog(this, "プロジェクト名を入力してください");
      }
      else if (pathT.getText().isEmpty()) {
        JOptionPane.showMessageDialog(this, "保存場所を選択してください");
      }
      else if (widthT.getText().isEmpty() || heightT.getText().isEmpty()) {
        JOptionPane.showMessageDialog(this, "サイズを入力してください");
      }
      else {
        file = new File(pathT.getText());
        da02.projectFile = file;
        da02.projectFolder = dir;
        da02.setTitle(file.getName());
        // da02.mcd = new Dimension(Integer.parseInt(widthT.getText()), Integer.parseInt(heightT.getText()));
        d = new Dimension(Integer.parseInt(widthT.getText()), Integer.parseInt(heightT.getText()));
        makeNpf = true;
        dispose();
      }
    }
    else if (e.getSource()==cancel) {
      makeNpf = false;
      dispose();
    }
  }
}
