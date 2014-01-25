SerialOSCHandler {
	var oscHandler, deviceLL;

	*new {
		^super.new.init;
	}

	init {
		oscHandler = OSCHandler.new(NetAddr.new("localhost", 12002));
		deviceLL = LinkedList.new;
		oscHandler.addResponders(this.recv);
		this.requestNotify;
		this.listDevices;
		"Initialized a SerialOSCHandler".postln;
	}

	listDevices {
		oscHandler.send(["/serialosc/list", oscHandler.client.hostname, oscHandler.client.port]);
	}

	requestNotify {
		oscHandler.send(["/serialosc/notify", oscHandler.client.hostname, oscHandler.client.port]);
	}

	handleListedDevice { |msg|
		var id, type, port, devAt;
		id    = msg.at(1).asString;
		type  = msg.at(2).asString;
		port  = msg.at(3).asInt;
		devAt = NetAddr.new(oscHandler.server.hostname, port);

		if(type.containsi("arc") == true, {
			(this.class.asString
				++ ":\tDiscovered Arc  " ++ id ++ "@" ++ oscHandler.server.hostname ++ ":" ++ port ++ "."
			).postln;

		});
		if(type.containsi("monome") == true, {
			(this.class.asString
				++ ":\tDiscovered Grid " ++ id ++ "@" ++ oscHandler.server.hostname ++ ":" ++ port ++ "."
			).postln;
			this.addListedDevice(MonomeGrid.new(devAt, nil, nil, nil));
		});
	}

	removeDevice { |msg|
		var id;
		id  = msg@1;
		deviceLL.do({ |obj, idx|
			if(obj.id.compare(id) == 0, deviceLL.removeAt(idx));
		});
	}

	addListedDevice { |newDevice|
		var found, newID;
		found = false;
		newID = newDevice.id;

		deviceLL.do({ |dev, idx|
			if( dev.id.compare(newID) == 0, { found = true; } );
		});

		if( found == false,	{
			deviceLL.add(newDevice);
			("Added device " ++ newDevice.asString).postln;
		});

		^found.not;
	}

	recv {
		var d, a, r;
		d = OSCFunc.newMatching(
			{ |msg, time, addr, recvPort| this.handleListedDevice(msg); },
			'/serialosc/device', oscHandler.server, oscHandler.client.port, nil);

		a = OSCFunc.newMatching(
			{ |msg, time, addr, recvPort| this.handleListedDevice(msg); },
			'/serialosc/remove', oscHandler.server, oscHandler.client.port, nil);

		r = OSCFunc.newMatching(
			{ |msg, time, addr, recvPort| this.removeDevice(msg); },
			'/serialosc/add', oscHandler.server, oscHandler.client.port, nil);

		^[d, a, r];
	}

	deviceLL {
		deviceLL.do({ |obj, i|
			i.postln;
		});
	}

	printOn { arg stream;
		stream << "SerialOSCHandler(" << "\n\t, osc: " << oscHandler << "\n\t, dev: " << deviceLL << ")";
	}
}