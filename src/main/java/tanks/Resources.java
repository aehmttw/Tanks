package tanks;

import java.io.InputStream;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Resources {
	private static final String PREFIX_RESOURCES = "/resources";
	private static final String PREFIX_TANKS = "/tanks";
	
	public InputStream getResourceAsStream(String name) {
		if (!name.startsWith("/"))
			name = "/" + name;
		
		InputStream in = Resources.class.getResourceAsStream(name);
		
		if (in == null && name.startsWith(PREFIX_TANKS))
			in = Resources.getResourceAsStream(name.substring(PREFIX_TANKS.length()));
		
		if (in == null && name.startsWith(PREFIX_RESOURCES))
			in = Resources.getResourceAsStream(name.substring(PREFIX_RESOURCES.length()));
		
		return in;
	}
}
