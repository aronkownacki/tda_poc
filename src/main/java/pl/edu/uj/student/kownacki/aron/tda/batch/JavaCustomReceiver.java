package pl.edu.uj.student.kownacki.aron.tda.batch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.receiver.Receiver;

import com.google.common.io.Closeables;
import scala.Tuple2;

/**
 * Custom Receiver that receives data over a socket. Received bytes is interpreted as
 * text and \n delimited lines are considered as records. They are then counted and printed.
 * <p>
 * Usage: JavaCustomReceiver <master> <hostname> <port>
 * <master> is the Spark master URL. In local mode, <master> should be 'local[n]' with n > 1.
 * <hostname> and <port> of the TCP server that Spark Streaming would connect to receive data.
 * <p>
 * To run this on your local machine, you need to first run a Netcat server
 * `$ nc -lk 9999`
 * and then run the example
 * `$ bin/run-example org.apache.spark.examples.streaming.JavaCustomReceiver localhost 9999`
 */

public class JavaCustomReceiver extends Receiver<String> {
    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main_(String[] args) throws Exception {
        args = new String[]{"localhost", "9999"};

        SparkConf sparkConf = new SparkConf().setAppName("JavaCustomReceiver").setMaster("spark://localhost:7077");
        JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, new Duration(1000));

        JavaReceiverInputDStream<String> lines = ssc.receiverStream(
                new JavaCustomReceiver(args[0], Integer.parseInt(args[1])));
        JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(SPACE.split(x)).iterator());
        JavaPairDStream<String, Integer> wordCounts = words.mapToPair(s -> new Tuple2<>(s, 1))
                .reduceByKey((i1, i2) -> i1 + i2);

        wordCounts.print();
        ssc.start();
        ssc.awaitTermination();
    }

    // ============= Receiver code that receives data over a socket ==============

    String host = null;
    int port = -1;

    public JavaCustomReceiver(String host_, int port_) {
        super(StorageLevel.MEMORY_AND_DISK_2());
        host = host_;
        port = port_;
    }

    @Override
    public void onStart() {
        // Start the thread that receives data over a connection
        new Thread(this::receive).start();
    }

    @Override
    public void onStop() {
        // There is nothing much to do as the thread calling receive()
        // is designed to stop by itself isStopped() returns false
    }

    /**
     * Create a socket connection and receive data until receiver is stopped
     */
    private void receive() {
        try {
            Socket socket = null;
            BufferedReader reader = null;
            try {
                // connect to the server
                socket = new Socket(host, port);
                reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                // Until stopped or connection broken continue reading
                String userInput;
                while (!isStopped() && (userInput = reader.readLine()) != null) {
                    System.out.println("Received data '" + userInput + "'");
                    store(userInput);
                }
            } finally {
                Closeables.close(reader, /* swallowIOException = */ true);
                Closeables.close(socket,  /* swallowIOException = */ true);
            }
            // Restart in an attempt to connect again when server is active again
            restart("Trying to connect again");
        } catch (ConnectException ce) {
            // restart if could not connect to server
            restart("Could not connect", ce);
        } catch (Throwable t) {
            restart("Error receiving data", t);
        }
    }
}

