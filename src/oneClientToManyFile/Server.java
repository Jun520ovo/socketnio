package oneClientToManyFile;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


public class Server {
	 private ServerSocketChannel serverSocketChannel = null;  
	 private  ServerTheard serverTheard;//������������ӵ�ѡ����
	 private  ServerTheard serverTheard2;//��������������ݵ�ѡ����
	 public boolean isReadBellRunning = false;
	 
	 
	public static void main(String[] args) throws Exception{
			new Server().serverthread();;
	}
	
	public void serverthread() throws Exception{
		serverTheard = new ServerTheard();//��������
		
		serverTheard2 =new ServerTheard();//������
		
	
       serverSocketChannel = ServerSocketChannel.open();  
       // ����ͨ��������ģʽ������  
       serverSocketChannel.configureBlocking(false);  
       serverSocketChannel.socket().setReuseAddress(true);  
       serverSocketChannel.socket().bind(new InetSocketAddress(1111));
       serverSocketChannel.register(serverTheard.selector(), SelectionKey.OP_ACCEPT);  
		new Thread(serverTheard).start();
		
	}
	

	public class ServerTheard implements Runnable{
		private String Filepath = "C:\\Users\\Administrator\\Desktop\\Socketserver";
		private Selector selector;
		
		public ServerTheard() throws Exception{
			selector  = Selector.open();  
	        // �򿪷������׽���ͨ��  
		}
		public Selector selector(){
			return this.selector;
		}
		@Override
		public void run() {
			try {
				while(true) {
					
					if(this.selector.select(3000) == 0) {
						continue;
					}
					
					Set<SelectionKey> set = this.selector.selectedKeys(); // �������Ϊͨ�������������кܶ��ֵ ����תΪһ��set Ȼ��ͨ������ȡ����
					Iterator<SelectionKey> keyIter = set.iterator();// ��ȡ�������SelectionKey
					while(keyIter.hasNext()) {
						SelectionKey key = keyIter.next(); // �Զ��ӵ��������ҵ����ӽ���ƥ��
						keyv(key);
						keyIter.remove();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		public void keyv(SelectionKey selectionKey) throws Exception{
			if (selectionKey.isAcceptable()) {/* �ж�accept�¼� */
				ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
				SocketChannel channel = server.accept();
				channel.configureBlocking(false);
				channel.register(serverTheard2.selector(), SelectionKey.OP_READ);
					if (!Server.this.isReadBellRunning) {
						Server.this.isReadBellRunning = true;
						new Thread(serverTheard2).start();
					}
			} else if (selectionKey.isReadable()) {
				new ServerUtil().Filereceiving(selectionKey,Filepath);
			}
		}
	}
}
