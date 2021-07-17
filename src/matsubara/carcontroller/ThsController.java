package matsubara.carcontroller;

import matsubara.gear.*;


/**
 * <p>Title: ＴＨＳシミュレータ : THS Simulator </p>
 * <p>Description: プリウスのハイブリッドシステム（ＴＨＳ）の心臓部、遊星歯車機構のシミュレーション<br>
 *    Simulation of THS Power split device</p>
 * <p>author: Copyright (c) 2002 m.matsubara </p>
 * @author m.matsubara
 * @version 1.2.0
 */

public class ThsController extends CarController {
  PlanetaryGear m_planetaryGear;
  boolean m_bGasoline;
  boolean m_bMotor;
  boolean m_bKaisei;
  double m_rBatterySOC = 60.0;      //  ＨＶバッテリー残量（SOC = State of charge）: A battery residual quantity (SOC = State of charge)
  boolean m_bEngineRequest = false; //  ＨＶバッテリーが足りないとき（４０％未満）ＯＮ　→　５０％になるまで : battery is not enough(under 40% ... engine on -> 50%)
  boolean m_bPowerSaveMode = false; //  いわゆるカメ : So-called tortoise
  //  THS 動作モード : THS State
  /** エネルギーの流れなし : Non energy flow*/
  public static final int thsStop = 0;
  /** 停止で充電 : Stop and charge (Engine -> battery)*/
  public static final int thsStopAndEngine = 1;
  /** モーターのみで発進 : Moter start*/
  public static final int thsStart = 2;
  /** 通常運転 : normal drive (engine)*/
  public static final int thsNormal = 3;
  /** 通常運転(オーバードライブ・モーターと発電機の役割が逆転) normal drive (overdrive/A motor and the role of the generator reverse)*/
  public static final int thsNormalOD = 4;
  /** 全開加速（エンジン+モーター）: Full acceleration */
  public static final int thsFullAccel = 5;
  /** 全開加速（エンジン+モーター(オーバードライブ)）: Full acceleration(overdrive) */
  public static final int thsFullAccelOD = 6;
  /** 回生ブレーキ : Brakes and generation */
  public static final int thsKaisei = 7;
  /** 回生ブレーキ＋エンジンは発電機の負荷で動いている : Brakes and generation(generator -> engine)*/
  public static final int thsKaiseiAndEngine = 8;
  /** エンジンを使って後退 : Retreat(engine)*/
  public static final int thsBackAndEngine = 9;

  /** 動作モード : A movement mode*/
  int m_nThsMode = thsStop;

  private final long m_nIgnitionUseTime = 2000;  //  イグニッションに必要な時間 : Time when necessary for an ignition

  /**
   * ThsController の初期化 : Initialize of ThsController
   * @param nPlanetaryGearX プラネタリギアを描くときのX座標 : Planetary ger X
   * @param nPlanetaryGearY プラネタリギアを描くときのX座標 : Planetary ger Y
   * @param nPlanetaryGearSize プラネタリギアを描くときのサイズ（半径）: Pranetary gear radius
   */
  public ThsController(int nPlanetaryGearX, int nPlanetaryGearY, int nPlanetaryGearSize) throws EPlanetaryGear {
    super();
    m_planetaryGear = new PlanetaryGear(nPlanetaryGearX, nPlanetaryGearY, nPlanetaryGearSize, 30, 23, 78, 4, 4); //  動力分割機構 : Power split device
    setFuel(50.0);  //  燃料満タン : It is filled up
//    setFuel(0.015);  //  ガス欠テスト用 ^^;)  : Gassoline empty test ^^;)
  }

  /**
   * シフトポジションの設定（安全装置付き♪） : Set Shift Position<br>
   * 安全装置（？）が付いているためDレンジからいきなり P や R に入ったりしない。
   * @param sShiftPosition 新しいシフトポジション
   */
  public void setShiftPosition(String sShiftPosition) {
    double rSpeed = getSpeed();
    if (sShiftPosition.equals("P")) {
      if (rSpeed == 0)
        super.setShiftPosition(sShiftPosition);
      else
        super.setShiftPosition("N");
    }
    else if (sShiftPosition.equals("R")) {
      if (rSpeed <= 3)
        super.setShiftPosition(sShiftPosition);
//      else
//        super.setShiftPosition("N");
    }
    else if (sShiftPosition.equals("N"))
      super.setShiftPosition(sShiftPosition);
    else if (sShiftPosition.equals("D") || sShiftPosition.equals("B")) {
      if (rSpeed >= -3)
        super.setShiftPosition(sShiftPosition);
//      else
//        super.setShiftPosition("N");
    }
  }

