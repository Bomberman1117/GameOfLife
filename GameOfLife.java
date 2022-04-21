//Adam Buerger
//Visualizes Conway's Game of Life

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameOfLife extends Application{
	// Create and initialize cell
	public int size = 32;
	private Cell[][] cell =  new Cell[size][size];
	private boolean activeHighLife = false;
	public Timeline animation = new Timeline();
	public boolean isPlaying = false;
	
	Slider speed = new Slider();
	// Create and initialize a status label

	@Override // Override the start method in the Application class
	public void start(Stage primaryStage) {
		HBox controls = new HBox();
		Button step = new Button("Step");
		Button play = new Button("Play");
		Button clear = new Button("Clear");
		RadioButton rbLife = new RadioButton("Life");
		RadioButton rbHighLife = new RadioButton("High Life");
		BorderPane radioBox = new BorderPane();
		BorderPane borderPane = new BorderPane();
		GridPane pane = new GridPane();

		speed.setMax(10);
		speed.setMin(0.0000001);
		speed.setValue(1);
				
		ToggleGroup group = new ToggleGroup();
		rbLife.setToggleGroup(group);
		rbHighLife.setToggleGroup(group);
		radioBox.setTop(rbLife);
		radioBox.setBottom(rbHighLife);
		rbLife.setSelected(true);
		
		rbLife.setOnMouseClicked(e -> activeHighLife = false);
		rbHighLife.setOnMouseClicked(e -> activeHighLife = true);
		clear.setOnMouseClicked(e -> newGame());
		step.setOnMouseClicked(e -> {
			GridPane temp = new GridPane();
			step(cell);
			for (int i = 0; i < size; i++)
				for (int j = 0; j < size; j++)
					temp.add(cell[i][j], i, j);
			borderPane.setCenter(temp);
		});
		play.setOnMouseClicked(e -> {
			if(!isPlaying) {
				isPlaying = true;
				play.setText("Pause");
				step.setDisable(true);
				GridPane temp = new GridPane();
				step(cell);
				for (int i = 0; i < size; i++)
					for (int j = 0; j < size; j++)
						temp.add(cell[i][j], i, j);
				borderPane.setCenter(temp);
				animation.play();
			}
			else {
				isPlaying = false;
				play.setText("Play");
				step.setDisable(false);
				animation.pause();
			}
		});
		
		controls.getChildren().add(step);
		controls.getChildren().add(play);
		controls.getChildren().add(new Label("Speed: "));
		controls.getChildren().add(speed);
		controls.getChildren().add(clear);
		controls.getChildren().add(radioBox);
		controls.setAlignment(Pos.CENTER);
		controls.setSpacing(10);
		// Create menu and menu items
		MenuBar menuBar = new MenuBar();    
		Menu menuFile = new Menu("File");
		menuBar.getMenus().addAll(menuFile);
		MenuItem menuItemNewGame = new MenuItem("New Game");
		MenuItem menuItemSaveAs = new MenuItem("Save As...");
		MenuItem menuItemLoadGame = new MenuItem("Load Game");
		MenuItem menuItemExit = new MenuItem("Exit");
		menuFile.getItems().addAll(menuItemNewGame, new SeparatorMenuItem(), 
				menuItemSaveAs, menuItemLoadGame, new SeparatorMenuItem(),
				menuItemExit);
	
		menuItemNewGame.setOnAction(e -> newGame());
		menuItemSaveAs.setOnAction(e -> saveAs(primaryStage));
		menuItemLoadGame.setOnAction(e -> loadGame(primaryStage));
		menuItemExit.setOnAction(e -> System.exit(0));
		
		menuItemNewGame.setAccelerator(
			KeyCombination.keyCombination("Ctrl+N"));
		menuItemSaveAs.setAccelerator(
    	    KeyCombination.keyCombination("Ctrl+S"));
		menuItemLoadGame.setAccelerator(
			KeyCombination.keyCombination("Ctrl+L"));
		menuItemExit.setAccelerator(
            KeyCombination.keyCombination("Ctrl+X"));
		// Pane to hold cell
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				pane.add(cell[i][j] = new Cell(), i, j);
		borderPane.setTop(menuBar);
		borderPane.setCenter(pane);
		borderPane.setBottom(controls);
		// Create a scene and place it in the stage
		Scene scene = new Scene(borderPane, 750, 750);
		primaryStage.setTitle("Game of Life"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
	}
	public void step(Cell[][] currentBoard){
		boolean[][] nextBoard = new boolean[size][size];
		
		for(int row = 0; row < size; row++) {
			for(int col = 0; col < size; col++) {
				int neighbors = 0;
				for(int i = -1; i <= 1; i++) {
					for(int j = -1; j <=1; j++) {
						if(currentBoard[(row+i+size)%size][(col+j+size)%size].isAlive)
							neighbors++;
					}
				}
				if(currentBoard[row][col].isAlive) {
					//ignore counted self
					neighbors--;
					if(neighbors == 2 || neighbors == 3)
						nextBoard[row][col] = true;
					else
						nextBoard[row][col] = false;
				}
				else {
					if(activeHighLife && (neighbors == 3 || neighbors == 6))
						nextBoard[row][col] = true;
					else if(neighbors == 3)
						nextBoard[row][col] = true;
					else {
						nextBoard[row][col] = false;
					}
				}
			}
		}
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
				cell[i][j].setState(nextBoard[i][j]);
	}
	public void fillBoard(Cell[][] board) {
		for(int i = 0; i < board.length; i++) {
			for(int j = 0; j < board[i].length; j++) {
				board[i][j] = new Cell();
			}
		}
	}
	public void newGame() {
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++) {
				cell[i][j].reset();
				cell[i][j].setState(false);
				cell[i][j].getChildren().clear();
			}
	}
	private void saveAs(Stage primaryStage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("."));
		fileChooser.setTitle("Enter file name");
		fileChooser.getExtensionFilters().add(
			new ExtensionFilter("Life files", "*.life"));
		File selectedFile = fileChooser.showSaveDialog(primaryStage);
		if (selectedFile != null) {
            try (
                     ObjectOutputStream output =
                      new ObjectOutputStream(new FileOutputStream(selectedFile));) {
              output.writeBoolean(activeHighLife);
              char[][] cellStates = new char[size][size];
              for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++)
                  cellStates[i][j] = cell[i][j].getToken();
              output.writeObject(cellStates);
            }
            catch (IOException ex) {
              ex.printStackTrace();
            }
        }
	}
	private void loadGame(Stage primaryStage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("."));
		fileChooser.setTitle("Enter file name");
		fileChooser.getExtensionFilters().add(
	         new ExtensionFilter("Life files", "*.life"));
		File selectedFile = fileChooser.showOpenDialog(primaryStage);
		if (selectedFile != null)
			try { // Create an input stream for file object.dat
				ObjectInputStream input =
				new ObjectInputStream(new FileInputStream(selectedFile)); {
					activeHighLife = input.readBoolean();
					char[][] cellStates = (char[][])(input.readObject());
				for (int i = 0; i < size; i++)
					for (int j = 0; j < size; j++)
						cell[i][j].setToken(cellStates[i][j]);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	// An inner class for a cell
	public class Cell extends Pane {
		// Token used for this cell
		public boolean isAlive = false;
		public boolean hasBeenAlive = false;
		private char token = ' ';
		public Cell() {
			animation = new Timeline(
				new KeyFrame(Duration.millis(1000), e -> {
					step(cell);
				}));
			animation.setCycleCount(Timeline.INDEFINITE);
			animation.rateProperty().bind(speed.valueProperty());
			setStyle("-fx-background-color: black;"
					+ "-fx-border-color: white;"
					+ "-fx-border-width: 0.5;"); 
			this.setPrefSize(800, 800);
			this.setOnMousePressed(e -> handleClick(e));
			this.setOnDragDetected(e -> this.startFullDrag());
			this.setOnMouseDragEntered(e -> handleClick(e));
		}
		public char getToken() {
			return token;
		}
		public void reset() {
			isAlive = false;
			hasBeenAlive = false;
		}
		public void setState(boolean living) {
			if(living) {
				isAlive = true;
				hasBeenAlive = true;
				token = 'X';
				setStyle("-fx-background-color: green;"
						+"-fx-border-color: white;"
						+ "-fx-border-width: 0.5;");
			}
			else if(hasBeenAlive) {
				isAlive = false;
				token  = 'O';
				setStyle("-fx-background-color: grey;"
						+"-fx-border-color: white;"
						+"-fx-border-width: 0.5");
			}
			else {
				isAlive = false;
				token = ' ';
				setStyle("-fx-background-color: black;"
						+"-fx-border-color: white;"
						+"-fx-border-width: 0.5");
			}
		}
		public void setToken(char t) {
			if(t == 'X') {
				isAlive = true;
				hasBeenAlive = true;
				token = 'X';
				setStyle("-fx-background-color: green;"
						+"-fx-border-color: white;"
						+ "-fx-border-width: 0.5;");
			}
			else if(t == 'O') {
				isAlive = false;
				token  = 'O';
				setStyle("-fx-background-color: grey;"
						+"-fx-border-color: white;"
						+"-fx-border-width: 0.5");
			}
			else {
				isAlive = false;
				token = ' ';
				setStyle("-fx-background-color: black;"
						+"-fx-border-color: white;"
						+"-fx-border-width: 0.5");
			}
		}
		public void handleClick(MouseEvent e) {
			if(e.isPrimaryButtonDown())
				setState(true);
			else if(e.isSecondaryButtonDown())
				setState(false);
		}
	}
	
	/**
	* The main method is only needed for the IDE with limited
	* JavaFX support. Not needed for running from the command line.
	*/
	public static void main(String[] args) {
		launch(args);
	}
}