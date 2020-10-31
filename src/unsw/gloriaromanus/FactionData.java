package unsw.gloriaromanus;

import unsw.gloriaromanus.*;
/**
 * This class will be used to send data between two scenes
 * it just has two fields 
 * Faction1
 * Faction2
 */
public class FactionData {
    private String faction1;
    private String faction2;

    public void setFaction1(String faction1) {
        this.faction1 = faction1;
    }

    public void setFaction2(String faction2) {
        this.faction2 = faction2;
    }

    public String getFaction1() {
        return faction1;
    }

    public String getFaction2() {
        return faction2;
    }
}