  /**
   * 車の状態をシミュレートする : Car simulation
   * @param rMilliSec 経過時間(ms) : elapsed time (ms)
   * @param nAccel アクセルの踏み込み量(%) : Accel (%)
   * @param nBrake ブレーキの踏み込み量(%) : Brake (%)
   */
  protected void driveInternal(double rMilliSec, int nAccel, int nBrake) {
    String sShiftPosition = getShiftPosition();
/*
    if (getFuel() <= 0) {
      //  インチキだがガス欠判断 : Judgment to Gasoline enpty ... Trickery??
      nAccel = 0;
      nBrake = 30;
    }
*/
    if (sShiftPosition.equals("D"))
      driveInternal_D_B(rMilliSec, nAccel, nBrake);
    else if (sShiftPosition.equals("B"))
      driveInternal_D_B(rMilliSec, nAccel, nBrake);
    else if (sShiftPosition.equals("R"))
      driveInternal_R(rMilliSec, nAccel, nBrake);
    else if (sShiftPosition.equals("P"))
      driveInternal_P(rMilliSec, nAccel, nBrake);
    else
      driveInternal_N(rMilliSec, nAccel, nBrake);     //  N レンジ : N range

    m_bMotor = (m_nThsMode == thsStart) || (m_nThsMode == thsFullAccel) || (m_nThsMode == thsFullAccelOD) || (m_nThsMode == thsBackAndEngine);
    m_bKaisei = (m_nThsMode == thsKaisei) || (m_nThsMode == thsKaiseiAndEngine);
    m_bGasoline = (m_nThsMode == thsFullAccel) || (m_nThsMode == thsFullAccelOD) || (m_nThsMode == thsNormal) || (m_nThsMode == thsNormalOD) || (m_nThsMode == thsStopAndEngine)|| (m_nThsMode == thsBackAndEngine);

    if (m_rBatterySOC > 80.0)
      m_rBatterySOC = 80.0;
    if ((m_bEngineRequest == false) && (m_rBatterySOC < 45)) {
      m_bEngineRequest = true;
//      System.out.println("m_bEngineRequest = true");
    }
    if (m_rBatterySOC < 40)
      m_bPowerSaveMode = true;
    if ((m_bEngineRequest != false) && (m_rBatterySOC >= 50)) {
      m_bEngineRequest = false;
      m_bPowerSaveMode = false;
//      System.out.println("m_bEngineRequest = false");
    }
  }

  private double engineRpm(double rTargetEngineRpm, double rMilliSec) {
    //  回転数の制御 : control revolutions
    if (getFuel() <= 0)
      rTargetEngineRpm = 0;
    double rEngineRpm = m_planetaryGear.getPlaCarrierSpeed();
    if (rEngineRpm < rTargetEngineRpm) {
      rEngineRpm += (rMilliSec / 1000) * 1000;
      if (rEngineRpm > rTargetEngineRpm)
        rEngineRpm = rTargetEngineRpm;
    }
    else if (rEngineRpm > rTargetEngineRpm) {
      rEngineRpm -= (rMilliSec / 1000) * 2000;
      if (rEngineRpm < rTargetEngineRpm)
        rEngineRpm = rTargetEngineRpm;
    }

    return rEngineRpm;
  }

