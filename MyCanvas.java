import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.ArrayList;

// キャンバスのクラス
// MouseMotionListenerを通じて、ユーザーがドラッグしたポインタの座標を取得し、描画モードに応じて図形を描く
// 新しく描画されるもの(mouseReleasedまで)はこのクラス内で描画される。
// 既に描画されたものは、描画情報を持つDrawingクラスのArrayList"draws"に保存され、
// Drawingクラス内のメソッドを通じて描画する。
// 編集モードがあり、mouseDraggedなどの挙動が大きく異なる。
class MyCanvas extends Canvas implements MouseListener, MouseMotionListener {
  DrawingApli02 da02;   // DrawingApli02クラス
  int px, py, x, y, w, h;   // 描画に必要な引数
  int n = 0;    // 現在地(lastと区別がない)
  int mode = 1;     // 描画モード
  int first = 0;    // 描画する最初の要素
  int last = 0;     // 描画する最後の要素
  Drawing editDraw;   // 編集用のDrawingオブジェクト
  Dimension d;    // キャンバスのサイズ用
  BufferedImage img;    // 描画するキャンバス
  boolean draw = true;    // paint実行時、再描画するか
  boolean drawing = false;    // paint実行時、新しく図形を描くか
  boolean cleared = true;   // クリアー済みか
  boolean editEnabled = false;    // 編集中か
  boolean ctrlFlag = false;   // Ctrlキーは押されているか(円、正方形描画用)
  boolean shiftFlag = false;    // Shiftキーは押されているか(編集する図形を選択するとき用、未実装)
  Color color = Color.black;    // ペンの色
  Color backColor = Color.white;    // キャンバスの背景色
  BasicStroke bs = new BasicStroke(5);    // 線の太さを変更(初期値：5)
  ArrayList<ArrayList<Integer>> arrays;   // フリーハンドの筆跡の座標を保存するArrayList
  ArrayList<Drawing> draws;   // 描画情報を持つDrawingクラスのインスタンスを保存するArrayList
  ArrayList<Layer> layers;    // レイヤーを保存するArrayList。drawsとlayersの中身は常に対応させるようにプログラムしていく
  ArrayList<Integer> cPositions = new ArrayList<Integer>();   // クリアーした場所を保存するArrayList

  MyCanvas(DrawingApli02 da02, ArrayList<Drawing> draws, ArrayList<Layer> layers, Dimension d) {
    this.setBounds(0, 0, d.width, d.height);    // 初期位置、サイズ設定
    addMouseListener(this);         // ↓リスナー登録(アクションリスナオブジェクトにこのクラスを指定)
    addMouseMotionListener(this);
    addMouseListener(da02);         // ↓リスナー登録(アクションリスナオブジェクトにDrawingApli02クラスを指定)
    addMouseMotionListener(da02);
    addKeyListener(da02);

    cPositions.add(0);          // clearPositionsに0を追加
    this.da02 = da02;           // DrawingApli02のオブジェクトをコピー
    this.draws = draws;         // DrawingApli02 のdrawsをshallow copy
    this.layers = layers;       // 上と同じ｡ layersとdrawsは常に対応させる
    this.d = d;                 // キャンバスサイズをコピー
  }
  // MyCanvas(DrawingApli02 da02) {
  //   addMouseListener(this);
  //   addMouseMotionListener(this);
  //   addMouseListener(da02);
  //   addMouseMotionListener(da02);
  //   addKeyListener(da02);
  //   this.da02 = da02;
  // }

  // メソッド
  // repaintを呼び出す(他クラスから再描画をさせるとき用)
  public void doPaint() {
    draw = true;
    repaint();
  }

  @Override
  public void update(Graphics g){
    paint(g);
  }

