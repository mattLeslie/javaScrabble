package hw07;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

//implementation of scrabble game square
//knows if it has a tile and displays the tile's character when set
public class BoardSquare extends StackPane{
	private boolean hasTile = false;
	
	public BoardSquare() {}
	
	public void setTile(Tile t) {
		Label l = new Label(t.getLetter());
		l.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");
		this.getChildren().add(l);
		this.setStyle("-fx-background-color:white; -fx-border-color: black");
		hasTile = true;
	}
	
	public boolean hasTile() {
		return hasTile;
	}
	
}
