MonomeGrid : MonomeDevice {
	var <>keyHandler, <>tiltHandler, <>ledHandler;
	var <rows, <cols, <gridState, <tiltHistory;

	*new { |argServer, argKeyFx = nil, argTiltFx =  nil, argLEDFx = nil|
		^super.new(argServer, "grid").init(argServer, argKeyFx, argTiltFx, argLEDFx);
	}

	init { |argServer, argKeyFx = nil, argTiltFx =  nil, argLEDFx = nil|
		super.init(argServer, "grid");
		oscHandler.addResponders(this.recv);
		this.tiltSet(0, 0);
		gridState = Array2D.new(8, 8);

		/* There is probably a better way to do a pointer to a function. */
		if(argTiltFx.isNil
			, { tiltHandler = { |n, p, r, i | this.defaultTiltHandler(n, p, r, i); } }
			, { tiltHandler = argTiltFx; } );
		if(argKeyFx.isNil
			, { keyHandler =  { |x, y, s| this.defaultKeyHandler(x, y, s); } }
			, { keyHandler = argKeyFx; } );
		if(argLEDFx.isNil
			, { ledHandler =  { |x, y, i| this.defaultLEDHandler(x, y, i); } }
			, { ledHandler = argLEDFx; } );

		"Initialized a MonomeGrid.".postln;
	}

	recv {
		var r_key, r_tilt;

		r_key = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort|
				keyHandler( msg.at(1).asInt, msg.at(2).asInt, msg.at(3).asInt );
		}, (prefix ++ "/grid/key").asSymbol, oscHandler.server, oscHandler.client.port, nil);

		r_tilt = OSCFunc.newMatching(
			{ |msg, time, fromAddr, recvdOnPort|
				tiltHandler(msg.at(1).asFloat, msg.at(2).asFloat, msg.at(3).asFloat, msg.at(4).asFloat);
		}, (prefix ++ "/tilt").asSymbol, oscHandler.server, oscHandler.client.port, nil);

		^[r_key, r_tilt];
	}

	// Overrides MonomeDevice.setSize(int)
	// setSize creates a new state array.
	setSize { |msg|
		rows = msg.at(1).asInt;
		cols = msg.at(2).asInt;
		gridState = Array2D.new(rows, cols);
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
		oscHandler.send([this.makeOSCpath("led/all"), s]);
	}

	ledSet { |x, y, s|
		oscHandler.send([this.makeOSCpath("led/set"), x, y, s]);
	}

	ledCol { |col, map|
	}

	ledRow { |row, map|
	}

	// ?
	ledIntensity { |i|
		oscHandler.send([this.makeOSCpath("led/intensity"), i]);
	}

	ledLevel { |x, y, i|
		oscHandler.send([this.makeOSCpath("led/level/set"), x, y, i]);
	}

	ledLevelAll { |i|
		oscHandler.send([this.makeOSCpath("led/level/all"), i]);
	}

	ledLevelRow { |x_off, y_off, i|
		oscHandler.send([this.makeOSCpath("/led/level/set"), x_off, y_off, i]);
	}

	tiltSet { |id, s|
		oscHandler.send([this.makeOSCpath("/tilt/set"), id, s]);
	}

	printOn { arg stream;
		stream << "MonomeGrid("
		<< "\n\t  osc:\t\t"   << oscHandler.asString
		<< "\n\t, id:\t\t"    << id
		<< "\n\t, post:\t\t"  << postfix
		<< "\n\t, pre:\t\t"   << prefix
		<< "\n\t, s:\t\t\t"   << size
		<< "\n\t, r:\t\t\t"   << rot
		<< "\n\t, keyFx:\t\t" << keyHandler
		<< "\n\t, tiltFx:\t"  << tiltHandler
		<< "\n\t, ledFx:\t\t" << ledHandler << ")";
	}
}