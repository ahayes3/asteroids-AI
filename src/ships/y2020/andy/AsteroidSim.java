package ships.y2020.andy;

import asteroidsfw.Vector2d;
import asteroidsfw.ai.AsteroidPerception;

public class AsteroidSim {
	
	Vector2d pos, v;
	int radius;
	
	public AsteroidSim(Vector2d pos, Vector2d v, int radius) {
		this.pos = pos;
		this.v = v;
		this.radius = radius;
	}
	public AsteroidSim(AsteroidPerception percept) {
		pos = percept.pos();
		v = percept.v();
		radius = percept.radius();
	}
	
	public AsteroidSim step(int i) {
		return new AsteroidSim(pos.$plus(v.$times(i)), v, radius);
	}
	public AsteroidSim step(float time) {
		return new AsteroidSim(pos.$plus(v.$times(time)),v,radius);
	}
	public AsteroidSim step(int i,double dt) {
		return new AsteroidSim(pos.$plus(v.$times(i).$times(dt)),v,radius);
	}
	public Vector2d pos() {
		return pos;
	}
	
	public Vector2d v() {
		return v;
	}
	
	public int radius() {
		return radius;
	}
}
