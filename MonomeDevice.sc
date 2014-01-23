MonomeDevice : OSCHandler {
	var <id, <type;
	var <prefix, <host, <port, <size, <rot;

	*new { |id, type, sa|
		^super.new.monomeDeviceInit(id, type, sa);
	}

	monomeDeviceInit { |iid, itype, sa|
		super.oscHandlerInit(sa);
		id   = iid;
		type = itype;
		oscResponders.addAll(this.recv);
		this.sysInfo;
	}

	makeOSCpath { |msg|
		var toSend;
		if(msg@0 == "/",
			toSend = prefix ++ "/" ++ type ++ msg,
			toSend = prefix ++ "/" ++ type ++ "/" ++ msg);
		^toSend;
	}

	// =====================================================================
	recv {
		var r_id, r_port, r_prefix, r_rot, r_sz;

		r_id     = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort| id     = msg.at(1).asInt; },
			'/sys/id', clientAddr.hostname,	clientAddr.port);
		r_port   = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort| port   = msg.at(1).asInt; },
			'/sys/port', clientAddr.hostname, clientAddr.port);
		r_prefix = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort| prefix = msg.at(1).asString; },
			'/sys/prefix', clientAddr.hostname,	clientAddr.port);
		r_rot    = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort| rot    = msg.at(1).asInt; },
			'/sys/rotation', clientAddr.hostname, clientAddr.port);
		r_sz     = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort| size   = [msg.at(1).asInt, msg.at(2).asInt]; },
			'/sys/size', clientAddr.hostname, clientAddr.port);

		^[r_id, r_port, r_prefix, r_rot, r_sz];
	}

	sysInfo {
		this.send(["sys/info", clientAddr.hostname, clientAddr.port]);
	}

	// =====================================================================

	rot_ { |r|
		if(r == 0 || r == 90 || r == 180 || r == 270,
			this.send(["sys/rotation", r]);
			rot = r;
		);
	}

	prefix_ { |p|
		if(p.beginsWith("/"),
			p.drop(0);
		);
		this.send(["sys/prefix", p]);
		prefix = p;
	}

	port_ { |p|
		if(p > 1025 && p < 65536,
			this.send(["sys/port", p]);
			clientAddr.port_(p);
		);
	}

	host_ { |h|
		this.send(["sys/host", h]);
		clientAddr.hostname_(h);
	}

	// =====================================================================
	printOn { arg stream;
		stream << "MonomeDevice( " << type << ", " << type << ", " << rot << ")";
	}

	// for asCompileString.
	storeOn { arg stream;
		stream << "MonomeDevice.new(" << id << ", \"" << type << "\", " << serverAddr.asCompileString << ", " << ")";
	}

	// Arguments to Monome.new
	storeArgs { arg stream;
		^[id, type, serverAddr];
	}

	doesNotUnderstand { arg selector...args;
		(this.class.asString ++ ":\tunrecognized method " ++ selector);

		If(UGen.findRespondingMethodFor(selector).notNil) {
			(this.class.asString ++ ":\tUGen recognizes method " ++ selector);
		}
	}
} 