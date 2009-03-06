import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


public class connection{

	private SocketChannel client;
	private int port = 80;//for http
	private StringBuffer message;

	public connection(SocketChannel sc){
		message = new StringBuffer();
		client = sc;
	}

	public SocketChannel getChannel(){
		return client;
	}

	public void connect(String host){
		try {
			client.connect(new java.net.InetSocketAddress(host, port));
		} catch (IOException e) {
			System.out.println("Failed to connect to " + host);
		}
	}

	public void write(String arg){

		try {
			if(client.isConnectionPending())client.finishConnect();
			ByteBuffer buff = ByteBuffer.wrap(arg.getBytes());
			client.write(buff);
			buff.clear();
		} catch (IOException e) {
			System.out.println("failed to write to SocketChannel");
		}
	}


	public void append(String code){
		message.append(code);

	}

	public String message(){
		return message.toString();
	}
}
