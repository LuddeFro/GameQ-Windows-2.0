package io.gameq.gameqwindows.Structs;

/**
 * Created by fabianwikstrom on 8/3/2015.
 */
public class Encoding {

   public static String getStringFromGame(Game game) {

        switch (game){
            case Dota2:
                return "Dota 2";
            case HoN:
                return "Heroes of Newerth";
            case CSGO:
                return "Counter Strike Global Offensive";
            case HoTS:
                return "Heroes of The Storm";
            case LoL:
                return "League of Legends";
            case NoGame:
                return "No Game Active";
        }
        return "";
    }

    public static int getIntFromGame(Game game) {

        switch (game){
            case NoGame:
                return 0;
            case Dota2:
                return 1;
            case HoN:
                return 2;
            case CSGO:
                return 3;
            case HoTS:
                return 4;
            case LoL:
                return 5;
        }
        return 6;
    }

   public static String getStringFromGameStatus(Game game, Status status) {

        switch (game) {
            case Dota2:
            switch (status) {
                case Offline: return "Offline";
                case Online: return  "Online";
                case InLobby: return "Detecting Game";
                case InQueue: return "Detecting Game";
                case GameReady: return "Your Match is Ready";
                case InGame:return "In Match";
            }
            case HoN:
            switch (status){
                case Offline: return "Offline";
                case Online: return  "Online";
                case InLobby: return "Detecting Game";
                case InQueue: return" Detecting Game";
                case GameReady: return "Your Match is Ready";
                case InGame:return "In Match";
            }
            case CSGO:
            switch (status){
                case Offline: return "Offline";
                case Online: return  "Online";
                case InLobby: return "Detecting Game";
                case InQueue: return "Detecting Game";
                case GameReady: return "Your Match is Ready";
                case InGame:return "In Match";
            }
            case HoTS:
            switch (status){
                case Offline: return "Offline";
                case Online: return  "Online";
                case InLobby: return "Detecting Game";
                case InQueue: return "Detecting Game";
                case GameReady: return "Your Match is Ready";
                case InGame:return "In Match";
            }
            case LoL:
            switch (status){
                case Offline: return "Offline";
                case Online: return  "Online";
                case InLobby: return "Detecting Game";
                case InQueue: return "Detecting Game";
                case GameReady: return "Your Match is Ready";
                case InGame:return "In Match";
            }

            case NoGame:
            switch (status){
                case Offline: return "Offline";
                case Online: return  "Online";
                case InLobby: return "";
                case InQueue: return "";
                case GameReady: return "";
                case InGame:return "";
            }
        }

        return "";
    }

    public static int getIntFromStatus(Status status) {
        switch (status){
            case Offline: return 0;
            case Online: return  1;
            case InLobby: return 2;
            case InQueue: return 3;
            case GameReady: return 4;
            case InGame: return 5;
        }
        return 6;
    }

    static Status getStatusFromInt(int status) {

        switch (status) {
            case 0 : return Status.Offline;
            case 1 : return Status.Online;
            case 2 : return Status.InLobby;
            case 3 : return Status.InQueue;
            case 4 : return Status.GameReady;
            case 5 : return Status.InGame;
            default: return Status.Offline;
        }
    }

    public static Game getGameFromInt(int game){

        switch (game){
            case 0 : return Game.NoGame;
            case 1 : return Game.Dota2;
            case 2 : return Game.HoN;
            case 3 : return Game.CSGO;
            case 4 : return Game.HoTS;
            case 5 : return Game.LoL;
            default: return Game.NoGame;
        }
    }


}
