package hw07;
//Author: Matt Leslie
//Networked Scrabble! For ITP 368 with BEK
//Fall 2021

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

//main scrabble application
//sets up networking and handles game flow
public class ScrabbleLeslie extends Application{
    
    private ServerSocket ss;
    private Socket clientSocket;
    private String ip;
    int socketNumber = 17777;
    private BufferedReader myIn = null;
    private PrintWriter myOut = null;
    private Ear oe;
    private Stage stage;
    private VBox root;
    private Group boardPane;
    private HBox scorePane;
    private Label turn;
    private BoardSquare[][] grid = new BoardSquare[15][15];
    private Label yScore;
    private Label tScore;
    private int yourScore = 0;
    private int theirScore = 0;
    private boolean myTurn = true;
    private TileBag tileBag = new TileBag();
    private Pane rack;
    private ArrayList<Tile> hand = new ArrayList<Tile>();
    private VBox welcomePane;

	public static void main(String[] args) {launch(args);}
	
	//set up lobby screen with buttons to host game or join game
	public void start(Stage stage) {
	    this.stage = stage;
	    stage.setTitle("Scrabble Leslie");
		root = new VBox();
		root.setAlignment(Pos.CENTER);
		root.setSpacing(20);
		root.setStyle("-fx-background-color: oldlace;");
		stage.setScene(new Scene(root, 600, 600));
		stage.setResizable(false);
		stage.show();
		
		welcomePane = new VBox();
		Text welcomeText = new Text("Welcome to Scrabble!");
		Button chooseHost = new Button("Host Game");
		Button chooseClient = new Button("Join Game");
		
		HBox buttons = new HBox();
		buttons.setAlignment(Pos.CENTER);
		buttons.getChildren().addAll(chooseHost, chooseClient);
		buttons.setSpacing(20);
		
		welcomePane.getChildren().addAll(welcomeText, buttons);
		welcomePane.setAlignment(Pos.CENTER);
		welcomePane.setSpacing(30);
		
		root.getChildren().add(welcomePane);
		
		//if host, start game set up and draw tiles
		chooseHost.setOnAction(e->{setUpHost();});
		//if client, let user type in ip address and connect
		chooseClient.setOnAction(e->{		
			Label ipLabel = new Label("IP: ");
			root.getChildren().add(ipLabel);
		 	TextField iptf = new TextField("localhost");
		    root.getChildren().add(iptf);
		    iptf.setOnAction( f-> { ip = iptf.getText(); setUpClient();});
		});
		
		//close both windows on x out
 	 	stage.setOnCloseRequest( (WindowEvent w) -> { 
 	 		try{ myOut.println("byebyebye"); System.exit(0); } 
 	 		catch (Exception e) { System.out.println("can't stop"); } 
 	    }); 
	}
	
	
	//set up main board display
	private void createBoard() {
		//move past lobby
		root.getChildren().remove(welcomePane);
		
		//set up score and turn prompts
		scorePane = new HBox();
		scorePane.setSpacing(40);
		yScore = new Label("Your Score: " + yourScore);
		tScore = new Label("Their Score: " + theirScore); 
		turn = new Label();
		
		turn.setText("THEIR TURN");
		if(myTurn) {
			turn.setText("YOUR TURN");
		}
		
		scorePane.getChildren().addAll(yScore, tScore);
		scorePane.setAlignment(Pos.CENTER);
		root.getChildren().addAll(scorePane, turn);
	
		
		//draw board grid and populate data structure with board squares
		boardPane = new Group();
		
		int xSpace = 0;
		int ySpace = 0;
	
		for(int i = 0; i < 10; i++) {
			boolean toggle = true;
			if(i%2 == 0) {toggle = false;}
			for(int j = 0; j < 10; j++) {
				BoardSquare r = new BoardSquare();
				
				if(toggle) {
					r.setStyle("-fx-background-color: mistyrose; -fx-border-color: black");
				}
				else {
					r.setStyle("-fx-background-color: aliceblue; -fx-border-color: black");
					}
					toggle = !toggle;
		
					r.setPrefSize(40, 40);
					grid[i][j] = r;							
					r.setLayoutX(xSpace);
					r.setLayoutY(ySpace);
					
		
					boardPane.getChildren().add(r);
					xSpace += 40;
				}
				ySpace += 40;
				xSpace = 0;
		}
				
		root.getChildren().add(boardPane);
		
		
		//add tile rack
		rack = new Pane();
		rack.setMaxWidth(280);
		rack.setPrefHeight(40);
		rack.setStyle("-fx-border-color:black; -fx-background-color: white; -fx-border-width: 4");

		this.root.getChildren().add(rack);
	
		//add end turn button to give control to other player
		Button nextTurn = new Button("End Turn");
		nextTurn.setOnAction(e->{signal();});
		this.root.getChildren().add(nextTurn);
		
		//fill hand with tiles from tile bag
		for(int i = 0; i < 7; i++) {
			addOneToHand();
		}
	}
	
