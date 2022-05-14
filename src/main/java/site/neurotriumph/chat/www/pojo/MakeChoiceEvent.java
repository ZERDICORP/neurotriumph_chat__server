package site.neurotriumph.chat.www.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MakeChoiceEvent extends Event {
  private Choice choice;

  {
    type = EventType.MAKE_A_CHOICE;
  }

  public MakeChoiceEvent(Choice choice) {
    this.choice = choice;
  }
}
