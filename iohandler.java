//must call io_init() before being able to use
import java.io.*;
import java.nio.*;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class iohandler {

	private static Selector select;
	private static Queue<String> toWriteList;
	private static int buffersize = 1024;//because 1024 bytes is just good wholesome fun
	private static Charset charset;
	private static CharsetDecoder decoder;
	private static SocketChannel socketome = null;


	public static void io_init(){

		try {
			select = Selector.open();
			toWriteList = new LinkedList<String>();
			charset = Charset.forName("ISO-8859-1");
			decoder = charset.newDecoder();
		} catch (IOException e) {
			System.out.println("unable to create iohandler");
		}

	}

	public static void destroy() throws IOException{
		socketome.close();
		select.close();
	}

	public static connection newConnection(String host){
		try {
			SocketChannel client = SocketChannel.open();
			client.configureBlocking(false);
			connection conn = new connection(client);
			SelectionKey selkey = client.register(select, SelectionKey.OP_CONNECT);
			conn.connect(host);
			selkey.attach(conn);
			return conn;
		} catch (IOException e) {
			System.out.println("failed to establish new connection");
			System.exit(1);
			return null;
		}

	}

	private static void input(connection c){
		try {
			c.getChannel().write(ByteBuffer.wrap(toWriteList.remove().getBytes()));
		} catch (IOException e) {
			System.out.println("Failed to write to SocketChannel");
		}
	}

	private static void output(connection c){
		ByteBuffer rawbuff = ByteBuffer.allocate(buffersize);
		CharBuffer charbuff = CharBuffer.allocate(buffersize);
		try {

			c.getChannel().read(rawbuff);
			rawbuff.flip();
			decoder.decode(rawbuff, charbuff, false);
			charbuff.flip();
			c.append(charbuff.toString());
			System.out.print(charbuff.toString());
			rawbuff.clear();
			charbuff.clear();
		} catch (IOException e) {
			System.out.println("Failed to read from SocketChannel");
		}
	}


	public static void handleConnections(){

		try {
			while(select.select(500) > 0){
				Set keys = select.selectedKeys();
				Iterator i = keys.iterator();
				while(i.hasNext()){
					SelectionKey key = (SelectionKey)i.next();
					i.remove();
					socketome = (SocketChannel)key.channel();

					if(key.isConnectable()){
						if(socketome.isConnectionPending())socketome.finishConnect();
						key.interestOps(SelectionKey.OP_WRITE);
						System.out.println("server found");
					}
					else if(key.isWritable()){
						if(!toWriteList.isEmpty()){
							input((connection)key.attachment());
							key.interestOps(SelectionKey.OP_READ);
							System.out.println("wrote to list");
						}else{
							System.out.println("nothing to write");
						}
					}
					else if(key.isReadable()){
						//System.out.println("reading buffer....");
						output((connection)key.attachment());
					}
				}

			}
		} catch (IOException e) {
			System.out.println("Unforseen IO exception in handleConnections()");
		}
	}

	public static void write(String arg){
		System.out.println("writing...." + '\n' + arg);
		toWriteList.add(arg);
	}

	public static void main(String args[]){

		iohandler.io_init();
		iohandler.newConnection("google.com");
		Scanner in = new Scanner(System.in);
		System.out.println("testing");
		StringBuilder msg = new StringBuilder();
		System.out.println("insert HTTP formatted packet");
		String line = in.nextLine();
		while(!line.equals("!")){
			msg.append(line);
			msg.append('\n');
			line = in.nextLine();
		}
		iohandler.write(msg.toString());
		iohandler.handleConnections();

	}

}
