package entity;

import core.AssetManager;
import core.gameData;
import core.polygon3D;
import core.vector;

import java.awt.*;

public abstract class Tank extends solidObject {
    public vector bodyCenter, turretCenter;
    public polygon3D[] body, turret;
    // a screen space boundary which is used to test if the harvester object is
    // visible from camera point of view
    public final static Rectangle visibleBoundary = new Rectangle(-70, -25,screen_width+140, screen_height+85);

    // a screen space boundary which is used to test if the entire harvester
    // object is within the screen
    public final static Rectangle screenBoundary = new Rectangle(40, 40, screen_width-90,screen_height-80);

    // a screen space boundary which is used to test if the vision polygon of
    // the object is visible.
    public final static Rectangle visionBoundary = new Rectangle(0, 0, 1400+(screen_width-768),1300+(screen_height-512));

    public final static int visionW = 500 + (screen_width-768);
    public final static int visionH = 650 + (screen_height-512);

    //a bitmap representation of the vision of the tank for enemy commander
    public static boolean[] bitmapVisionForEnemy;
    public static boolean[] bitmapVisionGainFromAttackingUnit;

    //the angle that the tank have rotated between current  frame and previous frame
    public int bodyAngleSum;

    //destination angle
    public int destinationAngle;

    //whether light tank has ling of sight to its target
    public boolean hasLineOfSightToTarget;

    //the oreintation of the tank
    public int bodyAngle, turretAngle;


}