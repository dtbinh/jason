import jason.architecture.AgArch;

/** Architecture for an agent that can no "sleep" */
public class CellArch extends AgArch {

    @Override
    public boolean canSleep() {
        return false;
    }
}
