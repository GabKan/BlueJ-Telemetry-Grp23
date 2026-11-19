package org.comp3607.model;

/**
 * Categories used to distinguish telemetry event payloads.
 */
public enum EventType {
    /** Project-open category; defined for the event model. */
    PROJECT_OPEN,
    /** Project-close category; defined for the event model. */
    PROJECT_CLOSE,
    /** Editor-open category; defined for the event model. */
    EDITOR_OPEN,
    /** Editor-close category; defined for the event model. */
    EDITOR_CLOSE,
    /** Beginning of a compilation attempt. */
    COMPILE_START,
    /** Completion of a compilation attempt, including its result. */
    COMPILE_FINISH,
    /** Completion of a method or object invocation. */
    EXECUTION,
    /** Interaction activity inferred from a compile callback. */
    EDIT_ACTIVITY,
    /** A sufficiently long gap between tracked activity callbacks. */
    IDLE_PERIOD,
    /** A class first observed after a successful compile. */
    CLASS_CREATED,
    /** Removal of a class from the project. */
    CLASS_DELETED,
    /** A class change such as a rename. */
    CLASS_MODIFIED
}
