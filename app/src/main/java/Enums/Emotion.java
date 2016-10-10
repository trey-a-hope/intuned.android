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
    private int getColor(){
        return this.color;
    }

    //Return hex code of color based on user id.
    public static int getColor(int emotionId){
        switch (emotionId){
            case 0:
                return Emotion.HAPPY.getColor();
            case 1:
                return Emotion.SAD.getColor();
            case 2:
                return Emotion.ANGRY.getColor();
            case 3:
                return Emotion.FEARFUL.getColor();
            case 4:
                return Emotion.DISGUSTED.getColor();
            default:
                //User is happy be default :)
                return Emotion.HAPPY.getColor();
        }
    }
}
