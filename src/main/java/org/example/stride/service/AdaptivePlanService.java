package org.example.stride.service;

import org.example.stride.model.entity.TrainingPlan;

public interface AdaptivePlanService {
    void generateOrUpdateNextWindow(TrainingPlan plan);
}
