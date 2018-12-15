/**
 *@author: Henry Keena, Samuel Okikiola Ilesanmi, Bobby Sexton, Charlie Cohen
 *@version: 
**/

//Declares Imports
import java.net.*;
import java.io.*;
import java.util.*;
import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;
import javax.swing.*;
import javafx.util.Pair;
import javafx.scene.control.ButtonBar.ButtonData;

public class SMTPClient extends Application implements SMTPConstants
{
	//Declares Stage and Scene
   private Stage stage;
   private Scene scene;

	//Declares VBox
   private VBox root = new VBox(8);

	//Declares GridPanes
   private GridPane grid1 = new GridPane();
   private GridPane grid2 = new GridPane();
   private GridPane grid3 = new GridPane();

	//Declares Buttons
   private Button btnSend = new Button("Send");
   private Button btnClearL = new Button("Clear Log");
   private Button btnClearF = new Button("Clear Fields");
   private Button btnEncrypt = new Button("Encrypt");
   private Button btnGetMail = new Button("Get Mail");

	//Declares MenuBar
   private MenuBar menuBar = new MenuBar();
	
	//Declares Menu 
   private Menu menuOpt = new Menu("Options");

	//Declares TextArea
   private TextArea taLog = new TextArea();
   private TextArea taMessage = new TextArea();

	//Declares MenuItems
   private MenuItem menConnect = new MenuItem("Connect");
   private MenuItem menLogin = new MenuItem("Login");
   private MenuItem menExit = new MenuItem("Exit");

	//Declares TextField
   private TextField tfRecipient = new TextField();
   private TextField tfSender = new TextField();
   private TextField tfSubject = new TextField();

	//Declares Label
   private Label lblSender = new Label("Sender: ");
   private Label lblRecipient = new Label("Recipient: ");
   private Label lblSubject = new Label("Subject: ");
   private Label lblMessage = new Label("Message: ");
   private Label lblLog = new Label("Log: ");

	//Declares Socket
   private Socket SOCK = null;
   private String string = "";	

	//Declares DataOutput/DataInput Streams
   private PrintWriter pwt = null;
   private Scanner scn = null;

	//Declares Sender Address
   private String sender = "";

	//Declares Whether Client Is Connected Or Not Connected
   private boolean isConnected = false;

	//Declares Whether Message Is Encrypted Or Not
   private boolean isEncrypted = false;

	//Creates String of Port For Text Output
   private String portStr = Integer.toString(PORT);
   private String user = "";
   private String passkey = "";
   

	/*
    * update - update the log when called opon
    * @param upd - string to update log
    */
   public void update(String upd)
   {
      Platform.runLater(
         new Runnable()
         {
            public void run()
            {
               try
               {
                  if(isConnected == true)
                  {
                     InetAddress addr = SOCK.getInetAddress();
                     String remote = addr.getHostAddress();
                     String ip = ("<"+remote+":"+portStr+">");
                     taLog.appendText(ip+" "+upd+"\n");
                  }
                  else
                  {
                     taLog.appendText(upd+"\n");
                  }
               }
               catch(Exception ex)
               {
                  Alert alert = new Alert(AlertType.ERROR, "Log Update Error");
                  alert.setTitle("SMTP Client");
                  alert.setHeaderText("Update Error");
                  alert.showAndWait();
               }
            }
         });
   }

	/*
    * doServerConnect - makes the connection to the client
    */
   public void doServerConnect()
   {
      try
      {
         JFrame frame = new JFrame("Server Connect");
    	 String host = JOptionPane.showInputDialog(frame, "Enter Server Connection: ");
         SOCK = new Socket(host, PORT);
         scn = new Scanner(new InputStreamReader(SOCK.getInputStream()));
         pwt = new PrintWriter(new OutputStreamWriter(SOCK.getOutputStream()));
         isConnected = true;         
         menConnect.setText("Disconnect");
         btnSend.setDisable(false);
         btnEncrypt.setDisable(false);
         btnGetMail.setDisable(false);
         update("Client Connected");
         String str = scn.nextLine();
         update(str);
         if(str.startsWith("220"))
         {
            pwt.println("HELO relay");
            pwt.flush();
         }
      }
      catch(Exception ex)
      {
         Alert alert = new Alert(AlertType.ERROR, "Socket Connect Error");
         alert.setTitle("SMTP Client");
         alert.setHeaderText("Connection Error");
         alert.showAndWait();
         update("Connect Error");
      }
   }

