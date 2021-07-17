package matsubara.gear;

import java.awt.Color;
import java.awt.Graphics;



/**
 * タイトル:  Planetary Gear
 * 説明:    プラネタリギア用汎用クラス
 * <p>著作権: Copyright (c) 2002 matsubara masakazu </p>
 * @author m.matsubara
 * @version 1.0
 */
public class PlanetaryGear extends GearBase {
  Gear             m_ringGear;
  PlanetaryCarrier m_plaCarrier;
  Gear             m_sunGear;
  Gear             mx_piniGear[];

  double m_rRingGearSpeed = 0;
  double m_rPlaCarrierSpeed = 0;

  /**
   * プラネタリギアを初期化
   * @param nX X座標
   * @param nY Y座標
   * @param nSize 半径
   * @param nSunGear サンギアの歯数
   * @param nPinionGear ピニオンギアの歯数（プラネタリキャリアに4つ付属）
   * @param nRingGear リングギアの歯数
   * @param nGearDepth ギアの歯の深さ
   * @param nPinionGearNumber ピニオンギアの個数（通常は３以上にしてください）
   * @throws EPlanetaryGear プラネタリギアの成立条件に違反するとき例外を発生させる
   */
  public PlanetaryGear(int nX, int nY, int nSize, int nSunGear, int nPinionGear, int nRingGear, int nGearDepth, int nPinionGearNumber) throws EPlanetaryGear {
    double rPinionGearSize = (nRingGear - nSunGear) / 2 * nSize / nRingGear;
    double rPlanetaryCarrierSize = nSunGear * nSize / nRingGear + rPinionGearSize;

    mx_piniGear = new Gear[nPinionGearNumber];

    gearCheck(nSunGear, nRingGear, rPinionGearSize, nPinionGearNumber, nGearDepth, rPlanetaryCarrierSize);

    m_ringGear   = new RingGear(        nX, nY, nRingGear * nSize / nRingGear,   nRingGear, nGearDepth);
    m_sunGear    = new Gear(            nX, nY, nSunGear * nSize / nRingGear   , nSunGear   , nGearDepth);
    m_plaCarrier = new PlanetaryCarrier(nX, nY, (int)rPlanetaryCarrierSize);
    //  ピニオンギア
    for (int nIdx = 0; nIdx < mx_piniGear.length; nIdx++) {
      mx_piniGear[nIdx] = new Gear(0, 0, rPinionGearSize, nPinionGear, nGearDepth);
      mx_piniGear[nIdx].setColor1(Color.orange);
      mx_piniGear[nIdx].setColor2(Color.darkGray);
      //  角度補正
      mx_piniGear[nIdx].setAngle((2 * Math.PI) * nIdx / mx_piniGear.length
        + (2 * Math.PI) / mx_piniGear[nIdx].getGearCount() / 2
        - (2 * Math.PI) / mx_piniGear[nIdx].getGearCount() * (m_ringGear.getGearCount() % nPinionGearNumber) * nIdx / nPinionGearNumber
      );
    }

    move(0);  //  ピニオンギアを正しい位置に配置するため

    //  プラネタリキャリアの色
    m_plaCarrier.setColor2(Color.red);
    //  サンギアの色
    m_sunGear.setColor2(Color.blue);
    //  リングギアの色
    m_ringGear.setColor2(new Color(0, 0xbb, 0));

    m_plaCarrier.setWidth((int)(mx_piniGear[0].getSize()));

    if (nPinionGear % 2 == 1)
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
    if (rPinionGearSize * 2 + nGearDepth >= 2 * rPlanetaryCarrierSize * Math.sin(Math.PI / nPinionGearNumber))
      throw new EPlanetaryGear("ピニオンギア同士がぶつかり合います。");
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
    m_ringGear.draw(g);
    m_plaCarrier.draw(g);
    m_sunGear.draw(g);
    for (int nIdx = 0; nIdx < mx_piniGear.length; nIdx++)
      mx_piniGear[nIdx].draw(g);
  }

  /**
   * 設定されているギアスピードで歯車を回転させる
   * 発電機回転数＝（エンジン回転数×（リンクギアサイズ＋サンギアサイズ）－モーター回転数×リンクギアサイズ）÷サンギアサイズ
   * @param nMilliSec 経過秒数
   */
  public void move(double nMilliSec) {
    int nRingGearCount = m_ringGear.getGearCount();
    int nSunGearCount = m_sunGear.getGearCount();
    double rPlanetaryCarrierSize = (m_sunGear.getSize() + m_ringGear.getSize()) / 2;
    double rSunGearSpeed = (m_rPlaCarrierSpeed * (nRingGearCount + nSunGearCount) - m_rRingGearSpeed * nRingGearCount) / (double)nSunGearCount;

    m_sunGear.setAngle(m_sunGear.getAngle() + rSunGearSpeed / 60 / 1000 * nMilliSec * 2 * Math.PI);
    m_plaCarrier.setAngle(m_plaCarrier.getAngle() + m_rPlaCarrierSpeed / 60 / 1000 * nMilliSec * 2 * Math.PI);
    m_ringGear.setAngle(m_ringGear.getAngle() + m_rRingGearSpeed / 60 / 1000 * nMilliSec * 2 * Math.PI);

    //  ピニオンギア
    double rPinionGearSpeed = m_rPlaCarrierSpeed + (m_rRingGearSpeed - m_rPlaCarrierSpeed) * m_ringGear.getGearCount() / mx_piniGear[0].getGearCount();
    for (int nIdx = 0; nIdx < mx_piniGear.length; nIdx++) {
      //  位置
      mx_piniGear[nIdx].setPosX(m_plaCarrier.rotateX(rPlanetaryCarrierSize, (2 * Math.PI) * nIdx / mx_piniGear.length));
      mx_piniGear[nIdx].setPosY(m_plaCarrier.rotateY(rPlanetaryCarrierSize, (2 * Math.PI) * nIdx / mx_piniGear.length));
      //  回転
      mx_piniGear[nIdx].setAngle(mx_piniGear[nIdx].getAngle() + rPinionGearSpeed / 60 / 1000 * nMilliSec * 2 * Math.PI);
    }
  }

