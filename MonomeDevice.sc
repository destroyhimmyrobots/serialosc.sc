MonomeDevice {
	var <postfix;
	var <id, <prefix, <size, <rot;
	var <oscHandler;

	*new { |argServer, argPostfix|
		^super.new.init(argServer, argPostfix);
	}

	init { |argServer, argPostfix|
		oscHandler = OSCHandler.new(argServer);
		postfix    = argPostfix;
		oscHandler.addResponders(this.recv);
		this.sysInfo;
		"Initialized a MonomeDevice.".postln;
	}

	makeOSCpath { |msg|
		var toSend;
		if(msg.at(0) == "/",
			toSend = prefix ++ "/" ++ postfix ++ msg,
			toSend = prefix ++ "/" ++ postfix ++ "/" ++ msg);
		^toSend;
	}

	close {
		oscHandler.closeOSCResponders;
	}

	// =====================================================================

	recv {
		var r_id, r_port, r_host, r_prefix, r_rot, r_sz;

		r_id     = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort| id = msg.at(1).asString; },
			'/sys/id', nil, nil, nil);
		r_port   = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort| oscHandler.server.port_(msg.at(1).asInt); },
			'/sys/port', oscHandler.server, oscHandler.client.port, nil);
		r_prefix = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort| prefix = msg.at(1).asString; },
			'/sys/prefix', oscHandler.server,	oscHandler.client.port, nil);
		r_rot    = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort| rot    = msg.at(1).asInt; },
			'/sys/rotation', oscHandler.server, oscHandler.client.port, nil);
		r_sz     = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort| this.setSize(msg); },
			'/sys/size', oscHandler.server, oscHandler.client.port, nil);
		r_host   = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort| oscHandler.server.hostname_(msg.at(1).asString); },
			'/sys/host', oscHandler.server, oscHandler.client.port, nil);

		^[r_id, r_port, r_prefix, r_rot, r_sz, r_host, r_port];
	}

	sysInfo {
		oscHandler.send(["/sys/info", oscHandler.client.hostname, oscHandler.client.port]);
	}

	setSize { |msg|
		this.subclassResponsibility(thisMethod);
	}

	// =====================================================================

	rot_ { |r|
		if(r == 0 || r == 90 || r == 180 || r == 270,
			oscHandler.send(["/sys/rotation", r]);
			rot = r;
		);
		this.sysInfo;
	}

	prefix_ { |p|
		oscHandler.send(["/sys/prefix", p]);
		prefix = p;
		this.sysInfo;
	}

	setClientPort { |p|
		if(p > 1025 && p < 65536,
			oscHandler.send(["/sys/port", p]);
			oscHandler.server.port_(p);
		);
		this.sysInfo;
	}

	host_ { |h|
		oscHandler.send(["/sys/host", h]);
		oscHandler.client.hostname_(h);
		this.sysInfo;
	}

	printOn { arg stream;
		stream << "MonomeDevice("
		<< "\n\t  osc:\t"   << oscHandler.asString
		<< "\n\t, id:\t"    << id
		<< "\n\t, post:\t"  << postfix
		<< "\n\t, pre:\t"   << prefix
		<< "\n\t, s:\t\t"   << size
		<< "\n\t, r:\t\t"   << rot << ")";
	}

}