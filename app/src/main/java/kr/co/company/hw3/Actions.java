package kr.co.company.hw3;

public enum Actions {
    MAINACTIVITY_ACTION("mainActivity"),
    PLAY_ACTION("play"),
    PAUSE_ACTION("pause"),
    PREPLAY_ACTION("pre"),
    NEXTPLAY_ACTION("next"),
    STARTFORGROUND_ACTION("startforeground"),
    STOPFOREGROUND_ACTION("stopforeground"),
    CLOSE_ACTION("close");

    public final String getName;
    Actions(String s) {
        this.getName="kr.co.company.hw3.foregroundservice.action."+s;
    }
}
