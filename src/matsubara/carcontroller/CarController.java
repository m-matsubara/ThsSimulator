package matsubara.carcontroller;

/**
 * <p>Title: Car Controller</p>
 * <p>Description: クルマの制御・トランスミッションなどを制御する<br>
 *    Car and transmission control</p>
 * <p>author: Copyright (c) 2002 m.matsubara </p>
 * @author m.matsubara
 * @version 1.0.2
 */

public abstract class CarController {
  String m_sShiftPosition;
  boolean m_bParkingBrake;

  double m_rOdoMeter = 0;     //  オドメーター（リセット不可） : ODO meter (unresettable)
  double m_rTripMeter = 0;    //  トリップメーター（リセット可） : TRIP meter (resettable)
  double m_rAcceleration = 0; //  加速度 : Acceleration
  double m_rFuel = 50;        //  燃料残量（仮の値なので派生クラスでオーバーライドしたほうが良い）: A fuel residual quantity
  double m_rTotalUseFuel = 0;      //  燃料使用量 : Fuel consumption
  double m_rFuelEfficiency = 0;
  protected long m_nIgnitionTime;       //  イグニッションを行った時刻(System.currentTimeMillis()) : The time when I performed an ignition

  public CarController() {
    m_sShiftPosition = "N";   //  Ｎポジションならたいていの車についているだろう（牽引しなきゃいけないから）。派生クラスで上書き推奨。 : An N position will stick to most cars
    m_bParkingBrake = false;
  }

  /**
   * シフトポジション<br>
   * ShiftPosition の値として実際にどのような値があるかはサブクラスに依存する<br>
   * "P","R","N","D","3","2","L" や "1","2","3","4","5","R","N" など<br>
   * <br>
   * Get Shift position<br>
   * depend on the subclass for what kind of value there is<br>
   * @return シフトポジション : Shift position
   */
  public String getShiftPosition() {
    return m_sShiftPosition;
  }
  /**
   * シフトポジションの設定<br>
   * Set Shift position
   * @param sShiftPosition シフトポジション : Shift position
   */
  public void setShiftPosition(String sShiftPosition) {
    m_sShiftPosition = sShiftPosition;
  }

  /**
   * パーキングブレーキの状態を取得<br>
   * Get Parking brake
   * @return パーキングブレーキを使用している間 true
   */
  public boolean isParkingBrake() {
    return m_bParkingBrake;
  }
  /**
   * パーキングブレーキの状態を設定
   * Set Parking brake
   * @param bParkingBrake
   */
  public void setParkingBkare(boolean bParkingBrake) {
    m_bParkingBrake = bParkingBrake;
  }

  /**
   * 燃料残量を取得<br>
   * Get fuel residual quantity
   * @return 燃料残量(L) : fuel residual quantity (Liter)
   */
  public double getFuel() {
    return m_rFuel;
  }
  /**
   * 燃料残量を設定<br>
   * Get fuel residual quantity
   * @param rFuel 燃料残量(L) : fuel residual quantity (Liter)
   */
  public void setFuel(double rFuel) {
    m_rFuel = rFuel;
  }

  /**
   * 車の状態をシミュレートする<br>
   * オドメーター、トリップメーター、燃料消費量もここで計算される<br>
   * simulate the state of the car<br>
   * calculate ODO meter, TRIP meter, Fuel consumption
   * @param rMilliSec 経過時間(ms) : elapsed time (ms)
   * @param nAccel アクセルの踏み込み量(%) : Accel (%)
   * @param nBrake ブレーキの踏み込み量(%) : Brake (%)
   */
  final public void drive(double rMilliSec, int nAccel, int nBrake) {
    double rLastSpeed = getSpeed();
    //  派生クラスでの実装を呼び出す : call for sub class
    driveInternal(rMilliSec, nAccel, nBrake);

    //  オドメーターとトリップメーター : ODO meter and TRIP meter
    double rSpeed = Math.abs(getSpeed());
    m_rOdoMeter += rSpeed * rMilliSec / (1000 * 60 * 60);
    m_rTripMeter += rSpeed * rMilliSec / (1000 * 60 * 60);
    m_rAcceleration = (getSpeed() - rLastSpeed) * 1000 / 3600 / (rMilliSec / 1000); // m/s2

    //  燃費計算 : calculate mileage
    double rUseFuel = getUseFuel(rMilliSec);
    if (rSpeed != 0) {
      if (rUseFuel != 0)
        m_rFuelEfficiency = (Math.abs(getSpeed()) / 3600 / 1000 * rMilliSec) / rUseFuel;
      else
        m_rFuelEfficiency = Double.POSITIVE_INFINITY;
      if (m_rFuelEfficiency > 99.9)
        m_rFuelEfficiency = java.lang.Double.POSITIVE_INFINITY;
    }
    else
      m_rFuelEfficiency = 0;

    m_rTotalUseFuel += rUseFuel;

    //  燃料残量 : A fuel residual quantity
    m_rFuel -= rUseFuel;
    if (m_rFuel < 0)
      m_rFuel = 0;
  }

