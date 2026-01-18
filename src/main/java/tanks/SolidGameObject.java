package tanks;

import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;

import java.util.HashSet;

public abstract class SolidGameObject extends GameObject implements ISolidObject
{
    public static HashSet<SolidGameObject> addFacesToChunks = new HashSet<>();

    public Face[] faces;
    private boolean lastEnableCollision = false;

    @Override
    public boolean collisionChanged()
    {
        boolean b = lastEnableCollision;
        lastEnableCollision = disableRayCollision();
        return b != lastEnableCollision;
    }

    public abstract double getSize();

    @Override
    public Face[] getFaces()
    {
        if (this.faces == null)
            this.updateFaces();
        return this.faces;
    }

    public boolean isFaceValid(Face ignored)
    {
        return !Double.isNaN(posX) && !Double.isNaN(posY) && (tankCollision() || bulletCollision());
    }

    public void updateFaces()
    {
        if (this.faces == null)
            this.faces = new Face[4];

        if (this.faces[0] == null || Game.immutableFaces)
        {
            for (int i = 0; i < 4; i++)
                this.faces[i] = new Face(this, Direction.fromIndex(i), tankCollision(), bulletCollision());
        }

        double s = this.getSize();

        boolean validChanged = false;
        for (int i = 0; i < 4; i++)
        {
            Face f = this.faces[i];
            f.update(
                    this.posX + s * (Face.x1[i] - 0.5),
                    this.posY + s * (Face.y1[i] - 0.5),
                    this.posX + s * (Face.x2[i] - 0.5),
                    this.posY + s * (Face.y2[i] - 0.5)
            );
            f.updateCollision(isFaceValid(f), tankCollision(), bulletCollision());
            validChanged = validChanged || f.valid != f.lastValid;
        }
        if (validChanged)
            addFacesToChunks.add(this);
    }

    public abstract boolean isRemoved();

    public void refreshFacesAndChunks()
    {
        updateFaces();
    }

    public boolean tankCollision()
    {
        return true;
    }

    public boolean bulletCollision()
    {
        return true;
    }
}
