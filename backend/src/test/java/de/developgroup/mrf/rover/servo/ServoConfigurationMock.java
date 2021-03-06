/**
 * This file is part of Mobile Robot Framework.
 * Mobile Robot Framework is free software under the terms of GNU AFFERO GENERAL PUBLIC LICENSE.
 */
package de.developgroup.mrf.rover.servo;

public class ServoConfigurationMock implements ServoConfiguration {

	boolean reversed = false;
	int offset = 0;
	int posRate = 100;
	int negRate = 100;
	
	@Override
	public boolean reversed() {
		return reversed;
	}

	@Override
	public int offset() {
		return offset;
	}

	@Override
	public int positiveRate() {
		return posRate;
	}

	@Override
	public int negativeRate() {
		return negRate;
	}

}
