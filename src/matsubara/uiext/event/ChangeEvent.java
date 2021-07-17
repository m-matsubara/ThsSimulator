package matsubara.uiext.event;

import java.util.EventObject;

/**
 * <p>タイトル: Change Event</p>
 * <p>著作権: Copyright (c) 2002</p>
 * @author m.matsubara
 * @version 1.3.0
 */

public class ChangeEvent extends EventObject {
  public ChangeEvent(Object source) {
    super(source);
  }
}