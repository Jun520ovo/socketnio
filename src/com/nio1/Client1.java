package com.nio1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client1 implements Runnable{
	 private Selector selector;//�ܵ�ѡ����
	 private SocketChannel socketChannel;//�ܵ�
	 private ByteBuffer temp = ByteBuffer.allocate(1024);
	//���м�����,������г���10��,�����server�Ƿ��ж�����.
	 private static int idleCounter = 0;

	public static void main(String[] args) throws Exception{
		Client1 client1= new Client1();
		 new Thread(client1).start();
	}

	
	public Client1()throws Exception {
		 this.selector = Selector.open();//ע��һ��ѡ����
		 socketChannel= SocketChannel.open();//����һ��Զ��socket����
		 // ������ٵĽ���������,����true.���û�н���,�򷵻�false,�������Ӻ����Connect�¼�.
	     Boolean isConnected = socketChannel.connect(new InetSocketAddress("localhost", 8888));
	     //���÷�����
	     socketChannel.configureBlocking(false);
	     //���Ϊ�����ǰ�����
	     SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
		 if(isConnected) {//���ӳɹ�����һ��true   
			 sendFirstMsg();
		 }else {//ʧ�ܾͼ�������
			   key.interestOps(SelectionKey.OP_CONNECT);
		 }
	     
	}
	   public void sendFirstMsg() throws IOException {
		   String str = "���ǿͻ��˷�������";
		   //д���ܵ�����ȥ
		   socketChannel.write(ByteBuffer.wrap(str.getBytes(Charset.forName("UTF-8"))));
	   }
	   
	
	@Override
	public void run() {
		try {
		while (selector.select() > 0) {
            for (SelectionKey sk : selector.selectedKeys()) {  
            	 // �����SelectionKey��Ӧ��Channel���пɶ�������  
                if (sk.isReadable()) {  
                    // ʹ��NIO��ȡChannel�е�����  
                    SocketChannel sc = (SocketChannel) sk.channel();  
                    ByteBuffer buffer = ByteBuffer.allocate(1024);  
                    sc.read(buffer);  
                    buffer.flip();  
                    // ���ֽ�ת��ΪΪUTF-16���ַ���  
                    String receivedString = Charset.forName("UTF-8").newDecoder().decode(buffer).toString();  
                    // ����̨��ӡ����  
                    System.out.println("���յ����Է�����" + sc.socket().getRemoteSocketAddress() + "����Ϣ:" + receivedString);  
                    // Ϊ��һ�ζ�ȡ��׼��  
                    sk.interestOps(SelectionKey.OP_READ); 
                    Thread.sleep(5 * 1000);
                }  
                // ɾ�����ڴ����SelectionKey  
                selector.selectedKeys().remove(sk); 
            }
        } 
		} catch (Exception e) {
			e.printStackTrace();
		}
	   
	}
	
	
}
