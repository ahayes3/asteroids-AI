package ships.y2020.andy;

import asteroidsfw.*;
import asteroidsfw.ai.AsteroidPerception;
import asteroidsfw.ai.Perceptions;
import asteroidsfw.ai.ShipControl;
import asteroidsfw.ai.ShipMind;

public class MyShip implements ShipMind
{
	private ShipControl control;
	private int state; //states: 1 is aiming, //2 is dodging //3 is get out of station
	@Override
	public void init(ShipControl shipControl)
	{
		control = shipControl;
		state = 3;
	}

	@Override
	public void think(Perceptions perceptions, double v)
	{
		switch(state)
		{
			case 1 -> aim(perceptions);
		}
	}

	public void aim(Perceptions perceptions)
	{

	}
	public void dodge(Perceptions perceptions)
	{

	}
	public boolean willHitAny(Perceptions perceptions)
	{
		//Vector2d trajectory = ((control.pos().$plus(control.v())).$plus(control.direction().$times(150)));
		Vector2d trajectory = control.v().$plus(control.direction().$times(150));
		if(trajectory.length() > 100)
		{
			trajectory = trajectory.normalize().$times(100);
		}
		AsteroidPerception[] asteroids = perceptions.asteroids().clone();
		for(AsteroidPerception ast:asteroids)
		{
			
			if(willHit(ast,trajectory))
				return true;
		}
		return false;
	}
	public boolean willHit(AsteroidPerception ast,Vector2d trajectory)
	{
		Vector2d toTarget = ast.pos().$minus(control.pos());
		float a = (float) ((ast.v().dot(ast.v())) - (trajectory.sqLength()));
		float b = (float) (2 * ast.v().dot(toTarget));
		float c = (float) toTarget.dot(toTarget);

		float p = -b / (2*a);
		float q = (float) (Math.sqrt((b*b) - 4 * a * c)/(2*a));

		float plus = p + q;
		float minus = p - q;
		float framesToCollision;
		if(minus > plus && plus > 0)
			framesToCollision = plus;
		else
			framesToCollision = minus;

		Vector2d targetMovedPosition = ast.pos().$plus(ast.v().$times(framesToCollision));
		Vector2d bulletInterceptPos = trajectory.$times(framesToCollision);

		return targetMovedPosition.$minus(bulletInterceptPos).sqLength() < ast.radius() * ast.radius();

	}
}
