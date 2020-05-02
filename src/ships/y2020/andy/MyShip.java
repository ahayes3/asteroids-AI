package ships.y2020.andy;

import asteroidsfw.*;
import asteroidsfw.ai.AsteroidPerception;
import asteroidsfw.ai.Perceptions;
import asteroidsfw.ai.ShipControl;
import asteroidsfw.ai.ShipMind;

import java.util.Arrays;

enum State {
	AIMING, DODGING, LEAVE_STATION;
}

//TODO list
//1: check if in danger
//2: dodge
//3: aim

//	public Vector2d step(int i,double dt) {
//		return pos.$plus(v.$times(i).$times(dt));
//	}
//	public boolean bulletCollide(int maxFrames,Vector2d trajectory) {
//		for(int i=0;i<maxFrames;i++) {
//			if(pos.$plus(v.$times(i)).$minus(trajectory.$times(i)).length() < radius)
//				return true;
//		}
//		return false;
//	}
//}
/* THINGS TO THINK ABOUT
- If trying to dodge in current direction at max speed can't accelerate
 */
public class MyShip implements ShipMind {
	private ShipControl control;
	private State state;
	private final int warnTime = 2000; //Time in millis before impact that ship will dodge asteroid
	private boolean forward,backward,left,right;
	
	@Override
	public void init(ShipControl shipControl) {
		control = shipControl;
		state = State.LEAVE_STATION;
	}
	
	@Override
	public void think(Perceptions perceptions, double v) {
		//System.out.println("Delta time "+v);
		control.shooting(false);
		control.thrustForward(true);
		control.thrustBackward(false);
		control.rotateLeft(false);
		control.rotateRight(true);
		
		AsteroidSim[] danger = getState(perceptions); //position already relative to ship
		switch (state) {
			case AIMING -> aim(perceptions,v);
			case DODGING -> dodge(perceptions,danger,v);
			case LEAVE_STATION -> leaveStation(perceptions,v);
		}
		if(simulateBullet(relativeAsteroids(perceptions.asteroids()),v)) {
			System.out.println(true);
			control.shooting(true);
		}
	}
	//TODO change simulate to use time as the scale instead of frames
	public AsteroidSim[] getState(Perceptions perceptions) {
		AsteroidSim[] danger = getDanger(perceptions);
		
		if(danger.length > 0)
			state = State.DODGING;
		else if(control.pos().$plus(control.direction().$times(14)).$minus(new Vector2d(Game.hRes() / 2f, Game.vRes() / 2f)).length() <= 20)
			state = State.LEAVE_STATION;
		else
			state = State.AIMING;
		return danger;
	}
	public void aim(Perceptions perceptions,double dt) {
	
	}
	
	public void dodge(Perceptions perceptions,AsteroidSim[] asteroids,double dt) {
		//TODO DODGE ASTEROIDS
		
	}
	
	public void leaveStation(Perceptions perceptions,double dt) {
		control.thrustForward(true);
		//TODO
	}
	
	public AsteroidSim[] getDanger(Perceptions perceptions) { //returns asteroids that will hit in 2 seconds
		AsteroidSim[] asteroids = relativeAsteroids(perceptions.asteroids());
		return Arrays.stream(asteroids).filter(p -> willHitShip(p) != -1).toArray(AsteroidSim[]::new);
	}
	
	public AsteroidSim[] relativeAsteroids(AsteroidPerception[] asteroids) {
		return Arrays.stream(asteroids).map(p -> new AsteroidSim(p.pos().$minus(control.pos()),p.v(),p.radius())).toArray(AsteroidSim[]::new);
	}
	public int willHitShip(AsteroidSim ast) { //returns number of frames until asteroid will hit ship or -1 if it won't
		Vector2d trajectory = control.v();
		Vector2d toTarget = ast.pos().$minus(control.pos());
		float a = (float) ((ast.v().dot(ast.v())) - (trajectory.sqLength()));
		float b = (float) (2 * ast.v().dot(toTarget));
		float c = (float) toTarget.dot(toTarget);
		
		float p = -b / (2 * a);
		float q = (float) (Math.sqrt((b * b) - 4 * a * c) / (2 * a));
		
		float plus = p + q;
		float minus = p - q;
		int timeToCollision;
		if (minus > plus && plus > 0)
			timeToCollision = (int) plus;
		else
			timeToCollision = (int) minus;
		
		Vector2d targetMovedPosition = ast.pos().$plus(ast.v().$times(timeToCollision));
		Vector2d shipInterceptPos = trajectory.$times(timeToCollision);
		if(targetMovedPosition.$minus(shipInterceptPos).sqLength() < (ast.radius()+control.radius())*(ast.radius()+control.radius()) && timeToCollision< 2) //will it collide within 2 seconds
			return timeToCollision;
		else
			return -1;
	}
	
