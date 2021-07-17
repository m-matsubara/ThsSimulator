package matsubara.gear;

import java.awt.Color;
import java.awt.Graphics;
import matsubara.gear.EPlanetaryGear;

/**
 * <p>タイトル: </p>
 * <p>説明: </p>
 * <p>著作権: Copyright (c) 2002</p>
 * <p>会社名: </p>
 * @author 未入力
 * @version 1.0
 */

public class PlanetaryGearW extends PlanetaryGear {
  Gear mx_piniGear2[];

  public PlanetaryGearW(int nX, int nY, int nSize, int nSunGear, int nPinionGear, int nRingGear, int nGearDepth, int nPinionGearNumber) throws EPlanetaryGear {
    super(nX, nY, nSize, nSunGear, nPinionGear, nRingGear, nGearDepth, nPinionGearNumber);
    double rPinionGearSize = (nRingGear - nSunGear) / 2 / 2;

    mx_piniGear2 = new Gear[nPinionGearNumber];

    //  ピニオンギア(外側)
    for (int nIdx = 0; nIdx < mx_piniGear.length; nIdx++) {
      //  角度補正
      mx_piniGear[nIdx].setAngle((2 * Math.PI) * nIdx / mx_piniGear.length
        + (2 * Math.PI) / mx_piniGear[nIdx].getGearCount() / 2
        - (2 * Math.PI) / mx_piniGear[nIdx].getGearCount() * (m_ringGear.getGearCount() % nPinionGearNumber) * nIdx / nPinionGearNumber
      );
    }

    //  ピニオンギア(内側)
    for (int nIdx = 0; nIdx < mx_piniGear2.length; nIdx++) {
      mx_piniGear2[nIdx] = new Gear(0, 0, rPinionGearSize, nPinionGear, nGearDepth);
      mx_piniGear2[nIdx].setColor1(Color.orange);
      mx_piniGear2[nIdx].setColor2(Color.darkGray);
      //  角度補正
      if (nPinionGear % 2 == 0) {
        mx_piniGear2[nIdx].setAngle((2 * Math.PI) * nIdx / mx_piniGear2.length
          - (2 * Math.PI) / mx_piniGear2[nIdx].getGearCount() * (m_ringGear.getGearCount() % nPinionGearNumber) * nIdx / nPinionGearNumber
        );
      }
      else {
        mx_piniGear2[nIdx].setAngle((2 * Math.PI) * nIdx / mx_piniGear2.length
          + (2 * Math.PI) / mx_piniGear2[nIdx].getGearCount() / 2
          - (2 * Math.PI) / mx_piniGear2[nIdx].getGearCount() * (m_ringGear.getGearCount() % nPinionGearNumber) * nIdx / nPinionGearNumber
        );
      }
    }

    move(0);  //  ピニオンギアを正しい位置に配置するため

    if (nPinionGear % 2 == 0)
      m_sunGear.setAngle(2 * Math.PI / (m_sunGear.getGearCount() * 2));
  }


  /**
   * 歯車の歯数などに問題がないかチェック
   * @param nSunGear サンギアのサイズ
   * @param nRingGear リングギアのサイズ
   * @param rPinionGearSize ピニオンギアのサイズ
   * @param nPinionGearNumber ピニオンギアの個数
   * @param nGearDepth 葉の深さ
   * @param rPlanetaryCarrierSize プラネタリキャリアのサイズ
   * @throws EPlanetaryGear パラメータに問題があった場合に例外を生成
   */
  public void gearCheck(int nSunGear, int nRingGear, double rPinionGearSize, int nPinionGearNumber, int nGearDepth, double rPlanetaryCarrierSize) throws EPlanetaryGear {
    if ((nRingGear + nSunGear) % nPinionGearNumber != 0)
      throw new EPlanetaryGear("歯車がかみ合いません。");
    if (nRingGear <= nSunGear)
      throw new EPlanetaryGear("リングギアはサンギアより大きくなければなりません。");
  }


  /**
   * プラネタリギアを描画
   * @param g 描画に使う Graphics オブジェクト
   */
  public void draw(Graphics g) {
    super.draw(g);
    for (int nIdx = 0; nIdx < mx_piniGear2.length; nIdx++) {
      mx_piniGear2[nIdx].draw(g);
    }
  }




