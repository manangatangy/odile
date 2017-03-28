package com.wolfie.odile.talker;

import android.support.annotation.IntDef;

public class TalkerCommand {
    @Command int command;

    @IntDef({Command.START, Command.PAUSE})
    public @interface Command {
        int START = 0;
        int PAUSE = 1;
        int RESUME = 1;
        int STOP = 1;
    }

}
