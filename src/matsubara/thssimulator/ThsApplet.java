package matsubara.thssimulator;

import java.applet.Applet;


//import java.awt.image.*;
//import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.util.ResourceBundle;
import java.util.ListResourceBundle;


//import java.text.*;
import java.text.NumberFormat;
import java.text.FieldPosition;
import matsubara.gear.*;
import matsubara.graphics.ProgressLine;
import matsubara.carcontroller.*;
import java.beans.*;
import matsubara.uiext.*;
import matsubara.uiext.event.*;
import java.awt.event.*;
import java.awt.*;


/**
 * <p>Title:  ＴＨＳシミュレータの動作と描画のスレッド : Action of THS Simulator and Draw thread </p>
 * <p>author: Copyright (c) 2002 matsubara masakazu </p>
 * @author m.matsubara
 * @version 1.2.0
 */
class ThsDrawThread extends Thread {
  private ThsApplet m_applet;
  private volatile boolean m_bStopRequest = false;

  /**
   * ＴＨＳシミュレータの動作と描画のスレッドの初期化 : initialize
   * @param applet 制御するＴＨＳシミュレータ本体 (THS Simulater body)
   */
  ThsDrawThread(ThsApplet applet) {
    m_applet = applet;
  }

  /**
   * スレッド本体 : Thread body
   */
  public void run() {
    long nSleep;
    long nTime, nTime2, nTime3;
    nTime = System.currentTimeMillis();
    nTime3 = System.currentTimeMillis();
    Graphics graphics = m_applet.getGraphics();
    while (m_bStopRequest == false) {
      long nInterval = nTime3 - nTime;
      nTime = nTime3;
//      m_applet.lbDebug.setText(String.valueOf(nInterval != 0 ? 1000 / nInterval : 0) + " fps");
      try {
        m_applet.moveThs(nInterval);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      try {
        m_applet.drawThsObjects(m_applet.getGraphics());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      nTime2 = System.currentTimeMillis();
      nSleep = 50 - (nTime2 - nTime);
      if (nSleep < 10)
        nSleep = 10;
      try {
        sleep(nSleep);
      }
      catch (Exception e) {
      }
      nTime3 = System.currentTimeMillis();
    }
  }
  /**
   * スレッドの停止要求を発行 : request stop thread
   */
  public void stopRequest() {
    m_bStopRequest = true;
  }
}


/**
 * <p>Title: ＴＨＳシミュレータ : THS Simurator</p>
 * <p>Description: プリウスのハイブリッドシステム（ＴＨＳ）の心臓部、遊星歯車機構のシミュレーション</p>
 * <p>author: Copyright (c) 2001 m.matsubara</p>
 * @author m.matsubara
 * @version 1.0.0
 */

public class ThsApplet extends Applet {
  /** アプリケーション名 : Application name*/
  public final String mc_sApplicationName = "Prius driving simulator";
  /** バージョン表示 : Version */
  public final String mc_sVersion = "version 1.4.0";
  /** 著作権表示 : Copyright */
  public final String mc_sCopyright = "copyright(c) 2002 m.matsubara";

  /** リソースバンドル : resource bundle(internationalization and localization) */
  private ResourceBundle m_res = ResourceBundle.getBundle("matsubara.thssimulator.ThsAppletRes");

  private ThsDrawThread m_threadDraw;
  private ThsController m_thsController = null;

//  BufferedImage m_imgPGear = new BufferedImage(160, 160, BufferedImage.TYPE_INT_RGB);  //  プラネタリギア描画用バッファ(Draw buffer(Pranetary gear))
  private Image m_imgPGear = null;


  private Image[] m_imgPrius = new Image[2];  //  走行中のプリウスの画像２枚（２枚を交代に表示させて走っている感じを出す） : Prius
  private Image[] m_imgKame  = new Image[2];  //  走行中（？）のカメの画像２枚（２枚を交代に表示させて走っている感じを出す） : tortoise
  private int m_nImgPriusNo = 0;              //  表示中のプリウスの番号(インデックス) : index
  private Image m_imgMotor;                   //  モーター動作を表すアイコン（オレンジの電球）: moter mode icon (orange lamp)
  private Image m_imgKaisei;                  //  回生発電を表すアイコン（緑の電球）: generation icon (green lamp)
  private Image m_imgGasoline;                //  エンジンの動作を表すアイコン（ガソリンスタンドアイコン）: engine mode icon (service station)
  private Image m_imgMeter;                   //  メーター（センターメーター）: Center meter
  private Image m_imgMeterKame;               //  メーター表示用カメ(出力制限警告灯) : tortoise lamp
  private Image m_imgEnergyMonitor;           //  エネルギーモニタの図 : energy monitor
  private Image m_imgPriusBrake;              //  プリウスのブレーキランプ : brake lamp

  //  メーターの位置 : meter position
  final int m_nMeterBaseX = 200;
  final int m_nMeterBaseY =  55;
  //  エネルギーモニタの位置 : energy monitor position
  final int m_nEmOffsetX = 447;
  final int m_nEmOffsetY =   0;
  //  エネルギーモニタでエネルギーの流れを表す線、２文字ずつついている大文字の意味は以下のとおり : energy monitor line
  //    B : バッテリー, I : インバーター,   M : モーター, F : ファイナルギア
  //    G : 発電機,     P : プラネタリギア, E : エンジン, T : タイヤ
  //    B : Battery, I : Inverter,   M : Moter, F : Final gear
  //    G : Generator,     P : Planetary gear, E : Engine, T : tire
  private ProgressLine m_lineBI = new ProgressLine(m_nEmOffsetX + 121, m_nEmOffsetY +  38, m_nEmOffsetX + 112, m_nEmOffsetY +  64, Color.red, 6);
  private ProgressLine m_lineIM = new ProgressLine(m_nEmOffsetX + 112, m_nEmOffsetY +  64, m_nEmOffsetX + 102, m_nEmOffsetY +  94, Color.red, 6);
  private ProgressLine m_lineMF = new ProgressLine(m_nEmOffsetX + 102, m_nEmOffsetY +  94, m_nEmOffsetX +  83, m_nEmOffsetY +  88, Color.red, 6);
  private ProgressLine m_lineIG = new ProgressLine(m_nEmOffsetX + 112, m_nEmOffsetY +  64, m_nEmOffsetX +  85, m_nEmOffsetY +  57, Color.red, 6);
  private ProgressLine m_lineGP = new ProgressLine(m_nEmOffsetX +  85, m_nEmOffsetY +  57, m_nEmOffsetX +  72, m_nEmOffsetY +  84, Color.red, 6);
  private ProgressLine m_lineEP = new ProgressLine(m_nEmOffsetX +  50, m_nEmOffsetY +  77, m_nEmOffsetX +  72, m_nEmOffsetY +  84, Color.red, 6);
  private ProgressLine m_linePF = new ProgressLine(m_nEmOffsetX +  72, m_nEmOffsetY +  84, m_nEmOffsetX +  83, m_nEmOffsetY +  88, Color.red, 6);
  private ProgressLine m_lineFT = new ProgressLine(m_nEmOffsetX +  83, m_nEmOffsetY +  88, m_nEmOffsetX +  70, m_nEmOffsetY + 116, Color.red, 6);

  //  道路描画用のバッファ、この上にプリウスやメーター、エネルギーモニタなどが表示される。: draw buffer (road)
  private Image m_imgRoad = null;
  private double m_rRoadPosition = 0; //  道路の白線を描画するためのインデックス、プリウスが進むと増える。: index of road white line

  //  アプレット最下段に表示されるメッセージ : message
  private String m_sMessage1 = "TOYOTA Hybrid System Simulation - Type HK-NHW10-AEEEB (Japanese specifications)";
  private String m_sMessage2 = "copyright (c) 2002 m.matsubara";

  private long m_nSystemStartTime;  //  アプリケーション起動時の時間 System.currentTimeMillis(); : application startup time

  boolean isStandalone = false;
  Label lbPowerSplitDevice = new Label();
  Label label2 = new Label();
  Label lbRingGearSpeed = new Label();
  Label label4 = new Label();
  Label lbPlaCarrierSpeed = new Label();
  Label label6 = new Label();
  Label lbSunGearSpeed = new Label();
  Choice cmbRange = new Choice();
  Panel panel1 = new Panel();
  Checkbox ckDriveMode = new Checkbox();
  Label label8 = new Label();
  Label label9 = new Label();
  Slider sliRingGear = new Slider();
  Slider sliPlaCarrier = new Slider();
  Slider sliSunGear = new Slider();
  Slider sliBrake = new Slider();
  Slider sliAccel = new Slider();
  //引数値の取得 : get parameter
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //アプレットの構築 : construct applet
  public ThsApplet() {
    try {
      m_thsController = new ThsController(80, 80, 60);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    m_nSystemStartTime = System.currentTimeMillis();
  }
  //アプレットの初期化 : initialize applet
  public void init() {
    //  地域化設定の強制 : Localization setting
    String sLanguage = getParameter("Language", null);
    String sCountry = getParameter("Country", null);
    if (sLanguage != null && sCountry != null) {
      m_res = ResourceBundle.getBundle("matsubara.thssimulator.ThsAppletRes", new java.util.Locale(sLanguage, sCountry));
      System.out.println("Language : " + sLanguage);
      System.out.println("Country : " + sCountry);
    }
    else if (sLanguage != null) {
      m_res = ResourceBundle.getBundle("matsubara.thssimulator.ThsAppletRes", new java.util.Locale(sLanguage, ""));
      System.out.println("Language : " + sLanguage);
    }
    System.out.println("Resource classes : " + m_res.getClass().getName());

    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  //コンポーネントの初期化 : initialize component
  private void jbInit() throws Exception {
    lbPowerSplitDevice.setAlignment(1);
    lbPowerSplitDevice.setFont(new java.awt.Font("Dialog", 0, 12));
    lbPowerSplitDevice.setForeground(new Color(182, 99, 0));
    lbPowerSplitDevice.setText(m_res.getString("sPowerSplitDevice_PlanetaryGear"));
    lbPowerSplitDevice.setBounds(new Rectangle(0, 7, 200, 17));
    this.setBackground(Color.white);
    this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        this_mouseMoved(e);
      }
    });
    this.setLayout(null);
    label2.setAlignment(1);
    label2.setFont(new java.awt.Font("Dialog", 0, 12));
    label2.setForeground(new Color(0, 186, 0));
    label2.setText(m_res.getString("sMotor"));
    label2.setBounds(new Rectangle(209, 6, 56, 17));
    lbRingGearSpeed.setAlignment(2);
    lbRingGearSpeed.setForeground(new Color(0, 186, 0));
    lbRingGearSpeed.setText("0 rpm");
    lbRingGearSpeed.setBounds(new Rectangle(204, 21, 67, 17));
    label4.setAlignment(1);
    label4.setFont(new java.awt.Font("Dialog", 0, 12));
    label4.setForeground(Color.red);
    label4.setText(m_res.getString("sEngine"));
    label4.setBounds(new Rectangle(276, 6, 59, 17));
    lbPlaCarrierSpeed.setAlignment(2);
    lbPlaCarrierSpeed.setForeground(Color.red);
    lbPlaCarrierSpeed.setText("0 rpm");
    lbPlaCarrierSpeed.setBounds(new Rectangle(272, 21, 67, 17));
    label6.setAlignment(1);
    label6.setFont(new java.awt.Font("Dialog", 0, 12));
    label6.setForeground(Color.blue);
    label6.setText(m_res.getString("sGenerator"));
    label6.setBounds(new Rectangle(387, 6, 63, 17));
    lbSunGearSpeed.setAlignment(2);
    lbSunGearSpeed.setForeground(Color.blue);
    lbSunGearSpeed.setText("0 rpm");
    lbSunGearSpeed.setBounds(new Rectangle(385, 21, 67, 17));
    cmbRange.setBackground(new Color(204, 204, 205));
    cmbRange.setEnabled(false);
    cmbRange.setBounds(new Rectangle(10, 27, 104, 24));
    cmbRange.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        cmbRange_itemStateChanged(e);
      }
    });
    panel1.setBackground(new Color(204, 204, 205));
    panel1.setBounds(new Rectangle(464, 10, 124, 177));
    panel1.setLayout(null);
    ckDriveMode.setBackground(new Color(204, 204, 205));
    ckDriveMode.setFont(new java.awt.Font("Dialog", 0, 12));
    ckDriveMode.setLabel(m_res.getString("sDriveMode"));
    ckDriveMode.setBounds(new Rectangle(19, 1, 88, 25));
    ckDriveMode.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        ckDriveMode_mouseMoved(e);
      }
    });
    ckDriveMode.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        ckDriveMode_itemStateChanged(e);
      }
    });
    label8.setAlignment(1);
    label8.setFont(new java.awt.Font("Dialog", 0, 12));
    label8.setText(m_res.getString("sBrake"));
    label8.setBounds(new Rectangle(7, 156, 51, 17));
    label9.setAlignment(1);
    label9.setFont(new java.awt.Font("Dialog", 0, 12));
    label9.setText(m_res.getString("sAccel"));
    label9.setBounds(new Rectangle(68, 156, 52, 17));
    sliRingGear.setBounds(new Rectangle(202, 43, 70, 136));
    sliRingGear.setMaximum(6000);
    sliRingGear.setMinimum(-2000);
    sliRingGear.setMajorTickSpacing(2000);
    sliRingGear.setMinorTickSpacing(1000);
    sliRingGear.setPrintLabel(true);
    sliRingGear.addChangeListener(new matsubara.uiext.event.ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        sliRingGear_stateChanged(e);
      }
    });
    sliRingGear.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        sliRingGear_mouseMoved(e);
      }
    });
    sliPlaCarrier.setPrintLabel(true);
    sliPlaCarrier.addChangeListener(new matsubara.uiext.event.ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        sliPlaCarrier_stateChanged(e);
      }
    });
    sliPlaCarrier.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        sliPlaCarrier_mouseMoved(e);
      }
    });
    sliPlaCarrier.setMajorTickSpacing(2000);
    sliPlaCarrier.setMaximum(4000);
    sliPlaCarrier.setBounds(new Rectangle(272, 73, 67, 79));
    sliPlaCarrier.setMinorTickSpacing(1000);
    sliSunGear.setPrintLabel(true);
    sliSunGear.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        sliSunGear_mouseMoved(e);
      }
    });
    sliSunGear.setMajorTickSpacing(2000);
    sliSunGear.setMinimum(-4000);
    sliSunGear.setMaximum(6000);
    sliSunGear.setEnabled(false);
    sliSunGear.setBounds(new Rectangle(384, 40, 69, 172));
    sliSunGear.setMinorTickSpacing(1000);
    sliBrake.setBounds(new Rectangle(21, 55, 37, 96));
    sliBrake.setEnabled(false);
    sliBrake.addChangeListener(new matsubara.uiext.event.ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        sliBrake_stateChanged(e);
      }
    });
    sliAccel.setBounds(new Rectangle(79, 55, 37, 96));
    sliAccel.setEnabled(false);
    sliAccel.addChangeListener(new matsubara.uiext.event.ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        sliAccel_stateChanged(e);
      }
    });
    this.add(label2, null);
    this.add(lbRingGearSpeed, null);
    this.add(label6, null);
    this.add(lbSunGearSpeed, null);
    panel1.add(label8, null);
    panel1.add(label9, null);
    panel1.add(ckDriveMode, null);
    panel1.add(cmbRange, null);
    panel1.add(sliAccel, null);
    panel1.add(sliBrake, null);
    this.add(lbPlaCarrierSpeed, null);
    this.add(label4, null);
    this.add(panel1, null);
    this.add(sliSunGear, null);
    this.add(lbPowerSplitDevice, null);
    this.add(sliRingGear, null);
    this.add(sliPlaCarrier, null);
  }
  //アプレットの開始 : start applet
  public void start() {
    System.out.println("Welcome to Prius");
    System.out.println("Prius driving simulator - ignition !!");

    m_imgPGear = createImage(160, 160);
    m_imgRoad = createImage(600, 180);

    m_imgPrius[0]      = loadImage("/matsubara/thssimulator/image/prius1.gif");
    m_imgPrius[1]      = loadImage("/matsubara/thssimulator/image/prius2.gif");
    m_imgKame[0]       = loadImage("/matsubara/thssimulator/image/kame1.gif");
    m_imgKame[1]       = loadImage("/matsubara/thssimulator/image/kame2.gif");
    m_imgMotor         = loadImage("/matsubara/thssimulator/image/motor.gif");
    m_imgKaisei        = loadImage("/matsubara/thssimulator/image/kaisei.gif");
    m_imgGasoline      = loadImage("/matsubara/thssimulator/image/gasoline.gif");
    m_imgMeter         = loadImage("/matsubara/thssimulator/image/meter.gif");
    m_imgMeterKame     = loadImage("/matsubara/thssimulator/image/meterKame.gif");
    m_imgEnergyMonitor = loadImage("/matsubara/thssimulator/image/energyMonitor.gif");
    m_imgPriusBrake    = loadImage("/matsubara/thssimulator/image/priusBrake.gif");

    cmbRange.addItem("P Range");
    cmbRange.addItem("R Range");
    cmbRange.addItem("N Range");
    cmbRange.addItem("D Range");
    cmbRange.addItem("B Range");
    cmbRange.select(0);
    m_thsController.setShiftPosition("P");

    m_threadDraw = new ThsDrawThread(this);
    m_threadDraw.start();
  }
  //アプレットの停止 : stop applet
  public void stop() {
    m_threadDraw.stopRequest();
    try {
      Thread.sleep(100);  //  描画系スレッドの停止を待つ : wait to stop draw thread
    }
    catch (Exception e) {
    }
    System.out.println(mc_sApplicationName + " " + mc_sVersion + " " + mc_sCopyright);
    System.out.println("good bye :-)");
  }
  //アプレットの破棄 : dispose applet
  public void destroy() {
  }
  //アプレットの情報取得 : applet information
  public String getAppletInfo() {
    return mc_sApplicationName + " " + mc_sVersion + " " + mc_sCopyright;
  }

  //引数情報の取得 : get parameter
  public String[][] getParameterInfo() {
    String[][] pinfo =
      {
      {"Language", "String", ""},
      {"Country", "String", ""},
      };
    return pinfo;
  }

  //Main メソッド
  public static void main(String[] args) {
    ThsApplet applet = new ThsApplet();
    applet.isStandalone = true;
    Frame frame;
    frame = new Frame() {
      protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
          System.exit(0);
        }
      }
      public synchronized void setTitle(String title) {
        super.setTitle(title);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      }
    };
    frame.setTitle(applet.mc_sApplicationName);
    frame.add(applet, BorderLayout.CENTER);
    applet.init();
