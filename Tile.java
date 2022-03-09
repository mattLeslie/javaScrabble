package hw07;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

//draggable scrabble tile
//remembers where it was on the tile rack
//and will return there if not placed on a BoardSquare

public class Tile extends StackPane {
	private String letter;
	private Label label;
	private double dx = 0.0;
	private double dy = 0.0;
	private double origX;
	private double origY;
	
	public Tile(String l) {
		this.letter = l;
		this.label = new Label(l);
		this.setStyle("-fx-border-color: grey; -fx-border-width: 3px; -fx-background-color:white; -fx-font-weight: bold; -fx-font-size: 20px;");
		this.setPrefSize(40, 40);
		this.getChildren().add(label);
		
	}
	//set coordinates on screen (position on tile rack)
	public void setOrigCoords(double x, double y) {
		origX = x;
		origY = y;
	}
	public double getOrigX() {
		return origX;
	}
	public double getOrigY() {
		return origY;
	}
	
	public String getLetter() {
		return this.letter;
	}
	
	
	//whenever the mouse is pressed over the shape's pane, record the distance 
	//that the mouse has traveled from its original location
	public void recordDist(MouseEvent m) {
        this.dx = this.getLayoutX() - m.getSceneX();
        this.dy = this.getLayoutY() - m.getSceneY();	

	}
	//whenever the mouse presses the shape's pane and drags from that position,
	//update the shape's pane's position with new coordinates
	public void drag(MouseEvent m) {
		this.setLayoutX(m.getSceneX() + dx);
		this.setLayoutY(m.getSceneY() + dy);	
	}
	
}
