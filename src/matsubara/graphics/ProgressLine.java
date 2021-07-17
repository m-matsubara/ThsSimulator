package matsubara.graphics;

import java.awt.Color;
import java.awt.Graphics;

/**
 * <p>タイトル:  Progress Line</p>
 * <p>説明:    進捗を表示するライン・反転表示させることも出来ます。</p>
 * <p>著作権: Copyright (c) 2002 matsubara masakazu </p>
 * @author m.matsubara
 * @version 1.0
 */

public class ProgressLine extends LineBase {
  int m_nPosition = 0;
  boolean m_bReverse = false;
  int m_nMarkSize = 0;

  /**
   * 進捗表示線分の初期化
   * @param nStartX 開始X座標
   * @param nStartY 開始Y座標
   * @param nEndX 終了X座標
   * @param nEndY 終了Y座標
   * @param color 色
   */
  public ProgressLine(int nStartX, int nStartY, int nEndX, int nEndY, Color color) {
    super(nStartX, nStartY, nEndX, nEndY, color);
  }
  /**
   * 進捗表示線分の初期化
   * @param nStartX 開始X座標
   * @param nStartY 開始Y座標
   * @param nEndX 終了X座標
   * @param nEndY 終了Y座標
   * @param color 色
   * @param nMarkSize 進捗を示す●記号の大きさ
   */
  public ProgressLine(int nStartX, int nStartY, int nEndX, int nEndY, Color color, int nMarkSize) {
    super(nStartX, nStartY, nEndX, nEndY, color);
    setMarkSize(nMarkSize);
  }

  /**
   * 進捗表示線分を描画する
   * @param g 描画に使う Graphics オブジェクト
   */
  public void draw(Graphics g) {
    /**@todo: この matsubara.arrow.ArrowBase abstract メソッドを実装*/
    drawArrow(g);
    int nX, nY;
    if (isReverse() == false) {
      nX = getStartX() + (getEndX() - getStartX()) * getPosition() / 100;
      nY = getStartY() + (getEndY() - getStartY()) * getPosition() / 100;
    }
    else {
      nX = getStartX() + (getEndX() - getStartX()) * (100 - getPosition()) / 100;
      nY = getStartY() + (getEndY() - getStartY()) * (100 - getPosition()) / 100;
    }
    g.setColor(Color.white);
    g.fillArc(nX - m_nMarkSize / 2, nY - m_nMarkSize / 2, m_nMarkSize, m_nMarkSize, 0, 360);
  }

  /**
   * 現在の進捗位置を設定する
   * @param nPosition 現在の進捗位置(0～100)
   */
  public void setPosition(int nPosition) {
    m_nPosition = nPosition % 100;
  }
  /**
   * 現在の進捗位置を取得する
   * @return 現在の進捗位置(n/100)
   */
  int  getPosition() {
    return m_nPosition;
  }
  /**
   * 進捗を進める
   * @param n 進める数
   */
  public void movePosition(int n) {
    m_nPosition = (m_nPosition + n) % 100;
    if (m_nPosition < 0)
      m_nPosition += 100;
  }
  /**
   * 進捗表示を逆転する
   * 開始座標と終了座標を入れ替える
   * @param bReverse 入れ替えるとき true
   */
  public void setReverse(boolean bReverse) {
    m_bReverse = bReverse;
  }
  /**
   * 進捗表示が逆転しているか取得する
   * @return 進捗表示が逆転しているとき true
   */
  public boolean isReverse() {
    return m_bReverse;
  }
  /**
   * 進捗マークサイズを取得する
   * @return 進捗マークサイズ
   */
  public int getMarkSize() {
    return m_nMarkSize;
  }
  /**
   * 進捗マークサイズを設定する
   * @param nMarkSize 新しい進捗マークサイズ
   */
  public void setMarkSize(int nMarkSize) {
    m_nMarkSize = nMarkSize;
  }
}