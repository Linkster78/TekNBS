package examples;

import com.tek.nbs.NBSSong;
import com.tek.nbs.obj.Note;

public class ExampleModify {
	
	public void modifySong() {
		NBSSong song = new NBSSong();
		
		song.putNote(new Note((byte)0x00, (byte)0x00, (short)0), 0);
		song.getLayer(0).setName("First layer");
		song.getLayer(0).setVolume((byte)50);
	}
	
}
