package matsubara.gear;

import java.awt.Color;
import java.awt.Graphics;

/**
 * <p>タイトル:  Gear base</p>
 * <p>説明:    すべての歯車クラスの基礎</p>
 * <p>著作権: Copyright (c) 2002 m.matsubara </p>
 * @author m.matsubara
 * @version 1.0
 */

public abstract class GearBase {
  int m_nPosX = 0;
  int m_nPosY = 0;
  double m_rSize = 100;
  double m_rAngle = 0.0;  //  歯車の傾き（どれだけ回転しているか・ラジアン）
  Color m_color1 = Color.white;
  Color m_color2 = Color.blue;

  public GearBase() {
  }
  /**
   * ギアの中心位置のX座標を取得する
   * @return ギアの中心のX座標
   */
  public int getPosX() {
    return m_nPosX;
  }
  /**
   * ギアの中心位置のX座標を設定する
   * @param newPosX 新しいギアの中心位置のX座標
   */
  public void setPosX(int newPosX) {
    m_nPosX = newPosX;
  }
  /**
   * ギアの中心位置のY座標を取得する
   * @return ギアの中心のY座標
   */
  public int getPosY() {
    return m_nPosY;
  }
  /**
   * ギアの中心位置のY座標を設定する
   * @param newPosY 新しいギアの中心位置のY座標
   */
  public void setPosY(int newPosY) {
    m_nPosY = newPosY;
  }
  /**
   * ギアの半径を取得する
   * @return ギアの半径
   */
  public double getSize() {
    return m_rSize;
  }
  /**
   * ギアの半径を設定する
   * @param newSize 新しいギアの半径
   */
  public void setSize(double newSize) {
    m_rSize = newSize;
  }
  /**
   * 角度を取得する
   * @return ラジアンでの現在の角度（０～２π）
   */
  public double getAngle() {
    return m_rAngle;
  }
  /**
   * 角度を設定する
   * 値が ０～２π から外れると自動的にこの範囲に収まるよう計算しなおされる
   * @param newAngle 新しい角度（ラジアン）
   */
  public void setAngle(double newAngle) {
    m_rAngle = newAngle;
    m_rAngle %= (2 * Math.PI);
    if (m_rAngle < 0)
      m_rAngle += 2 * Math.PI;

  }
  /**
   * カラー1を取得
   * @return 現在のカラー1
   */
  public Color getColor1() {
    return m_color1;
  }
  /**
   * カラー1を設定する
   * @param newColor1 新しいカラー1
   */
  public void setColor1(Color newColor1) {
    m_color1 = newColor1;
  }
  /**
   * カラー2を取得
   * @return 現在のカラー2
   */
  public Color getColor2() {
    return m_color2;
  }
  /**
   * カラー2を設定する
   * @param newColor2 新しいカラー2
   */
  public void setColor2(Color newColor2) {
    m_color2 = newColor2;
  }
  /**
   * 現在の角度を使ってX座標を計算するのに使われる
   * @param nSize X座標
   * @param rDefAngle angle に追加して計算する角度
   * @return 計算された X座標
   */
  public int rotateX(int nSize, double rDefAngle) {
    return (int)(m_nPosX + nSize * Math.cos(m_rAngle + rDefAngle));
  }
  /**
   * 現在の角度を使ってY標を計算するのに使われる
   * @param nSize Y座標
   * @param rDefAngle angle に追加して計算する角度
   * @return 計算された Y座標
   */
  public int rotateY(int nSize, double rDefAngle) {
    return (int)(m_nPosY + nSize * Math.sin(m_rAngle + rDefAngle));
  }
  /**
   * 現在の角度を使ってX座標を計算するのに使われる
   * @param rSize X座標
   * @param rDefAngle angle に追加して計算する角度
   * @return 計算された X座標
   */
  public int rotateX(double rSize, double rDefAngle) {
    return (int)(m_nPosX + rSize * Math.cos(m_rAngle + rDefAngle));
  }
  /**
   * 現在の角度を使ってY標を計算するのに使われる
   * @param rSize Y座標
   * @param rDefAngle angle に追加して計算する角度
   * @return 計算された Y座標
   */
  public int rotateY(double rSize, double rDefAngle) {
    return (int)(m_nPosY + rSize * Math.sin(m_rAngle + rDefAngle));
  }

  /**
   * ギアを描画するサブクラスで定義されなければならない
   * @param g 描画に使う Graphics オブジェクト
   */
  public abstract void draw(Graphics g);
}