	/*
    * doServerDisconnect - terminates the connection between client/server
    */
   public void doServerDisconnect()
   {
      try
      {
         menConnect.setText("Connect");
         btnSend.setDisable(true);
         btnEncrypt.setDisable(true);
         btnGetMail.setDisable(true);
         scn.close();
         pwt.close();
         SOCK.close();
         isConnected = false;
         update("Client Disconnected");
      }
      catch(Exception ex)
      {
         Alert alert = new Alert(AlertType.ERROR, "Socket Disconnect Error");
         alert.setTitle("SMTP Client");
         alert.setHeaderText("Connection Error");
         alert.showAndWait();
         update("Disconnect Error");
      }
   }

	 /*
    * doSend - sends the message written on client to the server along with sender, recipient, and subject
    */
   public void doSend()
   {
      String message;
      try
      {
         message = taMessage.getText();
         String from = tfSender.getText();
         String recp = tfRecipient.getText();
         String subj = tfSubject.getText();
         if(isEncrypted == true)
         {
            message = "E+##$ "+ message + ";!;";
         }
        
      
         String email = " From: " + from + "  To: " + recp + "  Subject: "+subj + "  Message: " + message;
         
         
         if(from.equals("") || from.equals(null) || from.equals(" ") || recp.equals("") || recp.equals(null) || recp.equals(" "))
         {
            Alert alert = new Alert(AlertType.ERROR, "Email Send Error");
            alert.setTitle("SMTP Client");
            alert.setHeaderText("Send Error");
            alert.showAndWait();
            update("ERROR: SENDER/RECIPIENT REQUIRED");
         }
         else
         {		
            pwt.println("MAIL FROM:"+from);
            pwt.flush();
            String upd = scn.nextLine();
            update(upd);
            pwt.println("RCPT TO:"+recp);
            pwt.flush();
            String upd1 = scn.nextLine();
            update(upd1);
            pwt.println("DATA");
            pwt.flush();
            String upd2 = scn.nextLine();
            update(upd2);
            pwt.flush();
            pwt.println(email);
            pwt.flush();
            update("Message Delivered");			
         }
      }
      catch(Exception ex)
      {
         Alert alert = new Alert(AlertType.ERROR, "Email Send Error");
         alert.setTitle("SMTP Client");
         alert.setHeaderText("Send Error");
         alert.showAndWait();
         update("Message Send Error");			
      }
   }
   
