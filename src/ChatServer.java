
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * A multithreaded chat room server.  When a client connects the
 * server requests a screen name by sending the client the
 * text "SUBMITNAME", and keeps requesting a name until
 * a unique one is received.  After a client submits a unique
 * name, the server acknowledges with "NAMEACCEPTED".  Then
 * all messages from that client will be broadcast to all other
 * clients that have submitted a unique screen name.  The
 * broadcast messages are prefixed with "MESSAGE ".
 *
 * Because this is just a teaching example to illustrate a simple
 * chat server, there are a few features that have been left out.
 * Two are very useful and belong in production code:
 *
 *     1. The protocol should be enhanced so that the client can
 *        send clean disconnect messages to the server.
 *
 *     2. The server should do some logging.
 */
public class ChatServer {

    /**
     * The port that the server listens on.
     */
    private static final int PORT = 9001;

    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */
    private static HashSet<String> names = new HashSet<String>();

    private static Map<String,PrintWriter> clients = new HashMap<String,PrintWriter>();
    
    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    Socket socket;
    static BufferedReader in;
    static PrintWriter out;
    
    /**
     * The application main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        
       ServerSocket listener = new ServerSocket(PORT);

        try {
            while (true) {
            	Socket socket  = listener.accept();
            	Handler handler = new Handler(socket);
                Thread handlerThread = new Thread(handler);
                handlerThread.start();
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private static class Handler implements Runnable {
    	
    	private ThreadLocal<String> myThreadLocal = new ThreadLocal<String>();
        private String name;
        private Socket socket;
        BufferedReader in;
        PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         * 
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME");
                    
                    name = in.readLine();
                    
                    if (name == null) {
                    	return;
                    }
                    
                    // synchronized lock to ensure the thread safety of the shared variable 'names'
					synchronized(names) {
						try{  
							if (!names.contains(name)) {
	                            names.add(name);
	                            clients.put(name, out);
	                            break;
	                        }
							else {
								Toolkit.getDefaultToolkit().beep();
							}
						}
						catch(Exception e){
							System.out.println(e);
						}  
                    }
                    
                     
                 }

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                writers.add(out);
                
                // Broadcast to all online clients that new user has joined the chat
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE <<< " + name + " has joined the conversation >>>");
                    
                }
                                           
               
                // TODO: You may have to add some code here to broadcast all clients the new
                // client's name for the task 9 on the lab sheet. 
                while(true) {
                	
                	// Iterating through names hash set using Iterator 
                	Iterator<String> hashSetIterator = names.iterator();
                	
                	// Broadcast all clients that a new user has been joined to the chat
                	// and clear the online user list for refresh the list
                	for (PrintWriter writer : writers) {
                		writer.println("NEW");
                    }
                	
                	// Refresh the online user list 
                	
                	while (hashSetIterator.hasNext()) {
                		
                    	Object o = hashSetIterator.next();
                          
                    	// sending each active client name to every active client
                    	// This process connected to a protocol named as ONLINEUSERS
                        for (PrintWriter writer : writers) {
                        	writer.println("ONLINEUSERS " + o);
                        }
                        
                    }
                	
                	break;
                	
                }
                
                
                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    
                    if (input == null || input == "") {
                        return;
                    }
                    
                  
                    
                    // Added code to send a message to a specific client and not
                    // all clients.
                    
                    if(input.contains(">>")) {
                    	
                    	// break the string into message and recipient part
        				StringTokenizer st = new StringTokenizer(input, ">>");
        				
        				// First part of the input string is recipient
        				String receipent = st.nextToken();
        				// Second part of the input string is message
        				String MsgtoSend = st.nextToken();
        				
        				// Check the recipient name in the online users list
        				if (names.contains(receipent)) {
        					for (Entry<String, PrintWriter> entry : clients.entrySet()) {
        						
        						// Getting the Key from clients hash map 
        						// and check it is available in  
            					if(entry.getKey().equals(receipent)) {
            						
            						// Getting the Value from the hash map 
            						// and write into recipient output stream to send message
            						entry.getValue().println("MESSAGE " + name + ": " + MsgtoSend);
            						writers.add(entry.getValue());
            					}
            				    
            				}
                        }
        				
                    }
                    else { // This will handle the broadcasting feature of the chat application
                    	for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                            
                        }
                    }
                    
                 
    				
    				
    				
    				
    				
    				
    			    
                    
                }
            }// Handled the SocketException here to handle a client closing the socket
            catch (IOException e) {
            	System.out.println("<<< " + name + " has left the conversation >>>");
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                    
                    // Inform all clients if a user left the chat
                    for (PrintWriter writer : writers) {
                    	
                    		// Sending message to client app to remove offline user from online user list
                    		// This will handle by a protocol named USERLEFT
                    		writer.println("USERLEFT " + name);
                    		writer.println("MESSAGE <<< " + name + " has left the chat >>>");
                    }
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}