	//convenience method for shifting all tiles on the tile rack to the left
	private void compactTiles() {
		for(int i = 0; i < hand.size(); i++) {
			hand.get(i).setLayoutX(i*40);
			hand.get(i).setOrigCoords(hand.get(i).getLayoutX(), hand.get(i).getLayoutY());
		}
	}
	
	//draw a tile from the tile bag and add it to the user's hand/on screen tile rack
	private void addOneToHand() {
		
		Tile tile = tileBag.drawOne();
		if(tile == null) {
			return;
		}
		
		tile.setOnMousePressed((m)->{tile.recordDist(m);});
		tile.setOnMouseDragged((m)->{tile.drag(m);});
		tile.setOnMouseReleased((m)->{checkTileDrop(m, tile);});
		
		tile.setLayoutX(hand.size()*40);
		tile.setOrigCoords(tile.getLayoutX(), tile.getLayoutY());
		
		hand.add(tile);
		rack.getChildren().add(tile);
		
		//tell other side what tiles you drew so that they can remove them from their tile bag too
		if(myOut != null) {
			myOut.println("TILES " + tile.getLetter());
		}
	}
	
	//see where a tile has been dragged to
	//if the tile is not dropped on an empty board square,
	//return the tile to the tile rack
	//otherwise, drop the tile onto the square
	private void checkTileDrop(MouseEvent m, Tile tile) {
		Double mX = m.getSceneX() - boardPane.getLayoutX();
		Double mY = m.getSceneY() - boardPane.getLayoutY();
		boolean placed = false;
		for(int i = 0; i < 10; i++) {
			//don't let a user play if it's not their turn or if the other player isn't connected yet
			if(!myTurn || myOut == null) {break;}
			for(int j = 0; j < 10; j++) {
				BoardSquare b = grid[i][j];
				//don't let player overwrite tiles on board
				if(b.hasTile()) {
					continue;
				}
				Double bX = b.getLayoutX();
				Double bY = b.getLayoutY();
				//if the cursor dragging the tile is over a board square when the mouse is released
				//drop the tile into the board square
				if(bX <= mX && bX + 40 >= mX && bY <= mY && bY + 40 >= mY) {
					b.setTile(tile);
					//make the tile invisible and impossible to drag
					tile.setDisable(true);
					tile.setVisible(false);
					hand.remove(tile);
					placed = true;
					//let other side know you played this tile so they can update their board accordingly
					myOut.println("SQUARE " + i + " " + j + " " + tile.getLetter());
					break;
				}
				if(placed) {
					break;
				}
			}
		}
		//return tile to its original position on rack
		if(!placed) {
			tile.setLayoutX(tile.getOrigX());
			tile.setLayoutY(tile.getOrigY());
		}
	}
	
	//let the other player take control of the game
	private void signal() {
		if(!myTurn) {
			return;
		}
		//calculate scored points
		int turnScore = 7 - this.hand.size();
		this.yourScore = yourScore + turnScore;
		myTurn = false;

		//compact tiles and draw new ones to fill rack 
		compactTiles();
		for(int i = hand.size(); i < 7; i++) {
			addOneToHand();
		}
		
		//update score
		this.yScore.setText("Your Score: " + this.yourScore);	
		//tell other player how to update the scoreboard
		if(myOut!= null) {
			myOut.println("END " + yourScore);
			turn.setText("THEIR TURN");
		}
	}	
		
