package tanks.gui.screen.leveleditor;

import tanks.Game;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public abstract class EditorAction
{
    public abstract void undo();

    public abstract void redo();

    public static class ActionObstacle extends EditorAction
    {
        public boolean add;
        public Obstacle obstacle;

        public ActionObstacle(Obstacle o, boolean add)
        {
            this.obstacle = o;
            this.add = add;
        }

        @Override
        public void undo()
        {
            if (!add)
                Game.addObstacle(this.obstacle);
            else
                Game.removeObstacles.add(this.obstacle);
        }

        @Override
        public void redo()
        {
            if (add)
                Game.addObstacle(this.obstacle);
            else
                Game.removeObstacles.add(this.obstacle);
        }
    }

    public static class ActionTank extends EditorAction
    {
        public boolean add;
        public Tank tank;

        public ActionTank(Tank t, boolean add)
        {
            this.tank = t;
            this.add = add;
        }

        @Override
        public void undo()
        {
            if (add)
                Game.removeMovables.add(this.tank);
            else
                Game.movables.add(this.tank);
        }

        @Override
        public void redo()
        {
            if (!add)
                Game.removeMovables.add(this.tank);
            else
                Game.movables.add(this.tank);
        }
    }

    public static class ActionPlayerSpawn extends EditorAction
    {
        public ScreenLevelEditor screenLevelEditor;
        public boolean add;
        public TankSpawnMarker tank;

        public ActionPlayerSpawn(ScreenLevelEditor s, TankSpawnMarker t, boolean add)
        {
            this.screenLevelEditor = s;
            this.tank = t;
            this.add = add;
        }

        @Override
        public void undo()
        {
            if (add)
            {
                Game.removeMovables.add(this.tank);
                screenLevelEditor.spawns.remove(this.tank);
            }
            else
            {
                Game.movables.add(this.tank);
                screenLevelEditor.spawns.add(this.tank);
            }
        }

        @Override
        public void redo()
        {
            if (!add)
            {
                Game.removeMovables.add(this.tank);
                screenLevelEditor.spawns.remove(this.tank);
            }
            else
            {
                Game.movables.add(this.tank);
                screenLevelEditor.spawns.add(this.tank);
            }
        }
    }

    public static class ActionMovePlayer extends EditorAction
    {
        public ScreenLevelEditor screenLevelEditor;
        public ArrayList<TankSpawnMarker> oldSpawns;
        public TankSpawnMarker newSpawn;

        public ActionMovePlayer(ScreenLevelEditor s, ArrayList<TankSpawnMarker> o, TankSpawnMarker n)
        {
            this.screenLevelEditor = s;
            this.oldSpawns = o;
            this.newSpawn = n;
        }

        @Override
        public void undo()
        {
            Game.removeMovables.add(newSpawn);
            screenLevelEditor.spawns.clear();

            for (TankSpawnMarker t: oldSpawns)
            {
                screenLevelEditor.spawns.add(t);
                Game.movables.add(t);
            }
        }

        @Override
        public void redo()
        {
            Game.removeMovables.addAll(oldSpawns);

            screenLevelEditor.spawns.clear();

            Game.movables.add(newSpawn);
            screenLevelEditor.spawns.add(newSpawn);
        }
    }

    public static class ActionSelectTiles extends EditorAction
    {
        public ScreenLevelEditor screenLevelEditor;
        public ArrayList<Integer> x;
        public ArrayList<Integer> y;
        public boolean select;

        public ActionSelectTiles(ScreenLevelEditor s, boolean select, ArrayList<Integer> x, ArrayList<Integer> y)
        {
            this.screenLevelEditor = s;
            this.select = select;
            this.x = x;
            this.y = y;
        }

        @Override
        public void undo()
        {
            for (int i = 0; i < this.x.size(); i++)
                screenLevelEditor.selectedTiles[this.x.get(i)][this.y.get(i)] = !select;

            screenLevelEditor.refreshSelection();
        }

        @Override
        public void redo()
        {
            for (int i = 0; i < this.x.size(); i++)
                screenLevelEditor.selectedTiles[this.x.get(i)][this.y.get(i)] = select;

            screenLevelEditor.refreshSelection();
        }
    }

    public static class ActionChangeHeight extends EditorAction
    {
        public ScreenLevelEditor editor;
        public int add;
        public ArrayList<Integer> x;
        public ArrayList<Integer> y;

        public ActionChangeHeight(ScreenLevelEditor editor, int add)
        {
            this.editor = editor;
            this.add = add;
        }

        @Override
        public void undo()
        {
            editor.adjustSelectionHeight(-add);
        }

        @Override
        public void redo()
        {
            editor.adjustSelectionHeight(add);
        }
    }

    public static class ActionGroup extends EditorAction
    {
        public ScreenLevelEditor screenLevelEditor;
        public ArrayList<EditorAction> actions;

        public ActionGroup(ScreenLevelEditor s, ArrayList<EditorAction> actions)
        {
            this.screenLevelEditor = s;
            this.actions = actions;
        }

        @Override
        public void undo()
        {
            for (EditorAction a: this.actions)
                a.undo();
        }

        @Override
        public void redo()
        {
            for (EditorAction a: this.actions)
                a.redo();
        }
    }

    public static class ActionDeleteCustomTank extends EditorAction
    {
        public ScreenLevelEditor screenLevelEditor;
        public ArrayList<EditorAction> actions;
        public TankAIControlled tank;

        public ActionDeleteCustomTank(ScreenLevelEditor s, ArrayList<EditorAction> actions, TankAIControlled t)
        {
            this.screenLevelEditor = s;
            this.actions = actions;
            this.tank = t;
        }

        @Override
        public void undo()
        {
            for (EditorAction a: this.actions)
                a.undo();

            this.screenLevelEditor.level.customTanks.add(this.tank);
        }

        @Override
        public void redo()
        {
            for (EditorAction a: this.actions)
                a.redo();

            this.screenLevelEditor.level.customTanks.remove(this.tank);
        }
    }


    public static class ActionPaste extends EditorAction
    {
        public ScreenLevelEditor levelEditor;
        public ArrayList<EditorAction> actions;

        public ActionPaste(ScreenLevelEditor s, ArrayList<EditorAction> actions)
        {
            this.levelEditor = s;
            this.actions = actions;
        }

        @Override
        public void undo()
        {
            for (int i = this.actions.size() - 1; i >= 0; i--)
                this.actions.get(i).undo();
        }

        @Override
        public void redo()
        {
            for (EditorAction a: actions)
                a.redo();
        }
    }

    public static class ActionCut extends EditorAction
    {
        public ArrayList<Tank> tanks;
        public ArrayList<Obstacle> obstacles;
        public ActionSelectTiles deselect;

        public ActionCut(ArrayList<Tank> tanks, ArrayList<Obstacle> obstacles, ActionSelectTiles deselect)
        {
            this.tanks = tanks;
            this.obstacles = obstacles;
            this.deselect = deselect;
        }

        @Override
        public void undo()
        {
            for (Obstacle o : obstacles)
                Game.addObstacle(o);
            Game.movables.addAll(this.tanks);
            this.deselect.undo();
        }

        @Override
        public void redo()
        {
            Game.removeObstacles.addAll(this.obstacles);

            for (int i = 0; i < Game.movables.size(); i++)
            {
                if (Game.movables.get(i) instanceof Tank)
                {
                    for (Tank o : this.tanks)
                    {
                        if (Game.movables.get(i).equals(o))
                            Game.movables.remove(i);
                    }
                }
            }

            this.deselect.redo();
        }
    }
}
