/* MonomeGrid.sc
 * Author: Marc J. Szalkiewicz
 * Date:   2014 Jan 25
 */

MonomeGrid : MonomeDevice {
	var <>keyHandler, <>tiltHandler, <>ledHandler;
	var <rows, <cols, <gridState, <tiltHistory;

	*new { arg argServer, argType, argKeyFx = nil, argTiltFx =  nil, argLEDFx = nil;
		^super.new(argServer, argType).gridInit(argKeyFx, argTiltFx, argLEDFx);
	}

	gridInit { arg argKeyFx = nil, argTiltFx =  nil, argLEDFx = nil;
		this.tiltSet(0, 0);
		gridState = Array2D.new(8, 8);

		/* There is probably a better way to do a pointer to a function. */
		if(argTiltFx.isNil
			, { tiltHandler = { |id, pitch, roll, inv| [id, pitch, roll, inv].postln; }; }
			, { tiltHandler = argTiltFx; } );
		if(argKeyFx.isNil
			, { keyHandler = { |x, y, s|
				[x, y, s].postln;
				this.updateGridState(x, y, s);
				this.ledHandler(x, y, s); }; }
			, { keyHandler = argKeyFx; } );
		if(argLEDFx.isNil
			, { ledHandler = { |x, y, i| this.ledSet(x, y, (i != 0)); }; }
			, { ledHandler = argLEDFx; } );
	}

	showReady {
		Task({ 3.do({ 1.wait; this.ledAll(1); 1.wait; this.ledAll(0); }) }).play;
	}

	whenReadyAction {
		var r_key, r_tilt;

		r_key = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort|
				msg.postln;
				// this.keyHandler( msg.at(1).asInt, msg.at(2).asInt, msg.at(3).asInt );
		}, (prefix ++ "/grid/key").asSymbol, oscHandler.server, oscHandler.client.port, nil);

		r_tilt = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort|
				this.tiltHandler(
					msg.at(1).asInt,
					msg.at(2).asFloat,
					msg.at(3).asFloat,
					msg.at(4).asFloat);
		}, (prefix ++ "/tilt").asSymbol, oscHandler.server, oscHandler.client.port, nil);

		oscHandler.addResponders([r_key, r_tilt]);
		this.showReady;
	}

	// Overrides MonomeDevice.setSize(int) creates a new state array.
	setSize { |msg|
		rows      = msg.at(1).asInt;
		cols      = msg.at(2).asInt;
		gridState = Array2D.new(rows, cols);
		size      = rows * cols;
	}

	updateGridState { |x, y, i|
		gridState.put(x, y, i);
	}

	ledAll { |s|
		oscHandler.send([this.makeOSCpath("/grid/led/all"), s]);
	}

	ledSet { |x, y, s|
		oscHandler.send([this.makeOSCpath("/grid/led/set"), x, y, s]);
	}

	ledCol { |r, off, mask|
		if((off % 8 == 0) && (r >= 0) && (r < rows),
			{ oscHandler.send([this.makeOSCpath("/grid/led/col"), r, off, mask]); },
			{ (this.class ++ ":\tInvalid arguments to ledCol!").postln; });
	}

	ledRow { |off, c, mask|
		if((off % 8 == 0) && (c >= 0) && (c < cols),
			{ oscHandler.send([this.makeOSCpath("/grid/led/row"), off, c, mask]); },
			{ (this.class ++ ":\tInvalid arguments to ledRow!").postln; });
	}

	ledMap { |offsets, bitmask|
		if(checkMapArgs(offsets, bitmask)
			{ oscHandler.send([this.makeOSCpath("/grid/led/map"), offsets, bitmask]); },
			{ (this.class ++ ":\tInvalid arguments to ledMap!").postln; });
	}

	checkMapArgs { |o, b|
		^(     (o.size  == 2) && (o.at(0) % 0 == 8)
			&& (o.at(1) % 0 == 8) && (b.size == rows))
	}

	// ?
	ledIntensity { |i|
		oscHandler.send([this.makeOSCpath("/grid/led/intensity"), i]);
	}

	ledLevel { |x, y, i|
		oscHandler.send([this.makeOSCpath("/grid/led/level/set"), x, y, i]);
	}

	ledLevelAll { |i|
		oscHandler.send([this.makeOSCpath("/grid/led/level/all"), i]);
	}

	ledLevelRow { |x_off, y_off, i|
		oscHandler.send([this.makeOSCpath("/grid/led/level/set"), x_off, y_off, i]);
	}

	tiltSet { |id, s|
		oscHandler.send([this.makeOSCpath("/tilt/set"), id, s]);
	}

	objInfo {
		^("MonomeGrid(" ++ super.objInfo
			++ ", keyFx: " ++ keyHandler
			++ ", tiltFx: "  ++ tiltHandler
			++ ", ledFx: " ++ ledHandler ++ ")");
	}
}