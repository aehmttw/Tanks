package tanks;

import tanks.obstacle.Face2;
import tanks.obstacle.ISolidObject2;

public abstract class SolidGameObject extends GameObject implements ISolidObject2
{
    public Face2[] faces;

    public abstract double getSize();

    @Override
    public Face2[] getFaces()
    {
        if (this.faces == null)
            this.updateFaces();
        return this.faces;
    }

    public boolean isFaceValid(Face2 ignored)
    {
        return !Double.isNaN(posX) && !Double.isNaN(posY) && (tankCollision() || bulletCollision());
    }

    public void updateFaces()
    {
        if (this.faces == null)
            this.faces = new Face2[4];

        if (this.faces[0] == null || Game.immutableFaces)
        {
            for (int i = 0; i < 4; i++)
                this.faces[i] = new Face2(this, Direction.fromIndex(i), tankCollision(), bulletCollision());
        }

        double s = this.getSize();

        for (int i = 0; i < 4; i++)
        {
            Face2 f = this.faces[i];
            f.update(
                    this.posX + s * (Face2.x1[i] - 0.5),
                    this.posY + s * (Face2.y1[i] - 0.5),
                    this.posX + s * (Face2.x2[i] - 0.5),
                    this.posY + s * (Face2.y2[i] - 0.5),
                    this.isFaceValid(f),
                    tankCollision(),
                    bulletCollision()
            );
        }
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
