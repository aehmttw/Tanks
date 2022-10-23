package tanks.gui;

import tanks.*;
import tanks.network.event.EventAddScoreboard;
import tanks.network.event.EventChangeScoreboardAttribute;
import tanks.network.event.EventScoreboardUpdateScore;

import java.util.ArrayList;
import java.util.HashMap;

public class Scoreboard implements IFixedMenu
{
    public HashMap<Player, Double> players = new HashMap<>();
    public HashMap<Team, Double> teams = new HashMap<>();
    public String name;

    public double titleColR = 255;
    public double titleColG = 255;
    public double titleColB = 0;
    public double titleFontSize = 24;
    public double namesFontSize = 20;

    public Team[] teamNames;
    public Player[] playerNames;

    public enum objectiveTypes {custom, kills, deaths, items_used, shots_fired, mines_placed, shots_fired_no_multiple_fire} // deaths and itemsUsed are coming soon!
    public objectiveTypes objectiveType;

    private double sizeX = 200;
    private double sizeY = 300;

    private final RemoteScoreboard remoteScoreboard;

    public Scoreboard(String objectiveName, objectiveTypes objectiveType)
    {
        this.name = objectiveName;
        this.objectiveType = objectiveType;

        this.remoteScoreboard = new RemoteScoreboard(objectiveName, objectiveType.toString(), new ArrayList<String>()
        {
        });
        this.remoteScoreboard.titleColR = this.titleColR;
        this.remoteScoreboard.titleColG = this.titleColG;
        this.remoteScoreboard.titleColB = this.titleColB;
        this.remoteScoreboard.namesFontSize = this.namesFontSize;
        this.remoteScoreboard.titleFontSize = this.titleFontSize;

        Game.eventsOut.add(new EventAddScoreboard(this.remoteScoreboard));
    }

    public Scoreboard(String objectiveName, objectiveTypes objectiveType, ArrayList<Player> players, boolean isPlayer)
    {
        this.name = objectiveName;
        this.objectiveType = objectiveType;

        ArrayList<String> names = new ArrayList<>();

        for (Player p : players)
        {
            this.players.put(p, 0.0);
            names.add(p.username);
        }

        this.playerNames = this.players.keySet().toArray(new Player[0]);

        this.remoteScoreboard = new RemoteScoreboard(objectiveName, objectiveType.toString(), names);
        this.remoteScoreboard.titleColR = this.titleColR;
        this.remoteScoreboard.titleColG = this.titleColG;
        this.remoteScoreboard.titleColB = this.titleColB;
        this.remoteScoreboard.namesFontSize = this.namesFontSize;
        this.remoteScoreboard.titleFontSize = this.titleFontSize;

        Game.eventsOut.add(new EventAddScoreboard(this.remoteScoreboard));
    }

    public Scoreboard(String objectiveName, objectiveTypes objectiveType, ArrayList<Team> teams)
    {
        this.name = objectiveName;
        this.objectiveType = objectiveType;

        ArrayList<String> names = new ArrayList<>();

        for (Team t : teams)
        {
            this.teams.put(t, 0.0);
            names.add(t.name);
        }

        teamNames = this.teams.keySet().toArray(new Team[0]);

        this.remoteScoreboard = new RemoteScoreboard(objectiveName, objectiveType.toString(), names);
        this.remoteScoreboard.titleColR = this.titleColR;
        this.remoteScoreboard.titleColG = this.titleColG;
        this.remoteScoreboard.titleColB = this.titleColB;
        this.remoteScoreboard.namesFontSize = this.namesFontSize;
        this.remoteScoreboard.titleFontSize = this.titleFontSize;

        Game.eventsOut.add(new EventAddScoreboard(this.remoteScoreboard));
    }

    public void addPlayer(Player player)
    {
        addPlayerScore(player, 0);
    }

    public void addPlayerScore(Player p, double value)
    {
        if (players.containsKey(p))
            players.replace(p, players.get(p) + value);
        else
            players.put(p, value);

        Game.eventsOut.add(new EventScoreboardUpdateScore(this.remoteScoreboard.id, p.username, players.get(p)));
    }

    public boolean addPlayerScore(String playerName, double value)
    {
        for (Player p : this.players.keySet()) {
            if (p.username.equals(playerName)) {
                players.replace(p, players.get(p) + value);
                Game.eventsOut.add(new EventScoreboardUpdateScore(this.remoteScoreboard.id, p.username, players.get(p)));
                return true;
            }
        }

        return false;
    }

    public boolean addTeamScore(String teamName, double value)
    {
        for (Team t : this.teams.keySet()) {
            if (t.name.equals(teamName)) {
                teams.replace(t, teams.get(t) + value);
                Game.eventsOut.add(new EventScoreboardUpdateScore(this.remoteScoreboard.id, t.name, teams.get(t)));
                return true;
            }
        }

        return false;
    }

