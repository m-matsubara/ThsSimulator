package matsubara.gear;

import java.awt.Color;
import java.awt.Graphics;

/**
 * <p>タイトル:  RingGear</p>
 * <p>説明:    内歯歯車/サイズはギアの部分までです。描画の際にはこれより大きく表示されるので注意してください。</p>
 * <p>著作権: Copyright (c) 2002 matsubara masakazu </p>
 * @author m.matsubara
 * @version 1.0
 */

public class RingGear extends Gear {
  int m_nWidth = 20;  //  リングギアのフレームの幅（太さ）
  public RingGear() {
  }
  /**
   * リングギアを初期化
   * @param nPosX X座標
   * @param nPosY Y座標
   * @param nSize 半径
   * @param nGearCount ギアの歯数
   * @param nGearDepth ギアの歯の深さ
   */
  public RingGear(int nPosX, int nPosY, int nSize, int nGearCount, int nGearDepth) {
    setPosX(nPosX);
    setPosY(nPosY);
    setSize(nSize);
    setGearCount(nGearCount);
    setGearDepth(nGearDepth);
  }

  /**
   * リングギアを描画する
   * @param g 描画に使う Graphics オブジェクト
   */
  public void draw(Graphics g) {
    int[] nxX = new int[4], nxY = new int[4];
    double rGearAngle;  //  ひとつの歯あたりの角度（ラジアン）
    double rIdx;
    double rSizeH = m_rSize - (double)m_nGearDepth / 2;  //  ギアの山部分の高さ
    double rSizeL = m_rSize + (double)m_nGearDepth / 2;  //  ギアの谷部分の高さ
    rGearAngle = 2 * Math.PI / m_nGearCount;  //  1つのギアあたりの角度

    g.setColor(m_color1);
    g.fillArc((int)(m_nPosX - rSizeL - m_nWidth / 2), (int)(m_nPosY - rSizeL - m_nWidth / 2), (int)(rSizeL * 2 + m_nWidth), (int)(rSizeL * 2 + m_nWidth), 180 - (int)(m_rAngle / (2 * Math.PI) * 360), 180);
    g.setColor(m_color2);
    g.fillArc((int)(m_nPosX - rSizeL - m_nWidth / 2), (int)(m_nPosY - rSizeL - m_nWidth / 2), (int)(rSizeL * 2 + m_nWidth), (int)(rSizeL * 2 + m_nWidth), 360 - (int)(m_rAngle / (2 * Math.PI) * 360), 180);

    g.setColor(Color.lightGray);
    g.drawArc((int)(m_nPosX - rSizeL - m_nWidth / 2), (int)(m_nPosY - rSizeL - m_nWidth / 2), (int)(rSizeL * 2 + m_nWidth), (int)(rSizeL * 2 + m_nWidth), 0, 360);

    //  中をくりぬく
    g.setColor(Color.lightGray);
//    g.fillArc(m_nPosX - rSizeL + 1, m_nPosY - rSizeL + 1, rSizeH * 2 + m_nWidth / 2, rSizeH * 2 + m_nWidth / 2, 0, 360);
    g.fillArc((int)(m_nPosX - rSizeL), (int)(m_nPosY - rSizeL), (int)(rSizeL * 2), (int)(rSizeL * 2), 0, 360);


    //  歯の部分・半円（Color1）
    g.setColor(m_color1);
    nxX[2] = rotateX(rSizeL, 0);
    nxY[2] = rotateY(rSizeL, 0);
    for (rIdx = 0.0; rIdx < Math.PI - 0.000001; rIdx += rGearAngle) {
      //  歯の谷１
      nxX[0] = nxX[2];
      nxY[0] = nxY[2];
      //  歯の山
      nxX[1] = rotateX(rSizeH, rIdx + rGearAngle / 2);
      nxY[1] = rotateY(rSizeH, rIdx + rGearAngle / 2);
      //  歯の谷２
      nxX[2] = rotateX(rSizeL, rIdx + rGearAngle);
      nxY[2] = rotateY(rSizeL, rIdx + rGearAngle);
      //
      nxX[3] = rotateX(rSizeL + 4, rIdx + rGearAngle / 2);
      nxY[3] = rotateY(rSizeL + 4, rIdx + rGearAngle / 2);
      g.fillPolygon(nxX, nxY, 4);
    }

    //  歯の部分・半円（Color2）
    g.setColor(m_color2);
    if (getGearCount() % 2 == 1) {
      //  歯の数が奇数のとき、歯の中央で色が分かれるための対処
      nxX[0] = rotateX(rSizeL, rIdx - rGearAngle / 2);
      nxY[0] = rotateY(rSizeL, rIdx - rGearAngle / 2);
      g.fillPolygon(nxX, nxY, 3);
    }
    nxX[2] = rotateX(rSizeL, rIdx);
    nxY[2] = rotateY(rSizeL, rIdx);
    for (rIdx = rIdx; rIdx < 2 * Math.PI - 0.000001; rIdx += rGearAngle) {
      //  歯の谷１
      nxX[0] = nxX[2];
      nxY[0] = nxY[2];
      //  歯の山
      nxX[1] = rotateX(rSizeH, rIdx + rGearAngle / 2);
      nxY[1] = rotateY(rSizeH, rIdx + rGearAngle / 2);
      //  歯の谷２
      nxX[2] = rotateX(rSizeL, rIdx + rGearAngle);
      nxY[2] = rotateY(rSizeL, rIdx + rGearAngle);
      //
      nxX[3] = rotateX(rSizeL + 4, rIdx + rGearAngle / 2);
      nxY[3] = rotateY(rSizeL + 4, rIdx + rGearAngle / 2);
      g.fillPolygon(nxX, nxY, 4);
    }
  }
}