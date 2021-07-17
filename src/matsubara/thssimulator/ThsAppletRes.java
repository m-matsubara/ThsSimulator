package matsubara.thssimulator;

import java.util.*;


/**
 * <p>title: Prius driving simulator internationalization & localization resource file</p>
 * <p>copyright: Copyright (c) 2001 </p>
 * @author m.matsubara
 * @version 1.4.0
 */

public class ThsAppletRes extends ListResourceBundle {
  static final Object[][] m_containts = {
    {"sNoGas1", "ガソリンが無くなりました。"},
    {"sNoGas2", "これ以上走ることが出来ません。"},
    {"sNoGas2MotorRun", "モーターでしばらく走ることが出来ます。"},
    {"sRunStop1", "エネルギーの流れはありません。"},
    {"sRunStop2", ""},
    {"sRunStopAndEngine1", "バッテリーの残量が少ないため、停止中も発電を行います。"},
    {"sRunStopAndEngine2", ""},
    {"sRunStart1", "モーターで走っています。エンジンは停止し、発電機は空転します。"},
    {"sRunStart2", "坂道発進でバックしないようにクリープ現象もこれで再現されます。"},
    {"sRunNormal1", "エンジンの回転は動力分割機構で２つの出力に分割されます。"},
    {"sRunNormal2", "片方の出力はタイヤを直接回しもう片方は発電機を回します"},
    {"sRunNormalOD1", "発電機をモーターとして使いエンジンの回転にプラスしてタイヤを回します。"},
    {"sRunNormalOD2", "モーターは逆に発電を行い、余った電力をバッテリーに充電します。"},
    {"sRunFullAccel1", "エンジン＋モーターでフル加速を行います。"},
    {"sRunFullAccel2", "エンジン回転の一部は発電機によって電力に変換されモーターを回します。"},
    {"sRunFullAccelOD1", "エンジン＋モーターでフル加速を行います。"},
    {"sRunFullAccelOD2", "発電機はモーターとしてエンジン回転を補助します。"},
    {"sRunKaiseiAndEngine1", "クルマの運動エネルギーをモーターを使って電力に変換して減速します。"},
    {"sRunKaiseiAndEngine2", "エンジンは運動エネルギーの一部で回り続けます。"},
    {"sRunKaisei1", "クルマの運動エネルギーをモーターを使って電力に変換して減速します。"},
    {"sRunKaisei2", "エンジンは停止し、発電機は空転します。"},
    {"sRunBackAndEngine1", "エンジンで発電するとともにモーターでバックします。"},
    {"sRunBackAndEngine2", ""},
    {"sDescRingGear1", "モーターはリングギア(緑/白)とつながっています。"},
    {"sDescRingGear2", "リングギアの回転はタイヤの回転（クルマのスピード）と比例します。"},
    {"sDescPlaCarrier1", "エンジンはプラネタリキャリア(赤/白)とつながっています。"},
    {"sDescPlaCarrier2", "プラネタリキャリアには４つのピニオンギア(黄/黒)がつきます。"},
    {"sDescSunGear1", "発電機はサンギア(青/白)とつながっています。"},
    {"sDescSunGear2", "発電機はエンジンのスターターとしても利用されます。"},
    {"sDescGeneral1", "スライダを動かすと、動力分割機構(遊星歯車)の回転をシミュレートできます。"},
    {"sDescGeneral2", ""},
    {"sDescDriveMode1", "\"運転する\"をチェックするとアクセルとブレーキで運転をシミュレーションできます。"},
    {"sDescDriveMode2", ""},
    {"sPowerSplitDevice_PlanetaryGear", "動力分割機構(遊星歯車)"},
    {"sPowerSplitDevice", "動力分割機構"},
    {"sAccel", "アクセル"},
    {"sBrake", "ブレーキ"},
    {"sMotor", "モーター"},
    {"sEngine", "エンジン"},
    {"sGenerator", "発電機"},
    {"sInverter", "インバーター"},
    {"sBattery", "バッテリー"},
    {"sMomentMileage", "瞬間燃費"},
    {"sTotalMileage", "累積燃費"},
    {"sDriveMode", "運転する"},
    {"sMsgFontName", "Dialog"},
    {"nMsgFontSize", "10"},

    {"sSpeedUnit", "km/h"},
    {"nSpeedRate", "1"},
    {"sDistanceUnit", "km"},
    {"nDistanceRate", "1"},
    {"sMileageUnit", "km/L"},
    {"nMileageRate", "1"},
    {"nMileageCalcMode", "0"},
    {"nMaxMileage", "40"},
  };//

  public ThsAppletRes() {
  }
  protected Object[][] getContents() {
    return m_containts;
  }
}