package FSM;

import MessageTemplate.Message;

import javax.sound.midi.Receiver;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TcpTransportServer implements IFSM, Runnable, Cloneable {
	private int id;
	private Dispatcher dispatcher;
	private IFSM receiver;
	private BlockingQueue<IMessage> messageQueue = new LinkedBlockingQueue<IMessage>();
	private int serverPort;
	private Thread runner = null;
	private ServerSocket server = null;
	private Socket client = null;



	private ArrayList<Socket> connectedSockets = new ArrayList<>();

	public TcpTransportServer(int id) {
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

	public int getServerPort() {
		return serverPort;
	}

	@Override
	public void start() {
		if (runner == null) {
			try {
				server = new ServerSocket(serverPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
			runner = new Thread(this);
			runner.start();
		}
	}

	@Override
	public void stop() {
		try {
			for(Socket s: connectedSockets)
				s.close();
			Thread.sleep(500);
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	@Override
	public void addMessage(IMessage message) {
		messageQueue.add(message);
	}

	public boolean isConnected() {
		return (client == null);
	}


	@Override
	public void run() {
		Thread ta = new Thread(new Runnable() {
			@Override
			public void run() {
				sendMessages();
			}
		});
		ta.start();
		while (true) {
			try {
				Socket datasocket = server.accept();
				connectedSockets.add(datasocket);
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						listenForMessages(datasocket);
					}
				});
				t.start();
				System.out.println("Client connected to server " + datasocket.getRemoteSocketAddress());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void sendToStream(String msg, OutputStream output) throws Exception{
		char eof = (char)(0);
		msg = msg + eof;
		var byts = msg.getBytes();
		output.write(byts,0, byts.length);
		output.flush();
	}
	public void sendMessages(){
		try{
			while(true){
				IMessage msgToSend = messageQueue.poll();
				if(msgToSend != null){
					String receiverAddr = "/" + msgToSend.getToAddress();
					for(Socket clientSocket: connectedSockets){
						InetSocketAddress sockaddr = (InetSocketAddress)clientSocket.getRemoteSocketAddress();
						String socketRemoteAddress = sockaddr.getAddress() + ":" + sockaddr.getPort();
						if(socketRemoteAddress.equals(receiverAddr)){
							//send msg
							sendToStream(new String(msgToSend.buildTransportMessage()),clientSocket.getOutputStream());
						}
					}
				}
				Thread.sleep(1);
			}
		}catch (Exception e){
			e.printStackTrace();
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
			connectedSockets.remove(connectionSocket);
			System.out.println("ostalo aktivnih soketa " + connectedSockets.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