  @Override
  public void paint(Graphics g) {
    // d = getSize();
    // System.out.println("d.width:" + d.width + ", d.height" + d.height);
    if (draw) {
      draw = false;
      // System.out.println("n:" + n + ", first:" + first + ", last:" + last);
      // System.out.println(cPositions);
      // img = createImage(d.width, d.height);
      System.out.println("d=(" + d.width + "," + d.height + ")");
      img = new BufferedImage(d.width, d.height, BufferedImage.TYPE_4BYTE_ABGR);    // キャンバスを作成(毎回)
      Graphics gc = img.getGraphics();        // キャンバス用のペンを作成
      Graphics2D gc2 = (Graphics2D)gc;        // ペンの設定を変更するオブジェクトを作成
      gc.setColor(backColor);                 // ペンの色を背景色に
      gc.fillRect(0, 0, d.width, d.height);   // 背景を塗る
      if (first==last) cleared = true;
      else cleared = false;
      for (int i=first; i<last; i++) {        // ArrayListの要素firstからlast-1まで
        draws.get(i).doPaint(gc, gc2);        // drawsの図形を描画
      }
      if (editEnabled) {                      // 編集中なら
        gc.setColor(new Color(0, 0, 0, 60));  // 透明度60の黒で
        gc.fillRect(0, 0, d.width, d.height); // 背景を塗って暗くして
        editDraw.needPaint(gc, gc2);          // 編集用のDrawingオブジェクトを描画して
        editDraw.drawFrame(gc, gc2, this, da02);  // 赤の四角い枠を描画する
      }
      if (drawing) {                          // 新しく図形を書くとき
        gc.setColor(color);                   // 色を設定
        gc2.setStroke(bs);                    // ペンを設定
        if (mode==1) {                        // フリーハンドモードなら
          for (int i=0; i<arrays.size()-1; i++) {   // 筆跡の座標を保存するArrayListに関し
            gc.drawLine(arrays.get(i).get(0),       // 全ての座標を繋げるように描画
            arrays.get(i).get(1),
            arrays.get(i+1).get(0),
            arrays.get(i+1).get(1));
          }
        }
        else if (mode==2) gc.drawLine(px, py, x, y);  // 直線モード
        else if (mode==3) gc.drawRect(x, y, w, h);    // 四角形モード
        else if (mode==4) gc.fillRect(x, y, w, h);    // 塗りつぶし四角形モード
        else if (mode==5) gc.drawOval(x, y, w, h);    // 楕円モード
        else if (mode==6) gc.fillOval(x, y, w, h);    // 塗りつぶし楕円モード
      }
      g.drawImage(img, 0, 0, this);   // 最後にimgを貼り付ける
    }
  }

  // Clearボタンが押された時に呼び出されるメソッド
  public void Clear() {
    if (!cleared) {   // クリアー済みでなければ
      cPositionsDel();    // cPositionsを見直す(現在地以降のcPositionsは削除)
      cPositions.add(n);    // cPositionsに現在地(n)を追加
      first = n;    // first,lastを現在地に
      last = n;     //  →何も描画されない
      int drawsSize = draws.size();   // draws.size()がfor文中で変わるので、ここに保存
      for (int i=last; i<drawsSize; i++) {    // 現在地以降のdraws,layersは
        draws.remove(last);   // 削除
        layers.remove(last);  // 削除
      }
      draw = true;        // 再描画オン、
      drawing = false;    // 新規描画オフ、
      repaint();          // 再描画
    }
  }

  // Undoボタンが押された時に呼び出されるメソッド
  public void Undo() {
    if (n>0) {    // 現在地が1枚目でなければ
      if (first == last) {    // クリアー済みなら
        first = cPositions.get(cPositions.indexOf(last)-1);   // クリアーポジションを1つ前に
      }
      else {    // 未クリアーなら
        n--;    // 1枚戻る
        last = n;   // lastも
      }
      draw = true;      // 再描画オン、
      drawing = false;  // 新規描画オフで、
      repaint();        // 再描画
    }
  }

  // Redoボタンが押された時に呼び出されるメソッド
  public void Redo() {
    if(n<draws.size()) {    // 現在地が最後でなければ
      if (cPositions.contains(last) && first!=last) {   // cPositionsが現在地を持っていて、かつクリアー済みでなければ
        first = last;   // firstを現在地へ
      } else {    // 一方でも違えば
        n++;    // 1枚先へ
        last = n;   // lastも
      }
      draw = true;      // 再描画オン、
      drawing = false;  // 新規描画オフで、
      repaint();        // 再描画
    }
  }

  // cPositionsを見直すメソッド(Clear,新規描画時(mouseDragged))
  public void cPositionsDel() {
    int k;    // cPositionsを削除する位置
    if (cPositions.contains(last) && first!=last) {   // cPositionsが現在地を持っていて、かつクリアー済みでなければ
      k = cPositions.get(cPositions.indexOf(last)-1);   // kは現在地より1つ後ろのクリアーポジション
    } else {    // 一方でも違えば
      k = last;   // kは現在地
    }
    for (int i=k+1; i<=cPositions.get(cPositions.size()-1); i++) {    // k+1からcPositionsの最後の要素のクリアーポジションまで
      if (cPositions.contains(i)) {   // cPositionsが持っていれば
        cPositions.remove(cPositions.indexOf(i));   // 削除
      }
    }
  }

  // 編集ボタンに呼び出されるメソッド
  public void edit(int index) {
    editEnabled = true;   // 編集中に変更
    editDraw = draws.get(index).clone();    // 編集対象のDrawingオブジェクトのクローンを、編集用のDrawingオブジェクトにいれる
    draws.get(index).visible2 = false;    // クローン元の編集対象を不可視化
    draw = true;    // 再描画オンで、
    repaint();      // 再描画
  }