	private void setUpHost() {
		new SetupHost().start();
		stage.setTitle("Scrabble Leslie: HOST");
		createBoard();
	}
	
		 
    public class SetupHost extends Thread{
	   // sets up this to be the first player / host ...
	   // First player opens a socket and announces the
	   // IP and number, then waits (hangs) until 2nd connects.  
	   @Override
	   public void run(){
	   		try{
	   			ss = new ServerSocket(socketNumber);
		   	    //InetAddress ad = serverSock.getInetAddress();
	   			//System.out.println(ad); //just prints 0s
		   
	   			// wait for client to make the connection ...
	   			// the next line hangs until client connects
	   			clientSocket = ss.accept(); 
		   
	   			// once connected set up i/o, do handshake.
	   			// handshake is: server reads one line from client, 
	   			// then sends one line to client.
	   			InputStream in = clientSocket.getInputStream();
	   			myIn = new BufferedReader( new InputStreamReader(in));
	   			myOut = new PrintWriter( clientSocket.getOutputStream(),true);
	   			myOut.flush();
	   			// start the Ear thread, which listens for messages
	   			// from the other.
	   			oe = new Ear();
	   			oe.start();     
	   			
	   			for(int i = 0; i < hand.size(); i++) {
	   				myOut.println("TILES " + hand.get(i).getLetter());
	   			}
	   			myOut.println("DONE");
	   		}	
	   		catch(Exception e) { 
	   			System.out.println("socket open error e="+e); 
			}
	   }
   }	
	   
   // sets up this to be the client, which logs into
   // the host. ...
   public void setUpClient(){
	   stage.setTitle("Scrabble Leslie: CLIENT");
	   try {	   		
		   clientSocket = new Socket(ip,socketNumber);
    	   InputStream in = clientSocket.getInputStream();
    	   myIn = new BufferedReader( new InputStreamReader(in) );
    	   myOut = new PrintWriter( clientSocket.getOutputStream(),true);
    	   myOut.flush();

		   // start the Ear thread, which listens for messages
		   // from the other end.
	       oe = new Ear();
	       oe.start();
	       root.getChildren().clear();
	       myTurn = false;
    	}
		catch( Exception e ){
			System.out.println("client setup error: " + e);}
   }
   
   
   // Ear is the thread that listens for information coming
   // from the other user.  Go into a loop reading
   // whatever they send and add it to the conversation.
   // If the other end sends "byebyebye", exit this app.
   public class Ear extends Thread{
	   @Override
	   public void run(){
		   while (true){
			   try{
				   String s = myIn.readLine(); // hangs for input 
				   if(s != null) {
					   StringTokenizer st = new StringTokenizer(s);
					   String n = st.nextToken();
   			
					   //gets information on how to update the board
					   if(n.equals("SQUARE")) {
						   Platform.runLater(new Runnable() {
							   @Override public void run() {
								    String row = st.nextToken();
								    String col = st.nextToken();
									String letter = st.nextToken();
									Integer i = Integer.parseInt(row);
									Integer j = Integer.parseInt(col);
									grid[i][j].setTile(new Tile(letter));
							   	}
						   });
					   }  
					   //get information about the scoreboard and takes control of the board
						else if(n.equals("END")) {
						   Platform.runLater(new Runnable() {
							   @Override public void run() {
									String score = st.nextToken();
									theirScore = Integer.parseInt(score);
									tScore.setText("Their Score: " + theirScore);
									myTurn = true;
									turn.setText("YOUR TURN");
							   }
						   });
						}
					   //removes tiles from bag that the other side took out of theirs
						else if(n.equals("TILES")) {
							Platform.runLater(new Runnable() {
								@Override public void run() {
									String c = st.nextToken();
									tileBag.removeFromBag(c);
								}
							});
						}
					   //client use only
					   //gets signal that the host has drawn their tiles
						else if(n.equals("DONE")) {
							Platform.runLater(new Runnable() {
								@Override public void run() {
									createBoard();
								}
							});
						}
					   //exit both windows
						else if ( s.equals("byebyebye") ) { System.exit(0); }

				   }
			   	}
				catch(Exception e) {
					System.out.println("error listening: " + e.getMessage());
				}
		   	}
	   	}
   	}
}