	public boolean simulateBullet(AsteroidSim[] asteroids,double dt) {
		if(inStation())
			return false;
		Vector2d nextDir;
		if(right)
			nextDir = control.direction().rotate(-Ship$.MODULE$.maxAngle() * Game.frameDuration()/1000);
		else
			nextDir = control.direction().rotate(Ship$.MODULE$.maxAngle() * Game.frameDuration()/1000);
		
		float fps = Math.round(1000/Game.frameDuration()); //10 seconds
		float maxTime = (float) (10*fps * (Game.frameDuration()/1000));
		//Vector2d trajectory = ((control.v().$times()).$plus(control.direction().$times(150))).$times(dt);
		for(float i=0;i<10;i+=.005) {
			float finalI = i;
			//Arrays.stream(asteroids).map(p -> p.step(finalI)).filter(p -> p.pos().$minus(trajectory.$times(finalI)).sqLength() < p.radius()*p.radius());
			Vector2d trajectory = ((control.v().$times(i)).$plus(nextDir.$times(150).$times(i)));
			if(Arrays.stream(asteroids).map(p -> p.step(finalI)).anyMatch(p -> p.pos().$minus(trajectory).length() <p.radius()))
				return true;
		}
		return false;
	}
	public boolean inStation() {
		return control.pos().$plus(control.direction().$times(14)).$minus(new Vector2d(Game.hRes() / 2f, Game.vRes() / 2f)).length() <= 20;
	}
	public boolean firstOrderIntercept(AsteroidSim ast,double dt) {
		Vector2d targetRelVelocity = (ast.v().$times(dt)).$minus(control.v().$times(dt));
		float time = firstOrderInterceptTime(ast.pos(),targetRelVelocity,dt);
		System.out.println("Intercept time: "+time);
		Vector2d interceptPosition = ast.pos().$plus(targetRelVelocity.$times(time));
		Vector2d bulletVel = control.v().$times(dt).$plus(control.direction().$times(150).$times(dt));
		return interceptPosition.$minus(bulletVel.$times(time)).sqLength() < ast.radius()*ast.radius();
	}
	public float firstOrderInterceptTime(Vector2d pos,Vector2d vel,double dt) {
		Vector2d targetVelRelative = vel;
		float bulletSpeed = (float) control.v().$times(dt).$plus(control.direction().$times(dt).$times(150)).length();
		float velocitySquared = (float) (targetVelRelative.$times(dt)).sqLength();
		
		if(velocitySquared < .00f)
			return 0;
		float a = velocitySquared - bulletSpeed*bulletSpeed;
		if(Math.abs(a)<.001f) {
			float t = (float) (-pos.sqLength()/(2*targetVelRelative.$times(dt).dot(pos)));
			return Math.max(t,0);
		}
		
		float b = (float) (2*targetVelRelative.dot(pos));
		float c = (float) pos.sqLength();
		float determinant = b*b -4*a*c;
		
		if(determinant > 0) {
			float t1 = (float) ((-b + Math.sqrt(determinant))/(2*a));
			float t2 = (float) ((-b - Math.sqrt(determinant))/(2*a));
			if(t1 > 0 && t2 > 0)
				return Math.min(t1,t2);
			else if(t1 > 0)
				return t1;
			else
				return Math.max(t2,0);
			
		}
		else if(determinant < 0)
			return 0;
		else
			return Math.max(-b/(2*a),0);
	}
}
