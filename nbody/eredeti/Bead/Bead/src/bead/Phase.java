
package bead;

import java.util.Random;

/**
 *
 * @author Oliver
 */
public enum Phase {
    PHASE1(50, 80), PHASE2(60, 90), PHASE3(85, 150), PHASE4(100, 250), DONE(0, 0);
    
    private final int T1;
    private final int T2;
    
    private Phase(int t1, int t2) {
        this.T1 = t1;
        this.T2 = t2;        
    }
    
    /*
    Az aktuális fázisnak megfelelően visszaad egy t1 és t2 közötti véletlen számot
    */
    public int getRobotDelay() {
        return new Random().nextInt(T2-T1+1) + T1;
    }
    
    public Phase getNextPhase() {
        Phase[] values = Phase.values();
        return values[this.ordinal()+1 % values.length];
    }
}
