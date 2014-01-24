MonomeDevice : OSCHandler {
	var <id, <postfix;
	var <prefix, <size, <rot, <devicePort;

	*new { |argServer, argID, argPostfix|
		^super.new.deviceInit(argServer, argID, argPostfix);
	}

	deviceInit { |argServer, argID, argPostfix|
		super.init(argServer);
		id      = argID;
		postfix = argPostfix;
		"Initialized a MonomeDevice.".postln;

		oscResponders.addAll(this.recv);
		this.sysInfo;
	}

	makeOSCpath { |msg|
		var toSend;
		if(msg.at(0) == "/",
			toSend = prefix ++ "/" ++ postfix ++ msg,
			toSend = prefix ++ "/" ++ postfix ++ "/" ++ msg);
		^toSend;
	}

	// =====================================================================

	recv {
		var r_id, r_port, r_prefix, r_rot, r_sz;

		r_id     = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort| id         = msg.at(1).asInt; },
			'/sys/id', server,	client.port, nil);
		r_port   = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort| devicePort = msg.at(1).asInt; },
			'/sys/port', server, client.port, nil);
		r_prefix = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort| prefix = msg.at(1).asString; },
			'/sys/prefix', server,	client.port, nil);
		r_rot    = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort| rot    = msg.at(1).asInt; },
			'/sys/rotation', server, client.port, nil);
		r_sz     = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort| size   = [msg.at(1).asInt, msg.at(2).asInt]; },
			'/sys/size', server, client.port, nil);

		^[r_id, r_port, r_prefix, r_rot, r_sz];
	}

	sysInfo {
		this.send(["sys/info", client.hostname, client.port]);
	}

	// =====================================================================

	rot_ { |r|
		if(r == 0 || r == 90 || r == 180 || r == 270,
			this.send(["sys/rotation", r]);
			rot = r;
		);
		this.sysInfo;
	}

	prefix_ { |p|
		if(p.beginsWith("/"),
			p.drop(0);
		);
		this.send(["sys/prefix", p]);
		prefix = p;
		this.sysInfo;
	}

	devicePort_ { |p|
		if(p > 1025 && p < 65536,
			this.send(["sys/port", p]);
			devicePort = p;
		);
		this.sysInfo;
	}

	host_ { |h|
		this.send(["sys/host", h]);
		client.hostname_(h);
		this.sysInfo;
	}

	// =====================================================================

	printOn { arg stream;
		stream << "MonomeDevice( " << postfix << ", " << postfix << ", " << rot << ")";
	}

	// for asCompileString.
	storeOn { arg stream;
		stream << "MonomeDevice.new(" << id << ", \"" << postfix << "\", " << server.asCompileString << ", " << ")";
	}

	// Arguments to Monome.new
	storeArgs { arg stream;
		^[server, id, postfix];
	}

	doesNotUnderstand { arg selector...args;
		(this.class.asString ++ ":\tunrecognized method " ++ selector);

		If(UGen.findRespondingMethodFor(selector).notNil) {
			(this.class.asString ++ ":\tUGen recognizes method " ++ selector);
		}
	}
}