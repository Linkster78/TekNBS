package examples;

import com.tek.nbs.NBSSong;
import com.tek.nbs.exception.NoteBlockAPINotFoundException;
import com.tek.nbs.obj.Note;
import com.xxmicloxx.NoteBlockAPI.Song;

public class ExampleConvert {
	
	public void convertSong() {
		NBSSong song = new NBSSong();
		song.putNote(new Note((byte)0, (byte)57, (short) 1), 0);
		
		try {
			Song converted = song.toNoteBlockAPISong();
			converted.getCustomInstruments();
			converted.getDelay();
			//Play the song or something
			
			//Back to NBSSong
			NBSSong nbssong = NBSSong.fromNoteBlockAPISong(converted);
			nbssong.getDurationMillis();
			System.out.println(nbssong.getNote(0, 1));
		} catch (NoteBlockAPINotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
