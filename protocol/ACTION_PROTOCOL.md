# JARVIS Android Action Protocol

The protocol defines the boundary between JARVIS and the Android control layer.

## Action Categories

- tap
- long_press
- swipe
- scroll
- drag
- text_input
- key_action
- back
- home
- recent_apps
- gesture_sequence

## Action Lifecycle

REQUEST
→ VALIDATE
→ PLAN
→ EXECUTE
→ OBSERVE
→ RESULT

## Result

Every executed action should eventually provide:

- action id
- action type
- execution status
- target information
- timing information
- observed result
- error information when applicable

## Human-like Interaction

Human-like behavior belongs to the gesture/action execution layer.

It must not modify JARVIS cognition or decision-making.

## Safety

Actions must be validated before execution.

Invalid coordinates, unavailable targets, unsupported actions and unsafe
execution states must produce a controlled failure instead of undefined
Android behavior.
