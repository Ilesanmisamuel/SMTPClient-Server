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

public class SMTPServer extends Application implements SMTPConstants
{
	//Declares Stage and Scene
   private Stage stage;
   private Scene scene;

	//Declares VBox
   private VBox root = new VBox(8);

	//Declares GridPanes
   private GridPane grid = new GridPane();


	//Declares Buttons
   private Button btnClear = new Button("Clear Log");

	//Declares MenuBar
   private MenuBar menuBar = new MenuBar();
	
	//Declares Menu 
   private Menu menuOpt = new Menu("Options");

	//Declares MenuItems
   private MenuItem menConnect = new MenuItem("Connect");
   private MenuItem menExit = new MenuItem("Exit");

	//Declares TextArea
   private TextArea taLog = new TextArea();

	//Declares Label
   private Label lblLog = new Label("Log: ");

	//Declares Whether Client Is Connected Or Not Connected
   private boolean isConnected = false;

	//Declares Socket
   private Socket SOCK = null;

	//Declares Server Socket
   private ServerSocket servSock = null;

	//Declares ServerThread
   private ServerThread server = null;

	//Creates String of Port For Text Output
   private String portStr = Integer.toString(PORT);
   
	//Declares Server Password
   private String password = "password";


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
    * alert - creates an alert for new errors
    * @param title - string for title
    * @param head - string for header text
    * @param aler - string for alert
    */
   public void alert(String title, String head, String aler)
   {
      Platform.runLater(
         new Runnable()
         {
            public void run()
            {
               Alert alert = new Alert(AlertType.ERROR, aler);
               alert.setTitle(title);
               alert.setHeaderText(head);
               alert.showAndWait();
               update(aler);
            }
         });
   }
      
   	/*
      * ServerThread
      *
      * Thread that starts the server at the port 30000 
      */	
   public class ServerThread extends Thread
   {
   		//Method To End Server Connection
      public void end()
      {
         try
         {
            servSock.close();
            isConnected = false;
            update("Closed Server Port: "+portStr+"\n");
            menConnect.setText("Connect");
         }
         catch(Exception ex)
         {
            alert("ServerThread Error", "Disconnect Error", "ERROR: Could Not Close Socket");		
         }
      }	
   
   		//Run Method For ServerThread To Start Server Connection/HandlerThread
      public void run()
      {
         try
         {
            update("Opening Server Port: "+portStr+"\n");			
            servSock = new ServerSocket(PORT);
            while(true)
            {
               SOCK = servSock.accept();
               HandlerThread handler = new HandlerThread(SOCK);
               handler.start();
               isConnected = true;
               update("Server Open");
               menConnect.setText("Disconnect");
            }
         }
         catch(Exception ex)
         {
            alert("ServerThread Error", "Connect Error", "ERROR: Could Not Connect Server");
         }
      }
   }
      
  	/*
   * ServerThread
   *
   * Thread that handles all of the server commands that are sent back to the client
   */	
   public class HandlerThread extends Thread
   {
      private Socket sock = null;
      private PrintWriter pwt = null;
      private Scanner scn = null;
      private DataOutputStream dout = null;
      private ArrayList<Email> mailList = new ArrayList();
      private String user = "";
      private String passkey = "";
   	
   		//Constructor For HandlerThread
      public HandlerThread(Socket _SOCK)
      {
         sock = _SOCK;
      }
   	
     
      /*
      * sendInbox - reads in the client information for each message and reads into a dataInputStream
      */
      public void sendInbox()
      {
         try
         {
            InetAddress addr = SOCK.getInetAddress();
            String remote = addr.getHostAddress();
         
            File fil = new File(user+".dat");
            DataInputStream din = new DataInputStream(new FileInputStream(fil));
            long filLeng = fil.length();
            for(int i = 0; i != filLeng; i++)
            {
                  String sender = din.readUTF();
                  pwt.println("SENDER: "+sender);
                  pwt.flush();
                  String rec = din.readUTF();
                  pwt.println("RECIEVER: "+rec);
                  pwt.flush();
                  String subj = din.readUTF();
                  pwt.println("SUBJECT: "+subj);
                  pwt.flush();
                  String mess = din.readUTF();
                  boolean isEnc = din.readBoolean();
            }
         }
         catch(Exception ex)
         {
            alert("Inbox Error", "Inbox Send Error", "Inbox Error");
         }
      }
      