  /**
   * Pレンジの時のシミュレーション : P range simulation
   * @param rMilliSec 経過時間(ms) : elapsed time (ms)
   * @param nAccel アクセルの踏み込み量(%) : Accel (%)
   * @param nBrake ブレーキの踏み込み量(%) : Brake (%)
   */
  private void driveInternal_P(double rMilliSec, int nAccel, int nBrake) {
    m_nThsMode = thsStop;

    //  ＴＨＳ各目標値 : target engine revolutions
    double rTargetEngineRpm = 0;

    //  エンジンの状態 : engine state
    if (nAccel >= 5) {
      //  エンジン燃焼 : Burning engine
      rTargetEngineRpm = 1000 + nAccel * 500 / 100 ;
    }

    long nTime = System.currentTimeMillis() - getIgnitionTime();
    if (nTime < 2000) {
      rTargetEngineRpm = 1100;
    } else if (nTime < 3000) {
      rTargetEngineRpm = 1050;
    } else if (nTime < 4000) {
      rTargetEngineRpm = 1000;
    }

    //　バッテリーの状態が悪いとき : battery is not enough
    if (m_bEngineRequest) {
      if (rTargetEngineRpm < 1250)
        rTargetEngineRpm = 1250;
      rTargetEngineRpm = 1250;
      m_nThsMode = thsStopAndEngine;
      m_rBatterySOC += 0.00010 * rMilliSec;
    }

    //  回転数の制御 : control revolutions
    double rEngineRpm = engineRpm(rTargetEngineRpm, rMilliSec);

    m_planetaryGear.setRingGearSpeed(0);
    m_planetaryGear.setPlaCarrierSpeed(rEngineRpm);
    m_planetaryGear.move(rMilliSec / 100);
  }

  /**
   * Rレンジの時のシミュレーション : R range simulation
   * @param rMilliSec 経過時間(ms) : elapsed time (ms)
   * @param nAccel アクセルの踏み込み量(%) : Accel (%)
   * @param nBrake ブレーキの踏み込み量(%) : Brake (%)
   */
  private void driveInternal_R(double rMilliSec, int nAccel, int nBrake) {
    m_nThsMode = thsStop;

    //  ＴＨＳ各現状値 THS current speed
    double rSpeed = getSpeed();

    //  ＴＨＳ各目標値 : THS target speed
    double rTargetSpeed = - 5 - (double)(nAccel * 35 / 100);
    double rTargetEngineRpm = 0;

    if (m_bEngineRequest) {
      //  エンジン回転要求のあるとき、速度は -16km/hで頭打ち : Request engine  -16km/h(max)
      if (rTargetSpeed < -16)
        rTargetSpeed = -16;
      rTargetEngineRpm = 1200;
    }
    if (m_bPowerSaveMode == false && m_bEngineRequest && rSpeed < -5)  //  エンジン回転要求があっても速度が -5km/h より速いときはエンジンを回さない。 : engine stop (under -5km/h)
      rTargetEngineRpm = 0;

    //  ガス欠判断 : gasoline enpty
    if (getFuel() <= 0) {
      rTargetEngineRpm = 0;
      if (m_rBatterySOC < 40)
        rTargetSpeed = 0;
      else
        rTargetSpeed = Math.max(rTargetSpeed, -16);  //  なったこと無いから良くワカラン : Because there is not what I became, I do not understand it
    }

    double rEngineRpm = engineRpm(rTargetEngineRpm, rMilliSec);

    //  加速・自然減速の計算 : Acceleration and Natural slowdown
    m_bMotor = true;
    m_nThsMode = thsStart;
    if (rSpeed > rTargetSpeed) {
      //  加速の計算
      rSpeed += (rTargetSpeed - rSpeed) * rMilliSec / 1000 * 0.15;
      if (rSpeed < rTargetSpeed)
        rSpeed = rTargetSpeed;
    }
    else {
      rSpeed += 2.20 * rMilliSec / 1000;

      if (rSpeed > rTargetSpeed)
        rSpeed = rTargetSpeed;
      if (rSpeed < -10)
        m_nThsMode = thsKaisei;
    }
    if (rEngineRpm > 1000)
      m_nThsMode = thsBackAndEngine;

    //  ブレーキの判断 : Brake
    if (isParkingBrake() && nBrake < 50)
      nBrake = 50;  //  パーキングブレーキを普通のブレーキの50%とみなす。: consider it Parking brake is half foot brake
    if (nBrake > 5) {
      rSpeed += 0.25 * nBrake * rMilliSec / 1000;
      if (rSpeed > 0)
        rSpeed = 0;
      if (rSpeed < -10)
        m_nThsMode = thsKaisei;
      else
        m_nThsMode = thsStop;
    }

    //  エンジンが回転していて速度が０の時 : Engine over 1000rpm and Speed to 0km/h
    if (rEngineRpm > 1000 && rSpeed == 0)
      m_nThsMode = thsStopAndEngine;

    if (m_nThsMode == thsStart)
      m_rBatterySOC -= rSpeed * -0.0000030 * rMilliSec;
    else if (m_nThsMode == thsKaisei)
      m_rBatterySOC += rSpeed * -0.0000010 * rMilliSec;
    else if (m_nThsMode == thsBackAndEngine)
      m_rBatterySOC += 0.0000020 * rMilliSec;
    else if (m_nThsMode == thsStopAndEngine)
      m_rBatterySOC += 0.00010 * rMilliSec;

//    double rTyreDiameter = 165 * 0.65 * 2 + 15 * 25.4;
    double rTyreDiameter = 285 * 2;
    double rTyreGirth = rTyreDiameter * Math.PI;
    double rRingGear = (3.927 / rTyreDiameter / Math.PI / 60 * 1000 * 1000) * rSpeed;

    m_planetaryGear.setRingGearSpeed(rRingGear);
    m_planetaryGear.setPlaCarrierSpeed(rEngineRpm);
    m_planetaryGear.move(rMilliSec / 100);
  }

