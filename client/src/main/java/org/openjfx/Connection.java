package org.openjfx;

import java.io.*;
import java.net.Socket;

public class Connection {

  private Socket socket = null;
  private InputStreamReader inputStreamReader = null;
  private OutputStreamWriter outputStreamWriter = null;
  private BufferedReader bufferedReader = null;
  private BufferedWriter bufferedWriter = null;

  public Connection(String IP, int Port) throws IOException {
    this.socket = new Socket(IP, Port);
    this.inputStreamReader = new InputStreamReader(socket.getInputStream());
    this.outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
    this.bufferedReader = new BufferedReader(inputStreamReader);
    this.bufferedWriter = new BufferedWriter(outputStreamWriter);
  }

  public void disconnect() throws IOException {
    if (bufferedReader != null)
      bufferedReader.close();
    if (bufferedWriter != null)
      bufferedWriter.close();
    if (inputStreamReader != null)
      inputStreamReader.close();
    if (outputStreamWriter != null)
      outputStreamWriter.close();
    if (socket != null && !socket.isClosed())
      socket.close();
  }

  public void send(String message) throws IOException {
    if (bufferedWriter != null) {
      bufferedWriter.write(message);
      bufferedWriter.newLine();
      bufferedWriter.flush();
    }
  }

  public String receive() throws IOException {
    if (bufferedReader != null) {
      return bufferedReader.readLine();
    }
    return null;
  }
}
