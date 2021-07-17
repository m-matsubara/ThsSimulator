package matsubara.gear;

/**
 * <p>タイトル: Car Controller</p>
 * <p>説明: プラネタリギア用のエラー</p>
 * <p>著作権: Copyright (c) 2002 m.matsubara </p>
 * @author m.matsubara
 * @version 1.0.2
 */
public class EPlanetaryGear extends Exception {
  /**
   * プラネタリギアのエラーを作成する
   * @param sMessage エラーメッセージ
   */
  EPlanetaryGear(String sMessage) {
    super(sMessage);
  }
}

