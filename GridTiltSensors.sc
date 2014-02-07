GridTiltSensors {
	var rollSensors, pitchSensors, invSensors;
	classvar historySize = 1024;

	*new { var cardinality = 1;
		^super.new.init(cardinality);
	}

	init { var c = 1;
		rollSensors    = LinkedList.new;
		pitchSensors   = LinkedList.new;
		invSensors     = LinkedList.new;
		this.addSensor;
	}

	addSensor {
		pitchSensors.add(CircularFloatQueue(historySize));
		rollSensors .add(CircularFloatQueue(historySize));
		invSensors  .add(CircularFloatQueue(historySize));
	}

	activeSensors {
		^invSensors.size;
	}

	updateSensor { |sensor, pitch, roll, inv|
		rollSensors .at(sensor).enqueue(roll);
		pitchSensors.at(sensor).enqueue(pitch);
		invSensors  .at(sensor).enqueue(inv);
	}

	readSensor { |sensor|
		var pitch, roll, inv;
		pitch = pitchSensors.at(sensor).dequeue;
		roll  = rollSensors .at(sensor).dequeue;
		inv   = invSensors  .at(sensor).dequeue;
		^[pitch, roll, inv];
	}

	clear { |s|
		pitchSensors.at(s).clear;
		rollSensors .at(s).clear;
		invSensors  .at(s).clear;
	}

	clearAll {
		(Array.series(this.activeSensors)).do({ |s, idx|
			this.clear(s);
		});
	}
}