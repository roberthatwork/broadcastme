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

package com.solace.demos.broadcastme;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.Topic;

import java.net.*;

public class OutputProxy implements XMLMessageListener {

    DatagramSocket dsocket;
    InetAddress address;
    String verbose;
    int port;
    public JCSMPSession session;	

    // XMLMessageListener
    public void onReceive(BytesXMLMessage message) {

	if (verbose.equals("verbose")) {
            System.out.println("Received SMF (udp size: " + message.getAttachmentContentLength() + ")" );
	}

	// Processing in callback not recommended.  For clarity, it is implemented this way for simplicity.

	// Pass through msg content to UDP stream
	DatagramPacket packet = new DatagramPacket(message.getAttachmentByteBuffer().array(), 
						   message.getAttachmentContentLength(), address, port);
	// Decapsulate and send to UDP
	try {
	    dsocket.send(packet);
	} catch (Exception ex) {
	    System.err.println("Encountered an Exception... " + ex.getMessage());
	}
    }

    // XMLMessageListener
    public void onException(JCSMPException exception) {
        exception.printStackTrace();
    }

    public void run(String[] args) {

	try {

        System.out.println("OutputProxy initializing...");

        // Create a JCSMP Session
        JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, args[0]); 		// msg-backbone ip:port
        properties.setProperty(JCSMPProperties.VPN_NAME, args[1]); 	// message-vpn
        properties.setProperty(JCSMPProperties.USERNAME, args[2]); 	// client-username (assumes no password)
	String t = args[3];
	Topic topic = JCSMPFactory.onlyInstance().createTopic(t);       // topic to subscribe to stream
	String host = args[4];						// redirect udp stream to this host
	port = Integer.parseInt(args[5]);				// redirect udp stream to this port	
	verbose = args[6];	   					// verbose
	
        session = JCSMPFactory.onlyInstance().createSession(properties);

        session.connect();

        Queue queue = session.createTemporaryQueue();

        FlowReceiver receiver = session.createFlow(queue, null, this);

        session.addSubscription(queue,topic,JCSMPSession.WAIT_FOR_CONFIRM);
        
        dsocket = new DatagramSocket();
        address = InetAddress.getByName(host);

        System.out.println("Connected.");
	System.out.println("Control-C to exit");

        receiver.start();
	Thread.sleep(1000000);

	} catch (Exception ex) {
            System.err.println("Encountered an Exception... " + ex.getMessage());
	}

    }

    public static void main(String... args) throws JCSMPException, InterruptedException {

        // Check command line arguments
        if (args.length < 7) {
            System.out.println("Usage: OutputProxy <msg_backbone_ip:port> <message-vpn> <username> <topic> <redirect_host_ip> <redirect_host_port> <verbose/none>");
            System.out.println();
            System.exit(-1);
        }

        final OutputProxy app = new OutputProxy();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
        	System.out.println("Existing...");
                app.session.closeSession();
            }
        });

        app.run(args);

    }

}
