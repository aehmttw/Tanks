package tanks;

import java.io.InputStream;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.SwingWorker;

public class SoundThread extends SwingWorker<Void, Object>
{
	@Override
	protected Void doInBackground() throws Exception
	{
		ArrayList<Clip> clips = new ArrayList<Clip>();
		ArrayList<Clip> removeClips = new ArrayList<Clip>();
		ArrayList<Clip> startedClips = new ArrayList<Clip>();

		while(true)
		{
			for (int i = 0; i < Drawing.drawing.pendingSounds.size(); i++)
			{
				String sound = Drawing.drawing.pendingSounds.get(i);

				try 
				{
					Clip clip = AudioSystem.getClip();
					
					InputStream in = Game.class.getResourceAsStream(sound);
					if (in == null && sound.startsWith("/resources"))
						in = Game.class.getResourceAsStream(sound.substring("/resources".length()));
					
					AudioInputStream inputStream = AudioSystem.getAudioInputStream(in);
					clip.open(inputStream);
					clip.start(); 
					clips.add(clip);
				}
				catch (Exception e) 
				{
					Game.exitToCrash(e);
				}
			}
			Drawing.drawing.pendingSounds.clear();
			
			for (int i = 0; i < clips.size(); i++)
			{
				if (clips.get(i).isRunning())
				{
					startedClips.add(clips.get(i));
				}
			}
			
			
			for (int i = 0; i < startedClips.size(); i++)
			{
				if (!startedClips.get(i).isRunning())
				{
					removeClips.add(startedClips.get(i));
				}
			}
			
			for (int i = 0; i < removeClips.size(); i++)
			{
				removeClips.get(i).close();
				clips.remove(removeClips.get(i));
			}
			
			removeClips.clear();

			Thread.sleep(1);
			
		}
	}

}
