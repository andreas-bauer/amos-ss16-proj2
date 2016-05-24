package de.developgroup.mrf.server.controller;

import org.cfg4j.provider.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DriveControllerMock extends AbstractDriveController {

    private static Logger LOGGER = LoggerFactory.getLogger(DriveControllerMock.class);

    @Override
    public void initialize(ConfigurationProvider configurationProvider) throws IOException {
        LOGGER.info("initializing DriveController");
    }

    @Override
    public void setAndApply(int speed, int turnrate) throws IOException {
        LOGGER.info("setting and applying speed {} and turnrate {}", speed, turnrate);
        super.setAndApply(speed, turnrate);
    }

    @Override
    public void setDesiredSpeed(int speed) {
        LOGGER.info("setting speed {}", speed);
    }

    @Override
    public void setDesiredTurnrate(int turnrate) {
        LOGGER.info("setting turnate {}", turnrate);
    }

    @Override
    public void updateMotors() throws IOException {
        LOGGER.info("updating motor settings");
    }
}