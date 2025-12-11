package tronka.justsync;


public class CompatUtil {

    private CompatUtil() {}

    public enum PermissionLevel {
       ALL(0),
       MODERATORS(1),
       GAMEMASTERS(2),
       ADMINS(3),
       OWNERS(4);

        private int level;
        private PermissionLevel(int level) {
            this.level = level;
        }
    }

    // ? if >= 1.21.11 {
    public static net.minecraft.command.permission.PermissionLevel getPermissionLevel(
            PermissionLevel level) {
        return net.minecraft.command.permission.PermissionLevel.fromLevel(level.level);
    }
    //?} else {
    /*public static int getPermissionLevel(PermissionLevel level) {
        return level.level;
    }
    *///?}
}