   /*
    * getMail - scan in mail recieved from the server
    */
   public void getMail()
   {
      try
      {
        pwt.println("RETRIEVE EMAIL");
        pwt.flush();

	Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
      
      	// Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
      
      	// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
      
        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
      
        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
      
      	// Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
      
      	// Do some validation (using the Java 8 lambda syntax).
        username.textProperty().addListener((observable, oldValue, newValue) -> 
      	{
        	loginButton.setDisable(newValue.trim().isEmpty());
        });
      
        dialog.getDialogPane().setContent(grid);
      	// Request focus on the username field by default.
      	Platform.runLater(() -> username.requestFocus());
      
      	// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> 
      	{
        	if (dialogButton == loginButtonType) 
      		{
        		return new Pair<>(username.getText(), password.getText());
        	}
        	return null;
        });
      
        Optional<Pair<String, String>> result = dialog.showAndWait();
      
        result.ifPresent(usernamePassword -> 
      	{
        	user = usernamePassword.getKey();
        	passkey = usernamePassword.getValue();
        });

	 pwt.println(user);
	 pwt.flush();
	 pwt.println(passkey);
	 pwt.flush();

         tfSender.clear();
         tfRecipient.clear();
         tfSubject.clear();
         taMessage.clear();
         //String str = scn.nextLine();
         //update(str);
      	
         while(scn.hasNextLine())
         {
            String from = scn.nextLine();
            taMessage.appendText(from+"\n");
            String to = scn.nextLine();
            taMessage.appendText(to+"\n");
            String subj = scn.nextLine();
            taMessage.appendText(subj+"\n");
            String mess = scn.nextLine();
	    boolean isEnc = scn.nextBoolean();
	    if(isEnc == true)
	    {
 		String deMess = deCipher(mess);
		taMessage.appendText(deMess+"\n\n");
            }
            else
	    {
            	taMessage.appendText(mess+"\n\n");
            }
	}
      }
      catch(Exception ex)
      {
      
         Alert alert = new Alert(AlertType.ERROR, "Email Inbox Retrieval Error");
         alert.setTitle("SMTP Client");
         alert.setHeaderText("Get Error");
         alert.showAndWait();
         update("Message Retrieval Error");
      }
   }


	/*
    * enCipher - encrypts the message being sent using 13 shifts with caesar cipher encryption
    * @param wor - the string being read into the method to encrypt the message
    * @return the String encrypted
    */
   public String enCipher(String wor)
   {
      String encrypted = "";
      try
      {
         int shif = SHIFT;
         ArrayList<String> encryptList = new ArrayList();	
         String[] arg = wor.split(" ");
         ArrayList<String> list = new ArrayList();
         for(int i = 0; i != arg.length; i++)
         {
            list.add(arg[i]);
         }		
         for(int x = 0; x != list.size(); x++)
         {
            String build = "";
            for(int i = 0; i < list.get(x).length(); i++)
            {
               char ca = (char)(list.get(x).charAt(i) + shif);
               if (ca > 'z')
               {
                  build += (char)(list.get(x).charAt(i) - (26-shif));
               }
               else
               {
                  build += (char)(list.get(x).charAt(i) + shif);
               }
            }
            encryptList.add(build);
         }
         for(int y = 0; y < encryptList.size(); y++)
         {
            encrypted = encrypted + " " + encryptList.get(y);
         }
         update("Message Encrypted");
      }
      catch(Exception ex)
      {
         Alert alert = new Alert(AlertType.ERROR, "Encryption Error");
         alert.setTitle("CaesarCipher");
         alert.setHeaderText("Encryption Error");
         alert.showAndWait();
         update("Encryption Error");
      }
      isEncrypted = true;
      return encrypted;
   }





	//Public Decryption Cipher
   public String deCipher(String wor)
   {
      String decrypted = "";
      try
      {
         int shif = SHIFT;
         ArrayList<String> decryptList = new ArrayList();
         shif = shif - (shif+shif);		
         String[] arg = wor.split(" ");
         ArrayList<String> list = new ArrayList();
         for(int i = 0; i != arg.length; i++)
         {
            list.add(arg[i]);
         }
         for(int x = 0; x != list.size(); x++)
         {
            String build = "";
            for(int i = 0; i < list.get(x).length(); i++)
            {
               char ca = (char)(list.get(x).charAt(i) + shif);
               if (ca > 'z')
               {
                  build += (char)(list.get(x).charAt(i) - (26-shif));
               }
               else
               {
                  build += (char)(list.get(x).charAt(i) + shif);
               }
            }
            decryptList.add(build);			
         }
         for(int y = 0; y < decryptList.size(); y++)
         {
            decrypted = decrypted + " " + decryptList.get(y);
         }
         update("Message Decrypted");
      }
      catch(Exception ex)
      {
         Alert alert = new Alert(AlertType.ERROR, "Decryption Error");
         alert.setTitle("CaesarCipher");
         alert.setHeaderText("Decryption Error");
         alert.showAndWait();
         update("Decryption Error");
      }
      return decrypted;
   }

	/*
    * start - starts the GUI for the client
    * @param _stage - object of Stage for displaying the GUI
    */
   public void start(Stage _stage) throws Exception
   {
      stage = _stage;
      stage.setTitle("SMTP Client");
   
      int width = 730;
      int height = 600;
   
      menuBar.getMenus().addAll(menuOpt);
      menuOpt.getItems().addAll(menConnect, menExit);
   
      btnSend.setDisable(true);
      btnGetMail.setDisable(true);
      btnEncrypt.setDisable(true);
   
      tfRecipient.setPrefWidth(600);
      tfSubject.setPrefWidth(600);
   	      // Set dimensions of the TextAreas for log and message
      taMessage.setPrefWidth(350);
      taMessage.setPrefHeight(600);
      taMessage.setWrapText(true);
      taLog.setPrefWidth(350);
      taLog.setPrefHeight(600);
      taLog.setWrapText(true);		
            // Set position of Top grid - Labels and TextFields for message info
      grid1.setAlignment(Pos.CENTER);
      grid1.setHalignment(lblRecipient, HPos.LEFT);
      grid1.setHalignment(lblSubject, HPos.LEFT);
      grid1.setHalignment(lblMessage, HPos.LEFT);
      grid1.setHgap(10);
      grid1.setVgap(10);
      grid1.addRow(1, lblSender, tfSender);
      grid1.addRow(2, lblRecipient, tfRecipient);
      grid1.addRow(3, lblSubject, tfSubject);
            // Set position of Middle grid - Labels and TextAreas for messages and log chat
      grid2.setAlignment(Pos.CENTER);
      grid2.setHgap(10);
      grid2.setVgap(10);
      grid2.addRow(1, lblMessage, lblLog);
      grid2.addRow(2, taMessage, taLog);
            // Set position of Encrypt and Send button on Middle grid
      grid2.addRow(3, btnEncrypt);
      grid2.addRow(4, btnSend);
      grid2.addRow(5, btnGetMail);
            // Set position of Bottom grid -  Buttons for Clearing Field and Log TextAreas
      grid3.setAlignment(Pos.CENTER);		
      grid3.setHgap(10);
      grid3.setVgap(10);
      grid3.addRow(0, btnClearF, btnClearL);			
   
   		//Button Handler
      btnSend.setOnAction(
         new EventHandler<ActionEvent>()
         {
            public void handle(ActionEvent evt)
            {
               doSend();
            }
         });
   
   		//Button Handler
      btnGetMail.setOnAction(
         new EventHandler<ActionEvent>()
         {
            public void handle(ActionEvent evt)
            {
               getMail();
            }
         });
   
   
   		//Button Handler
      btnEncrypt.setOnAction(
         new EventHandler<ActionEvent>()
         {
            public void handle(ActionEvent evt)
            {
               String mess = taMessage.getText();
               String enMess = enCipher(mess);
               taMessage.setText(enMess);
            }
         });
   
   		//Button Handler
      btnClearL.setOnAction(
         new EventHandler<ActionEvent>()
         {
            public void handle(ActionEvent evt)
            {
               taLog.clear();
            }
         });
   
   		//Button Handler
      btnClearF.setOnAction(
         new EventHandler<ActionEvent>()
         {
            public void handle(ActionEvent evt)
            {
               tfRecipient.clear();
               tfSubject.clear();
               taMessage.clear();
            }
         });
   
   		//Menu Handler
      menConnect.setOnAction(
         new EventHandler<ActionEvent>()
         {
            public void handle(ActionEvent evt)
            {
               if(menConnect.getText().equals("Connect"))
               {
                  doServerConnect();
               }
               else
               {
                  doServerDisconnect();
               }			
            }
         });
   
   	//Menu Handler
      menExit.setOnAction(
         new EventHandler<ActionEvent>()
         {
            public void handle(ActionEvent evt)
            {
               System.exit(0);
            }
         });
   
   		//Window Exit Handler
      stage.setOnCloseRequest(
         new EventHandler<WindowEvent>() 
         {
            public void handle(WindowEvent we) 
            {
               System.exit(0);
            }
         });
   
      root.getChildren().addAll(menuBar, grid1, grid2, grid3);
   
      scene = new Scene(root, width, height);
   
      stage.setScene(scene);
      stage.show();
   }

	/*
    * main - main program
    * @param args - array of command line arguments
    */
   public static void main(String[] args)
   {
      launch(args);
   }
}
