SerialOSCHandler {
	var oscHandler, <devices;

	*new {
		^super.new.init;
	}

	init {
		devices    = Dictionary.new;
		oscHandler = OSCHandler.new(NetAddr.new("localhost", 12002));

		oscHandler.addResponders(this.recv);
		this.requestNotify;
		this.listDevices;
	}

	at { |k| ^devices.at(k); }

	size { ^devices.size; }

	close {
		devices.values.do({ |dev, i|
			dev.close;
		});
		oscHandler.close;
		devices.makeEmpty;
		/* devices = dictionary.new; */
		(this.class.asString ++ ": closed all open devices.").postln;
	}

	listDevices {
		oscHandler.send(["/serialosc/list", oscHandler.client.hostname, oscHandler.client.port]);
	}

	requestNotify {
		oscHandler.send(["/serialosc/notify", oscHandler.client.hostname, oscHandler.client.port]);
	}

	handleListedDevice { |msg|
		var id, type, port, devAt, m;
		id    = msg.at(1).asString;
		type  = msg.at(2).asString;
		port  = msg.at(3).asInt;
		devAt = NetAddr.new(oscHandler.server.hostname, port);

		if(type.containsi("arc")    == true, { m = MonomeArc .new(devAt, type, nil, nil     ); });
		if(type.containsi("monome") == true, { m = MonomeGrid.new(devAt, type, nil, nil, nil); });
		devices.removeAt(id.asSymbol);
		devices.put(id.asSymbol, m);
		(this.class.asString ++ ": added " ++ id ++ ".").postln;
	}

	removeDevice { |msg|
		var id;
		id  = msg.at(1).asString;
		devices.removeAt(id);
		(this.class.asString ++ ": removed " ++ id ++ ".").postln;
	}

	recv {
		var d, a, r, w;
		d = OSCFunc.newMatching(
			{ |msg, time, addr, recvPort| this.handleListedDevice(msg); },
			'/serialosc/device', oscHandler.server, oscHandler.client.port, nil);

		a = OSCFunc.newMatching(
			{ |msg, time, addr, recvPort| this.handleListedDevice(msg); },
			'/serialosc/remove', oscHandler.server, oscHandler.client.port, nil);

		r = OSCFunc.newMatching(
			{ |msg, time, addr, recvPort| this.removeDevice(msg); },
			'/serialosc/add', oscHandler.server, oscHandler.client.port, nil);

		w = OSCFunc.newMatching(
			{ |msg, time, addr, recvPort| this.handleReady(msg); },
			'/monomedevice/register', NetAddr.localAddr, NetAddr.langPort, nil);

		^[d, a, r, w];
	}

	handleReady { |msg|
		var k, t; k = msg.at(1).asString; t = msg.at(2).asString;
		(this.class.asString ++ ": " ++ k ++ " (" ++ t ++ ") is ready.").postln;
	}

	printOn { arg stream;
		stream << "SerialOSCHandler(" << "osc: " << oscHandler << ", dev: " << devices << ")";
	}
}