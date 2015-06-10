package com.ithermit.debugproxy;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Main {

    private String remoteHost;
    private int remotePort;
    private int localPort;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.remoteHost = args[0];
        main.remotePort = Integer.parseInt(args[1]);
        main.localPort = Integer.parseInt(args[2]);
        main.doWork();
    }

    public void doWork() throws Exception {
        System.setProperty("javax.net.ssl.keyStore", "testkeystore.ks");
        System.setProperty("javax.net.ssl.keyStorePassword", "Passw0rd");

        SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketfactory.createServerSocket(localPort);

        while (true) {
            Socket socket = sslServerSocket.accept();
            final InputStream localIs = socket.getInputStream();
            final OutputStream localOs = socket.getOutputStream();

            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket s = (SSLSocket) factory.createSocket(remoteHost, remotePort);
            s.startHandshake();

            final OutputStream remoteOs = s.getOutputStream();
            final InputStream remoteIs = s.getInputStream();

            Pipe pipe = new Pipe(localIs, remoteOs);
            Thread th1 = new Thread(pipe);
            th1.start();

            Pipe pipe2 = new Pipe(remoteIs, localOs);
            Thread th2 = new Thread(pipe2);
            th2.start();
        }

    }

    class Pipe implements Runnable {
        private OutputStream os;
        private InputStream is;

        public Pipe(InputStream is, OutputStream os) {
            this.os = os;
            this.is = is;
        }

        @Override
        public void run() {
            System.out.println("\n[socket open]\n");
            try {
                int i;
                byte[] buffer = new byte[64];
                while ((i = is.read(buffer)) != -1) {
                    os.write(buffer, 0, i);
                    System.out.write(buffer, 0, i);
                }
                os.close();
                is.close();
                System.out.println("\n");
            } catch (IOException e) {
                System.out.println("[socket closed]");
            }
        }
    }
}
