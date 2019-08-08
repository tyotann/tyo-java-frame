package com.ihidea.component.jms.kafka;

import com.dianping.cat.Cat;
import com.ihidea.core.util.ClassUtilsEx;
import com.ihidea.core.util.JSONUtilsEx;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaConsumerRunner implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(KafkaConsumerRunner.class);

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private KafkaConsumer consumer;

    public KafkaConsumerRunner(KafkaConsumer consumer) {
        this.consumer = consumer;
    }

    public void preConsume(){}

    public void afterConsume(){}

    public void consume(ConsumerRecords<String, String> records){
        for (ConsumerRecord<String, String> record : records) {
            try {
                String topicName = record.topic();
                Method method = KafkaConsumerStarter.consumerMethodMap.get(topicName);
                if (method != null) {
                    Map<String, Object> paramMap = new HashMap<String, Object>();
                    paramMap.put("record", record);
                    ClassUtilsEx.invokeMethod(method.getDeclaringClass().getSimpleName(), method.getName(), paramMap);
                } else {
                    logger.error("[Kafka]处理消息发生异常: topic未找到相应的处理方法" + ",topic=" + topicName);
                }
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                Cat.logError(targetException);
                logger.error("[Kafka]处理消息发生异常:" + targetException.getMessage() + ",消息报文:" + JSONUtilsEx.serialize(record), targetException);
            } catch (Exception e) {
                logger.error("[Kafka]处理消息发生异常:" + e.getMessage() + ",消息报文:" + JSONUtilsEx.serialize(record), e);
            }
        }
    }

    public void run() {
        try {
            while (!closed.get()) {
                //必须在下次 poll 之前消费完这些数据, 且总耗时不得超过 SESSION_TIMEOUT_MS_CONFIG 的值
                ConsumerRecords<String, String> records = consumer.poll(1000);

                preConsume();

                consume(records);

                afterConsume();

                consumer.commitAsync();
            }
        } catch (WakeupException e) {
            // Ignore exception if closing
            if (!closed.get()) throw e;
        } finally {
            try {
                consumer.commitSync(); // 最后一次提交使用同步阻塞式提交
            } finally {
                consumer.close();
            }
        }
    }


    // Shutdown hook which can be called from a separate thread
    public void shutdown() {
        closed.set(true);
        consumer.wakeup();
    }

}