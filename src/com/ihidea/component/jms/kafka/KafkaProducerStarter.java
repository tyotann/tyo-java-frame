package com.ihidea.component.jms.kafka;

import com.ihidea.core.support.exception.ServiceException;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaProducerStarter {

    private final static Logger logger = LoggerFactory.getLogger(KafkaProducerStarter.class);

    private static KafkaProducer<String, String> producer = null;

    /**
     * 初始化producer
     * @param brokerAddress
     */
    public static void init(String brokerAddress) {
        Properties props = new Properties();
        //设置接入点，请通过控制台获取对应 Topic 的接入点
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddress);
        //消息队列 Kafka 消息的序列化方式
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        //请求的最长等待时间
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);
        //0: 不进行消息接收确认，即Client端发送完成后不会等待Broker的确认
        //1: 由Leader确认，Leader接收到消息后会立即返回确认信息
        //all: 集群完整确认，Leader会等待所有in-sync的follower节点都确认收到消息后，再返回确认信息 我们可以根据消息的重要程度，设置不同的确认模式。默认为1
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        //重试次数
        props.put(ProducerConfig.RETRIES_CONFIG, "10");
        //设置压缩算法
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
        //当同时有大量消息要向同一个分区发送时，Producer端会将消息打包后进行批量发送。如果设置为0，则每条消息都独立发送。默认为16384字节
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
        //发送消息前等待的毫秒数，与batch.size配合使用。在消息负载不高的情况下，配置linger.ms能够让Producer在发送消息前等待一定时间，以积累更多的消息打包发送，达到节省网络资源的目的。默认为0
        props.put(ProducerConfig.LINGER_MS_CONFIG, "100");
        //重连时间
        props.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, "1000");
        //构造 Producer 对象，注意，该对象是线程安全的。
        //一般来说，一个进程内一个 Producer 对象即可。如果想提高性能，可构造多个对象，但最好不要超过 5 个
        producer = new KafkaProducer<String, String>(props);

    }

    /**
     * 发送消息
     * @param topic
     * @param message
     */
    public static void send(final String topic, final String message) {
        if(producer == null) {
            throw new ServiceException("kafka producer还未初始化，请先执行init方法初始化!");
        }
        ProducerRecord<String, String> kafkaMessage =  new ProducerRecord<String, String>(topic, message);
        //使用带回调通知的发送API
        producer.send(kafkaMessage, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if(e == null) {
                    logger.debug("[KAFKA]-发送成功:{}", new Object[]{recordMetadata.toString()});
                } else {
                    logger.error("[KAFKA]-发送失败, topic:{}, message:{}", new Object[]{topic, message}, e);
                }
            }
        });
    }


    /**
     * 发送顺序消息
     * @param key
     * @param topic
     * @param message
     */
    public static void send(final String key, final String topic, final String message) {
        if(producer == null) {
            throw new ServiceException("kafka producer还未初始化，请先执行init方法初始化!");
        }
        ProducerRecord<String, String> kafkaMessage =  new ProducerRecord<String, String>(topic, key, message);
        //使用带回调通知的发送API
        producer.send(kafkaMessage, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if(e == null) {
                    logger.debug("[KAFKA]-发送成功:{}", new Object[]{recordMetadata.toString()});
                } else {
                    logger.error("[KAFKA]-发送失败, topic:{}, message:{}", new Object[]{topic, message}, e);
                }
            }
        });
    }


    /**
     * 发送消息,自定义回调处理
     * @param topic
     * @param message
     */
    public static void sendWithCallBack(final String topic, final String message, final Callback callback) {
        if(producer == null) {
            throw new ServiceException("kafka producer还未初始化，请先执行init方法初始化!");
        }
        ProducerRecord<String, String> kafkaMessage =  new ProducerRecord<String, String>(topic, message);
        //使用带回调通知的发送API
        producer.send(kafkaMessage, callback);
    }

    /**
     * 发送顺序消息
     * @param key
     * @param topic
     * @param message
     */
    public static void sendWithCallBack(final String key, final String topic, final String message, final Callback callback) {
        if(producer == null) {
            throw new ServiceException("kafka producer还未初始化，请先执行init方法初始化!");
        }
        ProducerRecord<String, String> kafkaMessage =  new ProducerRecord<String, String>(topic, key, message);
        //使用带回调通知的发送API
        producer.send(kafkaMessage, callback);
    }

}