  /**
   * drive メソッドより呼び出される
   * 各サブクラスで実際に車の動作をシミュレートする
   * call from drive method.<br>
   * Do override in a subclass.
   * @param rMilliSec 経過時間(ms) : elapsed time (ms)
   * @param nAccel アクセルの踏み込み量(%) : Accel (%)
   * @param nBrake ブレーキの踏み込み量(%) : Brake (%)
   */
  protected abstract void driveInternal(double rMilliSec, int nAccel, int nBrake);

  /**
   * 車速を取得する<br>
   * Get car speed
   * @return 車速(km/h)
   */
  public abstract double getSpeed();
  /**
   * エンジン回転数を取得する<br>
   * Get Engine revolution
   * @return エンジン回転数(rpm)
   */
  public abstract double getEngineRpm();

  /**
   * 前の drive メソッドで消費された燃料(L)<br>
   * サブクラスで実装されるべき<br>
   * 燃費計算のサポートは不要なら実装しなくても良いが、可能ならサブクラスでオーバーライドするべき<br>
   * Fuel consumption of last drive method
   * @param rMilliSec
   * @return 燃料使用量(L) : Fuel consumption(liter)
   */
  protected double getUseFuel(double rMilliSec) {
    return 0; //
  }

  /**
   * オドメーター値を取得する<br>
   * Get ODO meter
   * @return オドメーター値(km)
   */
  public int getOdoMeter() {
    return (int)m_rOdoMeter;
  }

  /**
   * トリップメーター値を取得する<br>
   * Get TRIP meter.
   * @return トリップメーター値(km)
   */
  public double getTripMeter() {
    return m_rTripMeter;
  }

  /**
   * トリップメーターをリセットする
   * Reset TRIP meter
   */
  public void resetTripMeter() {
    m_rTripMeter = 0;
  }

  /**
   * 現在までの累積ガソリン使用量<br>
   * Accumulation gasoline consumption
   * @return 現在までの累積ガソリン使用量
   */
  public double getTotalUseFuel() {
    return m_rTotalUseFuel;
  }

  /**
   * 現在までの累積ガソリン使用量をリセットする
   * Reset accumulation gasoline consumption
   */
  public void resetTotalUseFuel() {
    m_rTotalUseFuel = 0;
  }

  /**
   * 直前のdriveメソッドで呼び出されたときの加速度(m/s2)を取得する<br>
   * Acceleration of last time drive method call (meter/second 2)
   * @return 加速度(m/s2)･･･1秒間に加速した速度(m/s)
   */
  public double getAcceleration() {
    return m_rAcceleration;
  }

  /**
   * 直前のdriveメソッドで呼び出されたときの燃費(km/L)を取得する<br>
   * このメソッドが正しく動作するには getUseFuel がきちんと実装されていなければならない<br>
   * Milage of last time drive method call(km/liter)
   * @return 燃費(km/L)
   */
  public double getFuelEfficiency() {
    return m_rFuelEfficiency;
  }

  //  エンジンの点火 : ignition engine
  public void ignition() {
    m_nIgnitionTime = System.currentTimeMillis();
  }

  //  エンジンを切る : Stop engine
  public void unIgnition() {
    m_nIgnitionTime = 0;
  }

  //  エンジンが点火しているか : Does an engine light it?
  public boolean isIgnition() {
      return (m_nIgnitionTime != 0);
  }

  public long getIgnitionTime() {
    return m_nIgnitionTime;
  }

}
