import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.ArrayList;

// レイヤーのクラス
// キャンバスに描かれた図形1つごとにレイヤーを作成する
// コンポーネントは、図形のサムネイル、可視／不可視切り替えのチェックボックス、各種ボタンから成る
// ボタンには、削除、前に入れ替え、後ろに入れ替え、編集、編集キャンセル、編集完了の6つ設置されている
// mode(通常/編集)によってボタン、チェックボックスの配置が異なる
// このレイヤーパネルを選択してから上下矢印キーを押すことで、前後入れ替えができる
class Layer extends JPanel implements MouseListener {
  // フィールド変数
  int mode;   // 1:通常/2:編集
  Drawing draw;   // 対応するDrawingクラスのインスタンス用
  boolean visible = true;   // 可視/不可視
  boolean selected = false;   // このレイヤーが選択されているか
  Thumbnail tn;               // サムネイル用キャンバス
  JCheckBox visibleCB;        // 可視/不可視のチェックボックス
  LineBorder border = new LineBorder(new Color(220, 220, 220), 1);    // 未選択時のこのレイヤーパネルのボーダー(グレー)
  LineBorder selectedBorder = new LineBorder(Color.red, 1);           // 選択時のこのレイヤーパネルのボーダー(赤)
  LineBorder btnBorder = new LineBorder(Color.white, 2, true);        // 各ボタンのボーダー(白)
  ArrayList<JButton> btns = new ArrayList<JButton>();                 // ボタンのArrayList
  ArrayList<ImageIcon> icns = new ArrayList<ImageIcon>();             // ボタン用アイコンのArrayList
  ImageIcon editIcon = new ImageIcon("./icon/edit.png");              // ボタン用アイコン
  ImageIcon deleteIcon = new ImageIcon("./icon/delete.png");
  ImageIcon foreIcon = new ImageIcon("./icon/fore.png");
  ImageIcon backIcon = new ImageIcon("./icon/back.png");
  ImageIcon saveIcon = new ImageIcon("./icon/save.png");
  ImageIcon cancelIcon = new ImageIcon("./icon/cancel.png");
  ImageIcon editOverIcon = new ImageIcon("./icon/editOver.png");
  ImageIcon deleteOverIcon = new ImageIcon("./icon/deleteOver.png");
  ImageIcon foreOverIcon = new ImageIcon("./icon/foreOver.png");
  ImageIcon backOverIcon = new ImageIcon("./icon/backOver.png");
  ImageIcon saveOverIcon = new ImageIcon("./icon/saveOver.png");
  ImageIcon cancelOverIcon = new ImageIcon("./icon/cancelOver.png");
  ImageIcon visibleIcon = new ImageIcon("./icon/visible.png");        // チェックボックス用アイコン
  ImageIcon unvisibleIcon = new ImageIcon("./icon/unvisible.png");

  // コンストラクタ
  Layer (Drawing draw, DrawingApli02 da) {
    this.draw = draw;   // このレイヤーに対応するDrawing
    mode = 1;           // 初期値は通常モード
    setBorder(border);  // パネルのボーダーを設定
    addMouseListener(da);   // パネルが選択された時用
    addKeyListener(da);     // DrawingApli02がフォーカスを奪われたとき用
    tn = new Thumbnail(draw);     // draw元にサムネイルを作成
    tn.setBounds(10, 2, 70, 56);  // サムネイルの初期位置、サイズ設定
    add(tn);                      // このパネルに追加
    visibleCB = new JCheckBox(visibleIcon);     // チェックボックス作成、アイコン設定
    visibleCB.setSelectedIcon(unvisibleIcon);   // 選択時のアイコンも設定
    visibleCB.addActionListener(da.layerActionListener);    // リスナー登録(アクションリスナオブジェクトはDrawingApli02内のlayerActionListener)
    visibleCB.addKeyListener(da);   // DrawingApli02がフォーカスを奪われたとき用
    icns.add(editIcon);   // icnsにアイコンを追加
    icns.add(deleteIcon);
    icns.add(foreIcon);
    icns.add(backIcon);
    icns.add(cancelIcon);
    icns.add(saveIcon);
    icns.add(editOverIcon);
    icns.add(deleteOverIcon);
    icns.add(foreOverIcon);
    icns.add(backOverIcon);
    icns.add(cancelOverIcon);
    icns.add(saveOverIcon);
    setLayout(null);    // レイアウトマネージャーを無効
    for (int i=0; i<6; i++) {   // 各ボタンの設定
      btns.add(new JButton(icns.get(i)));   // アイコンからボタンを作成
      btns.get(i).addActionListener(da.layerActionListener);    // リスナー登録
      btns.get(i).addKeyListener(da);           // DrawingApli02がフォーカスを奪われたとき用
      btns.get(i).addMouseListener(this);
      btns.get(i).setContentAreaFilled(false);  // 背景を無効
      btns.get(i).setBorderPainted(false);      // ボーダーを無効
      btns.get(i).setBackground(Color.white);   // 背景色を白に
      btns.get(i).setBorder(btnBorder);         // ボーダーを設定
    }
    visibleCB.setBounds(110, 15, 30, 30);     // 初期位置、サイズを設定
    btns.get(0).setBounds(150, 15, 30, 30);
    btns.get(1).setBounds(190, 15, 30, 30);
    btns.get(2).setBounds(230, 0, 30, 30);
    btns.get(3).setBounds(230, 30, 30, 30);
    btns.get(4).setBounds(150, 15, 30, 30);
    btns.get(5).setBounds(190, 15, 30, 30);
    update(mode);                             // 表示
  }

