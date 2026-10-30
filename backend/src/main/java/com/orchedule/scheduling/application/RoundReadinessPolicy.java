package com.orchedule.scheduling.application;

import com.orchedule.scheduling.domain.RoundReadinessIssue;

import java.util.List;

public interface RoundReadinessPolicy {
    List<RoundReadinessIssue> validate(RoundReadinessContext context, int weekNumber);
}
