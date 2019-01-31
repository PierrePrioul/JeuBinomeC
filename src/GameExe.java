import org.jgroups.ChannelListener;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.stack.AddressGenerator;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class GameExe extends ReceiverAdapter implements ChannelListener {

    protected String               cluster_name="game";
    private JChannel channel = null;
    public ExampleDisplay                 exampleDisplay=null;
    boolean                        no_channel=false;
    boolean                        jmx;
    private boolean                use_state=false;
    private long                   state_timeout=5000;
    protected boolean              send_own_state_on_merge=true;


    public GameExe (String props, boolean no_channel, boolean jmx, boolean use_state, long state_timeout,
                boolean use_unicasts, String name, boolean send_own_state_on_merge, AddressGenerator gen) throws Exception {
        this.no_channel=no_channel;
        this.jmx=jmx;
        this.use_state=use_state;
        this.state_timeout=state_timeout;
        if(no_channel)
            return;

        channel=new JChannel(props).addAddressGenerator(gen).setName(name);
        channel.setReceiver(this).addChannelListener(this);
        this.send_own_state_on_merge=send_own_state_on_merge;
    }

    public GameExe(JChannel channel) throws Exception {
        this.channel=channel;
        channel.setReceiver(this);
        channel.addChannelListener(this);
    }


    public GameExe(JChannel channel, boolean use_state, long state_timeout) throws Exception {
        this.channel=channel;
        channel.setReceiver(this);
        channel.addChannelListener(this);
        this.use_state=use_state;
        this.state_timeout=state_timeout;
    }


    public String getClusterName() {
        return cluster_name;
    }

    public void setClusterName(String clustername) {
        if(clustername != null)
            this.cluster_name=clustername;
    }

    public void receive(Message msg) {

        int[] sweetLocation = (int[]) msg.getObject();

        if(null != exampleDisplay) {

            exampleDisplay.gameMap[sweetLocation[0]][sweetLocation[1]] = null;
            Circle c = exampleDisplay.gameMap[exampleDisplay.myRectangle.x][exampleDisplay.myRectangle.y];
            exampleDisplay.myContainer.remove(c);
            exampleDisplay.numberOfSweets--;



        }
    }

    public void getState(OutputStream output) throws Exception {
    Circle[][] gMap = exampleDisplay.gameMap;
        Util.objectToStream(gMap, new DataOutputStream(output));

    }

    public void sendMessage(int i, int j) throws Exception {
        int[] res = new int[2];
        res[0] = i;
        res[1] = j;

        Message mes = new Message(null, res);
        channel.send(mes);

    }

    public void setState(InputStream input) throws Exception {
        Circle[][] gMap;
        gMap=(Circle[][])Util.objectFromStream(new DataInputStream(input));
        createExampleDisplayWithState(gMap);
    }

    public void createExampleDisplayWithState(Circle[][] gMap){

        exampleDisplay = new ExampleDisplay(0, gMap);

    }

        public void go() throws Exception {
        if(!no_channel && !use_state) {
            channel.connect(cluster_name);
            channel.getState(null, 5000);
        }

        if(null == exampleDisplay) {
            exampleDisplay = new ExampleDisplay(0,null);

            exampleDisplay.pack();
        }

        if(!no_channel && use_state) {
            channel.connect(cluster_name, null, state_timeout);
            channel.getState(null, 5000);
        }
        exampleDisplay.setVisible(true);

            int sweets = exampleDisplay.numberOfSweets;

            while(exampleDisplay.numberOfSweets > 0){

            if(exampleDisplay.numberOfSweets < sweets){

                sweets--;
                sendMessage(exampleDisplay.myRectangle.x,exampleDisplay.myRectangle.y);

            }


        }

    }

    public static void main(String[] args) {
        String           props=null;
        boolean          no_channel=false;
        boolean          jmx=true;
        boolean          use_state=false;
        String           group_name=null;
        long             state_timeout=5000;
        boolean          use_unicasts=false;
        String           name=null;
        boolean          send_own_state_on_merge=true;
        AddressGenerator generator=null;

        try {
            GameExe gameExe = new GameExe(props, no_channel, jmx, use_state, state_timeout, use_unicasts, name,
                    send_own_state_on_merge, generator);
            if(group_name != null)
                gameExe.setClusterName(group_name);
        gameExe.go();
        }
        catch(Throwable e) {
            e.printStackTrace(System.err);
            System.exit(0);
        }

    }

    @Override
    public void channelConnected(JChannel channel) {

    }

    @Override
    public void channelDisconnected(JChannel channel) {

    }

    @Override
    public void channelClosed(JChannel channel) {

    }
}
