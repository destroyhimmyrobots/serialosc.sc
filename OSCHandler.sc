OSCHandler {
	/* Need to fix the need for internal setters for this.x = y */
	var <serverAddr, <clientAddr, <oscResponders;

	*new { |sa|
		^super.new.oscHandlerInit(sa);
	}

	oscHandlerInit { |sa|
		serverAddr    = sa;
		clientAddr    = this.makeClientAddr;
		oscResponders = List.new;
	}

	closeOSCResponders {
		oscResponders.do({ |obj, i|
			obj.free;
		});
		oscResponders.removeAll;
	}

	close {
		this.closeOSCResponders;
		serverAddr.disconnect;
		clientAddr.disconnect;
	}

	makeClientAddr {
		var listenAddr;
		var udpOK = false;
		var udptries = 0;
		var clientPort = -1;

		while( { udpOK.not && (udptries < 10) }, {
			clientPort = rrand(1025, 65535);
			udpOK      = thisProcess.openUDPPort(clientPort);
			udptries   = udptries + 1;
		});

		if(udpOK.not,
			clientPort = NetAddr.langPort);

		(	this.class.asString
			++ ":\tListening for SerialOSC messages on "
			++ NetAddr.localAddr.hostname.asString
			++ ":" ++ clientPort.asString ++ "."
		).postln;

		^NetAddr.new(NetAddr.localAddr.hostname, clientPort);
	}

	recv {
		// Intentionally Left Blank
	}

	send { |msg|
		/* Hack for passing arrays to this wrapper function. */
		serverAddr.sendRaw(msg.asRawOSC);
	}
}
