package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.event.EventChat;
import tanks.gui.Button;
import tanks.gui.ChatBox;
import tanks.gui.ChatMessage;
import tanks.network.Client;
import tanks.network.ConnectedPlayer;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class ScreenPartyLobby extends Screen
{
	public static ArrayList<ConnectedPlayer> connections = new ArrayList<ConnectedPlayer>();
	public static boolean isClient = false;
	public static int readyPlayers = 0;
	
	public static ArrayList<ChatMessage> chat = new ArrayList<ChatMessage>();

	public static ChatBox chatbox = new ChatBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 30, 1380, 40, GLFW.GLFW_KEY_T, 
			"\u00A7127127127255Click here or press 'T' to send a chat message", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.eventsOut.add(new EventChat(chatbox.inputText));
		}
		
	});
	
	
	Button exit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 210, 350, 40, "Leave party", new Runnable()
	{
		@Override
		public void run() 
		{
			Client.handler.ctx.close();
			Game.screen = new ScreenJoinParty();
			connections.clear();
		}
	}
	);
	
	@Override
	public void update() 
	{
		exit.update();
		chatbox.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();	

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(24);
		
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 400, Panel.winlose);
		
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 4 - 40, "Players in this party:");

		for (int i = 0; i < connections.size(); i++)
		{
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 4 + i * 30, connections.get(i).username);
		}
		
		long time = System.currentTimeMillis();
		for (int i = 0; i < chat.size(); i++)
		{
			ChatMessage c = chat.get(i);
			if (time - c.time <= 30000 || chatbox.selected)
			{
				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
			}
		}
		
		
		exit.draw();
		chatbox.draw();
	}

}
