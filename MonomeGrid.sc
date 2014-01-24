MonomeGrid : MonomeDevice {
	var <>keyHandler, <>tiltHandler, <>ledHandler;
	var <rows, <cols, <gridState, <tiltHistory;

	*new { |argServer, argID, /* argPostfix, */ argKeyFx, argTiltFx, argLEDFx|
		[ argServer, argID, argPostfix ].postln;
		^super.new.init(argServer, argID, "grid", argKeyFx, argTiltFx, argLEDFx);
	}

	init { arg argServer, argID, argPostfix, argKeyFx, argTiltFx, argLEDFx;
		[ argServer, argID, argPostfix ].postln;
		super.init(argServer, argID, argPostfix);

		oscResponders.addAll(this.recv);
		this.tiltSet(0, 0);
		gridState = Array2D.new(rows, cols);

		if(argTiltFx.isNil
			, { tiltHandler = this.defaultTiltHandler; }
			, { tiltHandler = argTiltFx; } );
		if(argKeyFx.isNil
			, { keyHandler = this.defaultKeyHandler; }
			, { keyHandler = argKeyFx; } );
		if(argLEDFx.isNil
			, { ledHandler = this.defaultLEDHandler; }
			, { ledHandler = argLEDFx; } );
	}

	recv {
		var r_key, r_tilt;

		r_key = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort|
				keyHandler( msg.at(1).asInt, msg.at(2).asInt, msg.at(3).asInt );
		}, (prefix ++ "/grid/key").asSymbol, server, client.port, nil);

		r_tilt = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort|
				tiltHandler(msg.at(1).asFloat, msg.at(2).asFloat, msg.at(3).asFloat, msg.at(4).asFloat);
		}, (prefix ++ "/tilt").asSymbol, server, client.port, nil);

		^[r_key, r_tilt];
	}

	// Overrides MonomeDevice.setSize(int)
	setSize { |msg|
		super.setSize(msg);
		rows = msg.at(0).asInt;
		cols = msg.at(1).asInt;
		size = rows * cols;
	}

	updateGridState { |x, y, i|
		gridState.put(x,y,i);
	}

	defaultKeyHandler { |x, y, s|
		this.updateGridState(x, y, s);
		this.ledSet(x, y, s);
	}

	defaultTiltHandler { |id, pitch, roll, inv|
		[id, pitch, roll, inv].postln;
	}

	defaultLEDHandler { |x, y, i|
		this.ledSet(x, y, ( i != 0));
	}

	ledAll { |s|
		this.send([this.makeOSCpath("led/all"), s]);
	}

	ledSet { |x, y, s|
		this.send([this.makeOSCpath("led/set"), x, y, s]);
	}

	ledCol { |col, map|
	}

	ledRow { |row, map|
	}

	// ?
	ledIntensity { |i|
		this.send([this.makeOSCpath("led/intensity"), i]);
	}

	ledLevel { |x, y, i|
		this.send([this.makeOSCpath("led/level/set"), x, y, i]);
	}

	ledLevelAll { |i|
		this.send([this.makeOSCpath("led/level/all"), i]);
	}

	ledLevelRow { |x_off, y_off, i|
		this.send([this.makeOSCpath("/led/level/set"), x_off, y_off, i]);
	}

	tiltSet { |id, s|
		this.send([this.makeOSCpath("/tilt/set"), id, s]);
	}

	storeArgs { arg stream;
		^super.storeArgs(stream).add(gridState);
	}
}