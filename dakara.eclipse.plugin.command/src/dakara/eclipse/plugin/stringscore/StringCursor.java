package dakara.eclipse.plugin.stringscore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class StringCursor {
	public String text;
	private int indexOfCursor = 0;
	private int currentMarker = 0;
	
	private List<Integer> markers = new ArrayList<>();
	public StringCursor(String text) {
		this.text = text;
	}
	
	public StringCursor setMarkers(List<Integer> markers) {
		this.markers = markers;
		return this;
	}
	
	public boolean markerPositionTerminal() {
		if (currentMarker == markers.size() || currentMarker == -1) return true;
		return false;
	}
	
	public char currentMarker() {
		if (currentMarker < markers.size())
			return text.charAt(markers.get(currentMarker));
		return 0;
	}	
	
	public char peekNextMarker() {
		int nextMarker = currentMarker + 1;
		if (nextMarker < markers.size())
			return text.charAt(markers.get(nextMarker));
		return 0;
	}
	
	public char setPreviousMarkCurrent() {
		if (currentMarker > 0) {
			return text.charAt(markers.get(currentMarker - 1));
		}
		return 0;
	}
	
	public StringCursor addMark(int indexOfMarkToAdd) {
		if (indexOfMarkToAdd >= text.length()) throw new IllegalArgumentException("Index is greater than text length " +indexOfMarkToAdd);
		
		// insert all marks in order
		int markerNumber = 0;
		for (Integer markerIndex : markers) {
			if (indexOfMarkToAdd > markerIndex) {
				markerNumber++;
			}
		}
		
		markers.add(markerNumber, indexOfMarkToAdd);
		//markers.add(indexOfMarkToAdd);
		return this;
	}
	
	public StringCursor clearMarkers() {
		markers.clear();
		return this;
	}
	
	public StringCursor markFillRangeForward(int charsForward) {
		if (charsForward + indexOfCursor > text.length()) throw new IllegalArgumentException("Index is greater than text length. index " + indexOfCursor + " range " + charsForward + " '" + text + "'");
		for (int index = indexOfCursor; index < indexOfCursor + charsForward; index++) {
			addMark(index);
		}
		return this;
	}
	
	public StringCursor markFillAlphaRangeForward(int charsForward) {
		final int originalCursorPosition = indexOfCursor;
		for (int count = 0; count < charsForward; count++) {
			
			addMark(indexOfCursor);
			moveCursorForwardNextAlpha();
		}
		
		indexOfCursor = originalCursorPosition;
		
		return this;
	}
	
	public List<Integer> markers() {
		return markers;
	}
	
	public int countUnMarkedWordsBetweenMarkers(int firstMarker, int lastMarker) {
		if (markers.isEmpty()) return 0;
		int wordCount = 0;
		final int originalCursorPosition = indexOfCursor;
		final int originalMarkerPosition = currentMarker;
		int startIndex = markers.get(firstMarker);
		indexOfCursor = startIndex;
		
		setFirstMarkCurrent().setCurrentMarkToEndOfCurrentMarkedRegion();
		while(currentMarker < lastMarker) {
			int nonAlphaRegionCount = countNonAlphabeticRegions(indexOfCurrentMark(), indexOfNextMark());
			if (nonAlphaRegionCount > 1) {
				// We are counting words which are surrounded by whitespace on each side.
				// So there must be 2 non alphabetic regions for this to be true.
				wordCount += nonAlphaRegionCount - 1;
			}
			setNextMarkCurrent().setCurrentMarkToEndOfCurrentMarkedRegion();
		}
		
		indexOfCursor = originalCursorPosition;
		currentMarker = originalMarkerPosition;
		
		return wordCount;
	}
	
	public int countNonAlphabeticRegions(int startIndex, int endIndex) {
		final int originalCursorPosition = indexOfCursor;
		indexOfCursor = startIndex;
		int count = 0;
		while(indexOfCursor < endIndex) {
			if (!Character.isAlphabetic(text.charAt(indexOfCursor))) {
				count++;
				// skip all whitespace
				moveCursorForwardUntil(cursor -> Character.isAlphabetic(cursor.currentChar()));
			} else {
				// skip all chars
				moveCursorForwardUntil(cursor -> !Character.isAlphabetic(cursor.currentChar()));
			}
		}
		indexOfCursor = originalCursorPosition;
		
		return count;
	}
	
	public int countAlphabeticCharsBetween(int startIndex, int endIndex) {
		final int originalCursorPosition = indexOfCursor;
		indexOfCursor = startIndex + 1;
		int count = 0;
		while(indexOfCursor < endIndex) {
			if (Character.isAlphabetic(text.charAt(indexOfCursor))) {
				count++;
			} 
			indexOfCursor++;
		}
		indexOfCursor = originalCursorPosition;
		
		return count;
	}
	
	public StringCursor setCurrentMarkToEndOfCurrentMarkedRegion() {
		if (markerPositionTerminal()) return this;
		
		// move until no more contiguous marks
		while (indexOfNextMark() == indexOfCurrentMark() + 1) {
			setNextMarkCurrent();
		}
		return this;
	}
	
	public boolean currentMarkIsFirstOfMarkedRegion() {
		if (markers.size() == 0) return false;
		if (markerPositionTerminal()) return false;
		if (currentMarker == 0) return true;
		// is previous marker's index next to this marker
		return markers.get(currentMarker - 1) != markers.get(currentMarker) - 1;
	}
	
	public String wordAtCursor() {
		if (cursorPositionTerminal()) return "";
		int currentIndex = indexOfCursor;
		int indexStart = moveCursorPreviousAlphaBoundary().indexOfCursor();
		int indexEnd   = moveCursorNextAlphaBoundary().indexOfCursor();
		indexOfCursor = currentIndex;
		return text.substring(indexStart, indexEnd + 1);
	}
	
	public String markersAsString() {
		StringBuilder builder = new StringBuilder();
		for(Integer index : markers) {
			builder.append(text.charAt(index));
		}
		return builder.toString();
	}
	
	public boolean cursorPositionTerminal() {
		if (indexOfCursor == text.length() || indexOfCursor == -1) return true;
		return false;
	}
	
	public boolean cursorAtWordStart() {
		if (!Character.isAlphabetic(text.charAt(indexOfCursor))) return false;
		return !Character.isAlphabetic(peekPreviousChar());
	}
	
	public char currentChar() {
		if (indexOfCursor < text.length())
			return text.charAt(indexOfCursor);
		return 0;
	}
	
	public char peekPreviousChar() {
		if (indexOfCursor > 0) {
			return text.charAt(indexOfCursor - 1);
		}
		return 0;
	}
	
	public char peekNextChar() {
		if (indexOfCursor < text.length() - 1) {
			return text.charAt(indexOfCursor + 1);
		}
		return 0;
	}
	
	public StringCursor moveCursorForwardUntil(Function<StringCursor, Boolean> shouldStop) {
		while(!shouldStop.apply(this) && !cursorPositionTerminal()) {
			indexOfCursor++;
		}
		return this;
	}
	
	public StringCursor moveCursorPreviousAlphaBoundary() {
		while(Character.isAlphabetic(peekPreviousChar())) {
			moveCursorBackward();
		}
		return this;
	}
	
	public StringCursor moveCursorNextAlphaBoundary() {
		while(Character.isAlphabetic(peekNextChar())) {
			moveCursorForward();
		}
		return this;
	}
	
	public StringCursor moveCursorIndexOf(String match) {
		 indexOfCursor = text.indexOf(match);
		 return this;
	}
	
	public StringCursor moveCursorForwardIndexOfAlphaSequenceWrapAround(String match) {
		if (indexOfCursor > 0) {
			moveCursorForwardIndexOfAlphaSequence(match);
			if (cursorPositionTerminal()) {
				// we did not find from current until end.  Try again from beginning
				indexOfCursor = 0;
				moveCursorForwardIndexOfAlphaSequence(match);
			}
		} else {
			moveCursorForwardIndexOfAlphaSequence(match);
		}
		
		return this;
	}
	
	public StringCursor moveCursorForwardIndexOfAlphaSequence(String match) {
		
		 StringCursor matchCursor = new StringCursor(match);
		 int startOfSequenceIndex = -1;
		 while(!matchCursor.cursorPositionTerminal()) {
			 if (matchCursor.indexOfCursor == 0) {
				 moveCursorForwardIndexOf(matchCursor.currentChar());
				 startOfSequenceIndex = indexOfCursor;
				 if (cursorPositionTerminal()) break;				 
			 } else {
				 moveCursorForwardNextAlpha();
				 if (cursorPositionTerminal()) break; 				 
				 if (currentChar() != matchCursor.currentChar()) {
					 // next char was not found. try again from the beginning
					 setCursorPosition(startOfSequenceIndex + 1);
					 matchCursor.setCursorPosition(0);
					 continue;
				 } 				 
			 }
			 matchCursor.moveCursorForward();
		 }
		 
		 // We matched all chars if we are at terminal position.  Reset cursor to initial index
		 if (matchCursor.cursorPositionTerminal()) {
			 indexOfCursor = startOfSequenceIndex;
		 } 
		 
		 return this;
	}
	
	public StringCursor moveCursorForwardIndexOf(char match) {
		 indexOfCursor = text.indexOf(match, indexOfCursor);
		 return this;
	}
	
	public StringCursor moveCursorForwardNextWord() {
		 moveCursorNextAlphaBoundary();  // end of current word
		 moveCursorForward(); // char beyond word
		 moveCursorForwardUntil(cursor -> Character.isAlphabetic(cursor.text.charAt(indexOfCursor))); // move until first char of next word
		 return this;
	}
	
	public StringCursor moveCursorForward() {
		 indexOfCursor++;
		 return this;
	}
	
	public StringCursor moveCursorForwardNextAlpha() {
		 while(!cursorPositionTerminal()) {
			 indexOfCursor++;
			 if (cursorPositionTerminal() || Character.isAlphabetic(text.charAt(indexOfCursor))) break;
		 }
		 return this;
	}
	
	public StringCursor moveCursorBackward() {
		 indexOfCursor--;
		 return this;
	}	
	
	public StringCursor setCursorPosition(int index) {
		 indexOfCursor = index;
		 return this;
	}	
	
	public StringCursor setNextMarkCurrent() {
		 currentMarker++;
		 return this;
	}
	
	public StringCursor setFirstMarkCurrent() {
		 currentMarker = 0;
		 return this;
	}
	
	public int indexOfFirstMark() {
		return markers.get(0);
	}
	
	public int indexOfLastMark() {
		return markers.get(markers.size() - 1);
	}
	
	public int indexOfCursor() {
		return indexOfCursor;
	}
	
	public int indexOfNextMark() {
		if (!markers.isEmpty() && currentMarker + 1 < markers.size())
			return markers.get(currentMarker + 1);
		return 0;
	}
	
	public int indexOfCurrentMark() {
		if (!markers.isEmpty() && !markerPositionTerminal())
			return markers.get(currentMarker);
		return -1;
	}
	
	public StringCursor maskRegions(List<Integer> maskIndexes) {
		if (maskIndexes.size() == 0) return this;
		StringBuilder builder = new StringBuilder(text);
		maskIndexes.stream().forEach(index -> builder.setCharAt(index, ' '));
		text = builder.toString();
		return this;
	}
	
	@Override
	public String toString() {
		return text + ":: cursorIndex[" + indexOfCursor + "] markerNumIndex[" + currentMarker + "," + indexOfCurrentMark()+ "] markers " + markers;
	}
}
