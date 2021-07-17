package matsubara.gear;

import java.awt.Color;
import java.awt.Graphics;

/**
 * <p>タイトル:  Gear</p>
 * </p>説明:    一般的な歯車</p>
 * <p>著作権: Copyright (c) 2002 m.matsubara </p>
 * @author m.matsubara
 * @version 1.0
 */
public class Gear extends GearBase {

  int m_nGearCount = 50;
  int m_nGearDepth = 4;

  public Gear() {
  }
  /**
   * ギアオブジェクトを初期化
   * @param nPosX X座標
   * @param nPosY Y座標
   * @param rSize 半径
   * @param nGearCount ギアの歯の枚数
   * @param nGearDepth ギアの歯の深さ
   */
  public Gear(int nPosX, int nPosY, double rSize, int nGearCount, int nGearDepth) {
    setPosX(nPosX);
    setPosY(nPosY);
    setSize(rSize);
    setGearCount(nGearCount);
    setGearDepth(nGearDepth);
  }

  /**
   * ギアの歯の枚数を取得
   * @return ギアの歯の枚数
   */
  public int getGearCount() {
    return m_nGearCount;
  }
  /**
   * ギアの歯の枚数を設定
   * @param newGearCount 新しいギアの歯の枚数
   */
  public void setGearCount(int newGearCount) {
    m_nGearCount = newGearCount;
  }
  /**
   * ギアの歯の深さを取得
   * @return ギアの歯の深さ
   */
  public int getGearDepth() {
    return m_nGearDepth;
  }
  /**
   * ギアの歯の深さを設定
   * @param newGearDepth 新しいギアの歯の深さ
   */
  public void setGearDepth(int newGearDepth) {
    m_nGearDepth = newGearDepth;
  }

  /**
   * ギアを描画
   * @param g 描画に使う Graphics オブジェクト
   */
  public void draw(Graphics g) {
    int[] nxX = new int[4], nxY = new int[4];
    double rGearAngle;  //  ひとつの歯あたりの角度（ラジアン）
    double rIdx;
    double rSizeH = m_rSize + (double)m_nGearDepth / 2;  //  ギアの山部分の高さ
    double rSizeL = m_rSize - (double)m_nGearDepth / 2;  //  ギアの谷部分の高さ
    rGearAngle = 2 * Math.PI / m_nGearCount;  //  1つのギアあたりの角度
    nxX[3] = getPosX();
    nxY[3] = getPosY();

    //  半円（Color1）
    g.setColor(m_color1);
    //  歯の谷１
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
      g.fillPolygon(nxX, nxY, 4);
    }
    g.fillArc(m_nPosX - (int)rSizeL, m_nPosY - (int)rSizeL, (int)(rSizeL * 2), (int)(rSizeL * 2), 180 - (int)(m_rAngle / (2 * Math.PI) * 360), 180);

    //  半円（Color2）
    g.setColor(m_color2);
    if (getGearCount() % 2 == 1) {
      //  歯の数が奇数のとき、歯の中央で色が分かれるための対処
      nxX[0] = rotateX(rSizeL - 1, rIdx - rGearAngle / 2);
      nxY[0] = rotateY(rSizeL - 1, rIdx - rGearAngle / 2);
      g.fillPolygon(nxX, nxY, 4);
    }
    //  歯の谷２
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
      g.fillPolygon(nxX, nxY, 4);
    }
    g.fillArc(m_nPosX - (int)rSizeL, m_nPosY - (int)rSizeL, (int)(rSizeL * 2), (int)(rSizeL * 2), 360 - (int)(m_rAngle / (2 * Math.PI) * 360), 180);

    //  ギアの軸
    int nAxles = 14;
    if (getSize() < 14)
      nAxles = (int)getSize(); //  ギアが小さい場合は半径と同じ大きさ
/*
    g.setColor(Color.darkGray);
    g.fillArc(m_nPosX - nAxles / 2, m_nPosY - nAxles / 2, nAxles, nAxles, 0, 360);
    g.setColor(Color.black);
    g.drawArc(m_nPosX - nAxles / 2, m_nPosY - nAxles / 2, nAxles, nAxles, 0, 360);
*/
    g.setColor(Color.black);
    g.fillArc(m_nPosX - nAxles / 2, m_nPosY - nAxles / 2, nAxles, nAxles, 0, 360);
    g.setColor(Color.darkGray);
    g.fillArc(m_nPosX - (nAxles - 2) / 2, m_nPosY - (nAxles - 2) / 2, (nAxles - 2), (nAxles - 2), 0, 360);

  }
}