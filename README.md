[![Build Status](https://travis-ci.org/SolaceSamples/solace-samples-java.svg?branch=master)](https://travis-ci.org/SolaceSamples/solace-samples-java)

# Live Video Streaming over Solace (Broadcast Me) Samples
## Overview

These samples (via JCSMP API illustrates how simple live video streaming over Solace can be achieved. 

The following diagram illustrates the flow of live stram from the live streaming source to the receiving device:

Live Source --(UDP)--> InputProxy --(SMF[UDP])--> Solace --(SMF[UDP])--> OutputProxy --(UDP) --> Receiving device

- The InputProxy expects and listens on a udp port for the live stream. It then encapsulate the udp packets into SMF as 
bineary attachment and forward them to Solace on a topic.  Here a topic can be viewed as the 'channel name' where the 
broadcaster is streaming the live video to. The delivery mode can be persistent or direct.

- ffmpeg can be used to broadcast live stream to an udp port using the mpegts transport protocol. An example command would be:

        ffmpeg -f video4linux2 -i /dev/video0 -b 900k -f mpegts udp://localhost:1235

- The OutputProxy creates a temporary queue with a topic (i.e. stream channel) subscription.  It then read off messages 
from its temporary queue and redirect the decapsulated UDP packet to the specified forwarding host and port.

- Network stream viewing programes such as VLC can then be used to pick up the redirected live stream based on the
forwarding host and port specified by the OutputProxy.  For example, from VLC, open a network source to an URL such as:

        udp://@127.0.0.1:1239


## Build the Samples

Just clone and build. For example:

  1. clone this GitHub repository
  1. `./gradlew assemble`


## Running the Samples

To try individual samples, build the project from source and then run samples like the following:

    ./build/staged/bin/inputProxy <msg_backbone_ip:port> <message-vpn> <username> <topic> <persistent/direct> <udp port> <verbose/none>

    ./build/staged/bin/outputProxy <msg_backbone_ip:port> <message-vpn> <username> <topic> <redirect_host_ip> <redirect_host_port> <verbose/none>


## License

This project is licensed under the Apache License, Version 2.0. - See the [LICENSE](LICENSE) file for details.


## Resources

For more information try these resources:

- The Solace Developer Portal website at: http://dev.solace.com
- Get a better understanding of [Solace technology](http://dev.solace.com/tech/).
- Check out the [Solace blog](http://dev.solace.com/blog/) for other interesting discussions around Solace technology
- Ask the [Solace community.](http://dev.solace.com/community/)
