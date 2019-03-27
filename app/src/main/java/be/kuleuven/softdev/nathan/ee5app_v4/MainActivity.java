package be.kuleuven.softdev.nathan.ee5app_v4;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView mList;
    private ArrayList<String> arrayList;
    private ClientListAdapter mAdapter;
    private TcpClient mTcpClient;
    private EditText editText;
    private Button connectButton;
    private Button disconnectButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrayList = new ArrayList<>();
        editText = findViewById(R.id.editText);
        connectButton = findViewById(R.id.connect_button);
        disconnectButton = findViewById(R.id.disconnect_button);

        //relate the listView from java to the one created in xml
        mList = findViewById(R.id.list);
        mAdapter = new ClientListAdapter(this, arrayList);
        mList.setAdapter(mAdapter);

    }

    // this method is executed when the connect button is pressed
    public void connect(View view){
        new ConnectTask().execute();
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(true);
    }

    //this method is executed when the disconnect button is pressed
    public void disconnect(View view){
        if (mTcpClient != null) {
            arrayList.add("DISCONNECTED FROM SERVER!");
            mAdapter.notifyDataSetChanged();
            mTcpClient.stopClient();
            mTcpClient = null;
            disconnectButton.setEnabled(false);
            connectButton.setEnabled(true);
        }
    }

    // this method is executed when the send button is pressed
    public void send(View view){
        // if there is no connection yet, try to connect first
        if(mTcpClient == null){
            connect(findViewById(android.R.id.content));
        }

        String message = editText.getText().toString();

        //add the text in the arrayList
        arrayList.add("C: " + message);

        //sends the message to the server
        if (mTcpClient != null) {
            mTcpClient.SendMessage(message);
        }
        editText.setText("");
        //refresh the list and update the UI
        mAdapter.notifyDataSetChanged();
    }

    // connect to the TCP server
    // this has to be an AsyncTask since the network can't be on the main thread
    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();
            return mTcpClient;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter.notifyDataSetChanged();
        }
    }
}
