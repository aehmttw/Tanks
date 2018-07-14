package tanks.legacy;

import java.awt.Color;

import tanks.Game;
import tanks.Movable;
import tanks.Ray;
import tanks.Tank;

// kgurazada
@Deprecated
public class EnemyTankPurple extends Tank {
	double aimAngle = 0;
	int idleTimer = (int) (Math.random() * 500) + 25;
	final int cooldownCeil = 60;
	int cooldownRem = 0;
	enum State { finding, shooting, traveling };
	State s1 = State.finding; // find or shoot?
	State s2 = State.finding; // find or travel?
	public EnemyTankPurple(double x, double y, int size) {
		super("legacy-purple", x, y, size, new Color (200, 0, 200));
		this.liveBulletMax = 5;
		// TODO Auto-generated constructor stub
	}
	public EnemyTankPurple (double x, double y, int size, double a) {
		this (x, y, size);
		this.angle = a;
	}
	@Override
	public void shoot() {
		
	}
	public void update () {
		if (!(this.s1.equals(State.shooting))) { // still rotating if travelling
			this.angle += 2*Math.PI/100; // tau/2 ~~ 0.0628...
		} else {
			Ray targeter = new Ray (this.posX, this.posY, this.angle, 1, this);
			Movable aimTarget = targeter.getTarget();
			// the below should be updating for the fire alogorithm. cooldown=60ms.
			try {
				if (aimTarget.equals(Game.player)) {
					EnemyTankPurple me = this;
					Thread shootThread = new Thread () {
						public void run () {
							me.s1 = EnemyTankPurple.State.shooting;
							// set the angles and fire. set the state to firing when the thread starts and back to finding when it is done.
							me.angle += 2*Math.PI/100;
							me.shoot();
							try {
								Thread.sleep(me.cooldownCeil);
							} catch (Exception e) {
								e.printStackTrace(); // uh...
							}
							me.angle -= 2*Math.PI/100;
							me.shoot();
							try {
								Thread.sleep(me.cooldownCeil);
							} catch (Exception e) {
								e.printStackTrace(); // uh...
							}
							me.angle -= 2*Math.PI/100;
							me.shoot();
							try {
								Thread.sleep(me.cooldownCeil);
							} catch (Exception e) {
								e.printStackTrace(); // uh...
							}
							me.s1 = EnemyTankPurple.State.finding;
						}
					};
					shootThread.run();
				}
			} catch (NullPointerException npe) {
				// eh
			}
		}
		if (this.s2.equals(State.finding)) {
			Ray pathfinder = new Ray (this.posX, this.posY, this.angle, 8, this); // 8 = big number!
			Movable driveTarget = pathfinder.getTarget();
			try {
				if (driveTarget.equals(Game.player)) {
					EnemyTankPurple me = this;
					/*
					 * it's just a small thing
					 * but I can't understand why
					 * I'm the enemy.
					 */
					Thread moveThread = new Thread () {
						public void run () {
							me.s2 = EnemyTankPurple.State.traveling;
							for (int i = 0; i < pathfinder.bounceX.size(); i++) {
								me.moveToPoint(pathfinder.bounceX.get(i), pathfinder.bounceY.get(i));
							}
							me.s2 = EnemyTankPurple.State.finding;
						}
					};
					moveThread.start();
				}
			} catch (NullPointerException npe) {
				// eh, no tanks here
			}
		} else {
			// dont look around for where I am, just goddamn move as in the thread
		}
	}
	public void moveToPoint (double x, double y) { // wait why did I do this
		this.setMotionInDirection(x, y, this.maxV);
	}
}