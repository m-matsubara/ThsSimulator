package matsubara.uiext;

import java.awt.Component;
import java.awt.*;
import java.awt.event.*;
import javax.accessibility.*;

import java.util.Vector;
import matsubara.uiext.event.ChangeEvent;
import matsubara.uiext.event.ChangeListener;

/**
 * <p>タイトル: スライダコンポーネント</p>
 * <p>説明: swing が使えない環境用の軽量スライダコンポーネント</p>
 * <p>著作権: Copyright (c) 2002</p>
 * @author m.matsubara
 * @version 1.3.0
 */

public class Slider extends Component {
  transient Vector m_listeners = new Vector();

  int m_nMinimum = 0;
  int m_nMaximum = 100;
  int m_nValue = 0;

  int m_nMajorTickSpacing = 10;
  int m_nMinorTickSpacing = 2;
  int m_nVisibleAmount = 10;
  boolean m_bPrintLabel = false;

  Dimension m_minimumSize;
  Dimension m_preferredSize;

  //  内部管理変数
  transient int m_nCharHeight = 0;    //  文字の高さ
  transient int m_nMoveRange = 0;     //  スライダを動かすことのできる範囲
  transient boolean m_bSlide = false; //  現在スライドを行っているか
  transient int m_nSliderPos = 0;     //  スライド開始時のつまみのY座標(相対座標)
  transient int m_nMousePos = 0;      //  スライド開始時のマウスのY座標(相対座標)

  public Slider() {
    super();
    enableEvents(MouseEvent.MOUSE_PRESSED);
    enableEvents(MouseEvent.MOUSE_RELEASED);
//    enableEvents(MouseEvent.MOUSE_MOVED);
    enableEvents(MouseEvent.MOUSE_DRAGGED);
    m_minimumSize = new Dimension(30, 120);
    m_preferredSize = new Dimension(70, 220);
    setBounds(0, 0, m_preferredSize.width, m_preferredSize.height);
  }

  public Dimension minimumSize() {
    return m_minimumSize;
  }

  public Dimension getMinimumSize() {
    return m_minimumSize;
  }

  public void setMinimumSize(Dimension size) {
    m_minimumSize = size;
  }

  public Dimension preferredSize() {
    return m_preferredSize;
  }

  public Dimension getPreferredSize() {
    return m_preferredSize;
  }

  public void setPreferredSize(Dimension size) {
    m_preferredSize = size;
  }

