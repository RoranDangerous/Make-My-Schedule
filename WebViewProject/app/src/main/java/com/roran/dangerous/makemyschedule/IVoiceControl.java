package com.roran.dangerous.makemyschedule;

import java.util.ArrayList;

/**
 * Created by dangerous on 12/03/17.
 */

public interface IVoiceControl {
    public abstract void processVoiceCommands(ArrayList<String> voiceCommands); // This will be executed when a voice command was found

    public void restartListeningService(); // This will be executed after a voice command was processed to keep the recognition service activated

    public void setActive(boolean t);

    public void stopListening();
}