  /**
   * Nレンジの時のシミュレーション : N range simulation
   * @param rMilliSec 経過時間(ms) : elapsed time (ms)
   * @param nAccel アクセルの踏み込み量(%) : Accel (%)
   * @param nBrake ブレーキの踏み込み量(%) : Brake (%)
   */
  private void driveInternal_N(double rMilliSec, int nAccel, int nBrake) {
    double rSpeed = getSpeed();
    //  Ｎレンジでは回生ブレーキが使えないのでブレーキの利きがやや緩い
    if  (rSpeed > 0) {
      rSpeed -= (1.4 + rSpeed / 100 * 1.5) * rMilliSec / 1000 * 0.5;      //  自然減速分 : Natural slowdown
      rSpeed -= (0.20 + rSpeed / 100 * 0.8) * nBrake * rMilliSec / 1000 * 0.5;  //  ブレーキ : Brake
      if (rSpeed < 0)
        rSpeed = 0;
    }
    else if (rSpeed < 0) {
      rSpeed += (1.4 - rSpeed / 100 * 1.5) * rMilliSec / 1000 * 0.5;      //  自然減速分 : Natural slowdown
      rSpeed += (0.20 - rSpeed / 100 * 0.8) * nBrake * rMilliSec / 1000 * 0.5;  //  ブレーキ : Brake
      if (rSpeed > 0)
        rSpeed = 0;
    }

//    double rTyreDiameter = 165 * 0.65 * 2 + 15 * 25.4;
    double rTyreDiameter = 285 * 2;
    double rTyreGirth = rTyreDiameter * Math.PI;
    double rRingGear = (3.927 / rTyreDiameter / Math.PI / 60 * 1000 * 1000) * rSpeed;

    m_planetaryGear.setRingGearSpeed(rRingGear);
    m_planetaryGear.setPlaCarrierSpeed(0);
    m_planetaryGear.move(rMilliSec / 100);
    m_nThsMode = thsStop;
  }

