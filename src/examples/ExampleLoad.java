package examples;

import java.io.File;

import com.tek.nbs.NBSSong;
import com.tek.nbs.util.ByteUtil;

public class ExampleLoad {
	
	public void loadSong() {
		//This example assumes that megalovania.nbs is in the same running folder as the jar
		
		byte[] bytes = null;
		try {
			bytes = ByteUtil.readBytes(new File("megalovania.nbs"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			NBSSong song = NBSSong.fromBytes(bytes);
			song.getNotesByInstrument((byte) 0x00); //Gets all piano notes
			song.getDurationMillis(); //Self explanatory
			//Do whatever with song
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
