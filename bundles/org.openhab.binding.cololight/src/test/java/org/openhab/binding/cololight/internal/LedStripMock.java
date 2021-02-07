package org.openhab.binding.cololight.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

public class LedStripMock extends Thread {
    private final Logger logger = LoggerFactory.getLogger(LedStripMock.class);
    private final DatagramSocket socket;
    private final byte[] buf = new byte[256];

    private final Map<String, String> expectation = ImmutableMap.<String, String> builder()
            // On
            .put("535a3030000000000020000000000000000000000000000000000100000000000000000004010301cf36",
                    "535a303080000000002e00000000000004c000000000000000000100000000000000003f84010301cf360d01384341414235413543304543")
            // Off
            .put("535a3030000000000020000000000000000000000000000000000100000000000000000004010301ce36",
                    "535a303080000000002e00000000000004a900000000000000000100000000000000004084010301ce360d01384341414235413543304543")
            // Status request
            .put("535a303000000000001e00000000000000000000000000000000010000000000000000000b070186",
                    "535a303080000000004100000000000004670000000000000000010000000000000000408b071600003c003c03000000000000000000000000000000000d01384341414235413543304543")
            .put("535a303000000000001e000000000000000000000000000000000100000000000000000003080101",
                    "535a303080000000002e00000000000004ce00000000000000000100000000000000004083080301cf350d01384341414235413543304543")
            // brightness
            .put("535a3030000000000020000000000000000000000000000000000100000000000000000004010301cf00",
                    "535a303080000000002e00000000000004d400000000000000000100000000000000003f84160301cf000d01384341414235413543304543")
            .put("535a3030000000000020000000000000000000000000000000000100000000000000000004010301cf0a",
                    "535a303080000000002e00000000000004d400000000000000000100000000000000003f84160301cf000d01384341414235413543304543")
            // Effect
            .put("535a3030000000000023000000000000000000000000000000000100000000000000000004010602ff04947f00",
                    "535a303080000000003100000000000005fa00000000000000000100000000000000004084010602ff04947f000d01384341414235413543304543")
            .build();

    public LedStripMock() throws SocketException {
        socket = new DatagramSocket(8900);
    }

    public void run() {
        boolean running = true;

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                byte[] receivedBytes = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, receivedBytes, 0, packet.getLength());
                String received = new String(receivedBytes, 0, receivedBytes.length);
                if ("end".equalsIgnoreCase(received)) {
                    logger.warn("Shutting down!!!!");
                    running = false;
                    continue;
                }

                logger.trace("Received: {}", LedStripDriver.bytesToHex(receivedBytes));
                byte[] send = new BigInteger(expectation.get(LedStripDriver.bytesToHex(receivedBytes)), 16)
                        .toByteArray();
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(send, send.length, address, port);

                socket.send(packet);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        socket.close();
    }
}
