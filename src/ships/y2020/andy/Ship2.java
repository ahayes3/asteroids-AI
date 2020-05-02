package ships.y2020.andy;

import asteroidsfw.Vector2d;
import asteroidsfw.ai.AsteroidPerception;
import asteroidsfw.ai.Perceptions;
import asteroidsfw.ai.ShipControl;
import asteroidsfw.ai.ShipMind;

import java.util.Arrays;
import java.util.Comparator;

public class Ship2 implements ShipMind {
	private ShipControl control;
	private AsteroidPerception target;
	
	@Override
	public void init(ShipControl shipControl) {
		control = shipControl;
		target = null;
	}
	
	@Override
	public void think(Perceptions perceptions, double v) {
		control.rotateRight(false);
		control.rotateLeft(false);
		target = Arrays.stream(perceptions.asteroids()).min(Comparator.comparingInt(AsteroidPerception::radius)).orElse(null);
		if(target!= null) {
			Vector2d targetDir = target.v().normalize();
			float angle = (float) Math.acos(targetDir.dot(control.direction())/(targetDir.length()*control.direction().length()));
			if(angle < 0)
				control.rotateLeft(true);
			else if(angle > 0)
				control.rotateRight(true);
		}
	}
}
