/* MonomeGrid.sc
 * Author: Marc J. Szalkiewicz
 * Date:   2014 Jan 25
 */

MonomeGrid : MonomeDevice {
	var <>keyHandler, <>tiltHandler, <>ledHandler;
	var <rows, <cols, <gridState, tiltSensors;

	*new { arg argServer, argType, argKeyFx = nil, argTiltFx =  nil, argLEDFx = nil;
		^super.new(argServer, argType).gridInit(argKeyFx, argTiltFx, argLEDFx);
	}

	gridInit { arg argKeyFx = nil, argTiltFx =  nil, argLEDFx = nil;
		gridState      = Int16Array(256);
		tiltSensors    = GridTiltSensors(1);

		tiltHandler = (argTiltFx.isNil).if({
			{ |id, pitch, roll, inv, o = nil|
				(id > tiltSensors.activeSensors).if({
					tiltSensors.addSensor;
				});
				tiltSensors.update(id, pitch, roll, inv);
				tiltSensors.readSensor(id).postln;
			};
		}, { argTiltFx; });

		ledHandler = (argLEDFx.isNil).if({
			{ |x, y, s, o = nil| this.ledSet(x, y, s); };
		}, { argLEDFx; } );

		keyHandler = (argKeyFx.isNil).if({
			{ |x, y, s, o = nil|
				this.setGridState(x, y, s);
				this.ledHandler.value(x, y, s);
			};
		}, { argKeyFx; } );
	}

	showReady {
		Task({
			2.do({ 0.1.wait; this.ledAll(1); 0.1.wait; this.ledAll(0); });
		}).play;
	}

	close {
		this.ledAll(0);
		super.close;
	}

	animation {
		Task({
			Int16Array.series(256).do({ |int, i|
				Int8Array.series(rows).do({ |r, j|
					this.ledCol(r, 0, int);
					this.ledRow(0, r, int);
					0.02.wait;
				});
			});
			this.ledAll(0);
		}).play;
	}

	whenReadyAction {
		var r_key, r_tilt;

		this.tiltSet(0, 0);

		r_key = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort|
			this.keyHandler.value(msg.at(1).asInt, msg.at(2).asInt, msg.at(3).asInt, this);
			}
			, (prefix ++ "/grid/key").asSymbol
			, oscHandler.server
			, oscHandler.client.port
			, nil);

		r_tilt = OSCFunc.newMatching({ |msg, time, fromAddr, recvdOnPort|
			this.tiltHandler.value(msg.at(1).asInt, msg.at(2).asFloat, msg.at(3).asFloat, msg.at(4).asFloat, this);
			}
			, (prefix ++ "/tilt").asSymbol
			, oscHandler.server
			, oscHandler.client.port
			, nil);

		oscHandler.addResponders([r_key, r_tilt]);
		this.showReady;
	}

	// Overrides MonomeDevice.setSize(int); creates a new state array.
	setSize { |msg|
		rows      = msg.at(1).asInt;
		cols      = msg.at(2).asInt;
		size      = rows * cols;

		((size == 128) && ((rot == 90) || rot == 270)).if({
			rows = msg.at(2).asInt;
			cols = msg.at(1).asInt;
		});

		gridState = Int8Array.newClear(size); // Array2D.new(rows, cols);
	}

	setGridState { |x, y, i|
		/* Column-ordered */
		gridState[(x * rows) + y] = i;
	}

	getGridState { |x, y|
		/* Column-ordered */
		^gridState[(x * rows) + y];
	}

	getTiltState { |sensorID = 0|
		^tiltSensors.readSensor(sensorID);
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