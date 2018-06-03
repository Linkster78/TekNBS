package com.tek.nbs;

import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

import com.tek.nbs.exception.NoteBlockAPINotFoundException;
import com.tek.nbs.obj.Chord;
import com.tek.nbs.obj.Instrument;
import com.tek.nbs.obj.Layer;
import com.tek.nbs.obj.Note;
import com.tek.nbs.util.ByteUtil;
import com.tek.nbs.util.NoteBlockAPIConstants;
import com.tek.nbs.util.ReflectionUtil;
import com.xxmicloxx.NoteBlockAPI.CustomInstrument;
import com.xxmicloxx.NoteBlockAPI.Song;

public class NBSSong {
	
	private final int layerAmount = 23;
	
	private double tempo;
	private short length, height;
	private String name, author, originalAuthor, description, songOrigin;
	private boolean autoSave;
	private byte autoSaveDuration, timeSignature;
	private int minutesSpent, leftClicks, rightClicks, blocksAdded, blocksRemoved;
	
	private ArrayList<Chord> chords;
	private ArrayList<Layer> layers;
	private ArrayList<Instrument> instruments;
	
	public NBSSong() {
		this("");
	}
	
	public NBSSong(String name) {
		this(name, "");
	}
	
	public NBSSong(String name, String author) {
		this(name, author, "");
	}
	
	public NBSSong(String name, String author, String originalAuthor) {
		this(name, author, originalAuthor, "");
	}
	
	public NBSSong(String name, String author, String originalAuthor, String description) {
		this.length = 0;
		this.height = layerAmount;
		this.name = name;
		this.author = author;
		this.originalAuthor = "";
		this.description = "";
		
		this.tempo = 10;
		this.autoSave = false;
		this.autoSaveDuration = 10;
		this.timeSignature = 4;
		
		this.minutesSpent = 0;
		this.leftClicks = 0;
		this.rightClicks = 0;
		this.blocksAdded = 0;
		this.blocksRemoved = 0;
		this.songOrigin = "";
		
		this.chords = new ArrayList<Chord>();
		this.layers = new ArrayList<Layer>();
		this.instruments = new ArrayList<Instrument>();
		
		verifyLayers((short)layerAmount);
	}
	
	public static NBSSong fromBytes(byte[] bytes) throws Exception{
		NBSSong nbsSong = new NBSSong();
		
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		//Header
		nbsSong.setLength(buffer.getShort());
		nbsSong.setHeight(buffer.getShort());
		nbsSong.setName(ByteUtil.getString(buffer));
		nbsSong.setAuthor(ByteUtil.getString(buffer));
		nbsSong.setOriginalAuthor(ByteUtil.getString(buffer));
		nbsSong.setDescription(ByteUtil.getString(buffer));
		
		nbsSong.setTempo(buffer.getShort() / 100);
		nbsSong.setAutoSave(buffer.get() == 1 ? true : false);
		nbsSong.setAutoSaveDuration(buffer.get());
		nbsSong.setTimeSignature(buffer.get());
		
		nbsSong.setMinutesSpent(buffer.getInt());
		nbsSong.setLeftClicks(buffer.getInt());
		nbsSong.setRightClicks(buffer.getInt());
		nbsSong.setBlocksAdded(buffer.getInt());
		nbsSong.setBlocksRemoved(buffer.getInt());
		nbsSong.setSongOrigin(ByteUtil.getString(buffer));
		
		//Adjust height and length
		nbsSong.verifyChords(nbsSong.getLength() - 1);
		nbsSong.verifyLayers((short) (nbsSong.getHeight() - 1));
		
		//Notes
		short tick = -1;
		short jumps = 0;
		
		while(true) {
			jumps = buffer.getShort();
			if(jumps == 0) break;
			
			tick += jumps;
			short layer = -1;
			while(true) {
				jumps = buffer.getShort();
				if(jumps == 0) break;
				
				layer += jumps;
				byte instrument = buffer.get();
				byte key = buffer.get();
				
				nbsSong.putNote(new Note(instrument, key, layer), tick);
			}
		}
		
		//Stop ?
		if(!buffer.hasRemaining()) return nbsSong;
		
		//Layers
		for(int h = 0; h < nbsSong.getHeight(); h++) {
			if(buffer.remaining() < 5) continue;
			String name = ByteUtil.getString(buffer);
			byte volume = buffer.get();
			nbsSong.putLayer((short) h, new Layer(name, volume));
		}
		
		//Stop ?
		if(!buffer.hasRemaining()) return nbsSong;
		
		//Instruments
		byte amount = buffer.get();
		
		for(int i = 0; i < amount; i++) {
			String name = ByteUtil.getString(buffer);
			String file = ByteUtil.getString(buffer);
			byte pitch = buffer.get();
			byte pressKey = buffer.get();
			
			nbsSong.getInstruments().add(new Instrument(name, file, pitch, pressKey));
		}
		
		return nbsSong;
	}
	
