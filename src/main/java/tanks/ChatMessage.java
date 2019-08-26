package tanks;

public class ChatMessage 
{
	public long time = System.currentTimeMillis();
	
	public String message;
	public String rawMessage;

	public ChatMessage(String s)
	{
		this.rawMessage = s;
		this.message = s;
		
		if (Game.enableChatFilter)
			this.message = Game.chatFilter.filterChat(s);
	}
	
	public ChatMessage(String u, String s)
	{
		this.rawMessage = u + ": " + s;
		if (Game.enableChatFilter)
			this.message = Game.chatFilter.filterChat(u) + ": " + Game.chatFilter.filterChat(s);
	}
}
