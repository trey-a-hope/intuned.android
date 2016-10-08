package Enums;

import com.intuned.app.R;

public enum Emotion {
    HAPPY(0, R.color.Yellow500),
    SAD(1, R.color.LightBlue100),
    ANGRY(2, R.color.Red500),
    FEARFUL(3, R.color.Teal300),
    DISGUSTED(4, R.color.Black);

    private int id;
    private int color;

    Emotion(int id, int color){
        this.id = id;
        this.color = color;
    }

    //Return Id of emotion.
    public int getValue(){
        return this.id;
    }

    //Return hex code of color associated with emotion.
    public int getColor(){
        return this.color;
    }
}