//    applet.start();
    frame.setSize(610,400);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
    frame.setVisible(true);
    applet.start();
  }

  public Image createImage(int nWidth, int nHeight) {
//    if (isStandalone)
//      return new java.awt.image.BufferedImage(nX, nY, java.awt.image.BufferedImage.TYPE_INT_RGB);
//    else
      Image img = super.createImage(nWidth, nHeight);
      if (img == null) {
        try {
          Class clsBi = Class.forName("java.awt.image.BufferedImage");
          java.lang.reflect.Constructor clsBiConstruct = clsBi.getConstructor(new Class[] {int.class, int.class, int.class});
          img = (Image)clsBiConstruct.newInstance(new Object[] {
            new Integer(nWidth), new Integer(nHeight), new Integer(clsBi.getField("TYPE_INT_RGB").getInt(null))
          });
        }
        catch (Exception e2) {
          e2.printStackTrace();
        }
      }
      return img;
  }

  /**
   * このクラスのリソースから画像をロードする
   * @param sImage リソースファイルのURL
   * @return ロードした Image オブジェクト
   */
  Image loadImage(final String sImage) {
//    System.out.println(this.getClass().getResource(sImage).toString());
    Class myClass = this.getClass();
    Image img;
    try {
      //throw new Exception("Image error");
      img = createImage((ImageProducer)myClass.getResource(sImage).getContent());
    }
    catch (Exception e) {
      try {
        img = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(sImage));
      } catch (Exception e2) {
        //  リソースの読み込み失敗に備えて : resource load filure
        System.out.println("image file load error : " + e.getMessage());
        System.out.println("  (" + sImage + ")");
        img = createImage(100, 20);  
        Graphics g = img.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, 100, 20);
        g.setColor(Color.red);
        g.drawString("image load error.", 5, 15);
      }
    }
