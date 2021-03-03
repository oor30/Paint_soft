import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.ArrayList;

// 色を選択するボタンをまとめるパネルのクラス
// 選択中のボタンcurrentColorButtonと、過去10回に選択した色のボタンbuttonArrayから成る
// currentColorButtonを押すとJColorChooserダイアログが開き、色を選択できる
// 色を変更後、キャンバスに描画をすると、buttonArrayに新しい色のボタンが追加される(同色は追加されない)
class ColorButtons extends JPanel {
  // フィールド変数
  Color color = Color.black;    // ボタンの色(初期設定：黒)
  ArrayList<Color> colorArray = new ArrayList<Color>();
  ArrayList<JButton> buttonArray = new ArrayList<JButton>();    // 選択された色を保存＆パレット表示
  JButton currentColorButton = new JButton();     // 今の色＆JColorChooser起動
  LineBorder border;    // ボタンのボーダー用

  // コンストラクタ
  ColorButtons () {
    setBounds(0, 420, 285, 60);   // 初期位置、サイズ設定
    setLayout(null);              // レイアウトマネージャー無効
    border = new LineBorder(new Color(220, 220, 220), 2);   // ボーダー設定
    currentColorButton.setBounds(0, 0, 60, 60);       // 初期位置、サイズ設定
    currentColorButton.setContentAreaFilled(false);   // 背景を無効
    currentColorButton.setContentAreaFilled(true);    // 背景を有効(こうしないとMacでは色が変わらない)
    currentColorButton.setBorderPainted(true);        // ボーダーを有効
    currentColorButton.setBackground(color);          // ボタンの色を設定
    currentColorButton.setBorder(border);             // ボーダーを設定
    add(currentColorButton);                          // このパネルにcurrentColorButtonを追加
  }

  // メソッド
  // buttonArrayに新しい色のボタンを追加し、パレットを再配置するメソッド
  public void addColor(Color color) {
    this.color = color;                 // 新しい色を取得
    colorArray.add(0, color);
    JButton button = new JButton();     // 新しいボタンを作成
    button.setContentAreaFilled(false); // 背景を無効
    button.setContentAreaFilled(true);  // 背景を有効(こうしないとMacでは色が変わらない)
    button.setBorderPainted(true);      // ボーダーを有効
    button.setBorder(border);           // ボーダーを設定
    button.setBackground(color);        // ボタンの色を設定
    buttonArray.add(0, button);         // buttonArrayの先頭に挿入

    for (int i=0; i<10 && i<buttonArray.size(); i++) {    // 過去10回に選んだ色のボタンに関し
      buttonArray.get(i).setBounds(60+30*(i%5), 30*(i/5), 30, 30);    // 初期位置を再設定
      add(buttonArray.get(i));       // このパネルに追加する
    }
  }
}
