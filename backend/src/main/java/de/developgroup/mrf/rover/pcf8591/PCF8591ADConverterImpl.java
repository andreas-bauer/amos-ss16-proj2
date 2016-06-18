package de.developgroup.mrf.rover.pcf8591;

import com.google.inject.Inject;
import com.pi4j.io.i2c.I2CDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PCF8591ADConverterImpl implements PCF8591ADConverter {

    private static Logger LOGGER = LoggerFactory.getLogger(PCF8591ADConverterImpl.class);

    /**
     * The i2CDevice that represents this A/D converter.
     */
    private I2CDevice i2CDevice;

    @Inject
    public PCF8591ADConverterImpl(I2CDevice i2CDevice) {
        this.i2CDevice = i2CDevice;
    }

    @Override
    public int getChannelValue(InputChannel channel) throws IOException {
        return doGetChannelValue((byte)channel.getValue());
    }

    /**
     * Private methods for writing/reading raw byte data from the device.
     * @param sensorNumber the sensor number to read; value in [0..3]
     * @return the value that was read.
     */
    private int doGetChannelValue(byte sensorNumber) throws IOException {
        byte command = (byte)((0x40) | (sensorNumber & 0x3));
        i2CDevice.write(command);
        // empty read to ensure getting correct values
        i2CDevice.read();
        return i2CDevice.read();
    }
}
