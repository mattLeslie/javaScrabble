package hw07;

import java.util.ArrayList;

public class TileBag {
	
	//acts as source of tiles for both players
	private ArrayList<String> bag = new ArrayList<String>();
	public TileBag() {
		//add all scrabble tiles to bag
		addToBag("A", 9);
		addToBag("B", 2);
		addToBag("C", 2);
		addToBag("D", 4);
		addToBag("E", 12);
		addToBag("F", 2);
		addToBag("G", 3);
		addToBag("H", 2);
		addToBag("I", 9);
		addToBag("J", 1);
		addToBag("K", 1);
		addToBag("L", 4);
		addToBag("M", 2);
		addToBag("N", 6);
		addToBag("O", 8);
		addToBag("P", 2);
		addToBag("Q", 1);
		addToBag("R", 6);
		addToBag("S", 4);
		addToBag("T", 6);
		addToBag("U", 4);
		addToBag("V", 2);
		addToBag("W", 2);
		addToBag("X", 1);
		addToBag("Y", 2);
		addToBag("Z", 1);		

	}
	
	private void addToBag(String c, Integer num) {
		for(int i = 0; i < num; i++) {
			bag.add(c);
		}
	}
	
	//removes tile from bag when signaled
	//happens when the other player modifies the tile bag
	public void removeFromBag(String c) {
		bag.remove(c);
	}
	
	//pick a random index and draw the tile
	public Tile drawOne() {
		if(bag.size() == 0) {
			return null;
		}
		Integer r =(int) (Math.random() * bag.size());
		Tile t = new Tile(bag.get(r));
		bag.remove(t.getLetter());
		return t;
	}	
}
