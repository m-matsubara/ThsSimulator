package matsubara.uiext.event;

import java.util.EventListener;

/**
 * <p>タイトル: Change Listner</p>
 * @author m.matsubara
 * @version 1.3.0
 */

public interface ChangeListener extends EventListener {
  /**
   * 操作対象の値が変更されたときに呼び出されるイベントリスナ
   * @param e
   */
  public void stateChanged(ChangeEvent e);
}