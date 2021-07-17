package matsubara.graphics;

import java.awt.Color;
import java.awt.Graphics;

/**
 * <p>タイトル:  Line Base</p>
 * <p>説明:    ビジュアルな線分 オブジェクトの基礎・Progress Line のベースクラスとして用意される</p>
 * <p>著作権: Copyright (c) 2002 m.matsubara </p>
 * @author m.matsubara
 * @version 1.0
 */

abstract public class LineBase {
  int m_nStartX = 0;
  int m_nStartY = 0;
  int m_nEndX = 0;
  int m_nEndY = 0;
  Color m_color = Color.blue;

  /**
   * LineBase を初期化
   * @param nStartX 開始X座標
   * @param nStartY 開始Y座標
   * @param nEndX 終了X座標
   * @param nEndY 終了Y座標
   * @param color 線分の色
   */
  public LineBase(int nStartX, int nStartY, int nEndX, int nEndY, Color color) {
    m_nStartX = nStartX;
    m_nStartY = nStartY;
    m_nEndX = nEndX;
    m_nEndY = nEndY;
    m_color = color;
  }

  /**
   * 線分を表示する
   * @param g 表示に使う Graphics オブジェクト
   */
  abstract public void draw(Graphics g);

  /**
   * 線分を表示する
   * サブクラスで使用するために用意されている
   * @param g 表示に使う Graphics オブジェクト
   */
  public void drawArrow(Graphics g) {
    g.setColor(m_color);
    g.drawLine(m_nStartX, m_nStartY, m_nEndX, m_nEndY);
  }

  /**
   * 開始X座標を設定する
   * @param nStartX 新しい開始X座標
   */
  public void setStartX(int nStartX) {
    m_nStartX = nStartX;
  }
  /**
   * 開始X座標を取得する
   * @return 開始X座標
   */
  public int getStartX() {
    return m_nStartX;
  }
  /**
   * 開始Y座標を設定する
   * @param nStartY 新しい開始Y座標
   */
  public void setStartY(int nStartY) {
    m_nStartY = nStartY;
  }
  /**
   * 開始Y座標を取得する
   * @return 開始Y座標
   */
  public int getStartY() {
    return m_nStartY;
  }
  /**
   * 終了X座標を設定する
   * @param nStartX 新しい終了X座標
   */
  public void setEndX(int nEndX) {
    m_nEndX = nEndX;
  }
  /**
   * 終了X座標を取得する
   * @return 終了X座標
   */
  public int getEndX() {
    return m_nEndX;
  }
  /**
   * 終了Y座標を設定する
   * @param nStartY 新しい終了Y座標
   */
  public void setEndY(int nEndY) {
    m_nEndY = nEndY;
  }
  /**
   * 終了Y座標を取得する
   * @return 終了Y座標
   */
  public int getEndY() {
    return m_nEndY;
  }
  /**
   * 描画の色を設定する
   * @param color 新しい描画の色
   */
  public void setColor(Color color) {
    m_color = color;
  }
  /**
   * 描画の色を取得する
   * @return 描画の色
   */
  public Color getColor() {
    return m_color;
  }
}