  /**
   * 表示メソッド
   * @param g 表示対象
   */
  public void paint(Graphics g) {
    int nWidth = this.getBounds().width;
    int nHeight = this.getBounds().height;
    g.setFont(getFont());

    m_nCharHeight = g.getFontMetrics().getHeight();
    m_nMoveRange = nHeight - m_nCharHeight;
    int nValue = m_nValue;
/*
    if (nValue > m_nMaximum)
      nValue = m_nMaximum;
    if (nValue < m_nMinimum)
      nValue = m_nMinimum;
*/
    int nSliderY = valueToPos(nValue);

    g.setColor(Color.gray);
    g.drawRect(10, m_nCharHeight / 2 - 2, 4, m_nMoveRange + 4);

    if (isEnabled()) {
      g.setColor(Color.darkGray);
      g.drawRect( 9, m_nCharHeight / 2 - 3, 5, m_nMoveRange + 5);
    }

//    g.setColor(getParent().getBackground());
    g.setColor(Color.lightGray);
    g.fillRect(3, nSliderY - (m_nVisibleAmount / 2 - 1), 18, m_nVisibleAmount);
    if (isEnabled()) {

      g.setColor(Color.white);
      g.drawLine(3, nSliderY - (m_nVisibleAmount / 2), 20, nSliderY - (m_nVisibleAmount / 2));
      g.drawLine(2, nSliderY - (m_nVisibleAmount / 2 - 1),  2, nSliderY + (m_nVisibleAmount / 2 - 1));
      g.drawLine(4, nSliderY + 1, 19, nSliderY + 1);

      g.setColor(Color.darkGray);
      g.drawLine( 3, nSliderY + (m_nVisibleAmount / 2), 20, nSliderY + (m_nVisibleAmount / 2));
      g.drawLine(21, nSliderY - (m_nVisibleAmount / 2 - 1), 21, nSliderY + (m_nVisibleAmount / 2 - 1));
      g.drawLine( 4, nSliderY - 1, 19, nSliderY - 1);
    }
    else {
      g.setColor(Color.black);

      g.drawLine(3, nSliderY - (m_nVisibleAmount / 2), 20, nSliderY - (m_nVisibleAmount / 2));
      g.drawLine(2, nSliderY - (m_nVisibleAmount / 2 - 1),  2, nSliderY + (m_nVisibleAmount / 2 - 1));

      g.drawLine( 3, nSliderY + (m_nVisibleAmount / 2), 20, nSliderY + (m_nVisibleAmount / 2));
      g.drawLine(21, nSliderY - (m_nVisibleAmount / 2 - 1), 21, nSliderY + (m_nVisibleAmount / 2 - 1));

      g.drawLine( 4, nSliderY, 19, nSliderY);
    }

    if (m_bPrintLabel) {
      g.setColor(this.getForeground());
      if (m_nMinorTickSpacing > 0) {
        for (int nIdx = m_nMinimum; nIdx <= m_nMaximum; nIdx+=m_nMinorTickSpacing) {
          int yPos = m_nCharHeight / 2 + m_nMoveRange - m_nMoveRange * (nIdx - m_nMinimum) / (m_nMaximum - m_nMinimum);
          g.drawLine(24, yPos, 27, yPos);
        }
      }

      if (m_nMajorTickSpacing > 0) {
        for (int nIdx = m_nMinimum; nIdx <= m_nMaximum; nIdx+=m_nMajorTickSpacing) {
          int yPos = m_nCharHeight / 2 + m_nMoveRange - m_nMoveRange * (nIdx - m_nMinimum) / (m_nMaximum - m_nMinimum);

          g.drawLine(24, yPos, 30, yPos);
          g.drawString(String.valueOf(nIdx), 32, yPos + m_nCharHeight / 2);
        }
      }
    }
  }

  public void setEnabled(boolean b) {
    super.setEnabled(b);
    repaint();
  }

  /**
   * スライダの値から、つまみの位置を計算する
   * @param nValue スライダ値
   * @return つまみの位置
   */
  public int valueToPos(int nValue) {
    return m_nCharHeight / 2 + m_nMoveRange - m_nMoveRange * (nValue - m_nMinimum) / (m_nMaximum - m_nMinimum);
  }

  /**
   * つまみの位置からスライダ値を計算する
   * @param nPos つまみの位置
   * @return スライダ値
   */
  public int posToValue(int nPos) {
    return m_nMinimum + (m_nCharHeight / 2 + m_nMoveRange - nPos) * (m_nMaximum - m_nMinimum) / m_nMoveRange;
  }


  /**
   * スライダの値を設定する
   * @param n 新しいスライダ値
   */
  public void setValue(int n) {

    if (n < m_nMinimum)
      n = m_nMinimum;
    if (n > m_nMaximum)
      n = m_nMaximum;

    if (m_nValue != n) {
      m_nValue = n;

      ChangeEvent event = new ChangeEvent(this);
      int nMax = m_listeners.size();
      for (int nIdx = 0; nIdx < nMax; nIdx++) {
        if (m_listeners.elementAt(nIdx) instanceof ChangeListener)
          ((ChangeListener)(m_listeners.elementAt(nIdx))).stateChanged(event);
      }
//      repaint(100, 0, 0, 28, this.getBounds().height);
      Graphics g = getParent().getGraphics();
      if (g != null) {
        Rectangle rect = this.getBounds();
        Graphics gi = g.create(rect.x, rect.y, rect.width, rect.height);
        gi.clearRect(0, 0, 28, rect.height);
        paint(gi);
      }
    }
  }

  /**
   * スライダの値を得る
   * @return 現在のスライダ値
   */
  public int getValue() {
    return m_nValue;
  }

  /**
   * 最大値を設定する
   * @param n 新しい最大値
   */
  public void setMaximum(int n) {
    if (n > getMinimum()) {
      m_nMaximum = n;
      setValue(getValue());
    }
  }

