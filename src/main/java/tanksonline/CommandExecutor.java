package tanksonline;

import java.util.Scanner;
import java.util.UUID;

public class CommandExecutor
{
    public void run()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Scanner sc = new Scanner(System.in);

                while (true)
                {
                    String command = sc.nextLine();

                    if (command.startsWith("accesscode"))
                    {
                        synchronized (AccessCode.accessCodes)
                        {
                            String[] parts = command.split(" ");
                            long duration = Long.parseLong(parts[1].substring(0, parts[1].length() - 1));

                            if (parts[1].endsWith("d"))
                                duration *= 1000 * 60 * 60 * 24;
                            else if (parts[1].endsWith("h"))
                                duration *= 1000 * 60 * 60;
                            else if (parts[1].endsWith("m"))
                                duration *= 1000 * 60;
                            else
                                duration *= 1000;

                            AccessCode ac = new AccessCode(UUID.randomUUID(), System.currentTimeMillis() + duration, Integer.parseInt(parts[2]));

                            if (command.contains("#"))
                                ac.comment = command.substring(command.indexOf("#") + 1);

                            AccessCode.accessCodes.put(ac.id, ac);
                            System.out.println("Created access code: " + ac.id);
                        }
                    }
                }
            }
        }).start();
    }
}
