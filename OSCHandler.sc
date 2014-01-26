/* OSCHandler.sc
 * Author: Marc J. Szalkiewicz
 * Date:   2014 Jan 25
 */

OSCHandler {
	var <>server, <>client, <oscResponders;

	*new { arg argServer;
		^super.new.init(argServer);
	}

	init { arg argServer;
		server        = argServer;
		client        = this.makeClient;
		oscResponders = Set.new;
	}

	empty {
		oscResponders.do({ |r, i| r.disable; r.clear; r.free; });
		oscResponders.makeEmpty;
		/* oscResponders = Set.new; */
	}

	close {
		this.empty;
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

		if(udpOK == false, { clientPort = NetAddr.langPort } );

		^NetAddr.new(NetAddr.localAddr.hostname.asString, clientPort);
	}

	addResponders { |argArray|
		/* addAll can return a new collection, so assign the result as well. */
		oscResponders = oscResponders.addAll(argArray);
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
		stream << "OSCHandler(server: " << server.asString << ", client: " << client.asString << ")" /* << "\n" << oscResponders.asString << "\n\n" */;
	}
}