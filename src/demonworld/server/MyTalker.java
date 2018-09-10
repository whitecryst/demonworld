package demonworld.server;

/*
 * Multiplayer Example - Simple User Interface
 * by ROOT
 * http://blakenet.no-ip.org
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

class MyTalker implements ActionListener
{
 protected JTextField textField;
 protected JTextArea textArea;
 Boolean exiting = false;
 
 Socket socket = null;
 PrintWriter out = null;
 BufferedReader in = null;
 
 public MyTalker()
 {
  JFrame frame = new JFrame("Mplay Simple User Interface");
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  frame.setLayout(new GridBagLayout());
  GridBagConstraints c = new GridBagConstraints();
  c.fill = GridBagConstraints.HORIZONTAL;
  c.insets = new Insets(5, 5, 0, 5);
  
  JLabel label1 = new JLabel("Message:");
  c.weightx = 0;
  c.weighty = 0;
  c.gridx = 0;
  c.gridy = 0;
  frame.add(label1, c);
  
  textField = new JTextField(30);
  c.weightx = 1;
  c.gridx = 1;
  c.gridy = 0;
  c.gridwidth = GridBagConstraints.REMAINDER;
  textField.addActionListener(this);
  frame.add(textField, c);
  
  JLabel label2 = new JLabel("Log:");
  c.gridx = 0;
  c.gridy = 1;
  frame.add(label2, c);
  
  textArea = new JTextArea(15, 30);
  c.weighty = 1;
  c.gridx = 0;
  c.gridy = 2;
  c.gridwidth = GridBagConstraints.REMAINDER;
  c.gridheight = GridBagConstraints.REMAINDER;
  textArea.setEditable(false);
  JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  frame.add(scrollPane, c);
  
  frame.pack();
  frame.setVisible(true);
 }
 
 public void actionPerformed(ActionEvent evt)
 {
  String text = textField.getText();
  if (text == "quit") exiting = true;
  else if(text == "all") {
	 // out.println
  }else {
  
   sendMessage(text);
   textArea.append("Client> " + text + "\n");
   textField.setText("");
   
   textArea.setCaretPosition(textArea.getDocument().getLength());
  }
 }
 
 private void sendMessage(String msg)
 {
  out.println(msg);
 }
 
 private void init(String host, int port) throws IOException
 {
  try
  {
   socket = new Socket(host, port);
   out = new PrintWriter(socket.getOutputStream(), true);
   in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  }
  catch(UnknownHostException e)
  {
   System.out.println("Unknown or unreachable host " + host + " on port " + port);
   System.exit(1);
  }
  catch(IOException e)
  {
   System.out.println("I/O error");
   System.exit(1);
  }
  
  sendMessage("hello");
  
  while (exiting == false)
  {
   if (in.ready()) textArea.append("Server> " + in.readLine() + "\n");
  }
  
  out.close();
  in.close();
  socket.close();
  System.exit(0);
 }
 
 public static void main(String[] args) throws IOException
 {
  if (args.length < 1 || args.length > 2) System.out.println("Usage: java MyTalker [host] port\n\nExamples:\njava MyTalker 8087\njava MyTalker localhost 5000\njava MyTalker blakenet.no-ip.org 8087");
  else
  {
   String host = "localhost";
   int port;
   if (args.length == 1) port = Integer.parseInt(args[0]);
   else{ host = args[0]; port = Integer.parseInt(args[1]); }
   
   MyTalker thisTalker = new MyTalker();
   thisTalker.init(host, port);
  }
 }
}