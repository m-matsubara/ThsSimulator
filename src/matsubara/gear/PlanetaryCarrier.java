package matsubara.gear;

import java.awt.Color;
import java.awt.Graphics;

/**
 * <p>タイトル:  Planetary Carrier</p>
 * <p>説明:    プラネタリギアのプラネタリキャリア用クラス</p>
 * <p>著作権: Copyright (c) 2002 matsubara masakazu </p>
 * @author m.matsubara
 * @version 1.0
 */

public class PlanetaryCarrier extends GearBase {
  int m_nWidth = 20;  //  プラネタリキャリアのフレームの幅（太さ）
  Color m_BackColor = Color.lightGray;

  /**
   * プラネタリギア用プラネタリキャリア
   * @param nPosX X座標
   * @param nPosY Y座標
   * @param nSize 半径
   */
  public PlanetaryCarrier() {
    super();
  }

  public PlanetaryCarrier(int nPosX, int nPosY, int nSize) {
    setPosX(nPosX);
    setPosY(nPosY);
    setSize(nSize);
  }

  /**
   * プラネタリキャリアのフレームの太さを取得する
   * @return プラネタリキャリアのフレームの太さ
   */
  public int getWidth() {
    return m_nWidth;
  }
  /**
   * プラネタリキャリアのフレームの太さを設定する
   * @param nWidth 新しいプラネタリキャリアのフレームの太さ
   */
  public void setWidth(int nWidth) {
    m_nWidth = nWidth;
  }

  /**
   * フレームのない部分の色を取得する
   * @return フレームのない部分の色
   */
  public Color getBackColor() {
    return m_BackColor;
  }
  /**
   * フレームのない部分の色を設定する
   * @param clBackColor フレームのない部分の色
   */
  public void setBackColor(Color clBackColor) {
    m_BackColor = clBackColor;
  }

  /**
   * プラネタリキャリアを描画
   * @param g 描画に使う Graphics オブジェクト
   */
  public void draw(Graphics g) {
    g.setColor(m_color1);
    g.fillArc((int)(m_nPosX - m_rSize - (double)m_nWidth / 2), (int)(m_nPosY - m_rSize - (double)m_nWidth / 2), (int)(m_rSize * 2 + m_nWidth), (int)(m_rSize * 2 + m_nWidth), 180 - (int)(m_rAngle / (2 * Math.PI) * 360), 180);
    g.setColor(m_color2);
    g.fillArc((int)(m_nPosX - m_rSize - (double)m_nWidth / 2), (int)(m_nPosY - m_rSize - (double)m_nWidth / 2), (int)(m_rSize * 2 + m_nWidth), (int)(m_rSize * 2 + m_nWidth), 360 - (int)(m_rAngle / (2 * Math.PI) * 360), 180);
    g.setColor(m_BackColor);
    g.fillArc((int)(m_nPosX - m_rSize + (double)m_nWidth / 2), (int)(m_nPosY - m_rSize + (double)m_nWidth / 2), (int)(m_rSize * 2 - m_nWidth), (int)(m_rSize * 2 - m_nWidth), 0, 360);
  }
}