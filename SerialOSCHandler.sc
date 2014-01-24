SerialOSCHandler : OSCHandler {
	var <deviceSet;

	*new {
		^super.new.init;
	}

	init {
		super.init(NetAddr.new("localhost", 12002));
		deviceSet = Set.new;
		oscResponders.addAll(this.recv);
		"Initialized a SerialOSCHandler".postln;
		this.requestNotify;
		/* this.listDevices; */
	}

	listDevices {
		this.send(["/serialosc/list", client.hostname, client.port]);
	}

	requestNotify {
		this.send(["/serialosc/notify", client.hostname, client.port]);
	}

	handleListedDevice { |msg|
		var id, type, port, devAt;
		id    = msg.at(1).asString;
		type  = msg.at(2).asString;
		port  = msg.at(3).asInt;
		devAt = NetAddr.new(server.hostname, port);

		case
		{ type.containsi("arc")    == true } {
			(	this.class.asString
				++ ":\tDiscovered Arc  "
				++ id ++ "@"
				++ server.hostname ++ ":" ++ port ++ "."
			).postln;
			this.addDevice(MonomeArc.new(devAt, id, nil, nil, nil));
		}
		{ type.containsi("monome") == true } {
			(this.class.asString
				++ ":\tDiscovered Grid "
				++ id ++ "@"
				++ server.hostname ++ ":" ++ port ++ "."
			).postln;
			this.addDevice(MonomeGrid.new(devAt, id, nil, nil, nil));
		};
	}

	removeDevice { |msg|
		var id;
		id  = msg@1;
		deviceSet.do({ |obj, idx|
			if(obj.id.compare(id) == 0, deviceSet.remove(idx));
		});
	}

	addListedDevice { |newDevice|
		var found, newID;
		found = false;
		newID = newDevice.id;

		deviceSet.do({ |dev, idx|
			if( dev.id.compare(newID) == 0,
				found = true);
		});
		if( found == false,
			deviceSet.add(newDevice));
		^found;
	}

	recv {
		var d, a, r;
		d = OSCFunc.newMatching(
			{ |msg, time, addr, recvPort| this.handleListedDevice(msg); },
			'/serialosc/device', server, client.port, nil);

		a = OSCFunc.newMatching(
			{ |msg, time, addr, recvPort| this.handleListedDevice(msg); },
			'/serialosc/remove', server.hostname, client.port, nil);

		r = OSCFunc.newMatching(
			{ |msg, time, addr, recvPort| this.removeDevice(msg); },
			'/serialosc/add', server.hostname, client.port, nil);

		^[d, a, r];
	}
}