/** Vector quantity representing either position or velocity or acceleration. */
class Vector {

	/** Co-ordinates in a 3D space. */
	final double x, y, z;  // 3-D space

	Vector( double x, double y, double z ){ this.x=x; this.y=y; this.z=z; }

	/** Linear change. this += derivative*dt. */
	Vector euler( Vector derivative, double dt ){
		return new Vector(
			x + derivative.x * dt,
			y + derivative.y * dt,
			z + derivative.z * dt
		);
	}
}

class Body {
	final double mass;
	final Vector position;
	final Vector velocity;

	Body( double mass, Vector position, Vector velocity ){
		this.mass = mass;
		this.position = position;
		this.velocity = velocity;
	}
}

/**
	Simulation for motion of n bodies in 3D space as affected by gravity.
*/
class NBody {

	/** Gravitational constant. */
	//static final double G = 6.67408E-11;
	static final double G = 1.0;

	/** Compute acceleration for bodies[index] with respect to
	    gravitational forces from other bodies. 
	    We assume that no collision will happen.
	*/
	static Vector acceleration( int index, Body[] bodies ){
		Body bodyInvestigated = bodies[index];
		double ax = 0.0, ay = 0.0, az = 0.0;
		for( int i = 0; i < bodies.length; ++i ){
			if( i != index ){
				Body bodyConsidered = bodies[i];
				double dx = bodyInvestigated.position.x - bodyConsidered.position.x;
				double dy = bodyInvestigated.position.y - bodyConsidered.position.y;
				double dz = bodyInvestigated.position.z - bodyConsidered.position.z;
				double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);  // Euclidean distance
				double factor = G * bodyConsidered.mass / distance / distance / distance;
				ax -= dx * factor;
				ay -= dy * factor;
				az -= dz * factor;
			}
		}
		return new Vector(ax,ay,az);
	}

	/** The velocity and position of bodies[i] is recomputed according
	    to elapsed time dt. We approximate real forces with constant
	    forces and real velocity with constant linear motion over the
	    time period dt. 
	*/
	static Body update( int i, Body[] bodies, double dt ){
		Vector acceleration = acceleration(i, bodies);
		Vector newVelocity = bodies[i].velocity.euler( acceleration, dt );
		Vector newPosition = bodies[i].position.euler( newVelocity, dt);
		return new Body( bodies[i].mass, newPosition, newVelocity );
	}

	/** Perform a certain amount of steps of the n-body simulation.
	    The array is not modified, an updated array is returned.
	*/
	static Body[] simulate( Body[] bodies, double dt, int steps ){
		Body[] from = new Body[bodies.length], to = new Body[bodies.length];
		java.util.Arrays.setAll(from, i -> bodies[i]);
		for( int count=0; count<steps; ++count ){
			for( int i=0; i<to.length; ++i ){
				to[i] = update(i,from,dt);
			}
			// swap from and to
			Body[] tmp = to; to = from; from = tmp; 
		}
		return to;
	}

	static Body[] solarSystem = {
		new Body(40.0, new Vector(0.0, 0.0, 0.0), new Vector(0.0, 0.0, 0.0)), // Sun
		new Body( 40*9.54791938424326609e-04,  // Jupiter
		          new Vector(4.84143144246472090e+00, -1.16032004402742839e+00, -1.03622044471123109e-01),
		          new Vector(1.66007664274403694e-03*365, 7.69901118419740425e-03*365, -6.90460016972063023e-05*365) ),
		new Body( 40*2.85885980666130812e-04,  // Saturn
		          new Vector(8.34336671824457987e+00, 4.12479856412430479e+00, -4.03523417114321381e-01),
		          new Vector(-2.76742510726862411e-03*365, 4.99852801234917238e-03*365, 2.30417297573763929e-05*365) ),
		new Body( 40*4.36624404335156298e-05,  // Uranus
		          new Vector(1.28943695621391310e+01, -1.51111514016986312e+01, -2.23307578892655734e-01),
		          new Vector(2.96460137564761618e-03*365, 2.37847173959480950e-03*365, -2.96589568540237556e-05*365) ),
		new Body( 40*5.15138902046611451e-05,  // Neptune
		          new Vector(1.53796971148509165e+01, -2.59193146099879641e+01, 1.79258772950371181e-01),
		          new Vector(2.68067772490389322e-03*365, 1.62824170038242295e-03*365, -9.51592254519715870e-05*365) )
	};

	static Body[] randomSystem( int size ){
		Body[] system = new Body[size];
		for( int i=0; i<size; ++i ){
			system[i] = new Body( 0.001*Math.random(),
			                      new Vector(100*Math.random()-50, 100*Math.random()-50, 100*Math.random()-50),
			                      new Vector(10*Math.random()-5, 10*Math.random()-5, 10*Math.random()-5)
		                    );
		}
		return system;
	}

	public static void main( String[] args ){
		int size = Integer.parseInt(args[0]);
		int steps = Integer.parseInt(args[1]);
		Body[] bodies = randomSystem(size);
		bodies = simulate(bodies,0.0001,3);   // to warm up
		long startTime = System.currentTimeMillis();
		bodies = simulate(bodies,0.0001,steps);
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime);
	}

}

