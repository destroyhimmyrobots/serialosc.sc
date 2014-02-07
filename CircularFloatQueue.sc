CircularFloatQueue {

	var fa, nextE, nextD;

	*new { var c = 2;
		^super.new.init(c);
	}

	init { var c = 2;
		fa = FloatArray(max(2, c));
		nextE = 1;
		nextD = 0;
	}

	clear {
		this.init(fa.size);
	}

	reset {
		this.clear;
	}

	size {
		^fa.size;
	}

	getNextE {
		var e = nextE;

		nextE = (nextE + 1) % fa.size;

		(nextE == nextD).if({
			nextD = (nextD + 1) % fa.size;
		});

		^e;
	}

	getNextD {
		var d = nextD;
		nextD = (nextD + 1) % fa.size;

		(nextD == nextE).if({
			nextE = (nextE + 1) % fa.size;
		});

		^d;
	}

	enqueue { |v|
		fa.wrapPut(this.getNextE, v);
	}

	dequeue {
		^fa.wrapAt(this.getNextD);
	}

	printOn { |stream|
		stream << "CircularFloatQueue(s: " << fa.size << ", e@" << nextE << ", d@" << nextD << ", fa: " << fa << ")";
	}
}