/* Demo.scd
 * Author: Marc J. Szalkiewicz
 * Date:   2014 Jan 25
 */

(
var t = Task({
	var d, g, keys;

	/* Let's listen for monome devices! */
	d = SerialOSCHandler.new;

	/* How many devices were found? */
	1.wait; d.size.postln;

	/* Let me get a direct reference to a grid. */
	1.wait;	g = d[\m0001021];

	/* Tilt can be turned on and off. */
	1.wait; g.tiltSet(0, 1);
	1.wait; g.tiltSet(0, 0);

	/* If you press keys here, nothing useful will happen. */
	5.wait;

	/* Let's handle the keys in a new way.
	* You should always have five arguments,
	* just as below. */
	keys = { |x, y, s, self|
		/* A simple LED toggler. */

		/* One important variable is the self-explanatory
		* 2D array `gridState' */
		var nextState = (self.gridState.at(x, y) == 0).asInt;

		if(s != 0, {
			/* Update the state like so. */
			self.updateGridState(x, y, nextState);
			/* LED functions can be found in MonomeGrid.sc near the bottom. */
			self.ledSet(x, y, nextState);
		});
	};

	/* You can write a tilt handler or a
	 * custom ledHandler in a similar fashion.
	 * Maybe I will make examples later. */

	/* Execute this, and then press some keys. */
	g.keyHandler = keys;

	/* Before reinstantiating a new SerialOSCHandler,
	* or before terminating your program, close the
	* current SerialOSCHandler. */
	5.wait; d.close;
});

t.play;
)