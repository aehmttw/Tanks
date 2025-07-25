package tanks;

import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;

public abstract class SolidGameObject extends GameObject implements ISolidObject
{
    public Face[] faces;

    public abstract double getSize();

    @Override
    public Face[] getFaces()
    {
        if (this.faces == null)
            this.updateFaces();
        return this.faces;
    }

    public boolean isFaceValid(Face f)
    {
        return !Double.isNaN(posX) && !Double.isNaN(posY);
    }

    public void updateFaces()
    {
        if (this.faces == null)
            this.faces = new Face[4];

        if (this.faces[0] == null || !Game.immutableFaces)
        {
            for (int i = 0; i < 4; i++)
                this.faces[i] = new Face(this, Direction.fromIndex(i), tankCollision(), bulletCollision());
        }

        double s = this.getSize();

        for (int i = 0; i < 4; i++)
        {
            Face f = this.faces[i];
            f.update(
                    this.posX + s * (Face.x1[i] - 0.5),
                    this.posY + s * (Face.y1[i] - 0.5),
                    this.posX + s * (Face.x2[i] - 0.5),
                    this.posY + s * (Face.y2[i] - 0.5),
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

    // deprecated, will be removed in next PR
    public Face[] horizontalFaces;
    public Face[] verticalFaces;

    @Override
    public Face[] getHorizontalFaces()
    {
        double s = getSize() / 2;

        if (this.horizontalFaces == null)
        {
            this.horizontalFaces = new Face[2];
            this.horizontalFaces[0] = new Face(this, this.posX - s, this.posY - s, this.posX + s, this.posY - s, true, true, true, true);
            this.horizontalFaces[1] = new Face(this, this.posX - s, this.posY + s, this.posX + s, this.posY + s, true, false,true, true);
        }
        else
        {
            this.horizontalFaces[0].update(this.posX - s, this.posY - s, this.posX + s, this.posY - s);
            this.horizontalFaces[1].update(this.posX - s, this.posY + s, this.posX + s, this.posY + s);
        }

        return this.horizontalFaces;
    }

    @Override
    public Face[] getVerticalFaces()
    {
        double s = getSize() / 2;

        if (this.verticalFaces == null)
        {
            this.verticalFaces = new Face[2];
            this.verticalFaces[0] = new Face(this, this.posX - s, this.posY - s, this.posX - s, this.posY + s, false, true, true, true);
            this.verticalFaces[1] = new Face(this, this.posX + s, this.posY - s, this.posX + s, this.posY + s, false, false, true, true);
        }
        else
        {
            this.verticalFaces[0].update(this.posX - s, this.posY - s, this.posX - s, this.posY + s);
            this.verticalFaces[1].update(this.posX + s, this.posY - s, this.posX + s, this.posY + s);
        }

        return this.verticalFaces;
    }
}