  /**
   * D, Bレンジの時のシミュレーション : D, B range simulation
   * @param rMilliSec 経過時間(ms) : elapsed time (ms)
   * @param nAccel アクセルの踏み込み量(%) : Accel (%)
   * @param nBrake ブレーキの踏み込み量(%) : Brake (%)
   */
  private void driveInternal_D_B(double rMilliSec, int nAccel, int nBrake) {
    //  ＴＨＳ各現状値 : current speed
    double rSpeed = getSpeed();
    double rEngineRpm = m_planetaryGear.getPlaCarrierSpeed();

    //  ＴＨＳ各目標値 : Target speed and engine revolutions
    double rTargetSpeed = 13 + nAccel * 1.6;
    double rTargetEngineRpm = 0;

    //  エンジンの状態 : engine state
//    double rEngineStopSpeed = (m_rBatterySOC >= 45) ? 45 : 40;    //  エンジン停止速度（エンジンとモーターの速度の境界）: Engine stop speed
    double rEngineStopSpeed = 45;
    double rMoterAccelDiff = (m_rBatterySOC >= 50) ? 20 : 10;  //  モーターのみでの加速のしやすさ（値の大きい方がモーターのみで加速しやすい）: Acceleration only with a motor expands; easiness of
    if ((rSpeed >= rEngineStopSpeed || rSpeed < rTargetSpeed - rMoterAccelDiff) && nAccel >= 10) {
      //  エンジン燃焼 : Engine burning
      rTargetEngineRpm = 1150 + (rTargetSpeed - 40) * (rTargetSpeed - 40) * 2850 / 10000 ;
      if (rTargetEngineRpm > 4000)
        rTargetEngineRpm = 4000;
      if (rEngineRpm >= 1000)
        m_nThsMode = thsNormal;
      else
        m_nThsMode = thsStart;
    }
    else if (rSpeed >= 3 && rSpeed < 15 && rEngineRpm >= 1000) {
      rTargetEngineRpm = 1150;
      m_nThsMode = thsNormal;
    }
    else {
      //  エンジンは発電機によって回されているか、または止まっている（燃焼していない）
      //  The engine does not burn
      rTargetEngineRpm = 1000 + (rSpeed - 50) * 10;
      if (rSpeed < 45)
        rTargetEngineRpm = 0;
      m_nThsMode = thsStart;
    }

    //  ＨＶバッテリーＳＯＣが少ないとき
    //  減速中（ブレーキを踏んでいて、なおかつ速度が１５ｋｍ／ｈ以上でないとき）は、エンジンを止めることがある
    //  When there is little battery, stop an engine at slowdown time (Braking and under 15km/h)
    if (m_bPowerSaveMode) {
      if (nBrake <= 5 || rSpeed < 15) {
        rTargetEngineRpm += 100;
        rTargetEngineRpm = rTargetEngineRpm > 1250 ? rTargetEngineRpm : 1250;
        rTargetEngineRpm = rTargetEngineRpm < 4000 ? rTargetEngineRpm : 4000;
      }
      if (rSpeed != 0)
        m_nThsMode = thsNormal;
      if (rTargetSpeed >= 140)
        rTargetSpeed = 140;
    }

    //  ガス欠判断 : Gasoline enpty
    if (getFuel() <= 0) {
      rTargetEngineRpm = 0;
      if (m_rBatterySOC < 40) {
        rTargetSpeed = 0;
        m_nThsMode = thsStop;
      }
      else {
        rTargetSpeed = Math.min(rTargetSpeed, 45);  //  なったこと無いから良くワカラン
        m_nThsMode = thsStart;
      }
    }

    rEngineRpm = engineRpm(rTargetEngineRpm, rMilliSec);

    //  加速・自然減速の計算 : Acceleration and Natural slowdown
    if (rSpeed < rTargetSpeed) {
      //  加速の計算 : Calculate acceleration
      double rMaxPowerRatio = 0.5 + (160 - rSpeed) / 320;
      if (m_bPowerSaveMode)
        rMaxPowerRatio *= 0.9;  //  カメモードの時は加速は鈍くする : Popwer save mode is slow
      if (getShiftPosition().equals("D"))
        rSpeed += Math.pow(rTargetSpeed - rSpeed, 0.9) * rMilliSec / 1000 * 0.120 * rMaxPowerRatio; //  D レンジの加速 : Acceleration of D range
      else
        rSpeed += Math.pow(rTargetSpeed - rSpeed, 0.9) * rMilliSec / 1000 * 0.111 * rMaxPowerRatio; //  B レンジの加速 : Acceleration of B range
      if (rSpeed > rTargetSpeed)
        rSpeed = rTargetSpeed;
      if ((rSpeed < rTargetSpeed - 15) || (rTargetSpeed > 140) || (rEngineRpm == 0)) {
        if (rEngineRpm >= 1000) {
          if (m_bPowerSaveMode == false)
            m_nThsMode = thsFullAccel;
        }
        else
          m_nThsMode = thsStart;  //　微妙な加速 : Slight acceleration
      }
    }
    else {
      //  自然減速 : Natural slowdown
      double rBrakeRatio = 1.0; //  Ｄレンジのときの自然減速の割合との比較 : ratio to D range
      if (getShiftPosition().equals("B"))
        rBrakeRatio = 1.4;  //  Ｂレンジの比率 : B range is much slowdown
      rSpeed -= (1.4 + rSpeed / 100 * 1.5) * rMilliSec / 1000 * rBrakeRatio;
      if (rSpeed < rTargetSpeed)
        rSpeed = rTargetSpeed;
      if ((nAccel == 0) && (rSpeed > 15))
          m_nThsMode = thsKaisei;
    }

    //  ブレーキの判断 : Brake
    if (isParkingBrake() && nBrake < 50)
      nBrake = 50;  //  パーキングブレーキを普通のブレーキの50%とみなす。: consider it Parking brake is half foot brake
    if (nBrake > 5) {
      rSpeed -= (0.20 + rSpeed / 100 * 0.8) * nBrake * rMilliSec / 1000;
      if (rSpeed < 0)
        rSpeed = 0;
      if (rSpeed > 10)
        m_nThsMode = thsKaisei;
      else
        m_nThsMode = thsStop;
    }

    if ((m_nThsMode == thsKaisei) && (rTargetEngineRpm > 0))
      m_nThsMode = thsKaiseiAndEngine;
    if ((m_nThsMode == thsStop) && rEngineRpm >= 1250)
      m_nThsMode = thsStopAndEngine;

//    double rTyreDiameter = 165 * 0.65 * 2 + 15 * 25.4;
    double rTyreDiameter = 285 * 2;
    double rTyreGirth = rTyreDiameter * Math.PI;
    double rRingGear = (int)((3.927 / rTyreDiameter / Math.PI / 60 * 1000 * 1000) * rSpeed);

    m_planetaryGear.setRingGearSpeed(rRingGear);
    m_planetaryGear.setPlaCarrierSpeed(rEngineRpm);

    //  発電機の回転数が極端にならないようにエンジンの回転数を調節 : Adjustment engine revolutions (revolutions of generator does not become extreme)
    if (m_planetaryGear.getSunGearSpeed() > 6000)
      m_planetaryGear.setSunGearSpeed(6000);
    else if (m_planetaryGear.getSunGearSpeed() < -4500)
      m_planetaryGear.setSunGearSpeed(-4500);
    if ((m_nThsMode == thsNormal) && (m_planetaryGear.getSunGearSpeed() < 300))
      m_nThsMode = thsNormalOD;   //  発電機は 300rpm で発電を失効する（と仮定する）このとき、オーバードライブ状態であるとみなす。 : The generator lapses by generation in 300rpm (supposition); consider that is an OverDrive state then.
    if ((m_nThsMode == thsFullAccel) && (m_planetaryGear.getSunGearSpeed() < 300))
      m_nThsMode = thsFullAccelOD;   //  発電機は 300rpm で発電を失効する（と仮定する）このとき、オーバードライブ状態であるとみなす。 : The generator lapses by generation in 300rpm (supposition); consider that is an OverDrive state then.

    m_planetaryGear.move(rMilliSec / 100);  //  プラネタリギアを回転させる : Planetary gear operation

    switch (m_nThsMode) {
    case thsStart:
    case thsFullAccel:
    case thsFullAccelOD:
      m_rBatterySOC -= (rSpeed) * 0.0000022 * rMilliSec + (rTargetSpeed - rSpeed) * 0.0000015 * rMilliSec;
      break;
    case thsKaisei:
    case thsKaiseiAndEngine:
      m_rBatterySOC += (rSpeed - rTargetSpeed) * 0.000001 * rMilliSec;
      break;
    case thsNormal:
    case thsNormalOD:
      m_rBatterySOC += 0.000030 * rMilliSec;
      break;
    case thsStopAndEngine:
      m_rBatterySOC += 0.00010 * rMilliSec;
      break;
    case thsStop:
      break;
    }
  }

