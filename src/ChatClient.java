
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;

import javax.swing.DefaultListCellRenderer;
/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area.
 */
public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 30);
    
    
    // Added a list box to view online users
    DefaultListModel<String> onlineUserListModel = new DefaultListModel<String>();
    
    JList<String> onlineUserList = new JList<String>(onlineUserListModel);
    
    // Added a check box to enable broadcasting
    JCheckBox broadcastCheckBox = new JCheckBox();
    
    // Added a button to confirm send messages
    JButton msgSendBtn = new JButton();
    
    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public ChatClient() {

    	
    	
        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        
        // set background color to message area
        messageArea.setBackground(Color.black);
        // set text color to message area
        messageArea.setForeground(Color.ORANGE);
        // set background color to text field
        textField.setBackground(Color.WHITE);
        // set text color to text field
        textField.setForeground(Color.RED);
        // set background color to online user list
        onlineUserList.setBackground(Color.BLACK);
        // set row color to online user list
        onlineUserList.setCellRenderer( new WhiteYellowCellRenderer() );
        // add text to button
        msgSendBtn.setText("<@> SEND MESSAGE TO SELECTED FRIENDS <@>");
        // set background color to message button
        msgSendBtn.setBackground(Color.black);
        // set text color to message button
        msgSendBtn.setForeground(Color.GREEN);
        // add label to check box
        broadcastCheckBox.setText("Enable Broadcasting");
        // set background color to check box
        broadcastCheckBox.setBackground(Color.BLACK);
        // set text color to check box
        broadcastCheckBox.setForeground(Color.RED);
        // set online user list's selection model
        onlineUserList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.getContentPane().add(new JScrollPane(onlineUserList), "East");
        frame.getContentPane().add(broadcastCheckBox, "West");
        frame.getContentPane().add(msgSendBtn,"South");
        frame.pack();
        
        
        // TODO: You may have to edit this event handler to handle point to point messaging,
        // where one client can send a message to a specific client. You can add some header to 
        // the message to identify the recipient. You can get the receipient name from the listbox.
        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server.    Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
        
        // added listener to check box
        broadcastCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if(broadcastCheckBox.isSelected()) {
					onlineUserList.setEnabled(false);
					msgSendBtn.setEnabled(false);
				}
				else {
					onlineUserList.setEnabled(true);
					msgSendBtn.setEnabled(true);
					
				}
			}
        	
        });
        
        // added listener to message button
        msgSendBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				// Get selected values from online user list
				List<String> selectedUsers = onlineUserList.getSelectedValuesList();
				
				// get message from pop up window
				String broadCastMessage = getMessage();
				
				// Iterate through selected user list 
				// and send each user name to the server with a token
				for(int i = 0;i < selectedUsers.size(); i++) {
					System.out.println(selectedUsers.get(i));
					out.println(selectedUsers.get(i) + ">>" + broadCastMessage);
				}
				
				// after completing message process clear selected list
				onlineUserList.clearSelection();
			}
        	
        });
        
    }
    
    // This function handles the online user list row coloring feature
    private static class WhiteYellowCellRenderer extends DefaultListCellRenderer {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
            Component c = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            if ( index % 2 == 0 ) {
                c.setBackground( Color.GREEN );
            }
            else {
                c.setBackground( Color.ORANGE );
            }
            return c;
        }
    }
    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Prompt for and return the message to be send to relevent users.
     */
    private String getMessage() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter message:",
            "To broadcast selected friends",
            JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        
        try {
        	Socket socket = new Socket(serverAddress, 9001);
        	in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
        }
        catch(Exception e) { // if the chat server is not running 
        	JOptionPane.showMessageDialog(
            		frame,
            		"Oops ! Server not online",
            		"Alert",
            		JOptionPane.WARNING_MESSAGE);
        	
        	// close opened windows 
        	frame.setVisible(false);
        }
        
        
        
        // Process all messages from server, according to the protocol.
        while (true) {
        	
            String line = in.readLine();
            
            if(line != null) {
            	if (line.startsWith("SUBMITNAME")) {
                    out.println(getName());
                } else if (line.startsWith("NAMEACCEPTED")) {            
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");
                } else if (line.startsWith("ONLINEUSERS"))  { // Added new protocol to handle viewing online users
                	onlineUserListModel.addElement(line.substring(12));
    	        } else if (line.startsWith("USERLEFT"))  { // Added new protocol to handle removing offline users from the list 
    	        	onlineUserListModel.removeElement(line.substring(9));	        
    	        }  else if (line.startsWith("NEW"))  { // Added new protocol to handle refreshing the online user list
    	        	onlineUserListModel.removeAllElements();
    	        }
            }
            
            
            
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
       
        try {
        	client.run();
        }
        catch(Exception e) { // This exception catches if the chat server disconnect
        	Toolkit.getDefaultToolkit().beep();
        	JOptionPane.showMessageDialog(
        			client.frame,
            		"Server Disconnected",
            		"Alert",
            		JOptionPane.WARNING_MESSAGE);
        	
        	// Close all opened windows
        	client.frame.setVisible(false);
        }
    }
}