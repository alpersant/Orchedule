# Orchedule

[![Status](https://img.shields.io/badge/status-ideation-blue)](https://github.com/)
[![License](https://img.shields.io/badge/license-TBD-lightgrey)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-web%20%7C%20iOS%20%7C%20Android-brightgreen)](https://flutter.dev)

Orchedule is a cross-platform sports scheduling application focused on generating fair, flexible, and optimized schedules for competitions with limited fields and time slots.

The product balances team preferences with equitable rotation across available hours, helping organizers produce schedules that are practical, consistent, and easier to manage.

## Table of Contents

- [Overview](#overview)
- [Problem Statement](#problem-statement)
- [Product Vision](#product-vision)
- [Core Principles](#core-principles)
- [Planned Features](#planned-features)
- [Target Users](#target-users)
- [Technology Stack](#technology-stack)
- [Repository Structure](#repository-structure)
- [Current Status](#current-status)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

## Overview

Orchedule is designed to solve one of the most common and difficult operational problems in sports competitions: assigning match schedules under limited resources while respecting preferences and fairness.

The application is being built as a cross-platform product with a strong focus on usability, scalability, and clear documentation from day one.

## Problem Statement

Sports organizers often need to schedule matches with:
- A limited number of fields.
- A limited number of time slots.
- Multiple teams with different preferences.
- A need for fair rotation across all available hours.

Manual scheduling usually becomes slow, repetitive, and difficult to balance fairly. Orchedule aims to automate that process with an optimization-oriented approach.

## Product Vision

The vision of Orchedule is to provide a scheduling tool that can be used by both non-technical users and advanced organizers.

The application should:
- Generate schedules from team preferences.
- Rotate time slots fairly across teams.
- Adapt to scarce field and hour availability.
- Present results in a clear and usable interface.
- Work on web, iOS, and Android from a single codebase.

## Core Principles

- Fairness over arbitrary assignment.
- Optimization over manual effort.
- Simplicity for first-time users.
- Flexibility for advanced users.
- Strong product documentation.
- Clean architecture from the beginning.

## Planned Features

- Team-based schedule generation.
- Preference-aware slot assignment.
- Fair rotation across time slots.
- Field and availability constraints.
- Calendar visualization.
- Data editing and management.
- Schedule metrics and validation.
- Audit view for rule and fairness checks.
- Multiplatform user interface.

## Target Users

Orchedule is intended for:
- Sports competition organizers.
- League coordinators.
- Club administrators.
- Users with no technical background.
- Advanced users needing detailed control.

## Technology Stack

### Frontend
- Flutter.

### Backend
- Java 17/21.
- Spring Boot.
- Hibernate 6.

### Architecture
- Modular monolith.
- Hexagonal / clean architecture approach.

### Data Layer
- To be defined.

### Deployment
- To be defined.

## Repository Structure

```text
.
├── backend/
├── frontend/
├── docs/
│   ├── product/
│   ├── ux/
│   └── architecture/
├── assets/
├── README.md
└── LICENSE
```

## Current Status

This repository is currently in the product ideation and documentation phase.

The current work is focused on:
1. Defining the product clearly.
2. Writing the MVP scope.
3. Documenting business rules.
4. Designing the domain model.
5. Preparing the architecture for implementation.

## Roadmap

### Phase 1: Product Definition
- [x] Define project name.
- [x] Define the main scheduling problem.
- [x] Draft the initial README.
- [ ] Define MVP scope.
- [ ] Define business rules.

### Phase 2: Architecture
- [ ] Design domain model.
- [ ] Define API contracts.
- [ ] Prepare backend structure.
- [ ] Prepare frontend structure.

### Phase 3: Implementation
- [ ] Build scheduling engine.
- [ ] Build schedule management screens.
- [ ] Add calendar views.
- [ ] Add edit and validation flows.
- [ ] Add metrics and audit views.

### Phase 4: Evolution
- [ ] Advanced analytics.
- [ ] Template-based competitions.
- [ ] Import and export.
- [ ] Rule configurators.
- [ ] Multi-organization support.

## Contributing

Contributions will be welcome once the initial product structure is established.

If you plan to contribute, please keep the following in mind:
- Follow the established architecture.
- Keep code and documentation in English.
- Prefer small, focused changes.
- Update documentation when behavior changes.

## License

License to be defined.

---

## Project Notes

Orchedule is being built with a product-first mindset. The repository will evolve from ideation to implementation in a controlled way, keeping business rules, UX decisions, and technical architecture aligned.