  /**
   * エンジンの回転数を取得する : engine revolutions
   * @return エンジンの回転数(rpm)
   */
  public double getEngineRpm() {
    return m_planetaryGear.getPlaCarrierSpeed();
  }
  /**
   * 車速を取得する : Car speed(km/h)
   * @return 車速(km/h)
   */
  public double getSpeed() {
//    double rTyreDiameter = 165 * 0.65 * 2 + 15 * 25.4;
    double rTyreDiameter = 285 * 2;
    double rTyreGirth = rTyreDiameter * Math.PI;
    return (m_planetaryGear.getRingGearSpeed() / 3.927 * rTyreDiameter * Math.PI * 60 / 1000 / 1000);
  }

  /**
   * シミュレーションに使うプラネタリギアオブジェクト : Planetary gear object
   * @return プラネタリギアオブジェクト
   */
  public PlanetaryGear getPlanetaryGear() {
    return m_planetaryGear;
  }

  /**
   * THS(Toyota Hybrid System) の状態を取得 : Get THS state
   * @return THS の状態
   */
  public int getThsMode() {
    return m_nThsMode;
  }

  /**
   * ガソリンを消費しているか判定 : use gasoline?
   * @return ガソリンを消費しているとき true
   */
  public boolean isGasoline() {
    return m_bGasoline;
  }
  /**
   * モーターを使用しているか判定 : use moter ?
   * @return モーターで走っているかエンジンをアシストしているとき true
   */
  public boolean isMotor() {
    return m_bMotor;
  }
  /**
   * 回生ブレーキ動作中か判定 : Brakes and generation
   * @return 回生ブレーキ動作中のとき true
   */
  public boolean isKaisei() {
    return m_bKaisei;
  }

