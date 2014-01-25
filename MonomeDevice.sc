/* MonomeGrid.sc
 * Author: Marc J. Szalkiewicz
 * Date:   2014 Jan 25
 */

MonomeDevice {
	var <id, <prefix, <size, <rot, <deviceType;
	var <oscHandler;

	/* Notes:
	 * This class cannot be instantiated directly.
	 * A device's `destination port' is the port to which the device sends its messages.
	 */

	*new { arg argServer, argType; ^super.new.deviceInit(argServer, argType); }

	deviceInit { arg argServer, argType;
		id = nil; prefix = nil; size = nil; rot = nil;
		oscHandler = OSCHandler.new(argServer);
		deviceType = argType;
		oscHandler.addResponders(this.recv); /* Calls subclass' recv method if this = sublcass */
		this.setClientAddr(oscHandler.client);
		this.sysInfo;
		this.waitForReady;
	}

	waitForReady {
		fork {
			while({ this.isReady == false }, { 0.1.wait; });
			this.whenReadyAction;
			NetAddr.localAddr.sendMsg('/monomedevice/register', id, deviceType, prefix, size, rot);
		};
	}

	showReady {
		this.subclassResponsibility(thisMethod);
	}

	isReady {
		^(id.isNil.not && prefix.isNil.not && size.isNil.not && rot.isNil.not);
	}

	whenReadyAction {
		this.subclassResponsibility(thisMethod);
	}

	makeOSCpath { |msg|
		var toSend;
		if(msg.at(0) == "/", { toSend = prefix ++ msg; }, { toSend = prefix ++ "/" ++ msg; });
		^toSend;
	}

	close {
		oscHandler.close;
	}

	// =====================================================================

	recv {
		var r_id, r_port, r_host, r_prefix, r_rot, r_sz;

		r_id = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort|
			id = msg.at(1).asString; },
			'/sys/id',
			oscHandler.server,
			oscHandler.client.port,
			nil);
		r_port = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort|
			oscHandler.client.port = msg.at(1).asInt; },
			'/sys/port',
			oscHandler.server,
			oscHandler.client.port,
			nil);
		r_prefix = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort|
			prefix = msg.at(1).asString; },
			'/sys/prefix',
			oscHandler.server,
			oscHandler.client.port,
			nil);
		r_rot = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort|
			rot = msg.at(1).asInt; },
			'/sys/rotation',
			oscHandler.server,
			oscHandler.client.port,
			nil);
		r_sz = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort|
			this.setSize(msg); },
			'/sys/size',
			oscHandler.server,
			oscHandler.client.port,
			nil);
		r_host = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort|
			oscHandler.client.hostname_(msg.at(1).asString); },
			'/sys/host',
			oscHandler.server,
			oscHandler.client.port,
			nil);

		^[r_id, r_prefix, r_rot, r_sz, r_host, r_port];
	}

	sysInfo {
		oscHandler.send(["/sys/info", oscHandler.client.hostname, oscHandler.client.port]);
	}

	setSize { |msg|
		this.subclassResponsibility(thisMethod);
	}

	rot_ { |r|
		if(r == 0 || r == 90 || r == 180 || r == 270, {
			oscHandler.send(["/sys/rotation", r]);
			rot = r;
		});
	}

	prefix_ { |p|
		if(p.at(0) != "/", { p = "/" ++ p; });
		oscHandler.send(["/sys/prefix", p]);
		prefix = p;
	}

	setClientAddr { |a|
		this.setClientPort(a.port);
		this.setClientHost(a.hostname);
	}

	setClientPort { |p|
		if(p > 1025 && p < 65536,
			oscHandler.send(["/sys/port", p]);
		);
	}

	setClientHost { |h|
		oscHandler.send(["/sys/host", h]);
	}

	printOn { arg stream;
		stream << this.objInfo;
	}

	objInfo {
		^( "MonomeDevice("
			++ "osc: " ++ oscHandler.asString
			++ ", id: "  ++ id
			++ ", pre: " ++ prefix
			++ ", s: " ++ size
			++ ", r: " ++ rot
			++ ")" );
	}
}