import java.awt.*;
import java.awt.event.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;
import javax.swing.filechooser.*;

// フレームのクラス
// ほぼ全てのコンポーネントが配置され、多くのリスナーのアクションリスナオブジェクトに指定されている。
// ActionEventに関しては、リスナーを必要とするコンポーネントが多かったため、コンポーネントごとにリスナーを分けた。
/*  コンポーネント構造(メニューバー除く)
DrawingApli02(JFrame)
  |--mc(MyCanvas)・・・・・・・・・・・・・・・・・・・・・・・・・・・・キャンバス
  |--tabbedPane(JTabbedPane)
      |--tab1(JPanel)
      |   |--pnl(JPanel)
      |   |   |--btn(JButton 配列)・・・・・・・・・・・・・描画モード変更ボタン
      |   |--scrl(JPanel)
      |   |   |--lbPnl(JPanel 配列)
      |   |   |   |--lb(JLabel) ・・・・・・・・・・・・・・・ペン設定項目ラベル
      |   |   |   |--text(JTextField) ・・・・・・・・・・・・・・ペン設定の値
      |   |   |--slider(JSlider 配列)・・・・・・・・・・・ペン設定のスライダー
      |   |--cb(ColorButtons)
      |   |   |--currentColorButton(JButton)・・・・・・・選択中の色＆色変更
      |   |   |--buttonArray(JButton ArrayList)・・・選択した色一覧＆色変更
      |--tab2(JPanel)
          |--layers(Layer ArrayList)
              |--tn(Thumbnail)・・・・・・・・・・・・・・・レイヤーのサムネイル
              |--visibleCB(JCheckBox)・・・・・・レイヤーの可視/不可視切り替え
              |--btns(JButton ArrayList) ・・・・・・・・・・・・レイヤー設定
*/
public class DrawingApli02 extends JFrame implements ChangeListener, KeyListener, MouseListener, MouseMotionListener, ComponentListener, WindowListener, MenuListener, Serializable {
  // ■ フィールド変数
  DrawingApli02 da;   // このクラス
  File projectFile, projectFolder;    // プロジェクトを保存するディレクトリとファイル
  Dimension d, mcd;               // フレーム､キャンバスのサイズ
  ArrayList<Drawing> draws = new ArrayList<Drawing>();    // 描画する図形のDrawingクラス型配列
  ArrayList<Layer> layers = new ArrayList<Layer>();   // 各図形のLayerクラス型配列
  int first, last;                // 描画する図形の配列上の範囲
  int selectedLayer;          // 選択されたレイヤーのindex
  Color color = Color.black;  // ペンの色
  JMenuBar menubar;           // このフレームのメニューバー
  JMenu file, edit;           // メニューバーのメニュー
  JMenuItem file1, file2, file3, file4, file5, filesub1, filesub2, edit1;    // メニューのアイテム
  MyCanvas mc;                // MyCanvasクラスの宣言(キャンバス)
  ColorButtons cb;             // ColorButtonsクラスの宣言(パレット)
  JTabbedPane tabbedPane;     // ペン設定､レイヤー編集をまとめるタブ
  JPanel  pnl, scrl, tab1, tab2;    // コンポーネント配置用･タブ用のパネル
  JScrollPane scrlPane;       // レイヤーを載せるスクロールペイン
  JButton[] btn = new JButton[9];   // 描画設定､clear,undoボタン
  String[] btnStr = {"~", "/", "□", "■", "○", "●", "Clear", "Undo", "Redo"};  // 描画モードボタンのテキスト用
  JPanel[] lbPnl = new JPanel[5];   // ペン設定の項目名ラベルと数値をまとめるパネル配列
  JLabel[] lb = new JLabel[5];      // ペン設定の項目名のラベル配列
  String[] lbStr = {"太さ", "赤", "緑", "青", "透明度"};    // ラベルのテキスト用
  JTextField[] text = new JTextField[5];    // ペン設定のテキストフィールド配列
  JSlider[] slider = new JSlider[5];        // ペン設定のスライダー配列
  Border borderRaised = new BevelBorder(BevelBorder.RAISED, Color.white, Color.black);      // ボタンが押されていないときのボーダー
  Border borderLowered = new BevelBorder(BevelBorder.LOWERED, Color.white, Color.black);    // ボタンが押されているとき
  NewProjectFrame npf;

