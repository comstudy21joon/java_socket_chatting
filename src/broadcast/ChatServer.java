package broadcast;

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {
	public static void main(String[] args) {
		try {
			ServerSocket server = new ServerSocket(10001);
			System.out.println("접속을 기다립니다.");
			HashMap hm = new HashMap();
			while (true) {
				System.out.println(".....");
				Socket sock = server.accept();
				ChatThread ct = new ChatThread(sock, hm);
				ct.start();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}

class ChatThread extends Thread {
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	// 생성자
	public ChatThread(Socket sock, HashMap hm) {
		System.out.println("ChatThread - Server");
		this.sock = sock;
		this.hm = hm;
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + "님이 접속했습니다.");
			System.out.println("접속한 사용자의 아이디는 " + id + "입니다.");
			synchronized (hm) {
				hm.put(this.id, pw);
			}
			initFlag = true;
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
	public void run() {
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.equals("/quit"))
					break;
				if (line.indexOf("/to") == 0) {
					sendmsg(line);
				} else {
					broadcast(id + " : " + line);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			synchronized (hm) {
				hm.remove(id);
			}
			broadcast(id + " 님이 접속 종료했습니다.");
			try {
				if (sock != null)
					sock.close();
			} catch (Exception ex) {}
		}
	}

	public void sendmsg(String msg) {
		int start = msg.indexOf(" ") + 1;
		int end = msg.indexOf(" ", start);
		if (end != -1) {
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end + 1);
			Object obj = hm.get(to);
			if (obj != null) {
				PrintWriter pw = (PrintWriter) obj;
				pw.println(id + "님이 다음의 귓속말을 보내셨습니다. :" + msg2);
				pw.flush();
			}
		}
	}

	public void broadcast(String msg) {
		synchronized (hm) {
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while (iter.hasNext()) {
				PrintWriter pw = (PrintWriter) iter.next();
				pw.println(msg);
				pw.flush();
			}
		}
	}
}