  // 編集キャンセルボタンに呼び出されるメソッド
  public void cancelEdit(int index) {
    editEnabled = false;    // 編集モード解除
    draws.get(index).visible2 = true;   // 編集対象を可視化
    draw = true;    // 再描画オンで、
    repaint();      // 再描画
  }

  // 編集完了ボタンに呼び出されるメソッド
  public void finEdit(int index) {
    editEnabled = false;    // 編集モードを解除
    draws.set(index, editDraw.clone());   // 編集済みのDrawingオブジェクトのクローンを、ArrayListの元の位置に入れ替える
    layers.set(index, new Layer(draws.get(index), da02));   // layersも入れ替える
    draw = true;    // 再描画オンで、
    repaint();      // 再描画
  }

  // フィールド変数を初期値に戻すメソッド
  public void reset() {
    first = last = n = 0;
    cPositions = new ArrayList<Integer>();
    cPositions.add(0);
    backColor = Color.white;
  }

  //*****イベントリスナー*****
  // MouseListener
  public void mouseClicked(MouseEvent e){}
  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){}
  public void mousePressed(MouseEvent e){
    px = e.getX();    // 座標を取得
    py = e.getY();
    if (shiftFlag) {
      editEnabled = true;
    }
    if (mode==1 && !editEnabled) {    // フリーハンドモードで、かつ編集中でなければ
      arrays = new ArrayList<ArrayList<Integer>>();   // 筆跡座標保存用ArrayListを作成
      ArrayList<Integer> array = new ArrayList<Integer>();    // 1つの座標(x, y)を入れるArrayList(可変長でなくていい)
      array.add(px);    // 筆跡座標を保存
      array.add(py);
      arrays.add(array);
    }
  }
  public void mouseReleased(MouseEvent e){
    if (drawing) {    // 新規描画がオンなら(mouseDraggedでオンになる)
      int drawsSize = draws.size();   // draws.size()がfor文中で変わるので、ここに保存
      for (int i=last; i<drawsSize; i++) {  // 現在地以降のdraws,layersは
        draws.remove(last);   // 削除
        layers.remove(last);  // 削除
      }
      cPositionsDel();    // cPositionsを見直す
      if (mode==1) {    // 描画情報をもとにDrawingインスタンスを作成し、ArrayListに追加
        draws.add(new Drawing(px, py, x, y, color, bs, arrays));
      } else if (mode==2) {
        draws.add(new Drawing(px, py, x, y, color, bs));
      } else {
        draws.add(new Drawing(x, y, w, h, mode, color, bs));
      }
      layers.add(new Layer(draws.get(draws.size()-1), da02));   // 新しく作ったDrawingインスタンスをもとにLayerも作成、ArrayListに追加
      n++;    // 1枚後ろへ
      last = n;   // lastも
      draw = true;      // 再描画オンで、
      drawing = false;  // 新規描画オフで、
      repaint();        // 再描画
      System.out.println(draws.size() + ", " + layers.size());
    }
  }

  // MouseMotionListener
  public void mouseDragged(MouseEvent e){
    if (editEnabled) {    // 編集中なら
      x = e.getX();   // 座標取得
      y = e.getY();
      editDraw.moveTo(px, py, x, y);  // ドラックした方向へ編集中のDrawingを移動
      draw = true;    // 再描画オンで、
      repaint();      // 再描画
      px = x;   // 始点更新
      py = y;
    }
    else {    // 編集中でなければ
      if (mode==1) {    // フリーハンドモードなら
        x = e.getX();   // 座標取得
        y = e.getY();
        ArrayList<Integer> array = new ArrayList<Integer>();  // 1つの座標を保存するArrayListを作成
        array.add(x);   // x座標を保存
        array.add(y);   // y座標を保存
        arrays.add(array);    // 座標を保存
        repaint();    // 再描画
        px = x;   // 始点を更新
        py = y;
      } else if (mode==2) {   // 直線モードなら
        x = e.getX();   // 座標取得
        y = e.getY();
      } else {    // その他の描画モードなら
        x = e.getX();   // 座標を取得
        y = e.getY();
        w = Math.abs(px-x);   // 横幅wを絶対値に
        h = Math.abs(py-y);   // 縦幅hを絶対値に
        if (px<x) x = px;     // 始点を位置によって修正(※このモードでは、描画時に引数に渡す始点は(x,y))
        if (py<y) y = py;
        if (ctrlFlag) {   // Ctrlキーが押されていたら
          w = h = Math.max(w, h);   // w,hは、2つの最大値に揃える(円、正方形描画)
        }
      }
      draw = true;    // 再描画オンで、
      drawing = true; // 新規描画オンで、
      repaint();      // 再描画
    }
  }
  public void mouseMoved(MouseEvent e){}
}
