package es.fempa.juanpomares.sockets;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class pantallaText extends AppCompatActivity
{
    TextView myTV;
    Button btncliente, btnservidor,bEnviar, bSalir;
    EditText ipServer;
    EditText etTexto;

    Bundle bundle;
    String ip;
    String identificacion;
    String nombre;
    Socket socket;
    ServerSocket serverSocket;
    boolean ConectionEstablished;
    boolean server = false;

    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;

    int mPuerto=1048;
    //Hilo para escuchar los mensajes que le lleguen por el socket
    GetMessagesThread HiloEscucha;


    /*Variable para el servidor*/
    WaitingClientThread HiloEspera;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_texto);

        bundle = getIntent().getExtras();
        ip = bundle.getString("ip");
        identificacion = bundle.getString("QuienSoy");
        nombre = bundle.getString("nombre");
        myTV=(TextView) findViewById(R.id.tvTexto);
        bEnviar = (Button)findViewById(R.id.bEnviar);
        etTexto = (EditText) findViewById(R.id.etTexto);

        if (identificacion.equals("server")){
            startServer();
        }else
            startClient();

        //bSalir.setEnabled(false);

        /*bSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisconnectSockets();
            }
        });*/

        bEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(etTexto.getText().toString());
            }
        });


    }

    public void startServer()
    {


        SetText("\nComenzamos Servidor!");
        (HiloEspera=new WaitingClientThread(this)).start();

       /* final pantallaText ma = this;
        bEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    (new EnvioMensajesServidor(ma)).start();
            }
        });*/
    }

    public void startClient()
    {
        String TheIP=ip;
        if(TheIP.length()>5)
        {

            (new ClientConnectToServer(TheIP, this)).start();

            SetText("\nComenzamos Cliente!");
            AppenText("\nNos intentamos conectar al servidor: "+TheIP);
        }

    }

    public void AppenText(String text)
    {
        runOnUiThread(new appendUITextView(text+"\n", this));
    }

    public void SetText(String text)
    {
        runOnUiThread(new setUITextView(text, this));
    }


    public void DisconnectSockets()
    {
        if(ConectionEstablished)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    btncliente.setEnabled(true);
                    btnservidor.setEnabled(true);
                    ipServer.setEnabled(true);
                }
            });
            ConectionEstablished = false;

            if (HiloEscucha != null)
            {
                HiloEscucha.setExecuting();
                HiloEscucha.interrupt();
                HiloEspera = null;
            }

            try {
                if (dataInputStream != null)
                    dataInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dataInputStream = null;
                try {
                    if (dataOutputStream != null)
                        dataOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    dataOutputStream = null;
                    try {
                        if (socket != null)
                            socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        socket = null;
                    }
                }
            }
        }
    }

    /*public void sendVariousMessages(String msgs, int time)
    {
        if(msgs!=null  &&time!=null && msgs.length==time.length)
            for (int i = 0; i < 1; i++) {
                sendMessage(msgs);
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
    }*/

    public void sendMessage(String txt)
    {
        new SendMessageSocketThread(txt,this).start();

    }


    //Aqui obtenemos la IP de nuestro terminal
    public String getIpAddress()
    {
        StringBuilder ip = new StringBuilder();
        try
        {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements())
            {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements())
                {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress())
                    {
                        ip.append("IP de Servidor: ").append(inetAddress.getHostAddress()).append("\n");
                    }

                }
            }
        } catch (SocketException e)
        {
            e.printStackTrace();
            ip.append("¡Algo fue mal! ").append(e.toString()).append("\n");
        }

        return ip.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DisconnectSockets();
    }
    public  void vaciarChat(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                etTexto.setText("");
            }
        });

    }
}
