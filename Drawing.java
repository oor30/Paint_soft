import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.io.Serializable;

// 1つの図形の描画に必要な情報を保管し、他クラスから渡されるペンなどを用いて、情報をもとに描画するクラス。
// 各変数はメソッドから変更可能で、MyCanvasクラス内の編集用Drawingオブジェクト"editDraw"で使われる
// このクラスはプロジェクトのセーブデータに必要なオブジェクトの1つなので、Serializableをimplementsしている
// フィールド変数にシリアライズ不可能な"BasicStroke型"が含まれているため、transient修飾子をつけ、float型のboldで代用している
// また、このクラスのインスタンスを編集するときcloneを作成する必要があるので、Cloneableをimplementsしている
class Drawing implements Cloneable, Serializable {
  int px, py, x, y, w, h;   // 始点座標、終点座標、幅、高さ
  int mode;   // 描画モード
  float bold;   // ペンの太さ(Serializable)
  Color color;    // ペンの色
  boolean visible, visible2;    // 可視、不可視
  ArrayList<ArrayList<Integer>> arrays = new ArrayList<ArrayList<Integer>>();   // フリーハンドの筆跡座標を保存するArrayList
  transient BasicStroke bs = new BasicStroke(1);    // ペンの太さ

  // コンストラクタ
  Drawing(int px, int py, int x, int y, Color color, BasicStroke bs, ArrayList<ArrayList<Integer>> arrays) {    // フリーハンド用
    this.px = px;
    this.py = py;
    this.x = x;
    this.y = y;
    mode=1;
    this.color = color;
    this.bs = bs;
    visible = true;
    visible2 = true;
    this.arrays = arrays;
    bold = bs.getLineWidth();   // ペンの太さを保存
  }
  Drawing(int px, int py, int x, int y, Color color, BasicStroke bs) {    // 直線用
    this.px = px;
    this.py = py;
    this.x = x;
    this.y = y;
    mode=2;
    this.color = color;
    this.bs = bs;
    visible = true;
    visible2 = true;
    bold = bs.getLineWidth();
  }
  Drawing(int x, int y, int w, int h, int mode, Color color, BasicStroke bs) {    // その他のモード用
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.mode = mode;
    this.color = color;
    this.bs = bs;
    visible = true;
    visible2 = true;
    bold = bs.getLineWidth();
  }

  // メソッド
  // 描画メソッド
  public void doPaint(Graphics gc, Graphics2D gc2) {    // 図形が可視なら描画
    if (visible && visible2) {
      needPaint(gc, gc2);
    }
  }
  public void needPaint(Graphics gc, Graphics2D gc2) {    // 図形を必ず描画
    gc.setColor(color);   // 色を設定
    bs = new BasicStroke(bold);   // 太さを設定
    gc2.setStroke(bs);
    if (mode==1) {    // フリーハンドモード
      for (int i=0; i<arrays.size()-1; i++) {   // 筆跡座標保存ArrayList内の全ての座標について
        gc.drawLine(arrays.get(i).get(0),   // 座標を繋げるように描画
        arrays.get(i).get(1),
        arrays.get(i+1).get(0),
        arrays.get(i+1).get(1));
      }
    }
    else if (mode==2) gc.drawLine(px, py, x, y);    // 直線モード
    else if (mode==3) gc.drawRect(x, y, w, h);    // 四角形モード
    else if (mode==4) gc.fillRect(x, y, w, h);    // 塗りつぶし四角形モード
    else if (mode==5) gc.drawOval(x, y, w, h);    // 楕円モード
    else if (mode==6) gc.fillOval(x, y, w, h);    // 塗りつぶし楕円モード
  }
  public void doPaint(Graphics gc, Graphics2D gc2, BasicStroke bs) {    // レイヤーのサムネイル用
    gc.setColor(color);
    bs = new BasicStroke((bs.getLineWidth()>10)? bs.getLineWidth():10);   // 線の太さが10以下なら10に設定し、デフォルメする
    gc2.setStroke(bs);
    if (mode==1) {
      for (int i=0; i<arrays.size()-1; i++) {
        gc.drawLine(arrays.get(i).get(0),
        arrays.get(i).get(1),
        arrays.get(i+1).get(0),
        arrays.get(i+1).get(1));
      }
    }
    else if (mode==2) gc.drawLine(px, py, x, y);
    else if (mode==3) gc.drawRect(x, y, w, h);
    else if (mode==4) gc.fillRect(x, y, w, h);
    else if (mode==5) gc.drawOval(x, y, w, h);
    else if (mode==6) gc.fillOval(x, y, w, h);
  }

  // 図形を動かすメソッド(編集中にキャンバスをドラッグすると呼び出される)
  public void moveTo(int px, int py, int x, int y) {
    int deltaX = x-px;    // ドラッグした距離
    int deltaY = y-py;
    if (mode==1) {        // フリーハンドモード
      for (int i=0; i<arrays.size(); i++) {       // 筆跡座標保存ArrayList内の全ての座標に対して
        int x_ = arrays.get(i).get(0) +deltaX;    // ドラッグ分だけ足した座標。一旦x_にいれる
        int y_ = arrays.get(i).get(1) +deltaY;
        arrays.get(i).set(0, new Integer(x_));    // x_をintのラッパークラスの形にして、新しい座標に入れ替える
        arrays.get(i).set(1, new Integer(y_));
      }
    } else {                // その他のモード
      this.px += deltaX;    // ドラッグ分だけ座標を変更
      this.py += deltaY;
      this.x += deltaX;
      this.y += deltaY;
    }
  }

  // 編集時、編集する図形に赤い枠を描画する(レイヤーの編集ボタンが押された時呼び出される。フリーハンド、直線は未対応)
  public void drawFrame(Graphics gc, Graphics2D gc2, MyCanvas mc, DrawingApli02 da) {
    if (mode>1) {
      gc.setColor(Color.red);   // 色を赤に
      BasicStroke bs2 = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[] {6}, 0);   // 線を破線に
      gc2.setStroke(bs2);
      gc.drawRect(x, y, w, h);    // 四角形を描画
    }
  }

  @Override
  public Drawing clone() {
    Drawing draw = null;    // 空っぽのDrawingオブジェクトを作成
    try {
      draw = (Drawing)super.clone();    // スーパークラス(Objectクラス)のcloneを呼び出し、Drawing型にキャストする。arrays以外のフィールド変数はクローン完了
      ArrayList<ArrayList<Integer>> arrays_ = new ArrayList<ArrayList<Integer>>();    // 2次元配列arraysは各配列ごとに写す必要があるみたい
      for (int i=0; i<this.arrays.size(); i++) {                            // drawのarraysとこのクラスのarraysを混同しないように、thisをつける
        ArrayList<Integer> array_ = new ArrayList<>(this.arrays.get(i));    // ディープコピー
        arrays_.add(array_);                                                // 新しいArrayListに追加
      }
      draw.arrays = arrays_;    // drawのarraysを設定
    } catch (Exception e) {
      // JOptionPane.showMessageDialog(da, "エラー：編集対象をクローンできませんでした");
    }
    return draw;    // cloneでできたdrawを返す
  }
}