	public static NBSSong fromNoteBlockAPISong(Song nbSong) throws NoteBlockAPINotFoundException {
		if(!ReflectionUtil.classPresent(NoteBlockAPIConstants.CLASS_SONG)) throw new NoteBlockAPINotFoundException();
		NBSSong song = new NBSSong();
		
		//Header
		song.setLength(nbSong.getLength());
		song.setHeight(nbSong.getSongHeight());
		song.setName(nbSong.getTitle());
		song.setAuthor(nbSong.getAuthor());
		song.setDescription(nbSong.getDescription());
		song.setTempo(nbSong.getSpeed());
		
		//Adjust height and length
		song.verifyChords(song.getLength() - 1);
		song.verifyLayers((short) (song.getHeight() - 1));
		
		//Notes
		for(int l : nbSong.getLayerHashMap().keySet()) {
			com.xxmicloxx.NoteBlockAPI.Layer nbLayer = nbSong.getLayerHashMap().get(l);
			song.putLayer((short) l, new Layer(nbLayer.getName(), nbLayer.getVolume()));
			
			for(int t : nbLayer.getHashMap().keySet()) {
				com.xxmicloxx.NoteBlockAPI.Note nbNote = nbLayer.getHashMap().get(t);
				song.putNote(new Note((byte)nbNote.getInstrument(), (byte)nbNote.getKey(), (short) l), t);
			}
		}
		
		//Instruments
		for(int i = 0; i < nbSong.getCustomInstruments().length; i++) {
			com.xxmicloxx.NoteBlockAPI.CustomInstrument inst = nbSong.getCustomInstruments()[i];
			song.putInstrument(new Instrument(inst.getName(), inst.getSoundfile(), (byte)ReflectionUtil.privateField(inst, "pitch"), (byte)ReflectionUtil.privateField(inst, "press")));
		}
		
		return song;
	}
	