    public void addTeam(Team t) {
        addTeamScore(t, 0);
    }

    public void addTeamScore(Team t, double value)
    {
        if (t == null)
            return;

        if (teams.containsKey(t))
            teams.replace(t, teams.get(t) + value);
        else
            teams.put(t, value);

        Game.eventsOut.add(new EventScoreboardUpdateScore(this.remoteScoreboard.id, t.name, teams.get(t)));
    }

    /** Use this function to change an attribute of a scoreboard.<br>
     You could change the variables directly, but side effects may occur if you do so.<br>

     @param attributeName Can be "titleColR", "titleColG", "titleColB", "titleFontSize", "namesFontSize", which each edit their own variables.
     @param value Can be any double.
     @return itself
     */
    public Scoreboard changeAttribute(String attributeName, double value)
    {
        switch (attributeName)
        {
            case "titleColR":
                this.titleColR = value;
                break;
            case "titleColG":
                this.titleColG = value;
                break;
            case "titleColB":
                this.titleColB = value;
                break;
            case "titleFontSize":
                this.titleFontSize = value;
                break;
            case "namesFontSize":
                this.namesFontSize = value;
                break;
            default:
                System.err.println("Invalid attribute name: " + attributeName);
                break;
        }

        Game.eventsOut.add(new EventChangeScoreboardAttribute(this.remoteScoreboard.id, attributeName, value));
        return this;
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(0, 0, 0, 128);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX - this.sizeX / 2, Drawing.drawing.interfaceSizeY / 2, sizeX, sizeY);

        Drawing.drawing.setColor(titleColR, titleColG, titleColB);
        Drawing.drawing.setInterfaceFontSize(this.titleFontSize);
        Drawing.drawing.drawInterfaceText(
                Drawing.drawing.interfaceSizeX - sizeX / 2,
                Drawing.drawing.interfaceSizeY / 2 - sizeY * 0.4,
                this.name);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleFontSize * 0.8);

        if (!this.objectiveType.equals(objectiveTypes.custom))
            Drawing.drawing.drawInterfaceText(
                    Drawing.drawing.interfaceSizeX - sizeX / 2,
                    Drawing.drawing.interfaceSizeY / 2 - sizeY * 0.2,
                    "Objective: " + this.objectiveType.toString().replaceAll("_", " "));

        double longestSX = 0;
        if (this.teams.isEmpty())
        {
            for (int i = 0; i < playerNames.length; i++)
            {
                double textSizeX = Game.game.window.fontRenderer.getStringSizeX(namesFontSize / 32, playerNames[i].username);

                Drawing.drawing.setInterfaceFontSize(this.namesFontSize);
                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.drawInterfaceText(
                        Drawing.drawing.interfaceSizeX - sizeX / 4 - 90,
                        Drawing.drawing.interfaceSizeY / 2 + i * 30,
                        playerNames[i].username
                );

                if (textSizeX > longestSX)
                    longestSX = textSizeX;

                Drawing.drawing.setInterfaceFontSize(this.namesFontSize);
                Drawing.drawing.setColor(255, 64, 64);
                Drawing.drawing.drawInterfaceText(
                        Drawing.drawing.interfaceSizeX - 15,
                        Drawing.drawing.interfaceSizeY / 2 + i * 30,
                        "" + (int)(double)(players.get(playerNames[i]))
                );
            }
        }
        else
        {
            for (int i = 0; i < teamNames.length; i++)
            {
                double textSizeX = Game.game.window.fontRenderer.getStringSizeX(namesFontSize / 32, teamNames[i].name);

                Drawing.drawing.setInterfaceFontSize(this.namesFontSize);
                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.drawInterfaceText(
                        Drawing.drawing.interfaceSizeX - sizeX / 4 - 90,
                        Drawing.drawing.interfaceSizeY / 2 + i * 30,
                        teamNames[i].name
                );

                if (longestSX > textSizeX)
                    longestSX = textSizeX;

                Drawing.drawing.setInterfaceFontSize(this.namesFontSize);
                Drawing.drawing.setColor(255, 64, 64);
                Drawing.drawing.drawInterfaceText(
                        Drawing.drawing.interfaceSizeX - 15,
                        Drawing.drawing.interfaceSizeY / 2 + i * 30,
                        "" + (int)(double)(teams.get(teamNames[i]))
                );
            }
        }

        sizeX = longestSX + 200;
        if (players.isEmpty())
            sizeY = this.teamNames.length * 30 + 70;
        else
            sizeY = this.playerNames.length * 30 + 70;
    }

    @Override
    public void update()
    {

    }
}
