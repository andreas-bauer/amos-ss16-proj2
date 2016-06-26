/*
 * This file is part of Mobile Robot Framework.
 * Mobile Robot Framework is free software under the terms of GNU AFFERO GENERAL PUBLIC LICENSE.
 */

package de.developgroup.mrf.server.controller;

/**
 * Object that contains motor settings for left and right motor as percentage values.
 */
public class MotorSetting {

    public double leftMotorPercentage;

    public double rightMotorPercentage;
    
    public MotorSetting(double leftMotorPercentage, double rightMotorPercentage) {
        this.leftMotorPercentage = leftMotorPercentage;
        this.rightMotorPercentage = rightMotorPercentage;
    }

    @Override
    public String toString() {
        return "MotorSetting{" +
                "leftMotorPercentage=" + leftMotorPercentage +
                ", rightMotorPercentage=" + rightMotorPercentage +
                '}';
    }
}