      /*
      * saveInbox - Saves the mail sent from the client to a .dat file called inbox
      */
      public void saveInbox()
      {
         try
         {
         
            File fil = new File(user+".dat");
            DataOutputStream dout = new DataOutputStream(new FileOutputStream(fil));
            for(int i = 0; i != mailList.size(); i++)
            {
               Email saveMail = mailList.get(i);
               String ip = saveMail.getIp();
               dout.writeUTF(ip);
               String sender = saveMail.getSender();
               dout.writeUTF(sender);
               String reciever = saveMail.getReciever();
               dout.writeUTF(reciever);
               String subj = saveMail.getSubject();
               dout.writeUTF(subj);
               String mess = saveMail.getMessage();
               dout.writeUTF(mess);
               boolean isEnc = saveMail.getEnc();
               dout.writeBoolean(isEnc);
            }
         }
         catch(Exception ex)
         {
            alert("Inbox Error", "Inbox Save Error", "Inbox Error");
         }
      }
   	
      public void run()
      {
         try
         {
            InetAddress addr = SOCK.getInetAddress();
            String remote = addr.getHostAddress();
         
            pwt = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            scn = new Scanner(new InputStreamReader(sock.getInputStream()));
           
            update("CONNECTED");
            pwt.println("220" + remote + "ESMTP Postfix");
            pwt.flush();
         
            while(scn.hasNextLine())
            {
               
              
               String in = scn.nextLine();
               update(in);
               if(in.startsWith("HELO"))
               {
                  pwt.println("250 Hello" + addr + "I am glad to meet you");
                  pwt.flush();
               
                
               }
                
               else if(in.startsWith("MAIL FROM:"))
               {
                
                  pwt.println("250 Ok");
                  pwt.flush();
               }
                  //String thrIn = scn.nextLine();
               else if(in.startsWith("RCPT TO:")){
                  pwt.println("250 Ok");
                  pwt.flush();
               }
                //String fourIn = scn.nextLine();
               else if(in.startsWith("DATA")){
                  pwt.println("354 please end data with ;!;");
                  pwt.flush();
		  String message = scn.nextLine();
		  update(message);
        pwt.println("250 Ok");
		  update("EMAIL SAVED TO INBOX");		
		}
     
		
		
		  //String[] inp = message.split(" ");
		  //for(int i = 0; i != inp.length; i++)
		  //{
		 //	update(inp[i]);
		  //}
                  //Email newMail = new Email(inp[2], inp[5], inp[8], inp[11], mess);
               	  //mailList.add(message);
               	  //saveInbox();
                  //update("EMAIL SAVED TO INBOX");
               
              
               
               
               
               
               //String reIn = scn.nextLine();
               else if(in.equals("RETRIEVE EMAIL"))
               {
                  pwt.println("250 ok");
                  pwt.flush();
		  user = scn.nextLine();
		  passkey = scn.nextLine();
  		  
                  sendInbox();
               }
               else
               {
                  pwt.println("221 Server received unknown command");
                  update("221 Server received unknown command");
               }
            }
               
         }
         	
         catch(Exception ex)
         {
            alert("HandlerThread", "Handle Error", "Run Error");
            update("HandlerThread Error");
         }
      }
   }


	   /*
      * start - starts the GUI for the client
      * @param _stage - object of Stage for displaying the GUI
      */
   public void start(Stage _stage) throws Exception
   {
      stage = _stage;
      stage.setTitle("SMTP Server");
   	
      int width = 700;
      int height = 400;
   	
      menuBar.getMenus().addAll(menuOpt);
      menuOpt.getItems().addAll(menConnect, menExit);
   
      taLog.setPrefWidth(670);
      taLog.setPrefHeight(300);
      taLog.setWrapText(true);	
   		
      grid.setAlignment(Pos.CENTER);
      grid.setHgap(10);
      grid.setVgap(10);
      grid.addRow(0, lblLog);
      grid.addRow(1, taLog);
      grid.addRow(2, btnClear);
   	
   		//Menu Handler
      btnClear.setOnAction(
         new EventHandler<ActionEvent>()
         {
            public void handle(ActionEvent evt)
            {
               taLog.clear();
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
                  server = new ServerThread();
                  server.start();
               }
               else
               {
                  server.end();
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
   
      root.getChildren().addAll(menuBar, grid);
   
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
