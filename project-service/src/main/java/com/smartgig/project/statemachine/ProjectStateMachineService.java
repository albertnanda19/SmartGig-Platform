package com.smartgig.project.statemachine;

import com.smartgig.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectStateMachineService {
    private final StateMachineFactory<ProjectStatus, ProjectEvent> factory;

    public ProjectStatus sendEvent(Project project, ProjectEvent event) {
        StateMachine<ProjectStatus, ProjectEvent> sm = factory.getStateMachine(String.valueOf(project.getId()));
        sm.stopReactively().block();
        sm.getStateMachineAccessor().doWithAllRegions(access -> access.resetStateMachineReactively(
                new DefaultStateMachineContext<>(project.getStatus(), null, null, null)
        ).block());
        sm.startReactively().block();

        Message<ProjectEvent> msg = MessageBuilder.withPayload(event)
                .setHeader(ProjectStateMachineConfig.HEADER_PROJECT, project)
                .build();

        boolean accepted = sm.sendEvent(msg);
        if (!accepted) {
            return project.getStatus();
        }
        return sm.getState().getId();
    }
}

