GridTiltSensor {
	var rollSensor, pitchSensor, invSensor;
	classvar historySize = 1024;

	*new {
		^super.new.init;
	}

	init {
		pitchSensor = CircularFloatQueue(historySize);
		rollSensor  = CircularFloatQueue(historySize);
		invSensor   = CircularFloatQueue(historySize);
	}

	updateSensor { |pitch, roll, inv|
		 rollSensor.enqueue( roll);
		pitchSensor.enqueue(pitch);
		  invSensor.enqueue(  inv);
	}

	readSensor {
		var pitch, roll, inv;
		pitch = pitchSensor.dequeue;
		roll  =  rollSensor.dequeue;
		inv   =   invSensor.dequeue;
		^[pitch, roll, inv];
	}

	clear {
		pitchSensor.clear;
		 rollSensor.clear;
		  invSensor.clear;
	}

	printOn { |stream|
			stream << pitchSensor;
			stream <<  rollSensor;
			stream <<   invSensor;
	}
}