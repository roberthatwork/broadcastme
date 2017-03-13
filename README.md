# Live Video Streaming over Solace (BroadcastMe) Samples
## Overview

This project illustrates how one can stream live video via Solace. Do also take a look at the blog post [here](https://solace.com/blog/use-cases/live-video-streaming-solace-part-2).  

The following diagram shows the flow of video stream from the live source to the receiving device:

![](https://github.com/roberthsieh/broadcastme/blob/master/resources/Diagrams.png)

Note: SMF stands for Solace Message Format and is the wireline message format used by the Solace Java API.

- The InputProxy expects and listens on an UDP port for the live stream. It then encapsulate the content of UDP packets into SMF as 
binary attachment and forwards them to the Solace Message Router on a topic.  In this scenario, a topic can be viewed as the 'channel name' where the broadcaster is streaming the live video to. The delivery mode can be persistent or direct.

- ffmpeg can be used to broadcast live stream to an UDP port using the mpegts transport protocol. An example command (under linux) would be:

        ffmpeg -f video4linux2 -i /dev/video0 -b 900k -f mpegts udp://localhost:1235

- The OutputProxy creates a temporary queue with a topic (i.e. stream channel) subscription.  It then receives messages 
from its temporary queue, re-encapsulates the content into UDP packets and redirect to the specified host and port.

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

If you want to learn more about Solace Technology try these resources:

- The Solace Developer Portal website at: http://dev.solace.com
- Get a better understanding of [Solace technology](http://dev.solace.com/tech/).
- Check out the [Solace blog](http://dev.solace.com/blog/) for other interesting discussions around Solace technology
- Ask the [Solace community.](http://dev.solace.com/community/)