  /**
   * 設定されているギアスピードで歯車を回転させる
   * 発電機回転数＝（エンジン回転数×（リンクギアサイズ＋サンギアサイズ）－モーター回転数×リンクギアサイズ）÷サンギアサイズ
   * @param nMilliSec 経過秒数
   */
  public void move(double nMilliSec) {
    int nRingGearCount = m_ringGear.getGearCount();
    int nSunGearCount = m_sunGear.getGearCount();
//    double rPinionGearSize = (m_ringGear.getSize() - m_sunGear.getSize()) / 2;
    double rPlanetaryCarrierSize = (m_sunGear.getSize() + m_ringGear.getSize()) / 2;
    double rSunGearSpeed = getSunGearSpeed();
//    rSunGearSpeed = 0;

    m_sunGear.setAngle(m_sunGear.getAngle() + rSunGearSpeed / 60 / 1000 * nMilliSec * 2 * Math.PI);
    m_plaCarrier.setAngle(m_plaCarrier.getAngle() + m_rPlaCarrierSpeed / 60 / 1000 * nMilliSec * 2 * Math.PI);
    m_ringGear.setAngle(m_ringGear.getAngle() + m_rRingGearSpeed / 60 / 1000 * nMilliSec * 2 * Math.PI);

    //  ピニオンギア(外側)
    double rPinionGearSpeed = m_rPlaCarrierSpeed + (m_rRingGearSpeed - m_rPlaCarrierSpeed) * m_ringGear.getGearCount() / mx_piniGear[0].getGearCount();
    for (int nIdx = 0; nIdx < mx_piniGear.length; nIdx++) {
      //  位置
      mx_piniGear[nIdx].setPosX(m_plaCarrier.rotateX(rPlanetaryCarrierSize + mx_piniGear[nIdx].getSize(), (2 * Math.PI) * nIdx / mx_piniGear.length));
      mx_piniGear[nIdx].setPosY(m_plaCarrier.rotateY(rPlanetaryCarrierSize + mx_piniGear[nIdx].getSize(), (2 * Math.PI) * nIdx / mx_piniGear.length));
      //  回転
      mx_piniGear[nIdx].setAngle( mx_piniGear[nIdx].getAngle()  + rPinionGearSpeed / 60 / 1000 * nMilliSec * 2 * Math.PI);
    }

    //  ピニオンギア(内側)
    double rPinionGearSpeed2 = m_rPlaCarrierSpeed - (m_rRingGearSpeed - m_rPlaCarrierSpeed) * m_ringGear.getGearCount() / mx_piniGear[0].getGearCount();
    if (mx_piniGear2 != null) {
      for (int nIdx = 0; nIdx < mx_piniGear2.length; nIdx++) {
        //  位置
        mx_piniGear2[nIdx].setPosX(m_plaCarrier.rotateX(rPlanetaryCarrierSize - mx_piniGear2[nIdx].getSize(), (2 * Math.PI) * nIdx / mx_piniGear2.length));
        mx_piniGear2[nIdx].setPosY(m_plaCarrier.rotateY(rPlanetaryCarrierSize - mx_piniGear2[nIdx].getSize(), (2 * Math.PI) * nIdx / mx_piniGear2.length));
        //  回転
        mx_piniGear2[nIdx].setAngle(mx_piniGear2[nIdx].getAngle() + rPinionGearSpeed2 / 60 / 1000 * nMilliSec * 2 * Math.PI);
      }
    }
  }

  /**
   * プラネタリギアのサイズ（半径）を設定
   * @param nSize 半径
   */
  public void setSize(double rSize) {
    super.setSize(rSize);
    for (int nIdx = 0; nIdx < mx_piniGear.length; nIdx++) {
      mx_piniGear[nIdx].setSize((m_ringGear.getSize() - m_sunGear.getSize()) / 2 / 2);
      mx_piniGear2[nIdx].setSize((m_ringGear.getSize() - m_sunGear.getSize()) / 2 / 2);
    }
    move(0);
  }

  /**
   * サンギアの回転数を取得
   * @return サンギアの回転数(rpm)
   */
  public double getSunGearSpeed() {
    double rRingGearCount = m_ringGear.getGearCount();
    double rSunGearCount = m_sunGear.getGearCount();

    return m_rPlaCarrierSpeed + (m_rRingGearSpeed - m_rPlaCarrierSpeed) * rRingGearCount / rSunGearCount;

//    return (m_rPlaCarrierSpeed * (rRingGearCount + rSunGearCount) - m_rRingGearSpeed * rRingGearCount) / rSunGearCount;
  }

  /**
   * サンギアの回転数を設定
   * サンギアの回転数にあわせてプラネタリキャリアの回転数が変化する
   * @param rSunGearSpeed 新しいサンギアの回転数(rpm)
   */
  public void setSunGearSpeed(double rSunGearSpeed) {
    double rRingGearCount = m_ringGear.getGearCount();
    double rSunGearCount = m_sunGear.getGearCount();
    setPlaCarrierSpeed((int)(m_rRingGearSpeed * rRingGearCount - rSunGearSpeed * rSunGearCount) / (rRingGearCount - rSunGearCount));
  }


}