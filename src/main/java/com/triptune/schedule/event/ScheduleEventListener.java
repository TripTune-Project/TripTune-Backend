package com.triptune.schedule.event;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ScheduleEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ScheduleEvent<?> event){
        ScheduleEventResponse<Object> eventResponse = ScheduleEventResponse.of(
                event.getEventType(),
                event.getMessage(),
                event.getData()
        );

        messagingTemplate.convertAndSend(
                "/sub/schedules/" + event.getScheduleId() + "/events",
                eventResponse
        );
    }

}
