import java.io.*;
import javax.swing.*;

// メインクラス
// 実際のアプリの動作はDrawingApli02が行う
public class Main {
  // フィールド変数
  DrawingApli02 da02;
  File projectFolder, appData;

  // mainメソッド
  public static void main(String[] args) {
    new Main();
  }

  // コンストラクタ
  Main () {
    da02 = new DrawingApli02();
    String home = System.getProperty("user.home");
    System.out.println(home);
    File dir = new File(home);
    File[] list = dir.listFiles();
    OUTSIDE: if (list != null) {
      for (int i=0; i<list.length; i++) {
        if (list[i].isDirectory()) {
          // System.out.println("ディレクトリです：" + list[i].toString());
          if (list[i].toString().equals(home + "/DrawingApli") || list[i].toString().equals(home + "\\DrawingApli")) {
            System.out.println("appDataを見つけました");
            if (PlatformUtils.isWindows()) {
              appData = new File(home + "\\DrawingApli");
            }
            else {
              appData = new File(home + "/DrawingApli");
            }

            break OUTSIDE;
          }
        }
      }
      if (PlatformUtils.isWindows()) {
        appData = new File(home + "\\DrawingApli");
      }
      else {
        appData = new File(home + "/DrawingApli");
      }
      if (appData.mkdir()) {
        System.out.println("appDataを作成しました");
      }
      JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int selected = fc.showOpenDialog(da02);
      if (selected == JFileChooser.APPROVE_OPTION) {
        projectFolder = fc.getSelectedFile();
        System.out.println(projectFolder.getAbsolutePath() + "" + appData.getAbsolutePath());
        try {
          char[] c = projectFolder.getAbsolutePath().toCharArray();
          for(char c1: c) {
            System.out.println(c1);

          }
          serialize(projectFolder.getAbsolutePath(), appData.getAbsolutePath());
        } catch (IOException e) {
          System.out.println("IOException");
        }
      }
    }
  }

  // プロジェクトデータをシリアライズ(直列化)し､.txtファイルに保存するメソッド
  public static void serialize(String filePath, String fileName) throws IOException {
    FileOutputStream fos = new FileOutputStream(fileName);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(filePath);
    oos.flush();
    oos.close();
  }

  // 直列化されたデータをデシリアライズ(復元)するメソッド
  public static String deSerialize(String fileName) throws IOException, ClassNotFoundException {
    FileInputStream fis = new FileInputStream(fileName);
    ObjectInputStream ois = new ObjectInputStream(fis);
    String filePath = (String) ois.readObject();
    ois.close();
    return filePath;
  }
}
