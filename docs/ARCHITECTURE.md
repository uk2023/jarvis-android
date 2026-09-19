# JARVIS Android Control Layer Architecture

## Purpose

This project provides the native Android control layer for JARVIS.

JARVIS cognition, identity, memory, reasoning and existing runtime remain outside
this repository.

## Architecture

JARVIS
  ↓
Android Bridge
  ↓
Action Protocol
  ↓
Action Planner
  ↓
Action Executor
  ├── Accessibility Layer
  └── Coordinate / Gesture Layer
  ↓
Android UI
  ↓
UI Observation
  ↓
Android Bridge → JARVIS

## Core Responsibilities

- UI observation
- Accessibility tree discovery
- Semantic element targeting
- Coordinate targeting
- Tap
- Long press
- Swipe
- Scroll
- Drag
- Text input
- Back
- Home
- Recent apps
- Gesture sequences
- Human-like gesture timing
- Action validation
- Safety boundaries
- Result reporting

## Separation Rule

The Android control layer must not become JARVIS cognition.

It executes actions requested through the defined bridge/protocol and reports
observable results back to JARVIS.
