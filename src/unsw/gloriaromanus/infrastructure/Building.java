package unsw.gloriaromanus.infrastructure;

/**
 * A base class for every type of building we have
 */
public abstract class Building {
    private int cost;
    private int buildTime;
    private int level;

    public void upgrade() {
        //actually upgrade the building to a higher level
    }

    public void checkUpgrade() {
        //return the cost and buildtime of the next building level
    }
}
