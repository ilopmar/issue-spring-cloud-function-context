package com.example.issue;

import com.example.issue.dto.ReleaseTopicMessageDTO.Payload;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class ReleaseEventHandler implements Consumer<Payload> {

  @Override
  public void accept(Payload payload) {
    System.out.println("==================== ReleaseEventHandler.accept ====================");
    System.out.println("If you can see this message it means the deserialization worked\n");
    System.out.println(payload);
    System.out.println("==================== ReleaseEventHandler.accept ====================");
  }

}
