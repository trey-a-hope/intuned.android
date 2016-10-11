package Enums;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import com.intuned.app.R;

public enum Font {
    CWG_Sans(0, "CWG_Sans.ttf"),
    DK_Midnight_Chalker(1, "DK_Midnight_Chalker.otf"),
    Coolvetica(2, "Coolvetica.ttf");

    private int id;
    private String name;

    Font(int id, String name){
        this.id = id;
        this.name = name;
    }

    //Return typeface of specific font.
    public Typeface getFont(AssetManager assetManager){
        return Typeface.createFromAsset(assetManager, "fonts/" + this.name);
    }

}
