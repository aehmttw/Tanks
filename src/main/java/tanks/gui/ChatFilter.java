package tanks.gui;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFilter
{
	protected ArrayList<String> badwords = new ArrayList<>();
	
	public ChatFilter()
	{
		this.addBadWords();
	}
	
	public String filterChat(String msg)
	{	
		if (msg == null)
			return null;
		
		String rawMessage = msg.toLowerCase().replaceAll("_", " ");
		String message = msg.toLowerCase().replaceAll("_", " ");
		
		for (String badword: this.badwords)
		{
			String p = "\\w" + badword + "\\w";
			Matcher m = Pattern.compile(p).matcher(rawMessage);
			
			while (m.find())
			{
				if (rawMessage.substring(m.start(), m.end()).contains(" "))
					rawMessage = rawMessage.substring(0, m.start() + 1) + rawMessage.substring(m.end() - 1);
			}
						
			message = message.replaceAll(badword, "");
		}
		
		if (!message.equals(rawMessage))
		{
			msg = "< Redacted >";
		}

		return msg;
	}
	
	public void registerBadWord(String word)
	{
		StringBuilder newWord = new StringBuilder();
		
		for (int i = 0; i < word.length(); i++)
		{
			char c = (char) (word.charAt(i) - 1);
			newWord.append(c).append("(").append(c).append("*)\\W*");
		}
		
		this.badwords.add(newWord.toString());
	}
	
	/** I've added 1 to every character in these bad words so that they look like nonsense.
	 *  This addition is removed when testing for bad words in chat*/
	public void addBadWords()
	{
		registerBadWord("cjudi");
		registerBadWord("gvdl");
		registerBadWord("tiju");
		registerBadWord("ojhhfs");
		registerBadWord("dvou");
		registerBadWord("ebno");
		registerBadWord("bttipmf");
		registerBadWord("tfy");
		registerBadWord("qfojt");
		registerBadWord("wbhjob");
		registerBadWord("gbhhpu");
	}
}
