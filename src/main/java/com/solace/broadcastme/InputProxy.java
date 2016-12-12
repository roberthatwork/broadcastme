/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.solace.broadcastme;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageProducer;

import java.net.*;

public class InputProxy {

    public JCSMPSession session;

    class PubCallback implements JCSMPStreamingPublishEventHandler {

         public void responseReceived(String messageID) {
		// Do nothing
            }
            public void handleError(String messageID, JCSMPException e, long timestamp) {
		// Do nothing
            }

    }

    public void run(String... args) throws JCSMPException, InterruptedException {

        System.out.println("Input initializing...");

        byte[] buffer = new byte[1500];    // Output from ffmpeg will not exceed 1500.
        byte[] data;
        DatagramPacket packet;

        // Create a JCSMP Session
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, args[0]); 		// msg-backbone ip:port
        properties.setProperty(JCSMPProperties.VPN_NAME, args[1]); 	// message-vpn
        properties.setProperty(JCSMPProperties.USERNAME, args[2]);  	// client-username (assumes no password)
	String t = args[3];
	Topic topic = JCSMPFactory.onlyInstance().createTopic(t);  	// topic to publish the smf encapsulated udp stream to
	String mode = args[4];						// direct or persistent 
        int port = Integer.parseInt(args[5]);  				// port to receive the udp stream 
	String verbose = args[6];					// verbose mode	


        session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();

        XMLMessageProducer prod = session.getMessageProducer(new PubCallback());

        // Publish-only session is now hooked up and running!
        System.out.println("Connected.");
	System.out.println("Control-C to exit");

	try {

        DatagramSocket dsocket = new DatagramSocket(port);

	while (true) {

	    // Create a new DatagramPacket each time
            packet = new DatagramPacket(buffer, buffer.length);

	    // Read data off socket
	    dsocket.receive(packet);
	    data = new byte[packet.getLength()];
	    System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

	    // Create SMF message and encapsulate
	    BytesXMLMessage msg = JCSMPFactory.onlyInstance().createMessage(BytesXMLMessage.class);
	    msg.writeAttachment(data);

	    if (mode.equals("persistent")) {
                msg.setDeliveryMode(DeliveryMode.PERSISTENT);
	    } else {
		// Everything else, even if wrong spelling, just default to direct.
                msg.setDeliveryMode(DeliveryMode.DIRECT);
	    }

            // Send message
            prod.send(msg, topic);

	    if (verbose.equals("verbose"))
 	        System.out.println("Sent SMF (udp size:" + packet.getLength() + ")" );

        }

	} catch (Exception ex) {
	   System.err.println("Encountered an Exception... " + ex.getMessage());
	}

    }

    public static void main(String... args) throws JCSMPException, InterruptedException {

        // Check command line arguments
        if (args.length < 7) {
            System.out.println("Usage: InputProxy <msg_backbone_ip:port> <message-vpn> <username> <topic> <persistent/direct> <udp port> <verbose/none>");
            System.out.println();
            System.exit(-1);
        }

        InputProxy app = new InputProxy();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Existing...");
                app.session.closeSession();
            }
        });

        app.run(args);

    }
}
