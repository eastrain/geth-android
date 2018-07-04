package com.geth.client;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import org.ethereum.geth.Context;
import org.ethereum.geth.Enode;
import org.ethereum.geth.Enodes;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Header;
import org.ethereum.geth.NewHeadHandler;
import org.ethereum.geth.Node;
import org.ethereum.geth.NodeConfig;
import org.ethereum.geth.NodeInfo;

public class MainActivity extends AppCompatActivity {

    private TextView textbox;

    private Node node;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textbox = findViewById(R.id.textbox);

        //----------------------run node----------------------------------------------
        Context ctx = new Context();
        Enodes bootnodes = new Enodes();
        //链接到以太坊测试环境（rinkeby）
        bootnodes.append(new Enode(
                "enode://a24ac7c5484ef4ed0c5eb2d36620ba4e4aa13b8c84684e1b4aab0cebea2ae45cb4d375b77eab56516d34bfbd3c1a833fc51296ff084b770b94fb9028c4d25ccf@52.169.42.101:30303"));

        NodeConfig config = new NodeConfig();
        config.setBootstrapNodes(bootnodes);
        config.setEthereumNetworkID(4);
        config.setEthereumGenesis(readAssets());
        //config.setEthereumNetStats("yournode:Respect my authoritah!@stats.rinkeby.io");

         node = new Node(getFilesDir() + "/.rinkeby", config);
        try {
            node.start();
            NodeInfo info = node.getNodeInfo();
            textbox.append("My name: " + info.getName() + "\n");
            textbox.append("My address: " + info.getListenerAddress() + "\n");
            textbox.append("My protocols: " + info.getProtocols() + "\n\n");

            EthereumClient ec = node.getEthereumClient();
            textbox.append("Latest block: " + ec.getBlockByNumber(ctx, -1).getNumber() + ", syncing...\n");

            NewHeadHandler handler = new NewHeadHandler() {
                @Override public void onError(String error) {
                }

                @Override public void onNewHead(final Header header) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            textbox.append("#" + header.getNumber() + ": " + header.getHash().getHex().substring(0, 20) + "…\n");
                        }
                    });
                }
            };
            ec.subscribeNewHead(ctx, handler, 16);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readAssets() {
        AssetManager manager = getAssets();
        try {
            InputStream inputStream = manager.open("genesis.json");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String content = new String(buffer, "utf-8");
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "error";
    }

    @Override protected void onPause() {
        super.onPause();
        try {
            node.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
