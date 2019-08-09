package tanks;

public class ScreenKicked extends Screen
{
	public String reason;
	
	public ScreenKicked(String reason)
	{
		this.reason = reason;
		ScreenPartyLobby.connections.clear();
	}
		
	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Ok", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenJoinParty();
		}
	}
	);

	@Override
	public void update() 
	{
		back.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setFontSize(24);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.reason);
	
		back.draw();
	}
	
}
