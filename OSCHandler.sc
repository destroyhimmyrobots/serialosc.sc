OSCHandler {
	var <server, <client, <oscResponders;

	*new { |argServer|
		^super.new.init(argServer);
	}

	init { |argServer|
		server        = argServer;
		/* Unfortunately, an actual parent port (whole object?) is created below. */
		client        = this.makeClient;
		oscResponders = List.new;
		"Initialized an OSCHandler".postln;
	}

	closeOSCResponders {
		oscResponders.do({ |obj, i|
			obj.free;
		});
		oscResponders.removeAll;
	}

	close {
		this.closeOSCResponders;
		server.disconnect;
		client.disconnect;
	}

	makeClient {
		var udpOK = false;
		var udptries = 0;
		var clientPort = -1;

		while({ (udpOK == false) && (udptries < 10) }, {
			clientPort = rrand(1025, 65535);
			udpOK      = thisProcess.openUDPPort(clientPort);
			udptries   = udptries + 1;
		});

		(	this.class.asString
			++ ":\tListening for SerialOSC messages on "
			++ NetAddr.localAddr.hostname ++ ":" ++ clientPort ++ "."
		).postln;

		if(udpOK == false, { clientPort = NetAddr.langPort } );

		^NetAddr.new(NetAddr.localAddr.hostname.asString, clientPort);
	}

	addResponders { |argArray|
		oscResponders.addAll(argArray);
	}

	recv {
		this.subclassResponsibility(thisMethod);
	}

	send { |msg|
		/* Hack for passing arrays to this wrapper function is: sendRaw(msg.asRawOSC). */
		/* Here we use 'collection as arguments to f' notation. */
		server.sendMsg(*msg);
	}

	printOn { arg stream;
		stream << "OSCHandler(server: " << server.asString << ", client: " << client.asString << /* ", " << oscResponders.asString << */ ")";
	}
}