	public Song toNoteBlockAPISong() throws NoteBlockAPINotFoundException {
		if(!ReflectionUtil.classPresent(NoteBlockAPIConstants.CLASS_SONG)) throw new NoteBlockAPINotFoundException();
		
		float speed = (float) this.getTempo();
		short songHeight = this.getHeight();
		short length = this.getLength();
		String title = this.getName();
		String author = this.getAuthor();
		String description = this.getDescription();
		File path = null;
		
		//Layer hashmap
		HashMap<Integer, com.xxmicloxx.NoteBlockAPI.Layer> layerHashMap = new HashMap<Integer, com.xxmicloxx.NoteBlockAPI.Layer>();
		
		int h = 0;
		for(Layer layer : layers) {
			com.xxmicloxx.NoteBlockAPI.Layer newLayer = new com.xxmicloxx.NoteBlockAPI.Layer();
			newLayer.setName(layer.getName());
			newLayer.setVolume(layer.getVolume());
			
			HashMap<Integer, com.xxmicloxx.NoteBlockAPI.Note> noteHashMap = new HashMap<Integer, com.xxmicloxx.NoteBlockAPI.Note>();
			
			for(Chord chord : chords) {
				Note layerNote = chord.getNoteByLayer(h);
				if(layerNote != null) {
					com.xxmicloxx.NoteBlockAPI.Note newNote = new com.xxmicloxx.NoteBlockAPI.Note(layerNote.getInstrument(), layerNote.getKey());
					noteHashMap.put(chord.getTick(), newNote);
				}
			}
			
			newLayer.setHashMap(noteHashMap);
			
			layerHashMap.put(h, newLayer);
			
			h++;
		}
		
		//Custom instruments
		CustomInstrument[] customInstruments = new CustomInstrument[instruments.size()];
		
		for(int i = 0; i < instruments.size(); i++) {
			Instrument instrument = instruments.get(i);
			customInstruments[i] = new CustomInstrument((byte)i, instrument.getName(), instrument.getFile(), instrument.getPitch(), instrument.getPressKey());
		}
		
		return new Song(speed, layerHashMap, songHeight, length, title, author, description, path, customInstruments);
	}
	
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try{
			//Header
			os.write(ByteUtil.toLittleEndian(ByteUtil.getShort(length)));
			os.write(ByteUtil.toLittleEndian(ByteUtil.getShort((short) layers.size())));
			os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(name.getBytes().length))); os.write(name.getBytes());
			os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(author.getBytes().length))); os.write(author.getBytes());
			os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(originalAuthor.getBytes().length))); os.write(originalAuthor.getBytes());
			os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(description.getBytes().length))); os.write(description.getBytes());
			
			os.write(ByteUtil.toLittleEndian(ByteUtil.getShort((short) (tempo * 100))));
			os.write(ByteUtil.toLittleEndian(ByteUtil.getBoolean(autoSave)));
			os.write(ByteUtil.toLittleEndian(autoSaveDuration));
			os.write(ByteUtil.toLittleEndian(timeSignature));
			
			os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(minutesSpent)));
			os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(leftClicks)));
			os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(rightClicks)));
			os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(blocksAdded)));
			os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(blocksRemoved)));
			os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(songOrigin.getBytes().length))); os.write(songOrigin.getBytes());
			
			//Notes
			int currentTick = -1;
			
			for(Chord chord : chords) {
				os.write(ByteUtil.toLittleEndian(ByteUtil.getShort((short) (chord.getTick() - currentTick))));
				currentTick = chord.getTick();
				int currentLayer = -1;
				
				for(Note note : chord.getNotes()) {
					os.write(ByteUtil.toLittleEndian(ByteUtil.getShort((short) (note.getLayer() - currentLayer))));
					currentLayer = note.getLayer();
					os.write(ByteUtil.toLittleEndian(note.getInstrument()));
					os.write(ByteUtil.toLittleEndian(note.getKey()));
				}
				
				os.write(ByteUtil.getShort((short) 0));
			}
			
			os.write(ByteUtil.getShort((short) 0));
			
			//Layers
			for(Layer layer : layers) {
				os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(layer.getName().getBytes().length))); os.write(layer.getName().getBytes());
				os.write(ByteUtil.toLittleEndian(layer.getVolume()));
			}
			
			//Instruments
			os.write(ByteUtil.toLittleEndian((byte)instruments.size()));
			
			for(Instrument instrument : instruments) {
				os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(instrument.getName().getBytes().length))); os.write(instrument.getName().getBytes());
				os.write(ByteUtil.toLittleEndian(ByteUtil.getInt(instrument.getFile().getBytes().length))); os.write(instrument.getFile().getBytes());
				os.write(ByteUtil.toLittleEndian(instrument.getPitch()));
				os.write(ByteUtil.toLittleEndian(instrument.getPressKey()));
			}
		}catch(Exception e) { }
		
		return os.toByteArray();
	}
	
	public long getDurationMillis() {
		return (long) (getLength() / getTempo() * 1000);
	}
	
	public void clearNotes() {
		for(Chord chord : chords) {
			chord.getNotes().clear();
		}
		
		this.length = 0;
	}
	
	public void putNote(Note note, int tick) {
		verifyChords(tick);
		verifyLayers(note.getLayer());
		
		chords.get(tick).getNotes().add(note);
		
		this.length = (short) (chords.size() - 1);
	}
	
	public void putLayer(short height, Layer layer) {
		verifyLayers(height);
		
		Layer lay = getLayer(height);
		lay.setName(layer.getName());
		lay.setVolume(layer.getVolume());
	}
	
	public void putChord(int tick) {
		verifyChords(tick);
	}
	
	public void putInstrument(Instrument instrument) {
		instruments.add(instrument);
	}
	
	public ArrayList<Note> getAllNotes(){
		ArrayList<Note> notesGlobal = new ArrayList<Note>();
		
		for(Chord chord : chords) {
			for(Note note : chord.getNotes()) {
				notesGlobal.add(note);
			}
		}
		
		return notesGlobal;
	}
	
	public HashMap<Point, Note> annexNotes() {
		HashMap<Point, Note> notes = new HashMap<Point, Note>();
		
		for(Chord chord : chords) {
			for(Note note : chord.getNotes()) {
				notes.put(new Point(chord.getTick(), note.getLayer()), note);
			}
		}
		
		return notes;
	}
	
	public Note getNote(int chord, int layer) {
		Chord chordon = getChord(chord);
		
		if(chordon != null) {
			for(Note note : chordon.getNotes()) {
				if(note.getLayer() == layer) return note;
			}
			
			return null;
		}
		
		return null;
	}
	
	public ArrayList<Note> getNotesByInstrument(byte instrument) {
		ArrayList<Note> notes = new ArrayList<Note>();
		
		for(Note note : getAllNotes()) {
			if(note.getInstrument() == instrument) notes.add(note);
		}
		
		return notes;
	}
	
	public ArrayList<Note> getNotesByKey(byte key) {
		ArrayList<Note> notes = new ArrayList<Note>();
		
		for(Note note : getAllNotes()) {
			if(note.getKey() == key) notes.add(note);
		}
		
		return notes;
	}
	
	public ArrayList<Note> getNotesByLayer(short layer) {
		ArrayList<Note> notes = new ArrayList<Note>();
		
		for(Note note : getAllNotes()) {
			if(note.getLayer() == layer) notes.add(note);
		}
		
		return notes;
	}
	
	public ArrayList<Note> getNotesByChord(int chord) {
		ArrayList<Note> notes = new ArrayList<Note>();
		
		Chord chordon = getChord(chord);
		
		if(chordon != null) {
			return chordon.getNotes();
		}
		
		return notes;
	}
	
	
	public Chord getChord(int position) {
		return chords.size() <= position ? null : chords.get(position);
	}
	
	public Layer getLayer(int position) {
		return layers.size() <= position ? null : layers.get(position);
	}
	
	public void verifyChords(int size) {
		if(chords.size() <= size) {
			for(int i = chords.size(); i < size + 1; i++) {
				chords.add(new Chord(i));
			}
		}
	}
	
	public void verifyLayers(short size) {
		if(layers.size() <= size) {
			for(int i = layers.size(); i < size + 1; i++) {
				layers.add(new Layer("", (byte)100));
			}
		}
		
		height = (short) layers.size();
	}
	
	public void setLength(short length) {
		this.length = length;
	}

	public short getHeight() {
		return height;
	}

	public void setHeight(short height) {
		this.height = height;
	}

	public void setTempo(double tempo) {
		this.tempo = tempo;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setOriginalAuthor(String originalAuthor) {
		this.originalAuthor = originalAuthor;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSongOrigin(String songOrigin) {
		this.songOrigin = songOrigin;
	}

	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
	}

	public void setAutoSaveDuration(byte autoSaveDuration) {
		this.autoSaveDuration = autoSaveDuration;
	}

	public void setTimeSignature(byte timeSignature) {
		this.timeSignature = timeSignature;
	}

	public void setMinutesSpent(int minutesSpent) {
		this.minutesSpent = minutesSpent;
	}

	public void setLeftClicks(int leftClicks) {
		this.leftClicks = leftClicks;
	}

	public void setRightClicks(int rightClicks) {
		this.rightClicks = rightClicks;
	}

	public void setBlocksAdded(int blocksAdded) {
		this.blocksAdded = blocksAdded;
	}

	public void setBlocksRemoved(int blocksRemoved) {
		this.blocksRemoved = blocksRemoved;
	}

	public void setChords(ArrayList<Chord> chords) {
		this.chords = chords;
	}

	public void setLayers(ArrayList<Layer> layers) {
		this.layers = layers;
	}

	public void setInstruments(ArrayList<Instrument> instruments) {
		this.instruments = instruments;
	}

	public short getLength() {
		return length;
	}
	
	public double getTempo() {
		return tempo;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getOriginalAuthor() {
		return originalAuthor;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getSongOrigin() {
		return songOrigin;
	}
	
	public boolean isAutoSave() {
		return autoSave;
	}
	
	public byte getAutoSaveDuration() {
		return autoSaveDuration;
	}
	
	public byte getTimeSignature() {
		return timeSignature;
	}
	
	public int getMinutesSpent() {
		return minutesSpent;
	}
	
	public int getLeftClicks() {
		return leftClicks;
	}
	
	public int getRightClicks() {
		return rightClicks;
	}
	
	public int getBlocksAdded() {
		return blocksAdded;
	}
	
	public int getBlocksRemoved() {
		return blocksRemoved;
	}
	
	public ArrayList<Chord> getChords() {
		return chords;
	}
	
	public ArrayList<Layer> getLayers() {
		return layers;
	}
	
	public ArrayList<Instrument> getInstruments() {
		return instruments;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
