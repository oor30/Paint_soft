import java.util.ArrayList;
import java.awt.*;
import java.io.Serializable;

// プロジェクト保存用データのクラス
// Drawingクラス型を含め、フィールド変数にはシリアライズ可能なものしかない
// これらフィールド変数をセーブすることで、レイヤー情報も復元できる
public class SaveData implements Serializable {
  // フィールド変数
  ArrayList<Drawing> draws;
  int first, last, n, width, height;
  Color backColor;
  ArrayList<Integer> cPositions;

  // コンストラクタ
  // プロジェクトを保存する時に呼び出される
  SaveData (ArrayList<Drawing> draws, MyCanvas mc) {
    this.draws = draws;   // このクラスのフィールド変数を、mc内のフィールド変数に置き換える
    first = mc.first;
    last = mc.last;
    n = mc.n;
    width = mc.d.width;
    height = mc.d.height;
    backColor = mc.backColor;
    cPositions = mc.cPositions;
  }

  // メソッド
  // デシリアライズした後、プロジェクト復元のため呼び出される
  public void recover (MyCanvas mc) {
    mc.first = first;   // mc内のフィールド変数を、このクラスのフィールド変数に置き換える
    mc.last = last;
    mc.n = n;
    mc.draws = draws;
    mc.d.width = width;
    mc.d.height = height;
    mc.backColor = backColor;
    mc.cPositions = cPositions;
  }
}