  // ■ コンストラクタ
  DrawingApli02() {
    // フレーム設定
    super("新規プロジェクト.txt");
    setSize(1000, 600);
    d = getSize();
    getContentPane().setBackground(new Color(220, 220, 220));   // フレームの背景をグレーに
    addKeyListener(this);             // ショートカットなどのため
    addComponentListener(this);       // ウィンドウサイズが変更されたとき用
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // 閉じるボタンを押したらシステム終了
    setFocusable(true);               // キーボードのフォーカスをフレームにセット
    da = this;                        // ActionListenerのイベント処理で使う
    System.out.println(PlatformUtils.isMac());        // Macならtrue､他のOSならfalseをプリント
    // String home = System.getProperty("user.home");
    // System.out.println(home);
    // File dir = new File(home);
    // File[] list = dir.listFiles();
    // OUTSIDE: if (list != null) {
    //   for (int i=0; i<list.length; i++) {
    //     if (list[i].isDirectory()) {
    //       System.out.println("ディレクトリです：" + list[i].toString());
    //       if (list[i].toString().equals(home + "/DrawingApli") || list[i].toString().equals(home + "\\DrawingApli")) {
    //         System.out.println("プロジェクトフォルダーを見つけました");
    //         if (PlatformUtils.isWindows()) {
    //           projectFolder = new File(home + "\\DrawingApli");
    //         }
    //         else {
    //           projectFolder = new File(home + "/DrawingApli");
    //         }
    //         break OUTSIDE;
    //       }
    //     }
    //   }
    //   if (PlatformUtils.isWindows()) {
    //     projectFolder = new File(home + "\\DrawingApli");
    //   }
    //   else {
    //     projectFolder = new File(home + "/DrawingApli");
    //   }
    //   if (projectFolder.mkdir()) {
    //     System.out.println("プロジェクトフォルダーを作成しました");
    //   }
    // }

    // メニューバー設定(インスタンス作成､リスナー登録､コンポーネント追加)
    menubar = new JMenuBar();
    file = new JMenu("ファイル");
    edit = new JMenu("編集");
    menubar.add(file);
    menubar.add(edit);
    file.addMenuListener(this);   // メニューが選択解除された際､キャンバスを再描画させるため､
    edit.addMenuListener(this);   // メニューにリスナー登録
    file1 = new JMenuItem("PNG画像として保存する");
    file2 = new JMenuItem("プロジェクトを上書き保存する");
    file3 = new JMenuItem("プロジェクトを開く");
    file4 = new JMenuItem("新規プロジェクト");
    file5 = new JMenuItem("プロジェクトを名前を付けて保存");
    // filesub1 = new JMenuItem("jpeg");
    // filesub2 = new JMenuItem("png");
    edit1 = new JMenuItem("背景色を変える");
    file1.addActionListener(menuActionListener);
    file2.addActionListener(menuActionListener);
    file3.addActionListener(menuActionListener);
    file4.addActionListener(menuActionListener);
    file5.addActionListener(menuActionListener);
    // filesub1.addActionListener(menuActionListener);
    // filesub2.addActionListener(menuActionListener);
    edit1.addActionListener(menuActionListener);
    file2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));   // メニューアイテムにショートカットキーを設定:[Ctrl+S]
    file3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));   // [Ctrl+O]
    file4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));   // [Ctrl+N]
    file4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));   // [Ctrl+N]
    edit1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));   // [Ctrl+B]
    file.add(file4);
    file.add(file3);
    file.add(file2);
    file.add(file5);
    file.add(file1);
    edit.add(edit1);
    // file1.add(filesub1);
    // file1.add(filesub2);
    setJMenuBar(menubar);

    //コンポーネントのインスタンス作成･リスナー登録
    mc = new MyCanvas(this, draws, layers, new Dimension(700, 540));
    tabbedPane = new JTabbedPane();
    tab1 = new JPanel();    // tabbedPane用のパネル1(描画モード・色変更)
    tab2 = new JPanel();    // tabbedPane用のパネル2(レイヤー)
    scrlPane = new JScrollPane(tab2, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);    // パネルtab2内をスクロールペインに(スクロールバーは縦のみ常時表示)
    scrlPane.getVerticalScrollBar().setUnitIncrement(25);   // スクロール量を設定
    pnl = new JPanel();     // 描画モードボタンをまとめるパネル
    scrl = new JPanel();    // 色変更(ラベル、テキストフィールド、スライダー)をまとめるパネル
    cb = new ColorButtons();    // 色変更(パレットボタン)をまとめるパネル
    cb.currentColorButton.addActionListener(cbActionListener);    // ActionListener追加(JColorChooserダイアログ表示→色選択)
    cb.addKeyListener(this);    // フォーカスを奪われたとき用
    tab2.addMouseListener(this);    // いらないかも

    //コンポーネントの位置･サイズを設定｡フレームに配置｡
    setLayout(null);   // レイアウトマネージャーは使わない
    tabbedPane.setBounds(d.width-300, 0, 285, 540);
    tab1.setLayout(null);   //フレーム同様､タブ用のパネルもレイアウトマネージャーは使わない
    tab2.setLayout(null);
    pnl.setBounds(0, 0, 285, 200);    // 初期位置､サイズを指定
    scrl.setBounds(0, 200, 285, 220);
    tabbedPane.addTab("ペン", tab1);    // タブを追加
    tabbedPane.addTab("レイヤー", scrlPane);
    tab1.add(pnl);    // タブにコンポーネントを追加(レイヤータブはまだ)
    tab1.add(scrl);
    tab1.add(cb);
    add(tabbedPane);    // フレームに追加
    add(mc);


    //描画モードボタン
    pnl.setLayout(new GridLayout(5,2));  // ボタンを配置するため，4行2列のグリッドをパネル上にさらに作成
    for (int i=0; i<btn.length; i++) {
      btn[i] = new JButton(btnStr[i]);
      btn[i].addActionListener(modeActionListener);           //ボタンが押されたか監視
      btn[i].addKeyListener(this);           //フォーカスを奪われたとき用
      btn[i].setFont(new Font("Arial", Font.PLAIN, 15));    //ボタンのフォント設定
      btn[i].setMaximumSize(new Dimension(50,50));
      btn[i].setBorder(borderRaised);
      pnl.add(btn[i]);
    }
    btn[0].doClick();           // 最初描画モードはフリーハンド
    btn[6].setEnabled(false);   // Clearは最初は無効
    btn[7].setEnabled(false);   // Undoは最初は無効
    btn[8].setEnabled(false);   // Redoは最初は無効

    //ペン設定
    scrl.setLayout(null);           //色変更設定を配置するため、10行1列のグリッドをパネル上にさらに作成
    for (int i=0; i<lb.length; i++) {               //色変更設定(項目名、数値、スクロールバー)を配置
      lbPnl[i] = new JPanel();                       //1項目をまとめるパネル
      lb[i] = new JLabel(lbStr[i], JLabel.CENTER);    //事前に用意した配列 lbStr で lb の文字を変更
      text[i] = new JTextField((i==0)? "5":"0", 3);     //太さ設定(i=0)のみ、ほかと初期値を変える
      text[i].setHorizontalAlignment(JTextField.RIGHT);
      text[i].addActionListener(enterActionListener);                //テキストの変更を監視
      text[i].addKeyListener(this);                //フォーカスを奪われたとき用
      lbPnl[i].add(lb[i]);
      lbPnl[i].add(text[i]);
      slider[i] = new JSlider((i==0)? 1:0, (i==0)? 50:255, (i==0)? 5:0);
      slider[i].addChangeListener(this);          //スクロールバーの変更を監視
      slider[i].addKeyListener(this);          //フォーカスを奪われたとき用
      slider[i].setSize(200,30);
      lb[i].setBounds(0, i*40, 45, 40);
      text[i].setBounds(45, i*40+10, 40, 40-20);
      slider[i].setBounds(95, i*40, 180, 40);
      scrl.add(lb[i]);
      scrl.add(text[i]);
      scrl.add(slider[i]);
    }
    setVisible(true); //可視化
  }


  // ■ メソッド
  // レイヤーパネルを更新
  public void refreshLayers() {
    tab2.removeAll();   // パネルのコンポーネントをすべて削除
    tab2.setPreferredSize(new Dimension(275, 60*(mc.last-mc.first)));   // サイズを変更
    for (int i=0; i<(mc.last-mc.first); i++) {    // キャンバスに描画する分だけ
      layers.get(mc.last-i-1).setBounds(0, 60*i, 265, 60);    // レイヤーの位置を再設定
      tab2.add(layers.get(mc.last-i-1));    // パネルに追加
    }
    tab2.repaint();
    scrlPane.revalidate();
  }

  // ArrayListの要素を交換するメソッド(ジェネリクスを用いてるため､ArrayListの型によらず使うことができる)
  public <T> void swap(ArrayList<T> array, int index1, int index2){
    if (Math.min(index1, index2)>=0 && Math.max(index1, index2)<array.size()) {   // indexがArrayListの範囲だったら
      T tmp = array.get(index1);    // ジェネリクスT型変数tmpに一旦代入
      array.set(index1, array.get(index2));   // もう一方の要素に入れ替え
      array.set(index2, tmp);   // tmpを代入
    }
  }

  // プロジェクトデータをシリアライズ(直列化)し､.txtファイルに保存するメソッド
  public static void serialize(SaveData sd, String fileName) throws IOException {
    FileOutputStream fos = new FileOutputStream(fileName);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(sd);
    oos.flush();
    oos.close();
  }

  // 直列化されたデータをデシリアライズ(復元)するメソッド
  public static SaveData deSerialize(String fileName) throws IOException, ClassNotFoundException {
    FileInputStream fis = new FileInputStream(fileName);
    ObjectInputStream ois = new ObjectInputStream(fis);
    SaveData sd = (SaveData) ois.readObject();
    ois.close();
    return sd;
  }


  // *****イベントリスナー*****
  // 描画モード切り替え･Clear･Undoボタンの ActionListener
  private ActionListener modeActionListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      OUTSIDE: if (!mc.editEnabled) {    // キャンバスが編集モードでないときのみ実行
        if (e.getSource() == btn[6]) {      // Clearボタン
          mc.Clear();                       // キャンバスクリアー
          refreshLayers();                  // レイヤー更新
          btn[6].setEnabled(false);         // Clearボタン無効
          btn[7].setEnabled(true);          // Undoボタン有効
          btn[8].setEnabled(false);         // Redoボタン無効
          break OUTSIDE;                    // ラベル付きif文から抜け出す
        }
        else if (e.getSource() == btn[7]) { // Undoボタン
          mc.Undo();                        // キャンバスアンドゥ
          refreshLayers();                  // キャンバスクリアー
          if (mc.first==mc.last || mc.n==0) {   // クリアー済み、または1枚目なら
            btn[6].setEnabled(false);       // Clearボタン無効
          }
          else btn[6].setEnabled(true);     // 未クリアー、かつ2枚目以降ならClearボタン有効
          if (mc.n==0) btn[7].setEnabled(false);    // 1枚目ならUndoボタン無効
          btn[8].setEnabled(true);          // 必ずRedoボタン有効
          break OUTSIDE;                    // ラベル付きif文から抜け出す
        }
        else if (e.getSource() == btn[8]) { // Redoボタン
          mc.Redo();                        // キャンバスリドゥ
          refreshLayers();                  // レイヤー更新
          if (mc.first == mc.last) {        // クリアー済みなら
            btn[6].setEnabled(false);       // Clearボタン無効
          }
          else btn[6].setEnabled(true);     // 未クリアーならClearボタン有効
          btn[7].setEnabled(true);          // 必ずUndoボタン有効
          if (mc.n == draws.size()) btn[8].setEnabled(false);   // ArrayListの最後のオブジェクトまで描画しているならRedoボタン無効
          break OUTSIDE;                    // ラベル付きif文から抜け出す
        }
        for (int i=0; i<btn.length-3; i++) {      // 描画モード変更ボタン
          if (e.getSource() == btn[i]){
            btn[mc.mode-1].setBorder(borderRaised);   // 選択されているボタンのボーダーを未選択のものに
            mc.mode=i+1;                              // 描画モード変更
            btn[mc.mode-1].setBorder(borderLowered);  // 選択されたボタンのボーダーを選択済みのものに
            break OUTSIDE;                        // ラベル付きif文から抜け出す
          }
        }
      }
    }
  };

  // メニューアイテムの ActionListener
  private ActionListener menuActionListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      if (!mc.editEnabled) {    // 編集中でなければ
        if (e.getSource() == edit1) {       //背景色を変えるメニューアイテム
          JColorChooser cc = new JColorChooser();   // JColorChooserのインスタンスccを作成
          Color color_ = cc.showDialog(da, "色の選択", mc.backColor);   // ccで選択された色を取得
          if (color_ != null) {     // 色が選択されていれば
            mc.backColor = color_;  // キャンバスの背景色を変更
            mc.doPaint();           // キャンバス再描画
          }
        }
        else if (e.getSource() == file1) {    // PNG画像として保存するメニューアイテム
          try {
            JFileChooser fc = new JFileChooser();
            fc.setFileHidingEnabled(true);
            FileFilter filter = new FileNameExtensionFilter("PNGファイル", "png");
            fc.addChoosableFileFilter(filter);
            fc.setAcceptAllFileFilterUsed(false);
            int selected = fc.showSaveDialog(da);
            if (selected == JFileChooser.APPROVE_OPTION){
              File file = fc.getSelectedFile();
              ImageIO.write(mc.img, "png", file);
            }
          } catch (Exception ee){
            JOptionPane.showMessageDialog(da, "エラー：画像を保存できませんでした。");
          }
        }
        else if (e.getSource() == file2) {    // プロジェクトを上書き保存するメニューアイテム
          if (draws.size()>0) {
            try {
              if (projectFile!=null) {
                SaveData sd = new SaveData(draws, mc);
                serialize(sd, projectFile.getAbsolutePath());
              }
              else {
                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File(projectFolder, "新規プロジェクト.txt"));
                fc.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
                int selected = fc.showSaveDialog(da);
                if (selected == JFileChooser.APPROVE_OPTION) {
                  SaveData sd = new SaveData(draws, mc);
                  File file = fc.getSelectedFile();
                  String fileName = file.getAbsolutePath();
                  if (fileName.substring(fileName.length()-4)!=".txt") {
                    String tmp = fileName.concat(".txt");
                    file = new File(tmp);
                  }
                  projectFile = file;
                  serialize(sd, projectFile.getAbsolutePath());
                }
              }
            } catch (IOException ie){
              System.out.println("エラー");
            }
          }
        }
        else if (e.getSource() == file3) {    // プロジェクトを開くメニューアイテム
          try {
            JFileChooser fc;
            if (projectFolder!=null) {
              fc = new JFileChooser(projectFolder);
            }
            else {
              fc = new JFileChooser();
            }
            fc.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
            int selected = fc.showOpenDialog(da);
            if (selected == JFileChooser.APPROVE_OPTION) {
              File file = fc.getSelectedFile();
              SaveData sd = deSerialize(file.getAbsolutePath());
              sd.recover(mc);
              draws = mc.draws;
              if (mc.first==mc.last) {
                btn[6].setEnabled(false);
                btn[7].setEnabled(false);
              } else {
                btn[6].setEnabled(true);
                btn[7].setEnabled(true);
              }
              layers.clear();
              for (int i=0; i<draws.size(); i++) {
                layers.add(new Layer(draws.get(i), da));
              }
              mc.layers = layers;
              mc.doPaint();
              projectFile = file;
              setTitle(projectFile.getName());
              refreshLayers();
            }
          } catch (IOException | ClassNotFoundException ie){
            System.out.println("エラー");
          }
        }
        else if (e.getSource() == file4) {    // 新規プロジェクトを作成するメニューアイテム
          npf = new NewProjectFrame(da);
          npf.addWindowListener(da);
        }
        else if (e.getSource() == file5) {    // プロジェクトを名前をつけて保存するメニューアイテム
          try {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(projectFolder, "新規プロジェクト.txt"));
            fc.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
            int selected = fc.showSaveDialog(da);
            if (selected == JFileChooser.APPROVE_OPTION) {
              SaveData sd = new SaveData(draws, mc);
              File file = fc.getSelectedFile();
              String fileName = file.getAbsolutePath();
              if (!fileName.contains(".") || fileName.substring(fileName.lastIndexOf(".")).equals(".txt")) {
                // System.out.println(fileName.substring(fileName.lastIndexOf(".")));
                String tmp = fileName.concat(".txt");
                file = new File(tmp);
              }
              projectFile = file;
              setTitle(projectFile.getName());
              serialize(sd, file.getAbsolutePath());
            }
          } catch (IOException ie) {}
        }
      }
    }
  };

  // cb(ColorButtons)の ActionListener
  public ActionListener cbActionListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == cb.currentColorButton) {    // JColorChooserで色変更
        JColorChooser cc = new JColorChooser();
        Color color_ = cc.showDialog(da, "色の選択", cb.color);
        if (color_ != null) {
          slider[1].setValue(color_.getRed());
          slider[2].setValue(color_.getGreen());
          slider[3].setValue(color_.getBlue());
          slider[4].setValue(255-color_.getAlpha());
        }
      }
      OUTSIDE: for(int i=0; i<cb.buttonArray.size() && i<10; i++) {    // パレットから色変更
        if (e.getSource() == cb.buttonArray.get(i)) {
          Color color_ = cb.colorArray.get(i);
          slider[1].setValue(color_.getRed());
          slider[2].setValue(color_.getGreen());
          slider[3].setValue(color_.getBlue());
          slider[4].setValue(255-color_.getAlpha());
          break OUTSIDE;
        }
      }
    }
  };

  // Layerのボタン､チェックボックスの ActionListener
  public ActionListener layerActionListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      for (int i=0; i<layers.size(); i++) {     // Layerパネル上のボタン、チェックボックス
        Layer layer_ = layers.get(i);
        if (e.getSource() == layer_.btns.get(0) && !mc.editEnabled) {        // 編集ボタン
          layer_.update(2);
          tab2.repaint();
          mc.edit(i);
          break;
        }
        else if (e.getSource() == layer_.btns.get(1)) {   // 削除ボタン
          mc.n--;
          mc.last--;
          draws.remove(i);
          layers.remove(i);
          refreshLayers();
          mc.doPaint();
          break;
        }
        else if (e.getSource() == layer_.btns.get(2)) {   // 1枚前に入れ替えるボタン
          swap(draws, i, i+1);
          swap(layers, i, i+1);
          refreshLayers();
          mc.doPaint();
          break;
        }
        else if (e.getSource() == layer_.btns.get(3)) {   // 1枚後ろに入れ替えるボタン
          swap(draws, i, i-1);
          swap(layers, i, i-1);
          refreshLayers();
          mc.doPaint();
          break;
        }
        else if (e.getSource() == layer_.btns.get(4)) {   // 編集キャンセルボタン
          layer_.update(1);
          refreshLayers();
          mc.cancelEdit(i);
          mc.doPaint();
          break;
        }
        else if (e.getSource() == layer_.btns.get(5)) {   // 編集完了ボタン
          layer_.update(1);
          mc.finEdit(i);
          mc.doPaint();
          refreshLayers();
          break;
        }
        else if (e.getSource() == layer_.visibleCB) {     // 可視/不可視切り替えチェックボックス
          draws.get(i).visible = !draws.get(i).visible;
          mc.doPaint();
          break;
        }
      }
    }
  };

  // テキストフィールドの ActionListener
  private ActionListener enterActionListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      int num = 0;
      JTextField text_ = (JTextField)e.getSource();
      try {
        num = Integer.parseInt(text_.getText());
      }catch (NumberFormatException f) {}
        for (int i=0; i<text.length; i++) {
          if (text_ == text[i]) {
            if (num < slider[i].getMinimum()) slider[i].setValue(slider[i].getMinimum());
            else if (num > slider[i].getMaximum()) slider[i].setValue(slider[i].getMaximum());
            else slider[i].setValue(num);
            text[i].setText(Integer.toString(slider[i].getValue()));
          }
        }
        mc.bs = new BasicStroke(slider[0].getValue(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        color = new Color(slider[1].getValue(), slider[2].getValue(), slider[3].getValue(), 255-slider[4].getValue());
        mc.color = color;
        cb.currentColorButton.setBackground(color);
        // System.out.println("Red:" + slider[1].getValue() + " Green:" + slider[2].getValue() + " Blue:" + slider[3].getValue());
      }
    };

  // スライダーの ChangeListener
  public void stateChanged(ChangeEvent e) {
    JSlider slider_ = (JSlider)e.getSource();
    for(int i=0; i<slider.length; i++) {
      if (slider_ == slider[i])
      text[i].setText(Integer.toString(slider[i].getValue()));
    }
    mc.bs = new BasicStroke(slider[0].getValue(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    color = new Color(slider[1].getValue(), slider[2].getValue(), slider[3].getValue(), 255-slider[4].getValue());
    if (mc.editEnabled) {
      mc.editDraw.color = color;
      mc.doPaint();
    }
    mc.color = color;
    cb.color = color;
    cb.currentColorButton.setBackground(color);
    System.out.println("Red:" + slider[1].getValue() + " Green:" + slider[2].getValue() + " Blue:" + slider[3].getValue());
  }

  // このフレームの ComponentListener
  public void componentHidden(ComponentEvent e){}
  public void componentMoved(ComponentEvent e){}
  public void componentShown(ComponentEvent e){}
  public void componentResized(ComponentEvent e){
    d = getSize();
    if (PlatformUtils.isMac()) {
      tabbedPane.setBounds(d.width-300, 0, 285, 540);
    } else {
      tabbedPane.setBounds(d.width-300, 0, 285, 540);
    }
    mc.doPaint();
  }

  // npf(NewProjectFrame)の WindowListener
  public void windowOpened(WindowEvent e){}
  public void windowClosing(WindowEvent e){}
  public void windowClosed(WindowEvent e){
    System.out.println("windowClosed");
    if (e.getSource() == npf && npf.makeNpf) {
      System.out.println("getSource == npf");
      draws = new ArrayList<Drawing>();
      layers = new ArrayList<Layer>();
      mcd = npf.getD();
      mc.draws = draws;
      mc.layers = layers;
      System.out.println("d=(" + mcd.width + "," + mcd.height + ")");
      mc.d = mcd;
      System.out.println("d=(" + mc.d.width + "," + mc.d.height + ")");
      mc.reset();
      mc.doPaint();
      revalidate();
      repaint();
    }
  }
  public void windowIconified(WindowEvent e){}
  public void windowDeiconified(WindowEvent e){}
  public void windowActivated(WindowEvent e){}
  public void windowDeactivated(WindowEvent e){}

  // メニューの MenuListener
  public void menuCanceled(MenuEvent e){}
  public void menuSelected(MenuEvent e){}
  public void menuDeselected(MenuEvent e){
    mc.doPaint();
  }

  // MouseMotionListener
  public void mouseDragged(MouseEvent e) {}
  public void mouseMoved(MouseEvent e) {}

  // MouseListener
  public void mouseClicked(MouseEvent e){}
  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){}
  public void mousePressed(MouseEvent e){}
  public void mouseReleased(MouseEvent e){
    if (e.getSource()==mc) {
      btn[6].setEnabled(true);
      btn[7].setEnabled(true);
      btn[8].setEnabled(false);
      refreshLayers();
      boolean addEnabled = true;
      if (cb.buttonArray.size()>0){
        for (int i=0; i<cb.buttonArray.size()&&i<10; i++) {
          Color color_ = cb.buttonArray.get(i).getBackground();
          if (color_.getRGB()==color.getRGB() && color_.getAlpha()==color.getAlpha()) {
            addEnabled = false;
          }
        }
        if (addEnabled) {
          cb.addColor(color);
          cb.buttonArray.get(0).addActionListener(cbActionListener);
          System.out.println("ActionListener追加");
        }
      }
      else {
        cb.addColor(color);
        cb.buttonArray.get(0).addActionListener(cbActionListener);
        System.out.println("ActionListener追加");
      }
    }
    else {
      int i=0;
      for (Layer layer1: layers) {
        if (e.getSource()==layer1) {
          for (Layer layer2: layers) {
            layer2.unselect();
          }
          layer1.select();
          selectedLayer = i;
          break;
        }
        i++;
      }
    }
  }

  // KeyListener
  public void keyPressed(KeyEvent ke) {
    int keycode = ke.getKeyCode();
    int mod = ke.getModifiersEx();
    if (keycode == KeyEvent.VK_CONTROL) mc.ctrlFlag=true;
    else if (keycode == KeyEvent.VK_F1) btn[0].doClick();   // フリーハンド
    else if (keycode == KeyEvent.VK_F2) btn[1].doClick();   // 直線
    else if (keycode == KeyEvent.VK_F3) btn[2].doClick();   // 四角形
    else if (keycode == KeyEvent.VK_F4) btn[3].doClick();   // 塗りつぶし四角形
    else if (keycode == KeyEvent.VK_F5) btn[4].doClick();   // 楕円
    else if (keycode == KeyEvent.VK_F6) btn[5].doClick();   // 塗りつぶし楕円
    else if (keycode == KeyEvent.VK_UP) {   // [↑]:レイヤーを一枚手前にするボタン
      for (Layer layer: layers) {
        if (layer.selected) {
          layer.btns.get(2).doClick();
          break;
        }
      }
    }
    else if (keycode == KeyEvent.VK_DOWN) {   // [↓]:レイヤーを一枚後ろにするボタン
      for (Layer layer: layers) {
        if (layer.selected) {
          layer.btns.get(3).doClick();
          break;
        }
      }
    }
    else if (keycode == KeyEvent.VK_DELETE) { // [Delete]:レイヤーを削除するボタン
      for (Layer layer: layers) {
        if (layer.selected) {
          layer.btns.get(1).doClick();
          break;
        }
      }
    }
    if ((mod&InputEvent.CTRL_DOWN_MASK) != 0) {   // 修飾キーCtrlが押されていたら
      if (keycode == KeyEvent.VK_C) btn[6].doClick();   // [Ctrl+C]:クリアーボタン
      else if ((mod&InputEvent.SHIFT_DOWN_MASK) !=0 && keycode == KeyEvent.VK_Z) {
        btn[8].doClick();
      }
      else if (keycode == KeyEvent.VK_Z) btn[7].doClick();   // [Ctrl+Z]:アンドゥボタン
      else if (keycode == KeyEvent.VK_Q) System.exit(0);   // [Ctrl+Q]:プログラム終了ボタン
      else if (keycode == KeyEvent.VK_T)
      tabbedPane.setSelectedIndex((tabbedPane.getSelectedIndex()+1)%2);   // [Ctrl+T]:タブ切り替え
    }
  }
  public void keyReleased(KeyEvent ke) {
    int keycode = ke.getKeyCode();
    if (keycode == KeyEvent.VK_CONTROL) mc.ctrlFlag = false;
  }
  public void keyTyped(KeyEvent ke) {}
}
