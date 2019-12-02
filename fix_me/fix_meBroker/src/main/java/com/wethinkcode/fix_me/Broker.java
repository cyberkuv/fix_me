package com.wethinkcode.fix_me;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.CharBuffer;

public class Broker {
    static BufferedReader userInputReader = null;

    public static boolean processReadySet(Set readySet) throws Exception {
        Iterator iterator = readySet.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = (SelectionKey) iterator.next();
            iterator.remove();
            if (key.isConnectable()) {
                boolean connected = processConnect(key);
                if (!connected) {
                    return true;
                }
            }
            if (key.isReadable()) {
                String msg = processRead(key);
                if (msg.length() > 0) {
                    System.out.println("[Server]: " + msg);
                    SocketChannel sChannel = (SocketChannel) key.channel();
                    sChannel.register(key.selector(), SelectionKey.OP_WRITE);
                }

            }
            if (key.isWritable()) {
                System.out.print("[input>](Bye to quit):");
                String msg = userInputReader.readLine();

                if (msg.equalsIgnoreCase("bye")) {
                    return true;
                }
                SocketChannel sChannel = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                sChannel.write(buffer);

                sChannel.register(key.selector(), SelectionKey.OP_READ);
            }
        }
        return false;
    }

    public static boolean processConnect(SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        while (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        return true;
    }

    public static String processRead(SelectionKey key) throws Exception {
        SocketChannel sChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        sChannel.read(buffer);
        buffer.flip();
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = decoder.decode(buffer);
        String msg = charBuffer.toString();
        return msg;
    }

    public static void main(String[] args) throws Exception {
        InetAddress serverIPAddress = InetAddress.getByName("localhost");
        int port = 5000;
        InetSocketAddress serverAddress = new InetSocketAddress(serverIPAddress, port);
        Selector selector = Selector.open();
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(serverAddress);
        int operations = SelectionKey.OP_CONNECT | SelectionKey.OP_READ;
        channel.register(selector, operations);

        userInputReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(userInputReader);
        while (true) {
            if (selector.select() > 0) {
                boolean doneStatus = processReadySet(selector.selectedKeys());
                if (doneStatus) {
                    break;
                }
            }
        }
        channel.close();
    }
}