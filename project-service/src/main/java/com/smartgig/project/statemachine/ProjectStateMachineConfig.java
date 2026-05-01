package com.smartgig.project.statemachine;

import com.smartgig.project.entity.Project;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Optional;

@Configuration
@EnableStateMachine
public class ProjectStateMachineConfig extends StateMachineConfigurerAdapter<ProjectStatus, ProjectEvent> {

    public static final String HEADER_PROJECT = "project";

    @Override
    public void configure(StateMachineStateConfigurer<ProjectStatus, ProjectEvent> states) throws Exception {
        states
                .withStates()
                .initial(ProjectStatus.DRAFT)
                .states(EnumSet.allOf(ProjectStatus.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ProjectStatus, ProjectEvent> transitions) throws Exception {
        transitions
                .withExternal().source(ProjectStatus.DRAFT).target(ProjectStatus.OPEN).event(ProjectEvent.PUBLISH)
                .and()
                .withExternal().source(ProjectStatus.OPEN).target(ProjectStatus.OPEN).event(ProjectEvent.RECEIVE_APPLICATION)
                .and()
                .withExternal().source(ProjectStatus.OPEN).target(ProjectStatus.IN_REVIEW).event(ProjectEvent.SELECT_FREELANCER).guard(hasApplicantsGuard())
                .and()
                .withExternal().source(ProjectStatus.IN_REVIEW).target(ProjectStatus.IN_PROGRESS).event(ProjectEvent.START_PROJECT)
                .and()
                .withExternal().source(ProjectStatus.IN_PROGRESS).target(ProjectStatus.COMPLETED).event(ProjectEvent.COMPLETE_PROJECT).guard(hasSelectedFreelancerGuard()).action(completeAction())
                .and()
                .withExternal().source(ProjectStatus.OPEN).target(ProjectStatus.CANCELLED).event(ProjectEvent.CANCEL_PROJECT).action(cancelAction())
                .and()
                .withExternal().source(ProjectStatus.IN_REVIEW).target(ProjectStatus.CANCELLED).event(ProjectEvent.CANCEL_PROJECT).action(cancelAction())
                .and()
                .withExternal().source(ProjectStatus.IN_PROGRESS).target(ProjectStatus.CANCELLED).event(ProjectEvent.CANCEL_PROJECT).action(cancelAction())
                .and()
                .withExternal().source(ProjectStatus.OPEN).target(ProjectStatus.EXPIRED).event(ProjectEvent.EXPIRE_PROJECT);
    }

    @SuppressWarnings("unchecked")
    private Guard<ProjectStatus, ProjectEvent> hasApplicantsGuard() {
        return ctx -> Optional.ofNullable((Project) ctx.getMessageHeader(HEADER_PROJECT))
                .map(p -> p.getCurrentApplicantCount() != null && p.getCurrentApplicantCount() > 0)
                .orElse(false);
    }

    private Guard<ProjectStatus, ProjectEvent> hasSelectedFreelancerGuard() {
        return ctx -> Optional.ofNullable((Project) ctx.getMessageHeader(HEADER_PROJECT))
                .map(p -> p.getSelectedFreelancerId() != null)
                .orElse(false);
    }

    private Action<ProjectStatus, ProjectEvent> completeAction() {
        return ctx -> {
            Project project = (Project) ctx.getMessageHeader(HEADER_PROJECT);
            if (project != null) {
                project.setCompletedAt(LocalDateTime.now(ZoneOffset.UTC));
            }
        };
    }

    private Action<ProjectStatus, ProjectEvent> cancelAction() {
        return ctx -> {
            Project project = (Project) ctx.getMessageHeader(HEADER_PROJECT);
            if (project != null) {
                project.setCancelledAt(LocalDateTime.now(ZoneOffset.UTC));
            }
        };
    }
}

