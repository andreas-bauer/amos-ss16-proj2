package de.developgroup.mrf.server.handler;

import java.io.IOException;
import java.util.Observable;

import org.cfg4j.provider.ConfigurationProvider;
import com.google.inject.Inject;
import de.developgroup.mrf.server.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import de.developgroup.mrf.rover.collision.CollisionController;
import de.developgroup.mrf.rover.collision.CollisionControllerImpl;
import de.developgroup.mrf.rover.motor.MotorController;
import de.developgroup.mrf.rover.motor.MotorControllerConfiguration;
import de.developgroup.mrf.rover.motor.MotorControllerImpl;
import de.developgroup.mrf.rover.pwmgenerator.PCA9685PWMGenerator;

@Singleton
public class RoverHandlerImpl implements RoverHandler {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RoverHandlerImpl.class);

	@Inject
	private static ClientManager clientManager;

	final CollisionController collisionController = new CollisionControllerImpl();
	final GpioController gpio = GpioFactory.getInstance();

	private I2CBus bus;
	MotorController leftMotor;
	MotorController rightMotor;
	private int desiredSpeed;
	private int desiredTurnRate;

	public RoverHandlerImpl() {
        LOGGER.info("RoverHandlerImpl startup");
	}

	public String handlePing(int sqn) {
        LOGGER.debug("handling ping for sqn " + sqn);
		return "pong " + (sqn + 1);
	}

	public void update(Observable o, Object arg) {
		GpioPinDigitalStateChangeEvent event = (GpioPinDigitalStateChangeEvent) arg;
		if (event.getState().isHigh()) {
			LOGGER.info("Sensor " + event.getPin().getName()
					+ " Collision detected");
		} else {
			LOGGER.info("Sensor " + event.getPin().getName()
					+ " Collision voided");
		}
	}

	@Override
	public void driveForward(int desiredSpeed) throws IOException {
		LOGGER.debug("driveForeward() with speed " + desiredSpeed);
		LOGGER.info("driveForeward() with speed " + desiredSpeed);
		this.desiredSpeed = desiredSpeed;
		this.desiredTurnRate = 0;
		updateMotors();
	}

	private void updateMotors() throws IOException {
		final int leftSpeed = limit(desiredSpeed - desiredTurnRate);
		final int rightSpeed = limit(desiredSpeed + desiredTurnRate);

		leftMotor.setSpeed(leftSpeed);
		rightMotor.setSpeed(rightSpeed);
	}

	private int limit(int value) {
		if (value > MotorController.SPEED_MAX_FORWARD) {
			return MotorController.SPEED_MAX_FORWARD;
		} else if (value < MotorController.SPEED_MAX_BACKWARD) {
			return MotorController.SPEED_MAX_BACKWARD;
		}

		return value;
	}

	@Override
	public void driveBackward(int desiredSpeed) throws IOException {
		LOGGER.debug("driveBackward() with speed " + desiredSpeed);
		LOGGER.info("driveBackward() with speed " + desiredSpeed);
		this.desiredSpeed = -desiredSpeed;
		this.desiredTurnRate = 0;
		updateMotors();
	}

	@Override
	public void initRover(ConfigurationProvider roverProperties)
			throws IOException {

		bus = I2CFactory.getInstance(I2CBus.BUS_1);
		I2CDevice device = bus.getDevice(0x40);
		PCA9685PWMGenerator driver = new PCA9685PWMGenerator(device);

		driver.open();
		driver.setFrequency(50);

		leftMotor = new MotorControllerImpl(driver.getOutput(14),
				roverProperties.bind("motorLeft",
						MotorControllerConfiguration.class));
		rightMotor = new MotorControllerImpl(driver.getOutput(15),
				roverProperties.bind("motorRight",
						MotorControllerConfiguration.class));

		((Observable) collisionController).addObserver(this);
		LOGGER.info("Rover initialized for collistion detection");
	}

	@Override
	public void shutdownRover() {
		gpio.shutdown();
	}

	@Override
	public void stop() throws IOException {
		desiredSpeed = 0;
		desiredTurnRate = 0;
		updateMotors();

	}

	@Override
	public void turnLeft(int turnRate) throws IOException {
		// links negativ recht pos
		this.desiredSpeed = 0;
		this.desiredTurnRate = turnRate;
		updateMotors();

	}

	@Override
	public void turnRight(int turnRate) throws IOException {
		this.desiredSpeed = 0;
		this.desiredTurnRate = -turnRate;
		updateMotors();
	}
}