  /**
   * プラネタリギアのリングギア（モーター直結・ファイナルギアへ接続）: Ring bear object (moter, final gear)
   * @return プラネタリギアのリングギア
   */
  public Gear getRingGear() {
    return m_planetaryGear.getRingGear();
  }
  /**
   * プラネタリギアのプラネタリキャリア（エンジンに直結）Planetary carrier object (engine)
   * @return プラネタリギアのプラネタリキャリア
   */
  public PlanetaryCarrier getPlanetaryCarrier() {
    return m_planetaryGear.getPlanetaryCarrier();
  }
  /**
   * プラネタリギアのサンギア（発電機に直結）: Sun gear object (generator)
   * @return プラネタリギアのサンギア
   */
  public Gear getSunGear() {
    return m_planetaryGear.getSunGear();
  }

  /**
   * リングギアの回転速度を取得する : Ring gear revolutions
   * @return リングギアの回転速度(rpm)
   */
  public double getRingGearSpeed() {
    return m_planetaryGear.getRingGearSpeed();
  }
  /**
   * プラネタリキャリアの回転速度を取得する : planetary carrier revolutions
   * エンジン回転数と等価となる
   * @return プラネタリキャリアの回転速度(rpm)
   */
  public double getPlaCarrierSpeed() {
    return m_planetaryGear.getPlaCarrierSpeed();
  }
  /**
   * サンギアの回転速度を取得する : Sun gear revolutions
   * 発電機の回転数となる
   * @return サンギアの回転速度(rpm)
   */
  public double getSunGearSpeed() {
    return m_planetaryGear.getSunGearSpeed();
  }
  /**
   * バッテリー残量を取得  : Battery SOC
   * @return バッテリー残量(%)
   */
  public double getBatterySOC() {
    return m_rBatterySOC;
  }
  /**
   * バッテリー残量が減り、充電の必要があるときに true : When a battery residual quantity needs decrease, the charge
   * エンジンが常に掛かった状態となる
   * @return 充電の必要があるときに true
   */
  public boolean isEngineRequest() {
    return m_bEngineRequest;
  }
  /**
   * 出力制限警告灯（いわゆるカメ）の表示状態を判定 : Power save mode (So-called tortoise)
   * @return 出力制限警告灯が点灯しているとき true
   */
  public boolean isPowerSaveMode() {
    return m_bPowerSaveMode;
  }

  /**
   * 直前の drive メソッドで使用された燃料使用量(L) : The fuel consumption that was used by a drive method just before that(L)
   * @param rMilliSec 経過時間 : elapsed time(ms)
   * @return 燃料使用量(L) : gasoline (liter)
   */
  protected double getUseFuel(double rMilliSec) {
    double rUseFuel = 0;
    String sShiftPosition = getShiftPosition();
    if (isGasoline()) {
      double rBatteryRatio = 1.0 + (m_rBatterySOC - 60.0) / 20.0 * 0.10;  //  バッテリーＳＯＣを基準として多ければ燃費を良くし、少なければ燃費を悪くする
      if (sShiftPosition.equals("D") || sShiftPosition.equals("R"))
        rUseFuel = Math.pow(getEngineRpm() / 1000, 1.4) * 1.7 / 3600 / 1000 * rMilliSec / rBatteryRatio;
      else
        rUseFuel = Math.pow(getEngineRpm() / 1000, 1.5) * 1.54 / 3600 / 1000 * rMilliSec / rBatteryRatio;
    }
    return rUseFuel;
  }

  public boolean isReady() {
    return (System.currentTimeMillis() - m_nIgnitionTime > m_nIgnitionUseTime);
  }

}