  // レイヤーのコンポーネントを更新
  public void update(int mode) {
    removeAll();                  // レイヤーパネルのコンポーネントをすべて削除
    add(tn);                      // サムネイルを追加
    if (mode==1) {                // 通常モードなら
      add(visibleCB);             // チェックボックスと
      for (int i=0; i<4; i++){
        add(btns.get(i));         // 削除、前に入れ替え、後ろに入れ替え、編集ボタンを追加
      }
    } else if (mode==2) {         // 編集モードなら
      for (int i=4; i<6; i++) {
        add(btns.get(i));         // 編集キャンセル、編集完了ボタンを追加
      }
    }
  }

  // このレイヤーパネルが選択された時に呼び出されるメソッド
  public void select() {
    selected = true;
    setBorder(selectedBorder);    // ボーダー変更
  }
  // このレイヤーパネルの選択が解除された時に呼び出されるメソッド
  public void unselect() {
    selected = false;
    setBorder(border);            // ボーダー変更
  }

  //*****イベントリスナー*****
  // MouseListener
  public void mouseClicked(MouseEvent e){}
  public void mouseEntered(MouseEvent e){
    for (int i=0; i<btns.size(); i++) {
      if (e.getSource() == btns.get(i)) {       // ボタン[i]なら
        btns.get(i).setIcon(icns.get(i+6));     // アイコンを変更
        btns.get(i).setContentAreaFilled(true); // 背景を有効
        btns.get(i).setBorderPainted(true);     // ボーダーを有効
        break;
      }
    }
  }
  public void mouseExited(MouseEvent e){
    for (int i=0; i<btns.size(); i++) {
      if (e.getSource() == btns.get(i)) {       // ボタン[i]なら
        btns.get(i).setIcon(icns.get(i));       // アイコンを変更
        btns.get(i).setContentAreaFilled(false);// 背景を無効
        btns.get(i).setBorderPainted(false);    // ボーダーを無効
        break;
      }
    }
  }
  public void mousePressed(MouseEvent e){}
  public void mouseReleased(MouseEvent e){}
}


// レイヤーパネルに表示するサムネイルのクラス
// paintメソッド内で呼び出されるDrawingクラスのdoPaint(Graphics, Graphics2D, BasicStroke)メソッドは、
// このサムネイルクラス専用の物で、線が細いときは小さいキャンバスでも見えるようにデフォルメしてくれる
class Thumbnail extends Canvas {
  Drawing draw;   // 表示する図形のDrawing
  Image img;      // 描画するimg
  Dimension d;    // キャンバスのサイズ用
  Graphics gc;    // imgのペン
  Graphics2D gc2; // gcの設定
  BasicStroke bs; // 線の太さ用

  // コンストラクタ
  Thumbnail (Drawing draw) {
    setSize(700, 560);  // 元のキャンバスのサイズ
    this.draw = draw;   // drawをコピー
    d = getSize();      // サイズ取得
    repaint();          // 再描画
  }

  // メソッド
  @Override
  public void update(Graphics g) {
    paint(g);
  }

  @Override
  public void paint(Graphics g) {
    img = createImage(d.width, d.height); // imgを作成
    gc = img.getGraphics();               // imgのペンを作成
    gc2 = (Graphics2D)gc;                 // gcのGraphics2Dを取得
    gc.setColor(Color.white);             // 色を白に
    gc.fillRect(0, 0, 700, 560);          // 背景を描画
    draw.doPaint(gc, gc2, draw.bs);       // 図形を描画
    g.drawImage(img, 0, 0, 70, 56, this); // imgをキャンバスに貼り付け
  }
}
