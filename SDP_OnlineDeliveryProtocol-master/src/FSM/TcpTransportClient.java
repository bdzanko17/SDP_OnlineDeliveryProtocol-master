package FSM;

import MessageTemplate.Message;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TcpTransportClient implements IFSM, Runnable, Cloneable {
	private int id;
	private Dispatcher dispatcher;
	private IFSM receiver;
	private BlockingQueue<IMessage> messageQueue = new LinkedBlockingQueue<IMessage>();
	private IMessage messageTemplate;
	private boolean connected;
	private int serverPort;
	private String serverAddress;
	private Thread runner = null;
	private Socket client = null;
	private boolean done = false;

	public TcpTransportClient(int id) {
		this.id = id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}

	public void setServerPort(int port) {
		this.serverPort = port;
	}

	public int getServerPortPort() {
		return serverPort;
	}
	public void setServerAddress(String serverAddress){
		this.serverAddress = serverAddress;
	}
	public String getServerAddress(){
		return serverAddress;
	}
	@Override
	public void start() {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}

	@Override
	public void stop() {
		//
		runner.interrupt();
	}

	@Override
	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	public Dispatcher getDispatcher() {
		return dispatcher;
	}

	public void setReceiver(IFSM receiver) {
		this.receiver = receiver;
	}

	public IFSM getReceiver() {
		return receiver;
	}


	private void sendToStream(String msg, OutputStream output) throws Exception{
		char eof = (char)(0);
		msg = msg + eof;
		var byts = msg.getBytes();
		output.write(byts,0, byts.length);
		output.flush();
	}
	@Override
	public void addMessage(IMessage message) {
		if (message.getMessageId() == 5555){
			serverAddress = ((Message)(message)).getParam(Message.Params.IP);
			runClient();
		}else
			messageQueue.add(message);
	}

	public boolean isConnected() {
		return (client == null);
	}
	public void runClient(){
		try {
			client = new Socket(serverAddress, serverPort);
			Thread ta = new Thread(new Runnable() {
				@Override
				public void run() {
					sendMessages();
				}
			});
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					listenForMessages(client);
				}
			});
			t.start();
			ta.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {

	}

	public void sendMessages(){
		try{
			while(true){
				if(client != null){
					IMessage msgToSend = messageQueue.poll();
					if(msgToSend != null){
						sendToStream(new String(msgToSend.buildTransportMessage()),client.getOutputStream());
					}
				}else{
					client = new Socket(serverAddress, serverPort);
					wait(100);
				}
				Thread.sleep(1);
			}
		}catch (Exception e){

		}
	}
	public void listenForMessages(Socket connectionSocket) {
		try {
			BufferedReader fromClient =
					new BufferedReader(
							new InputStreamReader(connectionSocket.getInputStream()));
			while (true){
				String recString = "";
				try{
					int rec;
					recString = "";
					while((rec = fromClient.read()) > 0){
						if((char)rec == (char)(0))
							break;
						else{
							recString += (char)rec;
						}
					}

				}catch(Exception e){

					break;
				}

				InetSocketAddress sockaddr = (InetSocketAddress)connectionSocket.getRemoteSocketAddress();

				String fromAddress = sockaddr.getAddress().toString().replace("/","");
				fromAddress += ":" + sockaddr.getPort();
				IMessage receivedMsg = new Message();
				var bytes = recString.getBytes();
				receivedMsg.parseTransportMessage(bytes, bytes.length);
				receivedMsg.setFromId(id);
				receivedMsg.setToId(receiver.getId());
				receivedMsg.setFromAddress(fromAddress);
				//System.out.println("Received " + receivedMsg.getMessageId());
				dispatcher.addMessage(receivedMsg);

				Thread.sleep(1);
			}

			connectionSocket.close();
			//System.out.println("TCP disconected from server!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