  /**
   * リングギアの回転数を設定する
   * リングギアの回転数にあわせてサンギアの回転数が変化する
   * @param rRingGearSpeed 新しいリングギアの回転数(rpm)
   */
  public void setRingGearSpeed(double rRingGearSpeed) {
    m_rRingGearSpeed = rRingGearSpeed;
  }
  /**
   * リングギアの回転数を取得する
   * @return リングギアの回転数(rpm)
   */
  public double getRingGearSpeed() {
    return m_rRingGearSpeed;
  }
  /**
   * プラネタリキャリアの回転数を設定する
   * プラネタリキャリアの回転数にあわせてサンギアの回転数が変化する
   * @param rPlaCarrierSpeed 新しいプラネタリギアの回転数(rpm)
   */
  public void setPlaCarrierSpeed(double rPlaCarrierSpeed) {
    m_rPlaCarrierSpeed = rPlaCarrierSpeed;
  }
  /**
   * プラネタリキャリアの回転数を取得する
   * @return プラネタリキャリアの回転数(rpm)
   */
  public double getPlaCarrierSpeed() {
    return m_rPlaCarrierSpeed;
  }

  /**
   * サンギアの回転数を取得
   * @return サンギアの回転数(rpm)
   */
  public double getSunGearSpeed() {
    double rRingGearCount = m_ringGear.getGearCount();
    double rSunGearCount = m_sunGear.getGearCount();
    return (m_rPlaCarrierSpeed * (rRingGearCount + rSunGearCount) - m_rRingGearSpeed * rRingGearCount) / rSunGearCount;
  }

  /**
   * サンギアの回転数を設定
   * サンギアの回転数にあわせてプラネタリキャリアの回転数が変化する
   * @param rSunGearSpeed 新しいサンギアの回転数(rpm)
   */
  public void setSunGearSpeed(double rSunGearSpeed) {
    double rRingGearCount = m_ringGear.getGearCount();
    double rSunGearCount = m_sunGear.getGearCount();
    setPlaCarrierSpeed((int)((rSunGearSpeed * rSunGearCount + m_rRingGearSpeed * rRingGearCount) / (rRingGearCount + rSunGearCount)));
  }

  /**
   * リングギアを取得する
   * @return リングギア
   */
  public Gear getRingGear() {
    return m_ringGear;
  }
  /**
   * プラネタリキャリアを取得する
   * @return プラネタリキャリア
   */
  public PlanetaryCarrier getPlanetaryCarrier() {
    return m_plaCarrier;
  }
  /**
   * サンギアを取得する
   * @return サンギア
   */
  public Gear getSunGear() {
    return m_sunGear;
  }

  /**
   * プラネタリギアのX座標設定
   * @param nX X座標
   */
  public void setPosX(int nX) {
    super.setPosX(nX);
    m_ringGear.setPosX(nX);
    m_plaCarrier.setPosX(nX);
    m_sunGear.setPosX(nX);
    move(0);
  }

  /**
   * プラネタリギアのY座標設定
   * @param nY Y座標
   */
  public void setPosY(int nY) {
    super.setPosY(nY);
    m_ringGear.setPosY(nY);
    m_plaCarrier.setPosY(nY);
    m_sunGear.setPosY(nY);
    move(0);
  }

  /**
   * プラネタリギアのサイズ（半径）を設定
   * @param nSize 半径
   */
  public void setSize(double rSize) {
    super.setSize(rSize);
    m_ringGear.setSize(rSize);
    m_sunGear.setSize(rSize * m_sunGear.getGearCount() / m_ringGear.getGearCount());
    for (int nIdx = 0; nIdx < mx_piniGear.length; nIdx++)
      mx_piniGear[nIdx].setSize((m_ringGear.getSize() - m_sunGear.getSize()) / 2);
    m_plaCarrier.setWidth((int)(m_ringGear.getSize() - m_sunGear.getSize()) / 2);
    m_plaCarrier.setSize(m_sunGear.getSize() * rSize / m_ringGear.getSize() + mx_piniGear[0].getSize());
    move(0);
  }
}