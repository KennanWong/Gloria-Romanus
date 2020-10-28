package unsw.gloriaromanus.Commands;

import unsw.gloriaromanus.*;

public class Command implements Comparable<Command>{
    private String command;         // the command, i.e "move", "invade", "recruit"
    private Faction faction;        // faction calling the command
    private int commandInt;

    public Command(String command, Faction faction) {
        this.command = command;
        this.faction = faction;
        switch (command) {
            case "move":
                commandInt = 0;
                break;
            case "invade":
                commandInt = 1;
                break;
            case "recuirt":
                commandInt = 2;
                break;
        
        }
    }

    public String getCommand() {
        return command;
    }

    public Faction getFaction() {
        return faction;
    }

    public int getCommandInt() {
        return commandInt;
    }

    

    @Override
    /**
     * Overriding method to compare to commands, so we are able to sort by command type
     * In order
     * move --> invade --> recruit
     */
    public int compareTo(Command command) {
        if (this.commandInt > command.getCommandInt()) {
            return 1;
        } else if (this.commandInt <  command.getCommandInt()) {
            return -1;
        } else {
            return 0;
        }
    }
}