  /**
   * 最大値を得る
   * @return 現在の最大値
   */
  public int getMaximum() {
    return m_nMaximum;
  }

  /**
   * 最小値を得る
   * @param n 新しい最小値
   */
  public void setMinimum(int n) {
    if (n < getMaximum()) {
      m_nMinimum = n;
      setValue(getValue());
    }
  }

  /**
   * 最小値を得る
   * @return 現在の最小値
   */
  public int getMinimum() {
    return m_nMinimum;
  }

  /**
   * ラベルの大きい表示間隔を設定
   * @param n
   */
  public void setMajorTickSpacing(int n) {
    m_nMajorTickSpacing = n;
  }

  /**
   * ラベルの大きい表示間隔を取得
   * @return
   */
  public int getMajorTickSpacing() {
    return m_nMajorTickSpacing;
  }

  /**
   * ラベルの小さい表示間隔を設定
   * @param n
   */
  public void setMinorTickSpacing(int n) {
    m_nMinorTickSpacing = n;
  }

  /**
   * ラベルの小さい表示間隔を取得
   * @return
   */
  public int getMinorTickSpacing() {
    return m_nMinorTickSpacing;
  }

  /**
   * つまみの高さを設定する
   * @param n 新しいつまみの高さ
   */
  public void setVisibleAmount(int n) {
    if (n > 0)
      m_nVisibleAmount = n;
  }

  /**
   * つまみの高さを得る
   * @return つまみの高さ
   */
  public int getVisibleAmount() {
    return m_nVisibleAmount;
  }

  /**
   * ラベルを表示するかどうか設定する
   * @param b ラベルを表示させるとき true
   */
  public void setPrintLabel(boolean b) {
    m_bPrintLabel = b;
  }

  /**
   * ラベルを表示させるかどうか得る
   * @return ラベルを表示させるとき true
   */
  public boolean isPrintLabel() {
    return m_bPrintLabel;
  }

  /**
   * ChangeListner を追加
   * @param l
   */
  public synchronized void addChangeListener(ChangeListener l) {
      if (l == null)
        return;
      m_listeners.addElement(l);
  }

  /**
   * ChangeListner を除去
   * @param l
   */
  public synchronized void removeChangeListener(ChangeListener l) {
      if (l == null)
        return;
      int nMax = m_listeners.size();
      for (int nIdx = 0; nIdx < nMax; nIdx++) {
        if (m_listeners.elementAt(nIdx) == l)
          m_listeners.remove(nIdx);
      }
  }

  public ChangeListener[] getChangeListeners() {
    return (ChangeListener[])m_listeners.toArray();
  }


  /**
   * オーバーライドされたマウスイベント
   * @param e
   */
  protected void processMouseEvent(MouseEvent e) {
    switch (e.getID()) {
    case MouseEvent.MOUSE_PRESSED:
      int nValue = m_nValue;
      if (nValue > m_nMaximum)
        nValue = m_nMaximum;
      if (nValue < m_nMinimum)
        nValue = m_nMinimum;
      if (isEnabled()
          && (valueToPos(nValue) - m_nVisibleAmount / 2) <= e.getY()
          && e.getY() <= (valueToPos(nValue) + m_nVisibleAmount / 2)
          && 3 <= e.getX()
          && e.getX() <= 28) {
        m_nMousePos = e.getY();
        m_nSliderPos = valueToPos(m_nValue);
        m_bSlide = true;
      }
      break;
    case MouseEvent.MOUSE_RELEASED:
      m_bSlide = false;
      break;
    }
    super.processMouseEvent(e);
  }

  /**
   * オーバーライドされたマウス移動イベント
   * @param e
   */
  protected void processMouseMotionEvent(MouseEvent e) {
    switch (e.getID()) {
    case MouseEvent.MOUSE_DRAGGED:
      if (m_bSlide) {
        int nValue = posToValue(m_nSliderPos + e.getY() - m_nMousePos);
        if (nValue > m_nMaximum)
          nValue = m_nMaximum;
        if (nValue < m_nMinimum)
          nValue = m_nMinimum;
        setValue(nValue);
        repaint(0, 0, 28, this.getBounds().height);
      }
      break;
    }
    super.processMouseMotionEvent(e);
  }
}

