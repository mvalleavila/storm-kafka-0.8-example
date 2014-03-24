//import spouts.WordReader;

import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.trident.GlobalPartitionInformation;
import storm.kafka.Broker;
import storm.kafka.BrokerHosts;
import storm.kafka.StaticHosts;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import bolts.WordCounter;
import bolts.WordNormalizer;


public class TopologyMain {
	public static void main(String[] args) throws InterruptedException {
         
        //Topology definition
		TopologyBuilder builder = new TopologyBuilder();

		//Creamos un SpoutConfig con los paramatros necesarios para kafka
		//TODO: Actualizar leyendo las configuraciones de un fichero

		GlobalPartitionInformation partitionInfo = new GlobalPartitionInformation();
		partitionInfo.addPartition(0,new Broker("mvalle1",9092));
		BrokerHosts brokerHosts = new StaticHosts(partitionInfo);
		
		//public SpoutConfig(BrokerHosts hosts, String topic, String zkRoot, String id)
		//SpoutConfig spoutConfig = new SpoutConfig(brokerHosts,"storm", "mvalle1:2181,mvalle2:2181,mvalle3:2181","KafkaSpout");
		SpoutConfig spoutConfig = new SpoutConfig(brokerHosts,"storm", "","KafkaSpout");




		// Creamos el spout
		builder.setSpout("kafka-spout",new KafkaSpout(spoutConfig));
		// El primer bolt, conectado al spout
		// el shuffle grouping envia a cada task de manera aleatoria, las dos task quedan equilibradas
		// cada task es considerada un bolt
		builder.setBolt("word-normalizer", new WordNormalizer())
			.shuffleGrouping("kafka-spout");
		// El segundo bolt, conectado al primer bolr
		// el fields grouping manda a cada task el conteo de las palabras iguales
		builder.setBolt("word-counter", new WordCounter(),2)
			.fieldsGrouping("word-normalizer", new Fields("word"));
		
	//Configuration Hash Map de java (string,objeto)
		Config conf = new Config();
		// El argumento en la primera posicion sera el fichero del que leera las palabras
		//conf.put("wordsFile", args[0]);
		conf.setDebug(false);
        //Topology run
	        // Maximo numero de tuplas que puede haber en un spout task (TASK = SUMIDERO??)
		conf.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);
		// Creacion del objeto LocalCluster 
		LocalCluster cluster = new LocalCluster();
		// Le pasamos nombre, configuracion y la topologia
		cluster.submitTopology("Getting-Started-Toplogie", conf, builder.createTopology());
		// Lo hacemos andar un par de segundos
		Thread.sleep(200000);
		// Lo matamos cuando pase el tiempo
		cluster.shutdown();
	}
}
