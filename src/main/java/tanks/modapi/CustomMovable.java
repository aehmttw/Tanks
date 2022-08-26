package tanks.modapi;

import tanks.Drawing;
import tanks.Movable;

public class CustomMovable extends Movable
{
    public String drawInstructions = "";
    public int startReadingAt = 0;

    public CustomMovable(double x, double y)
    {
        super(x, y);
    }

    @Override
    public void draw()
    {
        StringBuilder funcName = new StringBuilder();
        StringBuilder parameters = new StringBuilder();

        boolean endedFuncName = false;
        boolean endedParameters = false;

        for (int i = startReadingAt; i < this.drawInstructions.length(); i++)
        {
            char c = this.drawInstructions.charAt(i);

            if (c == '(')
            {
                if (!endedFuncName)
                    endedFuncName = true;
                else
                    throw new RuntimeException("Unexpected '(' at " + getStringAround(i));
            }
            else if (c == ')')
            {
                if (!endedParameters)
                    endedParameters = true;
                else
                    throw new RuntimeException("Unexpected ')' at " + getStringAround(i));
            }
            else if (c == ';')
            {
                startReadingAt = i + 1;
                break;
            }
            else
            {
                if (endedFuncName && endedParameters)
                    throw new RuntimeException("Expected ';' at " + getStringAround(i));

                else if (!endedFuncName)
                    funcName.append(c);

                else if (!endedParameters)
                    parameters.append(c);
            }
        }

        if (startReadingAt < this.drawInstructions.stripTrailing().length() - 1)
        {
            executeFunction(funcName.toString().stripTrailing(), parameters.toString().split(","));
            draw();
        }
        else
            startReadingAt = 0;
    }

    public String getStringAround(int index)
    {
        return this.drawInstructions.substring(Math.max(0, index - 3), Math.min(this.drawInstructions.length() - 1, index + 3));
    }

    public void executeFunction(String function, String[] parameters)
    {
        if (function.endsWith("fillBox"))
            Drawing.drawing.fillBox(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]), Double.parseDouble(parameters[2]), Double.parseDouble(parameters[3]), Double.parseDouble(parameters[4]), Double.parseDouble(parameters[5]));

        else if (function.endsWith("fillRect"))
            Drawing.drawing.fillRect(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]), Double.parseDouble(parameters[2]), Double.parseDouble(parameters[3]));

        else if (function.endsWith("drawRect"))
            Drawing.drawing.drawRect(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]), Double.parseDouble(parameters[2]), Double.parseDouble(parameters[3]));

        else if (function.endsWith("fillOval"))
            Drawing.drawing.fillOval(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]), Double.parseDouble(parameters[2]), Double.parseDouble(parameters[3]));

        else if (function.endsWith("drawOval"))
            Drawing.drawing.drawOval(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]), Double.parseDouble(parameters[2]), Double.parseDouble(parameters[3]));

        else if (function.endsWith("setColor"))
            if (parameters.length > 4)
                Drawing.drawing.setColor(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]), Double.parseDouble(parameters[2]), Double.parseDouble(parameters[3]), Double.parseDouble(parameters[4]));
            else if (parameters.length > 3)
                Drawing.drawing.setColor(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]), Double.parseDouble(parameters[2]), Double.parseDouble(parameters[3]));
            else
                Drawing.drawing.setColor(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]), Double.parseDouble(parameters[2]));

        else if (function.endsWith("setFontSize"))
            Drawing.drawing.setFontSize(Double.parseDouble(parameters[0]));

        else if (function.endsWith("drawText"))
            if (parameters.length > 3)
                Drawing.drawing.drawText(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]), Double.parseDouble(parameters[2]), parameters[3]);
            else
                Drawing.drawing.drawText(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]), parameters[2]);

        else
            throw new RuntimeException("Unknown function name '" + function + "'");
    }

    public CustomMovable setDrawInstructions(String instructions, Object... replace)
    {
        this.drawInstructions = instructions;

        for (int i = 0; i < replace.length; i++)
            this.drawInstructions = this.drawInstructions.replaceAll("_r" + (i + 1), replace[i].toString());

        return this;
    }
}