/*
    try {
      MediaTracker mt = new MediaTracker(this);
      mt.addImage(img, 0);
      mt.checkAll(true);
      mt.waitForAll(1000);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
*/
    return img;
  }

  /**
   * アプレットの描画 : draw applet
   * @param g 描画する Graphics オブジェクト
   */
  public void paint(Graphics g) {
    super.paint(g);
    try {
      drawThsObjects(g);
    } catch (Exception e) {
    }
  }


  private static Image m_imgOffScreen = null;
  public void update(Graphics g) {
    if (m_imgOffScreen == null) {
      m_imgOffScreen = createImage(600, 380);
    }
    synchronized (m_imgOffScreen) {
      Graphics gi = m_imgOffScreen.getGraphics();
      gi.clearRect(0, 0, 600, 380);
      print(gi);
      g.drawImage(m_imgOffScreen, 0, 0, null);
    }
//    System.out.println("ThsApplet.update(Graphics);");
  }


  /**
   * 現在のプリウスの速度 : current prius speed (km/h)
   * @return プリウスの速度(km/h)
   */
  public double getSpeed() {
    return m_thsController.getSpeed();
  }


  private boolean m_bDrawingThsObjects = false;
  /**
   * ギアやさまざまなオブジェクトの描画 : draw objects
   * @param g 描画する Graphics オブジェクト
   */
  /* synchronized */
  public void drawThsObjects(Graphics g) {
    synchronized (this) {
      if (m_bDrawingThsObjects == false) {
        m_bDrawingThsObjects = true;
        try {
          drawThsObjectsInternal(g);
        }
        finally {
          m_bDrawingThsObjects = false;
        }
      }
    }
  }


  /**
   * ギアやさまざまなオブジェクトの描画 : draw objects(internal)
   * @param g 描画する Graphics オブジェクト
   */
  /* synchronized */
  public void drawThsObjectsInternal(Graphics g) {
    PlanetaryGear planetaryGear = m_thsController.getPlanetaryGear();
    double rSpeed = getSpeed();
    //  プラネタリーギアの色設定 : Planetary gear color setting
    if (m_thsController.isGasoline()) {
      planetaryGear.getPlanetaryCarrier().setColor1(Color.white);
      planetaryGear.getPlanetaryCarrier().setColor2(Color.red);
    }
    else {
      planetaryGear.getPlanetaryCarrier().setColor1(new Color(224, 224, 224));
      planetaryGear.getPlanetaryCarrier().setColor2(new Color(224,   0,   0));
    }
    if (planetaryGear.getSunGearSpeed() > 0) {
      planetaryGear.getSunGear().setColor1(Color.white);
      planetaryGear.getSunGear().setColor2(Color.blue);
    }
    else {
      planetaryGear.getSunGear().setColor1(new Color(224, 224, 224));
      planetaryGear.getSunGear().setColor2(new Color(  0,   0, 210));
    }

    //  プラネタリーギア描画 : draw planetary gear
    Graphics gi = m_imgPGear.getGraphics();

    gi.setColor(Color.white);
    gi.fillRect(0, 0, m_imgPGear.getWidth(null), m_imgPGear.getHeight(null));
    planetaryGear.draw(gi);

    //  道路 : road
    gi = m_imgRoad.getGraphics();
    gi.setColor(new Color(160, 160, 160));
    gi.fillRect(0, 0, 445, 100);
    gi.setColor(Color.white);
    gi.fillRect(445, 0, 600 - 445, 100);
    gi.setColor(Color.white);
    gi.fillRect(0, 100, 600, 120);
    for (int nIdx = 0 - 65 - (int)m_rRoadPosition; nIdx < 600 + 100 + (int)m_rRoadPosition; nIdx += 100)
      gi.fillRect(nIdx, 48, 65, 4);

    //  プリウス : prius
    if (m_thsController.isPowerSaveMode()
        && (System.currentTimeMillis() - m_nSystemStartTime) % 2600 < 1300
        && ckDriveMode.getState()) {
      gi.drawImage(m_imgKame[m_nImgPriusNo],  60 + (int)(getSpeed() / 4), 1, null); //  カメ : tortoise
    }
    else {
      gi.drawImage(m_imgPrius[m_nImgPriusNo], 60 + (int)(getSpeed() / 4), 1, null); //  プリウス : prius
      if (sliBrake.getValue() >= 3)
        gi.drawImage(m_imgPriusBrake,         60 + (int)(getSpeed() / 4), 1, null); //  ブレーキランプ : brake lamp
      if (m_thsController.getShiftPosition().equals("R") || (ckDriveMode.getState() == false && rSpeed < 0)) { //  バックランプ : back lamp
        gi.setColor(Color.white);
        gi.fillRect(60 + (int)(getSpeed() / 4) + 2, 23, 1, 1);
      }
    }

    //  メーター : meter
    drawMeter(gi);

    //  エネルギーモニタ : energy monitor
    drawEnergyMonitor(gi);

    if (ckDriveMode.getState()) {
      //  インジケーターアイコン : indicator icon
      if (m_thsController.isMotor())
        gi.drawImage(m_imgMotor, 500 - 450, 55, null);
      if (m_thsController.isKaisei())
        gi.drawImage(m_imgKaisei, 500 - 450, 55, null);
      if (m_thsController.isGasoline())
        gi.drawImage(m_imgGasoline, 540 - 450, 55, null);

      //  燃費表示（グラフ）: milage graph
      String sFuelEfficiency;
      String sMileageUnit = m_res.getString("sMileageUnit");                                  //  燃費の単位 : milage unit
      double nMileageRate = Double.valueOf(m_res.getString("nMileageRate")).doubleValue();    //  換算レート : rate
      int nMileageCalcMode = Integer.valueOf(m_res.getString("nMileageCalcMode")).intValue(); //  計算方法 = nMileageRate で km/L を割る : 0 , nMileageRate を km/L で割る : 1  : calculate mode
      int nMaxMileage = Integer.valueOf(m_res.getString("nMaxMileage")).intValue();           //  燃費グラフの最大数値 : graph max

      java.text.DecimalFormat format = new java.text.DecimalFormat("0.0");

      //  瞬間燃費の計算と表示 (current milage)
      double rFuelEfficiency = m_thsController.getFuelEfficiency();
      //  燃費単位換算(km/L → 表示単位) : milage unit calculation
      if (nMileageCalcMode == 0)
        rFuelEfficiency /= nMileageRate;
      else if (rFuelEfficiency != 0)
        rFuelEfficiency = nMileageRate / rFuelEfficiency;
      else
        rFuelEfficiency = Double.POSITIVE_INFINITY;
      sFuelEfficiency = new StringBuffer(m_res.getString("sMomentMileage")).append(" : ").append(format.format(rFuelEfficiency)).append(" ").append(sMileageUnit).toString();
      final Font fontFuelEfficiency = new Font("dialog", Font.PLAIN, 12);
      final Color colorFuelEfficiency = new Color(114, 114, 153);
      gi.setFont(fontFuelEfficiency);
      gi.setColor(colorFuelEfficiency);
      gi.fillRoundRect(310, 2, (int)(130 * Math.min(rFuelEfficiency, nMaxMileage) / nMaxMileage), 18, 3, 3);
      gi.setColor(Color.white);
      gi.drawRoundRect(310, 2, 130, 18, 2, 2);
      gi.drawString(sFuelEfficiency, 315, 16);

      //  累積燃費の計算と表示 : accumulation mileage
      if (m_thsController.getTotalUseFuel() != 0)
        rFuelEfficiency = Math.abs(m_thsController.getTripMeter()) / m_thsController.getTotalUseFuel();
      else if (m_thsController.getTripMeter() == 0)
        rFuelEfficiency = 0;
      else
        rFuelEfficiency = Double.POSITIVE_INFINITY;
      //  燃費単位換算(km/L → 表示単位) : milage unit calculation
      if (nMileageCalcMode == 0)
        rFuelEfficiency /= nMileageRate;
      else if (rFuelEfficiency != 0)
        rFuelEfficiency = nMileageRate / rFuelEfficiency;
      else
        rFuelEfficiency = Double.POSITIVE_INFINITY;
      sFuelEfficiency = new StringBuffer(m_res.getString("sTotalMileage")).append(" : ").append(format.format(rFuelEfficiency)).append(" ").append(sMileageUnit).toString();
      gi.setColor(colorFuelEfficiency);
      gi.fillRoundRect(310, 22, (int)(130 * Math.min(rFuelEfficiency, nMaxMileage) / nMaxMileage), 18, 3, 3);
      gi.setColor(Color.white);
      gi.drawRoundRect(310, 22, 130, 18, 2, 2);
      gi.drawString(sFuelEfficiency, 315, 36);
    }

    gi.setFont(new Font(m_res.getString("sMsgFontName"), Font.PLAIN, Integer.valueOf(m_res.getString("nMsgFontSize")).intValue()));
    gi.setColor(Color.black);
    gi.drawString(m_sMessage1, 5, 115);
    gi.drawString(m_sMessage2, 5, 135);

    // 実際の描画
    g.drawImage(m_imgPGear, 20, 20, null);
    g.drawImage(m_imgRoad, 0, 215, null);
  }


  /**
   * メーターを描く : draw meter
   * @param g 描画する Graphics オブジェクト
   */
  public void drawMeter(Graphics g) {
    final Color colorGreen = new Color(64, 255, 255);
    final Color colorRed = new Color(255, 100, 0);
    final Font fontMeterSpeed = new Font("Monospaced", Font.PLAIN, 26);
    final Font fontMeterSpeedUnit = new Font("Dialog", Font.PLAIN, 10);
    final Font fontMeterTrip = new Font("Dialog", Font.PLAIN,9);
    double rSpeed = getSpeed();   //  車速(km/h) : Car speed(km/h)

    //  メーター本体 : meter body
    g.drawImage(m_imgMeter, m_nMeterBaseX, m_nMeterBaseY, null);

    //  READY マーク :"READY" mark
    if (m_thsController.isIgnition() == false) {
      g.setColor(Color.black);
      g.fillRect(m_nMeterBaseX + 47, m_nMeterBaseY + 1, 62, 10);
    }
    else {
      if (m_thsController.isReady() == false) {
        if ((System.currentTimeMillis() - m_thsController.getIgnitionTime()) % 400 >= 200) {
          g.setColor(Color.black);
          g.fillRect(m_nMeterBaseX + 47, m_nMeterBaseY + 1, 62, 10);
        }
      }
    }

    //  レンジ表示 (draw range)
    int nRange;
    if (ckDriveMode.getState())
      nRange = cmbRange.getSelectedIndex();
    else {
      if (rSpeed > 0)
        nRange = 3; //  D Range
      else if (rSpeed < 0)
        nRange = 1; //  R Range
      else
        nRange = 0; //  P Range
    }
    if (nRange == 1)
      g.setColor(colorRed);  //  赤 : red
    else
      g.setColor(colorGreen); //  薄い緑 : light green
    g.drawRoundRect(200 + 46 + 13 * nRange, 55 + 11, 10, 12, 2, 2);

    //  スピード表示 : draw speed
    java.text.DecimalFormat format = new java.text.DecimalFormat("0.0");
    String sSpeed = String.valueOf((int)Math.abs(rSpeed / Double.valueOf(m_res.getString("nSpeedRate")).doubleValue()));  //  メーターに表示する速度（km/hではないかもしれない）
    //DialogInput、Monospaced、Serif、SansSerif
    g.setFont(fontMeterSpeed);
    g.setColor(colorGreen);
    g.drawString(sSpeed, m_nMeterBaseX + 156 - g.getFontMetrics().stringWidth(sSpeed), m_nMeterBaseY + 35);
    g.setFont(fontMeterSpeedUnit);
    g.drawString(m_res.getString("sSpeedUnit"), m_nMeterBaseX + 157, m_nMeterBaseY + 35);

    //  トリップメーター : TRIP meter
    StringBuffer sTrip = format.format(m_thsController.getTripMeter() / Double.valueOf(m_res.getString("nDistanceRate")).doubleValue(), new StringBuffer(10), new FieldPosition(4));  //  トリップメーターに表示する走行距離（kmではないかもしれない）: Distance(TRIP meter)
    //DialogInput、Monospaced、Serif、SansSerif
    g.setFont(fontMeterTrip);
    g.setColor(colorGreen);
    sTrip.append(m_res.getString("sDistanceUnit"));
    int nWidth = g.getFontMetrics().stringWidth(sTrip.toString());
    g.drawString(sTrip.toString(), m_nMeterBaseX + 110 - nWidth, m_nMeterBaseY + 35);

    //  燃料残量（消費した分のメーターを黒で塗りつぶす）＆燃料警告灯（燃料の残量が７Ｌを切ると点灯）
    //  A fuel residual quantity
    int nNenryo = (int)((50 - m_thsController.getFuel()) / ((50 - 7) / 9.0));
    if (nNenryo != 0) {
      if (nNenryo > 10)
        nNenryo = 10;
      g.setColor(Color.black);
      g.fillRect(m_nMeterBaseX + 28, m_nMeterBaseY + 6, 7, nNenryo * 3);
      if (nNenryo >= 9) {
        //  燃料警告灯 : fuel warning lamp
        g.setColor(colorRed);
        g.fillRect(m_nMeterBaseX + 35, m_nMeterBaseY + 33, 4, 3);
      }
    }

    if (ckDriveMode.getState() != false) {
      //  出力制限警告灯（いわゆるカメ）: Power save warning lamp
      if (m_thsController.isPowerSaveMode())
        g.drawImage(m_imgMeterKame, m_nMeterBaseX + 8, m_nMeterBaseY + 5, null);

      //  マスターワーニングランプ : Master warning lamp
      if (m_thsController.getFuel() <= 0) {
        //  ガス欠 : gasoline empty
        g.setColor(colorRed);
        g.drawLine(m_nMeterBaseX + 167,     m_nMeterBaseY + 15, m_nMeterBaseX + 167 + 5, m_nMeterBaseY + 25);
        g.drawLine(m_nMeterBaseX + 167,     m_nMeterBaseY + 15, m_nMeterBaseX + 167 - 5, m_nMeterBaseY + 25);
        g.drawLine(m_nMeterBaseX + 167 + 5, m_nMeterBaseY + 25, m_nMeterBaseX + 167 - 5, m_nMeterBaseY + 25);

        g.drawLine(m_nMeterBaseX + 167, m_nMeterBaseY + 18, m_nMeterBaseX + 167, m_nMeterBaseY + 21);
        g.drawLine(m_nMeterBaseX + 167, m_nMeterBaseY + 23, m_nMeterBaseX + 167, m_nMeterBaseY + 23);
      }
    }
  }

  /**
   * エネルギーモニタを描く : draw energy monitor
   * @param g 描画する Graphics オブジェクト
   */
  public void drawEnergyMonitor(Graphics g) {
    g.drawImage(m_imgEnergyMonitor, m_nEmOffsetX, m_nEmOffsetY, null);

//    drawAxis(g, m_nEmOffsetX + 125, m_nEmOffsetY + 113, m_thsController.getRingGear().getAngle(), new Color(0, 0xbb, 0));
    drawAxis(g, m_nEmOffsetX + 112, m_nEmOffsetY + 98, m_thsController.getRingGear().getAngle(), m_thsController.getRingGear().getColor2(), 7);
    drawAxis(g, m_nEmOffsetX + 40, m_nEmOffsetY + 80, m_thsController.getPlanetaryCarrier().getAngle(), m_thsController.getPlanetaryCarrier().getColor2(), 7);
    drawAxis(g, m_nEmOffsetX + 70, m_nEmOffsetY + 62, m_thsController.getSunGear().getAngle(), m_thsController.getSunGear().getColor2(), 7);

//    drawAxis(g, m_nEmOffsetX + 72, m_nEmOffsetY + 84, m_thsController.getRingGear().getAngle(), m_thsController.getRingGear().getColor2(), 7);
//    drawAxis(g, m_nEmOffsetX + 72, m_nEmOffsetY + 84, m_thsController.getPlanetaryCarrier().getAngle(), m_thsController.getPlanetaryCarrier().getColor2(), 5);
//    drawAxis(g, m_nEmOffsetX + 72, m_nEmOffsetY + 84, m_thsController.getSunGear().getAngle(), m_thsController.getSunGear().getColor2(), 3);

    g.setFont(new Font("Dialog", 0, 10));
    g.setColor(Color.red);
    g.drawString(m_res.getString("sEngine"), m_nEmOffsetX + 8, m_nEmOffsetY + 65);
    g.setColor(Color.blue);
    g.drawString(m_res.getString("sGenerator"), m_nEmOffsetX + 50, m_nEmOffsetY + 50);
//    g.setColor(Color.blue);
    g.drawString(m_res.getString("sBattery"), m_nEmOffsetX + 75, m_nEmOffsetY + 30);
    g.setColor(Color.black);
    g.drawString(m_res.getString("sInverter"), m_nEmOffsetX + 90, m_nEmOffsetY + 72);
    g.setColor(m_thsController.getRingGear().getColor2());
    g.drawString(m_res.getString("sMotor"), m_nEmOffsetX + 90, m_nEmOffsetY + 115);

    g.setColor(lbPowerSplitDevice.getForeground());
    g.drawString(m_res.getString("sPowerSplitDevice"), m_nEmOffsetX + 8, m_nEmOffsetY + 103);


    if (ckDriveMode.getState()) {
      //  エンジンの start と stop : engine start and stop
      double rEnginRpm = m_thsController.getEngineRpm();
      if (rEnginRpm > 0 && rEnginRpm < 950 && m_thsController.getAcceleration() != 0) {
        g.setFont(new Font("Dialog", Font.BOLD, 11));
        String sEngineMsg;
        if (m_thsController.getAcceleration() >= 0)
          sEngineMsg = "start";
        else
          sEngineMsg = "stop";
        FontMetrics fm = g.getFontMetrics();

        g.setColor(new Color(0xAA, 0x44, 0));
        g.fillRect(m_nEmOffsetX + 28, m_nEmOffsetY + 73, fm.stringWidth(sEngineMsg) + 4, fm.getHeight());
        g.setColor(new Color(0xFF, 0x66, 0));
        g.drawRect(m_nEmOffsetX + 28, m_nEmOffsetY + 73, fm.stringWidth(sEngineMsg) + 4, fm.getHeight());
        g.drawString(sEngineMsg, m_nEmOffsetX + 30, m_nEmOffsetY + 73 + fm.getAscent());
      }

      //  エネルギーモニタ : energy monitor
      m_lineBI.setReverse(false);
      m_lineIM.setReverse(false);
      m_lineMF.setReverse(false);
      m_lineIG.setReverse(false);
      m_lineGP.setReverse(false);
      m_lineEP.setReverse(false);
      m_linePF.setReverse(false);
      m_lineFT.setReverse(false);
      m_lineBI.setColor(Color.red);
      m_lineIM.setColor(Color.red);
      m_lineMF.setColor(Color.red);
      m_lineFT.setColor(Color.red);

      switch (m_thsController.getThsMode()) {
      case ThsController.thsStop:
        break;
      case ThsController.thsStopAndEngine:
        m_lineGP.setReverse(true);
        m_lineIG.setReverse(true);
        m_lineBI.setReverse(true);
        m_lineBI.setColor(Color.green);
        m_lineEP.draw(g);
        m_lineGP.draw(g);
        m_lineIG.draw(g);
        m_lineBI.draw(g);
        break;
      case ThsController.thsStart:
        m_lineBI.draw(g);
        m_lineIM.draw(g);
        m_lineMF.draw(g);
        m_lineFT.draw(g);
        break;
      case ThsController.thsNormal:
        m_lineGP.setReverse(true);
        m_lineIG.setReverse(true);
        m_lineBI.setReverse(true);
        m_lineEP.draw(g);
        m_linePF.draw(g);
        m_lineFT.draw(g);
        m_lineGP.draw(g);
        m_lineIG.draw(g);
        m_lineBI.draw(g);
        break;
      case ThsController.thsNormalOD:
        m_lineIM.setReverse(true);
        m_lineMF.setReverse(true);
        m_lineBI.setReverse(true);
        m_lineBI.setColor(Color.green);
        m_lineEP.draw(g);
        m_linePF.draw(g);
        m_lineFT.draw(g);
        m_lineGP.draw(g);
        m_lineIG.draw(g);
        m_lineBI.draw(g);
        m_lineIM.draw(g);
        m_lineMF.draw(g);
        break;
      case ThsController.thsFullAccel:
        m_lineGP.setReverse(true);
        m_lineIG.setReverse(true);
        m_lineBI.draw(g);
        m_lineIM.draw(g);
        m_lineMF.draw(g);
        m_lineIG.draw(g);
        m_lineGP.draw(g);
        m_lineEP.draw(g);
        m_linePF.draw(g);
        m_lineFT.draw(g);
        break;
      case ThsController.thsFullAccelOD:
        m_lineBI.draw(g);
        m_lineIM.draw(g);
        m_lineMF.draw(g);
        m_lineIG.draw(g);
        m_lineGP.draw(g);
        m_lineEP.draw(g);
        m_linePF.draw(g);
        m_lineFT.draw(g);
        break;
      case ThsController.thsKaiseiAndEngine:
        m_lineIG.setReverse(true);
        m_lineGP.setReverse(true);
        m_lineEP.setReverse(true);
        m_linePF.setReverse(true);
        m_lineIG.draw(g);
        m_lineGP.draw(g);
        m_lineEP.draw(g);
        m_linePF.draw(g);
      case ThsController.thsKaisei:
        m_lineBI.setReverse(true);
        m_lineIM.setReverse(true);
        m_lineMF.setReverse(true);
        m_lineFT.setReverse(true);
        m_lineBI.setColor(Color.green);
        m_lineIM.setColor(Color.green);
        m_lineMF.setColor(Color.green);
        m_lineFT.setColor(Color.green);
        m_lineBI.draw(g);
        m_lineIM.draw(g);
        m_lineMF.draw(g);
        m_lineFT.draw(g);
        break;
      case ThsController.thsBackAndEngine:
        m_lineBI.setReverse(true);
        m_linePF.setReverse(true);
        m_lineIG.setReverse(true);
        m_lineGP.setReverse(true);
        m_lineBI.setColor(Color.green);
        m_lineBI.draw(g);
        m_lineIM.draw(g);
        m_lineMF.draw(g);
        m_lineIG.draw(g);
        m_lineGP.draw(g);
        m_lineEP.draw(g);
        m_linePF.draw(g);
        m_lineFT.draw(g);
        break;
      }
      //  バッテリー表示 : draw battery
      java.text.DecimalFormat format = new java.text.DecimalFormat("0.0");
      double rBatterySOC = m_thsController.getBatterySOC();
      String sBatterySOC = "batt " + format.format(rBatterySOC) + "%";
      int nBarWidth = (int)(rBatterySOC / 100 * 40);
      if (m_thsController.isPowerSaveMode())
        g.setColor(Color.red);
      else if (m_thsController.getBatterySOC() < 45)
        g.setColor(Color.yellow);
      else
        g.setColor(Color.green);
      g.fillRect(455, 10, nBarWidth, 10);
      g.setColor(Color.white);
      g.drawRect(455, 10, 40, 10);
      g.drawRect(495, 13, 2, 4);
      g.setFont(new Font("Dialog", Font.PLAIN, 9));
      g.drawString(sBatterySOC, 455, 33);
    }
  }

  /**
   * 各オブジェクトをその状態に応じて動かす : move THS objects
   * @param nMilliSec 経過時間 : elapsed time (ms)
   */
  /* synchronized */
  void moveThs(long nMilliSec) {
//    lbDebug.setText("" + m_thsController.getBatterySOC());
    PlanetaryGear planetaryGear = m_thsController.getPlanetaryGear();
    //  動かす
    double rSpeed = getSpeed();
    if (ckDriveMode.getState()) {
      m_thsController.drive(nMilliSec, sliAccel.getValue(), sliBrake.getValue());
      sliRingGear.setValue((int)planetaryGear.getRingGearSpeed());
      sliPlaCarrier.setValue((int)planetaryGear.getPlaCarrierSpeed());
    }
    else
      planetaryGear.move((double)nMilliSec / 100);
    m_rRoadPosition += rSpeed / 4;
    m_rRoadPosition %= 100;
    if ((int)rSpeed != 0) {
      //  プリウスがわずかでも動いているときは画像を切り替える : change image
      m_nImgPriusNo++;
      m_nImgPriusNo %= 2;
    }

    if (ckDriveMode.getState()) {
      //  エネルギーモニタの線を動かす : update energy monitor
      int nMove = (int)(nMilliSec / 10);
      m_lineBI.movePosition(nMove);
      m_lineIM.movePosition(nMove);
      m_lineMF.movePosition(nMove);
      m_lineIG.movePosition(nMove);
      m_lineGP.movePosition(nMove);
      m_lineEP.movePosition(nMove);
      m_linePF.movePosition(nMove);
      m_lineFT.movePosition(nMove);
    }

    //  説明表示 : draw message
    if (ckDriveMode.getState()) {
      if (m_thsController.getFuel() <= 0) {
        m_sMessage1 = m_res.getString("sNoGas1");
        if (m_thsController.getBatterySOC() < 40)
          m_sMessage2 = m_res.getString("sNoGas2");
        else
          m_sMessage2 = m_res.getString("sNoGas2MotorRun");
      } else {
        switch (m_thsController.getThsMode()) {
        case ThsController.thsStop:
          m_sMessage1 = m_res.getString("sRunStop1");
          m_sMessage2 = m_res.getString("sRunStop2");
          break;
        case ThsController.thsStopAndEngine:
          m_sMessage1 = m_res.getString("sRunStopAndEngine1");
          m_sMessage2 = m_res.getString("sRunStopAndEngine2");;
          break;
        case ThsController.thsStart:
          m_sMessage1 = m_res.getString("sRunStart1");
          m_sMessage2 = m_res.getString("sRunStart2");
          break;
        case ThsController.thsNormal:
          m_sMessage1 = m_res.getString("sRunNormal1");
          m_sMessage2 = m_res.getString("sRunNormal2");
          break;
        case ThsController.thsNormalOD:
          m_sMessage1 = m_res.getString("sRunNormalOD1");
          m_sMessage2 = m_res.getString("sRunNormalOD2");
          break;
        case ThsController.thsFullAccel:
          m_sMessage1 = m_res.getString("sRunFullAccel1");
          m_sMessage2 = m_res.getString("sRunFullAccel2");
          break;
        case ThsController.thsFullAccelOD:
          m_sMessage1 = m_res.getString("sRunFullAccel1");
          m_sMessage2 = m_res.getString("sRunFullAccel2");
          break;
        case ThsController.thsKaiseiAndEngine:
          m_sMessage1 = m_res.getString("sRunKaiseiAndEngine1");
          m_sMessage2 = m_res.getString("sRunKaiseiAndEngine2");
          break;
        case ThsController.thsKaisei:
          m_sMessage1 = m_res.getString("sRunKaisei1");
          m_sMessage2 = m_res.getString("sRunKaisei2");
          break;
        case ThsController.thsBackAndEngine:
          m_sMessage1 = m_res.getString("sRunBackAndEngine1");
          m_sMessage2 = m_res.getString("sRunBackAndEngine2");
          break;
        }
      }
    }
  }


  /**
   * エンジン、モーター、発電機の軸を表すマークを書く。 : engine, moter, generator mark(energy monitor)
   * @param g グラフィック
   * @param nX Ｘ座標
   * @param nY Ｙ座標
   * @param rAngle 角度
   * @param color 色
   * @param nSize 半径（ドット）
   */
  void drawAxis(Graphics g, int nX, int nY, double rAngle, Color color, int nSize) {
/*
    g.setColor(color);
    g.drawArc(nX - 7, nY - 7, 14, 14, 0, 360);
    g.fillArc(nX - 7, nY - 7, 14, 14, 360 - (int)(rAngle / (2 * Math.PI) * 360), 90);
    g.fillArc(nX - 7, nY - 7, 14, 14, 360 - (int)(rAngle / (2 * Math.PI) * 360 + 180), 90);
*/
    g.setColor(Color.white);
    g.fillArc(nX - nSize, nY - nSize, nSize * 2, nSize * 2, 360 - (int)(rAngle / (2 * Math.PI) * 360) + 180, 360);
    g.setColor(color);
    g.fillArc(nX - nSize, nY - nSize, nSize * 2, nSize * 2, 360 - (int)(rAngle / (2 * Math.PI) * 360), 180);
  }

  /** フォームのラベルにギアの速度をセットする : set gear revolutions */
  public void viewGearsSpeed() {
    PlanetaryGear planetaryGear = m_thsController.getPlanetaryGear();
    int nRingGearSpeed = (int)planetaryGear.getRingGearSpeed();
    int nPlanetaryCarrierSpeed = (int)planetaryGear.getPlaCarrierSpeed();
    int nSunGearSpeed = (int)planetaryGear.getSunGearSpeed();

    nRingGearSpeed -= nRingGearSpeed % 50;
    if (nPlanetaryCarrierSpeed >= 1000)
      nPlanetaryCarrierSpeed -= nPlanetaryCarrierSpeed % 50;
    else
      nPlanetaryCarrierSpeed -= nPlanetaryCarrierSpeed % 10;
    nSunGearSpeed -= nSunGearSpeed % 10;

    lbRingGearSpeed.setText(String.valueOf(nRingGearSpeed) + " rpm");
    lbPlaCarrierSpeed.setText(String.valueOf(nPlanetaryCarrierSpeed) + " rpm");
    lbSunGearSpeed.setText(String.valueOf(nSunGearSpeed) + " rpm");
  }

  void sliRingGear_mouseMoved(MouseEvent e) {
    if (ckDriveMode.getState() == false) {
      m_sMessage1 = m_res.getString("sDescRingGear1");
      m_sMessage2 = m_res.getString("sDescRingGear2");
    }
  }

  void sliPlaCarrier_mouseMoved(MouseEvent e) {
    if (ckDriveMode.getState() == false) {
      m_sMessage1 = m_res.getString("sDescPlaCarrier1");
      m_sMessage2 = m_res.getString("sDescPlaCarrier2");
    }
  }

  void sliSunGear_mouseMoved(MouseEvent e) {
    if (ckDriveMode.getState() == false) {
      m_sMessage1 = m_res.getString("sDescSunGear1");
      m_sMessage2 = m_res.getString("sDescSunGear2");
    }
  }

  void this_mouseMoved(MouseEvent e) {
    if (ckDriveMode.getState() == false) {
      m_sMessage1 = m_res.getString("sDescGeneral1");
      m_sMessage2 = m_res.getString("sDescGeneral2");
    }
  }



  void ckDriveMode_itemStateChanged(ItemEvent e) {
    sliRingGear.setValue(0);
    sliPlaCarrier.setValue(0);
    sliSunGear.setValue(0);

    m_thsController.getPlanetaryGear().setRingGearSpeed(0.0);
    m_thsController.getPlanetaryGear().setPlaCarrierSpeed(0.0);

    lbRingGearSpeed.setText("0 rpm");
    lbPlaCarrierSpeed.setText("0 rpm");
    lbSunGearSpeed.setText("0 rpm");
    if (ckDriveMode.getState()) {
      sliAccel.setEnabled(true);
      sliBrake.setEnabled(true);
      cmbRange.setEnabled(true);
      cmbRange.select(0);
      m_thsController.setShiftPosition("P");

      sliRingGear.setEnabled(false);
      sliPlaCarrier.setEnabled(false);
//      sliSunGear.setEnabled(false);
      m_thsController.ignition();
    }
    else {
      sliAccel.setEnabled(false);
      sliBrake.setEnabled(false);
      cmbRange.setEnabled(false);
      cmbRange.select(0);
      sliAccel.setValue(0);
      sliBrake.setValue(0);
      m_thsController.setShiftPosition("P");

      sliRingGear.setEnabled(true);
      sliPlaCarrier.setEnabled(true);
//      sliSunGear.setEnabled(true);
      m_thsController.unIgnition();
    }
  }

  void cmbRange_itemStateChanged(ItemEvent e) {
    //  シフトポジションの変更 : change shift position
    switch (cmbRange.getSelectedIndex()) {
    case 0:
      m_thsController.setShiftPosition("P");
      break;
    case 1:
      m_thsController.setShiftPosition("R");
      break;
    case 2:
      m_thsController.setShiftPosition("N");
      break;
    case 3:
      m_thsController.setShiftPosition("D");
      break;
    case 4:
      m_thsController.setShiftPosition("B");
      break;
    }
    //  実際に切り替わったかチェックする : check
    if (m_thsController.getShiftPosition().equals("P")) {
      if (cmbRange.getSelectedIndex() != 0)
        cmbRange.select(0);
    }
    else if (m_thsController.getShiftPosition().equals("R")) {
      if (cmbRange.getSelectedIndex() != 1)
        cmbRange.select(1);
    }
    else if (m_thsController.getShiftPosition().equals("N")) {
      if (cmbRange.getSelectedIndex() != 2)
        cmbRange.select(2);
    }
    else if (m_thsController.getShiftPosition().equals("D")) {
      if (cmbRange.getSelectedIndex() != 3)
        cmbRange.select(3);
    }
    else if (m_thsController.getShiftPosition().equals("B")) {
      if (cmbRange.getSelectedIndex() != 4)
        cmbRange.select(4);
    }
    if (m_thsController.getShiftPosition().equals("P") == false) {
/*
      if (m_thsController.isReady() == false) {
        ckDriveMode.setState(false);
        ckDriveMode_itemStateChanged(null);
      }
*/
    }
  }

  void sliBrake_stateChanged(ChangeEvent e) {
    sliAccel.setValue(0);
  }

  void sliAccel_stateChanged(ChangeEvent e) {
    sliBrake.setValue(0);
  }


  void sliRingGear_stateChanged(ChangeEvent e) {
    PlanetaryGear planetaryGear = m_thsController.getPlanetaryGear();
    planetaryGear.setRingGearSpeed((double)sliRingGear.getValue());
    sliSunGear.setValue((int)planetaryGear.getSunGearSpeed());

    viewGearsSpeed();
  }

  void sliPlaCarrier_stateChanged(ChangeEvent e) {
    PlanetaryGear planetaryGear = m_thsController.getPlanetaryGear();
//    planetaryGear.setPlaCarrierSpeed((int)sliPlaCarrier.getValue() - sliPlaCarrier.getValue() % 50);
    planetaryGear.setPlaCarrierSpeed((int)sliPlaCarrier.getValue());
    sliSunGear.setValue((int)planetaryGear.getSunGearSpeed());

    viewGearsSpeed();
  }

  void ckDriveMode_mouseMoved(MouseEvent e) {
    if (ckDriveMode.getState() == false) {
      m_sMessage1 = m_res.getString("sDescDriveMode1");
      m_sMessage2 = m_res.getString("sDescDriveMode2");
    }
  }
}