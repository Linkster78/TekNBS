package examples;

import java.io.File;

import com.tek.nbs.NBSSong;
import com.tek.nbs.util.ByteUtil;

public class ExampleSave {
	
	public void saveSong() {
		NBSSong song = new NBSSong();
		byte[] bytes = song.getBytes();
		
		try {
			ByteUtil.writeBytes(new File("newSong.nbs"), bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
