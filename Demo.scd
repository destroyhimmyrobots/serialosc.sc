/* Demo.scd
 * Author: Marc J. Szalkiewicz
 * Date:   2014 Jan 25
 */

(
/* Set this before starting */
var devID, t;
devID = \m0001021;

t = Task({
	var devices, grid, newKeyFx;

	/* Let's listen for monome devices! */
	devices = SerialOSCHandler.new;

	/* Let me get a direct reference to a grid. */
	1.wait;	grid = devices[devID];

	/* Tilt can be turned on and off. */
	1.wait; "Tilt on. ".postln; grid.tiltSet(0, 1);
	2.wait; grid.tiltSet(0, 0); "Tilt off.".postln;

	/* If you press keys here, nothing useful will happen. */
	"Press some keys.".postln;

	/* Let's handle the keys in a new way.
	* You should never change the arguments to this function
	* without modifying the MonomeGrid class. */
	newKeyFx = { |x, y, s, self|
		/* A simple LED toggler. */

		/* You must always use the `self' variable to reference
		 * variables and functions in MonomeGrid and MonomeArc. */

		/* One important variable is the self-explanatory `gridState' */
		var nextState = (self.getGridState(x, y) == 0).asInt;

		if(s != 0, {
			/* Update the state like so. */
			self.setGridState(x, y, nextState);
			/* LED functions can be found in MonomeGrid.sc near the bottom.
 			 * be sure to use `f.value(args)' notation with these handlers. */
			self.ledHandler.value(x, y, nextState);
		});
	};

	/* You can write a tilt handler or a
	 * led handler in a similar fashion.
	 * Maybe I will make examples later. */
	5.wait; grid.keyHandler = newKeyFx;
	"Try pressing keys now.".postln;

	/* Before reinstantiating a new SerialOSCHandler,
	* or before terminating your program, close the
	* current SerialOSCHandler. */
	5.wait; devices.close;
});

t.play;
)

(
/* Let's listen for monome devices! */
d = SerialOSCHandler.new;
)
/* Let me get a direct reference to a grid. */
grid = d[\m0001021];
grid.animation;
grid.rot_(0);
grid.ledSet(0, 9, 1);
grid.ledSet(0, 9, 0);
grid.gridState;

grid.rows;