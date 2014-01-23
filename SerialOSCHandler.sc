SerialOSCHandler : OSCHandler {
	var <deviceSet;

	*new {
		^super.new.serialOSCHandlerInit(NetAddr.new("localhost", 12002));
	}

	serialOSCHandlerInit { |sa|
		super.oscHandlerInit(sa);
		deviceSet = Set.new;
		oscResponders.addAll(this.recv);
		this.requestNotify;
		this.listDevices;
	}

	listDevices {
		this.send(["/serialosc/list", clientAddr.hostname, clientAddr.port]);
	}

	requestNotify {
		this.send(["/serialosc/notify", clientAddr.hostname, clientAddr.port]);
	}

	handleListedDevice { |msg|
		var id, type, port, devAt;
		id    = msg.at(1).asString;
		type  = msg.at(2).asString;
		port  = msg.at(3).asInt;
		devAt = NetAddr.new(serverAddr.hostname.asString, port);

		case
		{ type.containsi("arc")    == true } {
			(	this.class.asString
				++ ":\tDiscovered Arc  "
				++ id ++ "@"
				++ serverAddr.hostname.asString ++ ":" ++ port.asString ++ "."
			).postln;
			this.addDevice(MonomeArc.new(id, devAt));
		}
		{ type.containsi("monome") == true } {
			(this.class.asString
				++ ":\tDiscovered Grid "
				++ id ++ "@"
				++ serverAddr.hostname.asString ++ ":" ++ port.asString ++ "."
			).postln;
			this.addDevice(MonomeGrid.new(id, devAt));
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
			{ |msg, time, fromAddr, recvdOnPort| this.handleListedDevice(msg); },
			'/serialosc/device');

		a = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort| this.handleListedDevice(msg); },
			'/serialosc/remove', clientAddr.hostname, clientAddr.port);

		r = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort| this.removeDevice(msg); },
			'/serialosc/add', clientAddr.hostname, clientAddr.port);

		^[d, a, r];
	}
}