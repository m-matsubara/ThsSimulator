package matsubara.gear;

import java.awt.Color;
import java.awt.Graphics;

/**
 * <p>タイトル:  Gear Box</p>
 * <p>説明:    複数の歯車を組み合わせて1つのセットにする。
 * 本当は歯車をリスト構造にして1つの歯車を動かすとすべての歯車
 * が連動するようにしたかったのだが、無理だった（悲しい）。</p>
 * <p>著作権: Copyright (c) 2002 m.matsubara </p>
 * @author m.matsubara
 * @version 1.0
 */

public abstract class GearBox extends GearBase {

  public GearBox() {
  }

  /**
   * ギアボックスを描画
   * スーパークラス(Gear)およびサブクラスを参照のこと
   * @param g 描画に使用する Graphics オブジェクト
   */
  public abstract void draw(Graphics g);
}